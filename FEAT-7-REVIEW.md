# FEAT-7 review: refresh token branch

Review of `feature/FEAT-7-implement-refresh-token` against `master` (2026-10-27). The branch is merged (`12f5ab1`) and deployed as Lambda version 17 (2026-10-27). **This file is also the tracker for next tasks**; see [Next tasks](#next-tasks) at the bottom.

## Overdone (can be simplified)

1. ✅ **Fixed.** **`flushAutomatically` / `clearAutomatically` on every `@Modifying` query.** These were added only so the old DB-backed tests wouldn't read stale cached rows. Those tests are gone, so plain `@Modifying` is enough again in `RefreshTokenRepository` (2 queries) and `UrlRepository.incrementClickCount`.
2. ✅ **Fixed** (`revoke(jti, userCode)`; `existsByJtiAndUserCodeAndDeleted` for logout-all). **Two DB calls where one would do.**
   - In `UserImplementation.refresh()`, `findByJti` is followed by `revoke`, and the lookup only checks that `userCode` matches. Adding `AND r.userCode = :userCode` to `revoke`'s `WHERE` makes it one atomic query.
   - Same for the "is the session active" check in `logout(..., all = true)`.
3. ✅ **Partly fixed:** `AuthContextInterceptor` now parses once via `getAccessClaims()` (5 → 3 parses). The filter is unchanged by choice, and the dead `getRoleFromToken` is removed. **The token is parsed 5 times per authenticated request.** The filter calls `validateToken` and `getUserCodeFromToken`; `AuthContextInterceptor` calls `validateToken`, `getUserCodeFromToken` and `getRoleFromToken`. Each parse is fast, but reusing one `getClaims()` result would be cleaner. That means touching the filter, which was deliberately left unchanged.
4. ⏭️ **Kept by choice.** **Belt and braces on the refresh token:** a separate secret (`jwt.refresh-secret`) and a `typ` claim. Either alone keeps the two token kinds apart. Both was a deliberate choice, so this is fine, just more than strictly needed.
5. ⏭️ **Kept** (the conversion was requested); split it into its own commit. **Scope creep in the diff.** The `//` → `/* */` comment conversion touched `LambdaHandler`, `RedirectController` and `CommonFunctionHelper`, which have nothing to do with refresh tokens. Harmless, but it makes this PR noisier. Consider a separate commit.
6. ✅ **Fixed** (trimmed in `JwtTokenProvider` and `UserImplementation`). **Some comments are long** compared with the rest of the codebase, e.g. the Javadoc in `JwtTokenProvider` and the comment on `refresh()`.

## Underdone (gaps / real issues)

1. ✅ **Fixed** (`findByEmailAndStatusAndDeleted(email, ACTIVE, false)`, test added). **Login doesn't check the user's status.** `login()` looks the user up by email only, so a `BLOCKED` or deleted user can still log in and get tokens, while `refresh()` correctly rejects them. This predates the branch but is now inconsistent. It should require `ACTIVE` and not deleted, as `refresh()` does.
2. **The app can't start locally right now.**
   - The local `application.properties` has no `jwt.refresh-secret`, and `@Value` has no default, so startup fails.
   - Liquibase now runs on local startup against whatever DB that file points to. If it points at Neon, it applies `0002` to prod.
3. ✅ **Fixed** (nirdeshak FEAT-6, live-tested 2026-11-14). **Nirdeshak frontend.**
   - Auth tokens drop from 7 days to 24h, and every existing token stops working on deploy (no `typ` claim).
   - Until the frontend calls `/auth/refresh` on a 401, users have to log in daily.
   - The frontend must keep two tabs from refreshing at once, or one of them gets a 401.
4. **Test coverage dropped where it mattered most.** The deleted web-slice tests covered:
   - the `/api/v1/public/**` permitAll fix, which was a real bug
   - the "no token → 403" guard for nirdeshak
   - the controller wiring (`@RequestAttribute`, `?all`)

   None of that is tested now, and neither are the JPQL queries. A manual smoke test after deploy is the only safety net.
5. **Swagger.** `bearerAuth` is still not defined, so there's no Authorize button. Swagger UI also drops a header named `Authorization` when it's listed as a parameter, so `/auth/refresh` and `/auth/logout` can't be tried from Swagger at all.
6. **Docs.** `README.md:161` still lists "token expiration and refresh mechanism" as a to-do. Nothing documents the new `/auth/refresh` and `/auth/logout` contract for frontend work:
   - `Authorization: Bearer <refresh token>`
   - `?all=true`
   - `M01212` / `M01213` / `M01214`
7. ✅ **Fixed** (nirdeshak FEAT-6 clears tokens on a refresh 401). **Logout needs a valid, unexpired refresh token.** After 7 days of inactivity the client can't call logout. That's acceptable, since the session is dead anyway, but the frontend should just clear its tokens on a 401.

## Pre-existing (not from this branch, worth knowing)

- The CORS origin `https://url.ajkumarray.com/` has a trailing slash, so it never matches. Check how nirdeshak's browser calls get through today before relying on the new endpoints.
- `ApplicationExceptionHandler` is a growing chain of `if` statements mapping codes to statuses.
- Interceptor and security paths are hard-coded as `/api/v1/...`, while controllers use `${spring.api}`.
- `GET /user/{userCode}` lets any logged-in user read any user, including their role.

## Deployment (2026-10-27)

1. ⏳ **Yours, local only:** add `jwt.refresh-secret` to the local `application.properties`. Ideally also set `spring.jpa.hibernate.ddl-auto=validate`, and point it at the local DB.
2. ✅ SSM `/config/margdarshak/jwt.refresh-secret` created (version 1).
3. ✅ `mvn liquibase:update` on Neon: `Run: 1`, `refresh_token` created.
4. ✅ `mvn -P lambda -DskipTests clean package` + `sam deploy`. Lambda version 17 is live, and its code hash matches the build.
5. Smoke test:
   - ✅ Read-only checks passed:
     - `GET /` → 302
     - `/api/v1/url` without a token or with an invalid one → 403
     - `/auth/refresh` without a token → 401 `M01212`
     - `/auth/logout?all=true` with a bad token → 401
     - bad login → 401
     - `/public/url` → 405
     - `/site/ping` → 403
     - unknown code → 404
   - ✅ **With a real account** (live-tested through nirdeshak FEAT-6, 2026-11-14):
     - login returns `lc.refreshToken`
     - refresh returns a new pair, and reusing the old token returns 401
     - logout, then refresh with the same token returns 401

## Next tasks

Tick these off here as they're done.

### 1. Refresh-token cleanup (next branch)
- [x] A daily midnight job (00:00 IST) that hard-deletes `refresh_token` rows where `deleted = true OR expires_at < now()`. Implemented 2026-11-14, not deployed yet.
- [x] An EventBridge `Schedule` on the existing function (option A, not a second function). `LambdaHandler` routes `"source": "aws.events"` to `UserService.cleanupRefreshTokens()` through `SpringContextHolder`.
- [x] A `template.yaml` schedule event, plus "1c. Scheduled jobs" in `DEPLOY-LAMBDA.md` (with a manual-invoke command).
- [ ] Deploy (`package` + `sam deploy`; no migration or SSM changes), then invoke it once by hand and check CloudWatch for the removed-row count.

### 2. Decisions needed before the Site API admin endpoints
- [ ] Portfolio login gate: a shared-secret `X-Client-Key` header (only the portfolio server may log in as admin)? And should `/site/**` accept only portfolio-issued tokens (`aud` claim)?
- [ ] Unauthenticated `/site/**`: keep the default 403, or return 401 as the original spec asked?
- [ ] Swagger `bearerAuth`: add it, which also lets `/auth/refresh` and `/auth/logout` be tried from Swagger UI (Underdone #5).

### 3. Portfolio Site API
- [ ] Step 4: changeset `0003-site-tables.sql` (`site_content`, and `site_post` with a slug unique among non-deleted posts), plus entities and repositories.
- [ ] Step 5: site response codes and messages, plus the 404/409 mappings in `ApplicationExceptionHandler`.
- [ ] Step 6: the content API.
  - `GET /api/v1/public/site/content`
  - admin-only `PUT /api/v1/site/content`
- [ ] Step 7: the posts API.
  - public list and detail, published posts only
  - admin CRUD with slug validation
- [ ] Step 8: `SITE-API.md` with the contract, then deploy.

### 4. Nirdeshak frontend (outside this repo)
- [x] Call `/auth/refresh` on a 401, retry the request, and let only one tab refresh at a time (Underdone #3). Done in nirdeshak FEAT-6; it refreshes on 401 and on the empty-body 403 Spring returns for an expired access token (not on `M01211`). Live-tested 2026-11-14.
- [x] Send `/auth/logout` (or `?all=true`) on sign-out, and clear the stored tokens whenever refresh returns 401 (Underdone #7). Done in nirdeshak FEAT-6; `?all=true` isn't used by the UI yet. Live-tested 2026-11-14.

### 5. Smaller items
- [ ] Docs: update `README.md:161` and document the `/auth/refresh` and `/auth/logout` contract (Underdone #6).
- [ ] Fix the CORS origin `https://url.ajkumarray.com/` (trailing slash).
- [ ] Restrict `GET /user/{userCode}` to the caller's own user.
- [ ] Consider bringing back lightweight coverage for the permitAll fix, the no-token 403 and the controller wiring (Underdone #4).
