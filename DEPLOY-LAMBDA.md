# margdarshak on AWS Lambda - how it all fits together

Backend runs as an AWS Lambda function (Java 21, SnapStart), fronted by CloudFront for the custom
domains, talking to a Neon Postgres database. Total cost at current traffic: **$0/month**
(everything is within free tiers - Lambda, CloudFront, Neon).

## How a request flows

```
browser / frontend
      |
      v
CloudFront (dhldqt46f9xnx.cloudfront.net)
  - api.url.ajkumarray.com  \_ same distribution, same cert, same origin
  - link.ajkumarray.com     /
      |
      v
Lambda Function URL (private - see Resource inventory below)
      |
      v
Lambda function "margdarshak" (alias "live" -> a published version, SnapStart-enabled)
      |
      v
Neon Postgres (ap-southeast-1, pooled connection)
```

## The three domains

| Domain | What it is | Points at |
|---|---|---|
| `url.ajkumarray.com` | The frontend (React app "nirdeshak") | Render - **unrelated to this backend**, don't confuse the two |
| `api.url.ajkumarray.com` | Backend API calls: register, login, create/list URLs | CloudFront -> this Lambda |
| `link.ajkumarray.com` | The short links people actually click (`/{code}` -> redirect) | CloudFront -> this Lambda (same function as above) |

`api.url.ajkumarray.com` and `link.ajkumarray.com` are the exact same backend - just two names for
convenience (one reads like an API host, one reads like a shareable short link). The `GET /{code}`
redirect route and the `/api/v1/**` routes both exist on both domains; the split is cosmetic.

## Config lives in two separate, unsynced places

| | Where | Used by |
|---|---|---|
| `src/main/resources/application.properties` | Local file only, git-ignored, never committed | Only when you run the app on your own machine (`mvn spring-boot:run`, or the plain `java -jar target/margdarshak-0.0.1-SNAPSHOT.jar`) |
| SSM Parameter Store, path `/config/margdarshak/*` | AWS, region `ap-southeast-1`, encrypted (SecureString) | The deployed Lambda, fetched at cold-init time via `application-lambda.yml` (`spring.config.import: optional:aws-parameterstore:...`), then frozen into the SnapStart snapshot |

Editing the local file does not touch production. Changing an SSM parameter does not touch your
local runs, and **does nothing to the live Lambda either until a new version is published** - see
"Updating a secret" below. There is no `.env` file, no `application-local.properties`, and the old
`application.properties.j2` + AWS Secrets Manager template that used to feed the (now-deleted) EC2
pipeline is gone.

## Resource inventory

| Thing | Value |
|---|---|
| AWS account | `<aws-account-id>` - see `aws sts get-caller-identity` |
| Region (Lambda, Neon) | `ap-southeast-1` |
| Region (ACM cert, CloudFront requirement) | `us-east-1` |
| Lambda function name | `margdarshak` |
| Live alias | `live` (points at whichever version is current) |
| Raw Function URL (bypasses CloudFront, no custom domain - treat as sensitive, not published here) | `aws lambda get-function-url-config --function-name margdarshak --region ap-southeast-1` |
| CloudFormation / SAM stack name | `margdarshak` |
| CloudFront distribution | `E6ZI4H85ELRGA` (`dhldqt46f9xnx.cloudfront.net`) - already public, it's the CNAME target of the live domains |
| ACM cert (covers `api.url.ajkumarray.com` + `link.ajkumarray.com`) | `aws acm list-certificates --region us-east-1` |
| SSM parameter path | `/config/margdarshak/` |
| Neon DB host (pooled) | in your local `application.properties` (git-ignored) or `aws ssm get-parameter --name /config/margdarshak/spring.datasource.url --with-decryption --region ap-southeast-1` |
| DNS registrar / host | Porkbun (nameservers `*.ns.porkbun.com`) |
| IAM user used for deploys | has `AdministratorAccess` - root login is not used for anything day-to-day |

Find the function/logs in the console: https://ap-southeast-1.console.aws.amazon.com/lambda/home?region=ap-southeast-1#/functions/margdarshak

## Current gaps (known, not yet done)

- **No CI/CD.** Deploys are manual (`mvn package` + `sam deploy`, below). The old EC2 GitHub
  Actions pipeline was deleted because it pointed at a terminated EC2 instance. A replacement
  workflow can be added later - deliberately skipped for now.

---

# Deploy runbook

## Prerequisites (once)

