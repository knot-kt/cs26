# Release and Deployment / 发布与部署

## Package / 安装包

`CS26 Package` runs only for a `v*` tag or manual dispatch. It runs the full Gradle checks and uploads the unsigned Debug APK as a short-lived artifact. This package is for local acceptance and is not a production release.

`CS26 Package` 只在 `v*` 标签或手动触发时运行。它执行完整 Gradle 检查并上传未签名 Debug APK，artifact 保留 14 天。该包用于本地验收，不代表生产发布包。

Before a signed release, add signing configuration through GitHub Environment secrets and document the keystore rotation owner. Never commit a keystore or signing password.

正式签名发布前，需要通过 GitHub Environment secrets 配置签名，并记录密钥轮换负责人。不得提交 keystore 或签名密码。

## Server / 服务端

1. Build with JDK 21: `./gradlew :server:installDist`.
2. Apply `server/src/main/resources/db/migration/` in filename order against PostgreSQL.
3. Set `CS26_DATABASE_URL`, `CS26_DATABASE_USER`, `CS26_DATABASE_PASSWORD`, and optionally `CS26_MEDIA_DIR`.
4. Start the distribution and verify `GET /health` before accepting traffic.

1. 使用 JDK 21 执行 `./gradlew :server:installDist`。
2. 按文件名顺序将 `server/src/main/resources/db/migration/` 应用到 PostgreSQL。
3. 配置 `CS26_DATABASE_URL`、`CS26_DATABASE_USER`、`CS26_DATABASE_PASSWORD`，可选配置 `CS26_MEDIA_DIR`。
4. 启动发行目录，先验证 `GET /health` 再接收流量。

Production deployment, backups, TLS, and OSS credentials remain environment-owned tasks. Keep `.env.example` free of values.

生产部署、备份、TLS 和 OSS 凭证由部署环境管理，`.env.example` 只保留字段名。