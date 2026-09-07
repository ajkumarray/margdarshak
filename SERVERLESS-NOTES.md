# Cold Start Notes

How margdarshak's backend actually runs on AWS Lambda, why each piece is there, and the interview
questions this project is built to answer — drawn from the real bugs hit shipping it.

`cost: $0/mo` &nbsp;·&nbsp; `warm restore: ~650ms` &nbsp;·&nbsp; `cold init: ~15s` &nbsp;·&nbsp; `runtime: Java 21` &nbsp;·&nbsp; `db: Neon Postgres`

---

## Part I — Theory: how it actually works

Not a tool tutorial — the reasoning behind each layer, in the order a request passes through them.

### 1. What "serverless" means here

"Serverless" doesn't mean no server — it means you stop owning the server's *lifecycle*. On EC2, a
JVM sits running 24/7 whether or not anyone visits, and you pay for every one of those idle hours.
On Lambda, AWS keeps your code as an inert package until a request arrives, spins up an execution
environment to handle it, and can freeze or destroy that environment when it's done. You pay per
invocation and per millisecond of actual compute, not per hour of a machine existing.

The trade you make for that is **cold starts**: the first request after idle has to pay for the
environment to spin up before your code even runs. For a JVM app that's normally brutal — a Spring
context boot is seconds, not milliseconds. Sections 2–3 are about that specific problem and how
this project solves it.

> **Worth saying in an interview:** Serverless isn't "cheaper" in the abstract — it's cheaper *at
> low or spiky traffic*, because idle time costs nothing. At sustained high, predictable traffic,
> an always-on instance can be cheaper per request. The right call depends on the traffic shape,
> which is exactly why this migration made sense for a low-traffic personal URL shortener and
> might not for a hot internal API.

### 2. The Lambda execution model

Every Lambda invocation moves through three phases, and which phase you're in explains almost
every serverless quirk you'll hit:

- **Init** — runs once per execution environment: load the runtime, run your static initializers,
  build the Spring context. Expensive, and only paid once per environment, not once per request.
- **Invoke** — runs your handler for one request. AWS reuses a *warm* environment for the next
  invocation if one arrives soon enough, skipping Init entirely.
- **Shutdown** — the environment is frozen or torn down after a period of no traffic. Frozen state
  does not persist connections, open sockets, or timers reliably across a freeze/thaw cycle.

A *cold start* is a request that draws a fresh environment and pays the full Init cost. A *warm*
request reuses one and skips straight to Invoke. This is why load testing a Lambda function with
one request at a time looks nothing like production traffic — concurrency determines how many
separate execution environments (and therefore how many cold starts) you get.

### 3. SnapStart

A cold Init for this app — full JVM boot, Spring context, Hibernate, JPA metamodel, security
filter chain — measured at **~15 seconds** in testing. That's unusable for a request path.

**SnapStart** changes what "Init" means. Instead of running Init on every cold start, AWS runs it
*once*, at the moment you publish a new Lambda version — then takes a full checkpoint of that
initialized execution environment's memory (backed by Firecracker microVM snapshotting) and caches
it. Every subsequent cold start restores that snapshot instead of re-running Init from scratch.
Measured restore time for this app: **~650ms**, consistently — roughly a 20× improvement, and
specifically because most of the 15s was JVM class-loading and Spring reflection-heavy startup,
which a memory snapshot sidesteps entirely.

> **Why it's free on Java specifically:** AWS prices SnapStart per-GB-cached and per-restore on
> some runtimes, but Java incurs **no additional charge** — you pay the normal Lambda invocation
> and duration rates, nothing extra for the snapshot mechanism itself. That's a deliberate
> incentive: Java has the worst cold-start profile of the mainstream Lambda runtimes, so it's the
> runtime AWS subsidizes the fix for.

#### The gotcha: a snapshot is a photograph, not a live process

Anything that depends on *when* Init actually ran breaks under SnapStart unless you account for it:

- **Randomness** — a `SecureRandom` seeded at snapshot time and then restored into many parallel
  execution environments risks correlated output across them. AWS's Java runtime specifically
  re-seeds `SecureRandom` on restore to guard against this, but code that rolls its own PRNG
  without going through the standard APIs can still be caught out.