- AWS CLI configured with credentials for the target account (`aws sts get-caller-identity` to check)
- [AWS SAM CLI](https://docs.aws.amazon.com/serverless-application-model/latest/developerguide/install-sam-cli.html)
- Java 21 + Maven

## 1. Store config in SSM Parameter Store (once)

```bash
REGION=ap-southeast-1

aws ssm put-parameter --region $REGION --type SecureString \
  --name /config/margdarshak/spring.datasource.url \
  --value 'jdbc:postgresql://<neon-pooled-host>/margdarshak?sslmode=require&channelBinding=require'

aws ssm put-parameter --region $REGION --type SecureString \
  --name /config/margdarshak/spring.datasource.username --value '<neon-username>'

aws ssm put-parameter --region $REGION --type SecureString \
  --name /config/margdarshak/spring.datasource.password --value '<neon-password>'

aws ssm put-parameter --region $REGION --type SecureString \
  --name /config/margdarshak/jwt.secret --value "$(openssl rand -hex 64)"

aws ssm put-parameter --region $REGION --type SecureString \
  --name /config/margdarshak/url.shortener.base-url --value 'https://link.ajkumarray.com/'
```

### Updating a secret later

Add `--overwrite` to the command above for the one parameter you're changing. Then - because
SnapStart snapshots freeze whatever the app read at startup - force a fresh version so the change
actually takes effect:

```bash
aws lambda update-function-configuration --function-name margdarshak --region $REGION \
  --environment "Variables={SPRING_PROFILES_ACTIVE=lambda,CONFIG_REFRESH=$(date +%s)}"
aws lambda wait function-updated --function-name margdarshak --region $REGION
NEW_VERSION=$(aws lambda publish-version --function-name margdarshak --region $REGION --query 'Version' --output text)
aws lambda update-alias --function-name margdarshak --name live --function-version "$NEW_VERSION" --region $REGION
```

(`CONFIG_REFRESH` is a throwaway value just to make Lambda see the config as "changed" - a plain
`sam deploy`/`publish-version` is a no-op if nothing about the function itself changed, even if an
SSM value did. The next real `sam deploy` drops `CONFIG_REFRESH` again, which is harmless.) The
very first invocation of the new version pays a full cold init (~15s, no SnapStart restore yet) -
expect one slow or timed-out request immediately after, then it's fine.

## 2. Build the deployment artifact

```bash
mvn -P lambda -DskipTests clean package
```

Produces `target/margdarshak-lambda.zip` (the classes jar + `lib/*.jar` - see "Why not a single
uber-jar" below). `template.yaml` points `CodeUri` at this zip directly, so no `sam build` step.

## 3. Deploy

First time:

```bash
sam deploy --guided --region ap-southeast-1
```

Accept the defaults; say yes to letting SAM create IAM roles and to the Function URL having no
auth (auth is handled in-app via JWT, not at the infrastructure layer). This writes
`samconfig.toml`. Every deploy after that:

```bash
mvn -P lambda -DskipTests clean package
sam deploy --stack-name margdarshak --region ap-southeast-1 --resolve-s3 \
  --capabilities CAPABILITY_IAM --no-confirm-changeset --no-fail-on-empty-changeset
```

## 4. Smoke test

```bash
URL=https://api.url.ajkumarray.com   # or the raw Function URL from the resource table above

curl -s -X POST "$URL/api/v1/auth/register" -H 'Content-Type: application/json' \
  -d '{"name":"Test","email":"t@example.com","password":"secret123"}'

TOKEN=$(curl -s -X POST "$URL/api/v1/auth/login" -H 'Content-Type: application/json' \
  -d '{"email":"t@example.com","password":"secret123"}' | sed 's/.*"token":"\([^"]*\)".*/\1/')

curl -s -X POST "$URL/api/v1/url" -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"url":"https://example.com","expirationDays":30,"status":"ACTIVE"}'
# then: curl -sI https://link.ajkumarray.com/<code>   -> expect 302
```

Check the first (cold) invocation's `Init Duration` in CloudWatch Logs - with SnapStart it should
be well under a second after the first post-deploy call warms the snapshot.

## Custom domains

Function URLs can't take a custom domain directly, so CloudFront sits in front (see resource table
for the distribution ID/domain and cert ARN). To add another domain to it:

1. Extend the ACM cert (**us-east-1**, DNS validation) - request a cert (or a new one) with the
   extra name as a Subject Alternative Name.
2. Add the validation CNAME(s) ACM gives you at Porkbun; wait for `ISSUED`.
3. `aws cloudfront get-distribution-config --id E6ZI4H85ELRGA` -> edit `Aliases` (add the new
   domain) and `ViewerCertificate` (point at the new cert ARN) -> `aws cloudfront
   update-distribution --id E6ZI4H85ELRGA --if-match <ETag from step 3's output>`.
4. Add a CNAME at Porkbun: `<new-host>` -> `dhldqt46f9xnx.cloudfront.net`.
5. Wait for the distribution `Status` to go back to `Deployed` (5-15 min) before testing.

## Notes

- **SnapStart**: no extra charge on Java 21. The Spring context is built once at publish time and
  restored per invocation (~600-650ms restore vs. a ~15s cold JVM boot).
- `aws-serverless-java-container`'s default binary-content-type list is just
  octet-stream/jpeg/png/gif - anything else (JS, CSS, fonts) gets pushed through a lossy text
  round-trip unless registered as binary. `LambdaHandler` does this explicitly for
  `application/javascript`, `text/css`, fonts and SVG (this is what made Swagger UI render blank
  until fixed - the JS bundle was getting silently corrupted in transit).
- **Why not a single uber-jar**: shading merges every dependency's `META-INF/spring.factories`
  (a Java `.properties` file) by concatenation, and duplicate keys across jars mean the *last one
  wins* - this silently dropped `ConfigDataEnvironmentPostProcessor`, which broke all
  `application*.properties`/`.yml` loading with no obvious error. The `classes-jar + lib/*.jar`
  zip layout keeps every dependency's metadata intact.
- Local run of the lambda artifact (SSM import is `optional:`, falls back to env vars):
  ```bash
  cd target && unzip -o margdarshak-lambda.zip -d lam && cd lam
  # then invoke com.ajkumarray.margdarshak.LambdaHandler with a test harness, or use `sam local`
  ```
