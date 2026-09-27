# ADR 0001: Authentication Persistence Boundary

## 状态 / Status

已接受。当前只实现内存适配器；PostgreSQL 迁移脚本已建立边界。

Accepted. The in-memory adapter is active for local development; the PostgreSQL migration defines the persistence boundary.

## 决策 / Decision

认证规则通过 `AuthService` 使用 `AuthStore`，不直接依赖数据库。`InMemoryAuthStore` 用于本地开发和 CI；后续 PostgreSQL 实现替换存储端口，不改变 Ktor 路由、契约或 Android 客户端。

Authentication rules access storage through `AuthService` and `AuthStore`, never through database code directly. `InMemoryAuthStore` serves local development and CI; a PostgreSQL implementation will replace the port without changing Ktor routes, contracts, or the Android client.

验证码只保存哈希、过期时间、重发冷却和剩余尝试次数。会话表只保存 access token 哈希、用户、过期和撤销状态。真实短信适配器和 PostgreSQL 连接必须在后续变更中单独验证。

Only verification-code hashes, expiry, resend cooldown, and remaining attempts are stored. Sessions store an access-token hash, user, expiry, and revocation state. The real SMS adapter and PostgreSQL connection require separate follow-up validation.

## Alternatives

- 让路由直接操作 Exposed/JDBC：边界泄漏到 HTTP 层，测试和替换成本更高。
- 继续使用进程内 Map：无法跨实例、重启恢复或审计。
- 把明文 Token 写入数据库：撤销方便，但泄露影响过大，拒绝。

## Verification

`server/src/main/resources/db/migration/V1__auth.sql` is reviewed with the store interface. Current behavior remains covered by `./gradlew :server:test`; PostgreSQL integration will be added when a local database profile is introduced.