- **Open connections** — a database connection pool warmed during Init is frozen mid-flight and
  thawed later; the far end may have long since closed it. This project avoids the problem by not
  opening a DB connection during Init at all (see Section 7) and keeping the pool at
  `minimum-idle: 0`.
- **Config baked in at snapshot time** — whatever your app read from its environment during Init
  is frozen into the snapshot. Changing a secret afterward does nothing until a new version is
  published and snapshotted. This is the subject of the second war story in Part II.

### 4. Running a servlet app on Lambda

Lambda's native contract is `handler(event, context) -> response` — it knows nothing about
servlets, `HttpServletRequest`, or Spring MVC's `DispatcherServlet`.
[aws-serverless-java-container](https://github.com/aws/serverless-java-container) bridges that
gap: it translates the incoming Lambda event into a synthetic servlet request, runs it through the
real, unmodified Spring MVC pipeline, and translates the servlet response back into the Lambda
response shape Lambda expects.

The practical upshot: every controller, every `@Service`, the entire security filter chain in this
app is **completely unmodified** from the version that used to run on EC2. Only one new class
exists for Lambda — a thin handler that builds the adapter once in a static initializer (so it's
captured by the SnapStart snapshot) and delegates each invocation to it.

```java
public class LambdaHandler implements RequestStreamHandler {
    private static final SpringBootLambdaContainerHandler<...> HANDLER;
    static {
        HANDLER = SpringBootLambdaContainerHandler
            .getHttpApiV2ProxyHandler(MargdarshakApplication.class, "lambda");
    }
    public void handleRequest(InputStream in, OutputStream out, Context ctx) {
        HANDLER.proxyStream(in, out, ctx);
    }
}
```

> #### War story — the packaging bug
> **An uber-jar silently deleted the app's own config loader**
>
> The obvious way to package a fat set of dependencies for Lambda is a single shaded uber-jar. It
> built fine, deployed fine — and then no `application*.yml` file loaded at all, with no error,
> just every custom property failing to resolve.
>
> The cause: every dependency jar ships its own `META-INF/spring.factories`, a flat `key=value`
> file. Shading concatenates same-named files across all jars, and when a `.properties`-style file
> has the same key defined more than once, **the last one silently wins**. One of those keys
> registers `ConfigDataEnvironmentPostProcessor` — the exact component that loads
> `application.yml` in the first place. It got overwritten by a later jar's shorter list and
> quietly vanished, with zero errors anywhere in the stack trace.
>
> Fix: stop shading. Package as the app's own thin jar plus every dependency jar loose under
> `lib/` — the layout Lambda's Java runtime natively adds to the classpath. Every dependency's
> metadata stays intact because nothing gets merged.

### 5. CloudFront in front

A Lambda **Function URL** gives you a working HTTPS endpoint with zero extra infrastructure — but
it's a fixed, ugly, AWS-owned hostname (`*.lambda-url.<region>.on.aws`) that can never take a
custom domain directly. To serve traffic on your own domain you need something that terminates the
custom hostname and forwards to the Function URL as an origin — here, CloudFront.

That adds two real requirements: an ACM (Certificate Manager) TLS certificate for the custom
domain, and — specifically because CloudFront is a global edge service — that certificate has to
be requested in **`us-east-1`** no matter what region the Lambda itself runs in. Getting the cert
issued means proving domain ownership via a DNS CNAME record ACM hands you, which is the one
manual step in an otherwise scriptable setup.

> #### War story — the header CloudFront quietly drops
> **Swagger's docs page leaked the internal Function URL**
>
> Springdoc builds its "Servers" entry in the OpenAPI spec from the `Host` header of whatever
> request generated it. By default, CloudFront's origin request policy does *not* forward the
> viewer's original `Host` header to a custom origin — it substitutes the origin's own hostname.
> So every request the Lambda ever saw reported its Host as the raw, undocumented Function URL,
> and the public API docs page displayed that internal address to anyone who opened it.
>
> Fixed at the application layer rather than by reconfiguring header forwarding: declare the
> OpenAPI server URL explicitly from a Spring property instead of letting springdoc infer it from
> the request, with that property set differently per environment (`localhost:8080` locally, the
> real domain under the Lambda profile).

### 6. Config, secrets & versions

Secrets (DB credentials, JWT signing key) live in **SSM Parameter Store** as encrypted
`SecureString` values, fetched over the network during Init and folded into Spring's environment —
never baked into the deployed package. Two design reasons for SSM specifically over the
alternatives:

| Option | Trade-off here |
|---|---|
| `SecureString` in SSM Parameter Store | Free at this scale, encrypted with a KMS key, versioned. Chosen. |
| AWS Secrets Manager | Adds automatic rotation and cross-account sharing this project doesn't need, at a small per-secret monthly cost. |
| Plain Lambda environment variables | Visible in the console/API in cleartext to anyone with read access to the function — no encryption at rest by default. |

> #### War story — changing a secret did nothing
> **Versions, aliases, and why an SSM update alone is a no-op**
>
> After changing the short-link domain, the SSM parameter that drives it was updated — and the app
> kept generating links with the old value. The reason connects straight back to Section 3:
> SnapStart's snapshot is taken *at publish time*, and it's the `live` alias pointing at a
> specific, already-snapshotted version that serves traffic. An SSM value living outside the
> function's own definition doesn't make Lambda think anything about the function changed, so
> `publish-version` is a no-op — it just hands back the existing version number.
>
> The fix is to force the function's own configuration to change (a throwaway environment variable
> works), which makes a genuinely new version publishable, get a fresh snapshot taken, and only
> then move the `live` alias onto it. The first invocation of that new version pays the full cold
> Init again, since AWS hasn't finished preparing its snapshot yet — expect one slow or even
> timed-out request immediately after any deploy like this.

