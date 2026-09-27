# CS26 开发流程 / CS26 Development Workflow

## 目标 / Goal

CS26 优先完成 Android 可验收的真实业务，再逐步验证 iOS、Desktop 和 Knot 的复用能力。任何技术选择都必须服务于可运行、可测试、可演示的纵向流程。

CS26 first delivers real, verifiable Android flows, then validates iOS, Desktop, and Knot reuse incrementally. Every technical choice must support a runnable, testable, demonstrable vertical slice.

## 开发顺序 / Implementation Order

1. 固定 Gradle、JDK、Kotlin、AGP、Compose 和 Ktor 版本，提交 Gradle Wrapper。
2. 建立 `androidApp`、`shared`、`contracts` 和 `server`，先完成 health endpoint 纵向流程。
3. 按认证、动态、聊天、通知、资料实现，每个切片同时提交契约、测试和验收证据。
4. 使用 fake/local adapter 开发短信、OSS、ntfy 等外部依赖；真机验证前不承诺送达能力。
5. 真实功能稳定后，再提取到 Knot，并在独立 PR 中升级依赖。

1. Pin Gradle, JDK, Kotlin, AGP, Compose, and Ktor versions and commit the Gradle Wrapper.
2. Create `androidApp`, `shared`, `contracts`, and `server`, starting with a health-endpoint vertical slice.
3. Implement auth, feed, chat, notices, and profile in slices; submit contracts, tests, and acceptance evidence together.

M1 当前使用服务端内存假适配器和固定开发验证码 `123456` 验证端到端流程。适配器已包含验证码哈希、过期、五次尝试上限、重复发送冷却和会话撤销；它只用于本地开发和 CI，不代表真实短信验收。接入短信和 PostgreSQL 前仍需把这些边界迁移到持久化实现。

M1 currently uses an in-memory server adapter and the fixed development code `123456` to verify the end-to-end flow. The adapter covers code hashing, expiry, a five-attempt limit, resend cooldown, and session revocation; it is for local development and CI only, not real SMS acceptance. These boundaries must move to persistent storage before SMS and PostgreSQL integration.
4. Use fake/local adapters for SMS, OSS, ntfy, and other external dependencies; do not claim delivery before device validation.
5. Extract to Knot only after real behavior is stable, then upgrade the dependency in a separate PR.

## 本地命令 / Local Commands

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
export ANDROID_HOME="$HOME/Library/Android/sdk"
export PATH="$JAVA_HOME/bin:$ANDROID_HOME/platform-tools:$PATH"

./gradlew check
./gradlew :androidApp:assembleDebug
./gradlew :server:test
```

本地 PostgreSQL profile 使用 `CS26_DATABASE_URL`、`CS26_DATABASE_USER` 和 `CS26_DATABASE_PASSWORD`；先执行 `server/src/main/resources/db/migration/V1__auth.sql`。未配置这些变量时，服务端继续使用内存适配器，适合快速测试。

The local PostgreSQL profile uses `CS26_DATABASE_URL`, `CS26_DATABASE_USER`, and `CS26_DATABASE_PASSWORD`; apply `server/src/main/resources/db/migration/V1__auth.sql` first. Without these variables, the server keeps using the in-memory adapter for fast tests.

当前基线使用 JDK 21、Gradle 9.3.1 Wrapper、AGP 9.1.0、Kotlin 2.3.21、Ktor 3.6.0 和 Android API 35。AGP 9 的 KMP 共享模块使用 `com.android.kotlin.multiplatform.library`，Android 应用入口保持独立。JDK 21 是本机和 CI 的统一工具链，满足 AGP 9 的 JDK 17 最低要求。

The current baseline uses JDK 21, the Gradle 9.3.1 Wrapper, AGP 9.1.0, Kotlin 2.3.21, Ktor 3.6.0, and Android API 35. KMP shared modules use `com.android.kotlin.multiplatform.library` with AGP 9, while the Android application entry point remains separate. JDK 21 is the shared local and CI toolchain and satisfies AGP 9's JDK 17 minimum.

CI 只执行仓库中已经本地验证过的命令。Android 模拟器测试使用单一 API，按手动、每周回归或发布前流程运行。

CI runs only commands already verified locally. Android emulator tests use one API level and run manually, weekly, or before a release.

## 证据与配置 / Evidence and Configuration

在 `docs/evidence/` 记录需求编号、提交号、测试步骤、设备、构建类型和结果。敏感配置只存在于本地环境或 GitHub Environments；提交 `.env.example`，不提交真实值。

Record requirement IDs, commit SHAs, test steps, devices, build types, and results in `docs/evidence/`. Keep sensitive configuration in local environments or GitHub Environments; commit `.env.example` without real values.
