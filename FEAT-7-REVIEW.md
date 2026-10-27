# FEAT-7 review: refresh token branch

Review of `feature/FEAT-7-implement-refresh-token` against `master` (2026-10-27). Nothing has been changed yet; this is a list to act on.

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
3. **Nirdeshak frontend.**
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
7. **Logout needs a valid, unexpired refresh token.** After 7 days of inactivity the client can't call logout. That's acceptable, since the session is dead anyway, but the frontend should just clear its tokens on a 401.

## Pre-existing (not from this branch, worth knowing)

- The CORS origin `https://url.ajkumarray.com/` has a trailing slash, so it never matches. Check how nirdeshak's browser calls get through today before relying on the new endpoints.
- `ApplicationExceptionHandler` is a growing chain of `if` statements mapping codes to statuses.
- Interceptor and security paths are hard-coded as `/api/v1/...`, while controllers use `${spring.api}`.
- `GET /user/{userCode}` lets any logged-in user read any user, including their role.

## Before deploying this branch

1. Add `jwt.refresh-secret` to the local `application.properties`. Ideally also set `spring.jpa.hibernate.ddl-auto=validate`, and make sure it points at the local DB.
2. Create the SSM parameter:
   ```bash
   aws ssm put-parameter --region ap-southeast-1 --type SecureString \
     --name /config/margdarshak/jwt.refresh-secret --value "$(openssl rand -hex 64)"
   ```
3. Run `mvn liquibase:update` against Neon. It should report `Run: 1` (creates `refresh_token`).
4. Run `mvn -P lambda -DskipTests clean package`, then `sam deploy`.
5. Smoke test:
   - login, refresh, logout, logout `?all=true`
   - `/api/v1/url` with and without a token
   - anonymous `/api/v1/public/url`
   - a redirect

## Suggested first fixes

Underdone #1 (the login status check), then overdone #1 and #2 (plain `@Modifying`, one query instead of two). They're small, and they make the code both simpler and more correct.