### 7. Serverless meets a connection pool

Connection pooling exists to amortize the cost of opening a database connection across many
requests handled by *one long-lived process*. Lambda breaks that assumption: under real
concurrency, AWS runs many separate execution environments in parallel, each with its own
independent pool. A pool sized for a normal app (say, 10 connections) times even modest Lambda
concurrency can open hundreds of simultaneous connections against a database that might only
allow 100.

Two changes make this manageable:

- **Shrink the pool per environment** — this app runs Hikari at `maximum-pool-size: 1`,
  `minimum-idle: 0`. One environment, one request in flight at a time (Lambda doesn't hand a
  single environment two concurrent invocations), so one connection is enough — and idle
  environments hold zero connections open against the database while frozen.
- **Let the database absorb the fan-out** — Neon terminates client connections through
  **PgBouncer** in front of Postgres itself, multiplexing many thin client connections onto a much
  smaller number of real backend connections. That's why the datasource URL points at Neon's
  `-pooler` hostname specifically rather than the direct one.

The direct (non-pooled) Neon endpoint still exists and matters for one specific case: Postgres
session-level operations like advisory locks or certain DDL don't behave correctly through a
transaction-mode pooler, so schema migrations should run against the direct endpoint, ordinary app
traffic through the pooled one.

### 8. Three domains, one function

The deployed system answers to three different hostnames, and only one Lambda function sits
behind two of them:

```
url.ajkumarray.com  ──  frontend (React, hosted on Render) — unrelated infrastructure

api.url.ajkumarray.com  ─┐
                          ├──  CloudFront  ──  Lambda Function URL
link.ajkumarray.com     ─┘
     (one distribution, one ACM cert covering both names)
```

The split is purely cosmetic — both routes exist on both domains, since it's the same Spring app
either way. It exists because a link meant to be shared and clicked reads better as
`link.ajkumarray.com/AbC123` than as an API-shaped hostname, while a frontend calling the backend
for data reads better hitting something that says `api`. One certificate with both names as
Subject Alternative Names covers the whole distribution.

---

## Part II — Interview prep

Grounded in what actually happened building this — not textbook answers.

### 9. The 60-second pitch

> "Margdarshak is a URL shortener — Spring Boot on Java 21, originally deployed on an EC2 box. I
> moved the backend to AWS Lambda with SnapStart, put CloudFront in front for a custom domain, and
> moved the database to Neon, a serverless Postgres with connection pooling built in. The whole
> thing runs inside AWS and Neon's free tiers, so it costs nothing at this traffic level, and cold
> starts are down to roughly 650 milliseconds thanks to SnapStart instead of the 15-second full
> JVM boot it'd otherwise take. The interesting part wasn't the happy path — it was three separate
> failure modes specific to serverless that don't show up in a normal deployment: a packaging bug
> that silently broke config loading, a header-forwarding default that leaked an internal URL, and
> a version-publishing gotcha that meant a secret change didn't take effect until I understood how
> SnapStart snapshots actually get created."

### 10. System design Q&A

<details>
<summary><b>Why Lambda instead of keeping it on a server, or moving to containers?</b></summary>

Traffic is low and spiky — a personal project, not a product with steady load. An always-on EC2
instance or container bills for 24 hours whether or not it's used; Lambda bills per request and
per millisecond of actual compute, so idle time is free. Containers on something like ECS Fargate
sit between the two: no server to patch, but still billed while running, not while idle. For this
traffic shape, function-as-a-service was the only option that gets to genuinely zero cost at rest.
</details>

<details>
<summary><b>Why Lambda Function URLs instead of API Gateway?</b></summary>

API Gateway adds request-based pricing beyond its own free tier and a second piece of
infrastructure to reason about, for features this project doesn't use — usage plans, request
transformation, multiple stages. A Function URL is a first-class, free HTTPS endpoint straight on
the function. The one thing API Gateway would add is native custom-domain support; here that gap
is filled by CloudFront instead, which was already the right layer for TLS and edge presence
anyway.
</details>

<details>
<summary><b>Walk through what happens when someone visits a short link.</b></summary>

Browser resolves `link.ajkumarray.com` to CloudFront via DNS → CloudFront terminates TLS using the
ACM cert and forwards the request to the Lambda Function URL origin → the Function URL invokes the
Lambda, restoring a SnapStart snapshot if the environment was cold (~650ms) or reusing a warm one
(single-digit milliseconds) → `aws-serverless-java-container` turns the event into a servlet
request and runs it through the same Spring MVC redirect controller that used to run on EC2 → one
indexed lookup against Neon through the pooled connection → a 302 with the destination URL flows
back through the same chain.
</details>

<details>
<summary><b>How do you guarantee two short links never collide?</b></summary>

Two layers. In the service, before insert, the randomly generated code (`"MD"` + 7 base-62
chars ≈ 3.5×10¹² space) is checked with `existsByCode` and regenerated on a hit, up to 5 attempts
before giving up with an error. That handles the everyday case. The backstop is a `UNIQUE`
constraint on `url_master.code` at the database — so even if two concurrent requests pass the
pre-check with the same code in the same instant, the second `INSERT` fails cleanly (surfaced as
a creation error the client can retry) instead of writing a duplicate. Without the constraint a
collision would silently create two rows and then break every future `findByCode` lookup with a
non-unique-result error. The repository call lives only in the service/implementation class, not
in the mapping helper, to keep the helper free of a persistence dependency.
</details>

<details>
<summary><b>How would you scale this if traffic grew 1000×?</b></summary>

Lambda itself scales horizontally by default — more concurrent environments spin up automatically,
no capacity planning required, up to the account's concurrency limit (raisable). The two things
that would need attention first: the Hikari pool is intentionally tiny per environment, so Neon's
pooler and its own compute ceiling become the real bottleneck at high concurrency — likely
upgrading off the free tier or moving to Aurora Serverless with RDS Proxy. Second, CloudFront's
free tier (1TB / 10M requests a month) is generous but not infinite, and at that point provisioned
concurrency might be worth paying for on the redirect path specifically, to guarantee warm
environments instead of relying on traffic volume to keep them warm.
</details>

### 11. Three debugging war stories, as interview answers

These map directly onto "tell me about a hard bug you fixed" — each is a real, verifiable incident
from this project, already told in STAR shape in Part I. Quick index back to them:

- **Situation:** config silently stopped loading after a packaging change → **Root cause:** shaded
  uber-jar merged conflicting `spring.factories` keys, deleting the config loader — Section 4.
- **Situation:** public API docs displayed an internal, undocumented URL → **Root cause:**
  CloudFront's default origin policy substitutes its own Host header — Section 5.
- **Situation:** a secret rotation had no effect on the running system → **Root cause:** SnapStart
  snapshots freeze config at publish time; external state changes don't trigger a new version —
  Section 6.

What ties all three together, and is worth saying explicitly if asked "what's hard about
serverless specifically": every one of them was invisible in local testing and only surfaces once
a request actually passes through the full chain of managed services. None was a logic bug in
application code — all three were about how compute, config, and infrastructure evaluate *time*
differently than a normal always-on process does.

### 12. "What would you change?"

A senior interviewer will ask this, and pretending everything is perfect reads worse than naming
real gaps with a plan attached. Actual open items on this project, honestly:

| Gap | Why it's there / what fixes it |
|---|---|
| Deploy IAM user has `AdministratorAccess` | Expedient for a solo project; a real team would scope a deploy role to exactly CloudFormation, Lambda, IAM role creation, S3, SSM, and ACM/CloudFront. |
| No CI/CD | Deploys are a manual two-command sequence right now. The previous EC2 pipeline was retired outright rather than pointed at nothing; a GitHub Actions workflow running the same `sam deploy` is the natural next step, deliberately deferred. |
| No rate limiting | Nothing stops one client hammering the register or redirect endpoints. Would add at the CloudFront/WAF layer rather than in application code, so it doesn't cost a Lambda invocation to reject. |
| JWT has no revocation or refresh | A stolen token is valid until it naturally expires. A refresh-token flow with a server-side revocation list would close this. |
| No automated tests | Every verification in this project was a manual curl smoke test against a live deployment. The honest answer under pressure: expedient for solo iteration speed, not defensible at team scale. |

### 13. Rapid-fire definitions

<details>
<summary><b>SnapStart vs. Provisioned Concurrency — what's the actual difference?</b></summary>

Provisioned Concurrency keeps a chosen number of execution environments permanently warm, fully
Init'd and idling — you pay for that idle time continuously, in exchange for zero cold starts,
ever. SnapStart doesn't keep anything running; it makes the *cold* path fast by restoring a memory
snapshot instead of re-running Init, and costs nothing extra on Java. They solve the same symptom
differently: one buys warmth, the other makes cold cheap.
</details>

<details>
<summary><b>What's actually inside a Lambda deployment package for this app?</b></summary>

A zip containing the app's own compiled classes as a thin jar at the root, plus a `lib/` directory
holding every runtime dependency as separate jars — the layout Lambda's Java runtime natively puts
on the classpath, deliberately not a single merged uber-jar (see Section 4).
</details>

<details>
<summary><b>Lambda version vs. alias?</b></summary>

A version is an immutable, numbered snapshot of a function's code and configuration at the moment
it was published — SnapStart's checkpoint is taken per-version. An alias is a named, mutable
pointer at one specific version (here, `live`) that the Function URL is actually configured to
invoke, so traffic can be cut over to a new version by moving the alias, not by changing the URL.
</details>

<details>
<summary><b>Why does the ACM certificate have to be in us-east-1?</b></summary>

CloudFront is a single global service, not deployed per-region, and AWS's internal integration for
attaching a certificate to a CloudFront distribution only reads certificates from the `us-east-1`
ACM endpoint — regardless of where the certificate will actually be presented to end users, or
where the origin behind it lives.
</details>

<details>
<summary><b>Why can Neon's free tier be "serverless" the same way Lambda is?</b></summary>

Neon separates storage from compute: the Postgres compute node that actually executes queries can
suspend entirely after a period of inactivity and resume on the next connection, so an idle
database costs nothing beyond stored data, the same idle-is-free property Lambda has for compute.
</details>

---

*Reference notes for the margdarshak project — written for the person who built it, to remember why.*
