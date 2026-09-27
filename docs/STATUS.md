# Delivery Status / 交付状态

Updated: 2026-09-28

## M0-M6 / 阶段状态

| Milestone | Status | Verified scope / 已验证范围 | Remaining / 剩余 |
| --- | --- | --- | --- |
| M0 | Complete / 完成 | Kotlin/Compose/Ktor modules, PostgreSQL migrations, health endpoint, CI and PR gate; local checks pass | Final task-sheet difference confirmation |
| M1 | Foundation / 基础完成 | Development SMS flow, session lookup/revocation, PostgreSQL auth adapter; real-device sign-in verified | Real SMS, third-party auth, secure device token storage (deferred, does not block local work) |
| M2 | In progress / 进行中 | Text and attachment-only feed posts, picker/camera capture, local multipart upload, media metadata, like/comment/delete; Pixel 6 smoke test passed | OSS adapter and production bucket credentials (deferred), pagination/detail evidence |
| M3 | In progress / 进行中 | History, client-ID idempotency, WebSocket server/client, message receipts, retry-safe resend, image/audio capture, authenticated playback; real-device recording/upload passed | Intentional device disconnect/reconnect evidence and two-device delivery evidence |
| M4 | In progress / 进行中 | Announcement records, unread/read sync, deep links, realtime stream, Android reconnect state; Pixel 6 online stream passed | ntfy/UnifiedPush background delivery and provider credentials (deferred) |
| M5 | Foundation / 基础完成 | Debug APK, configurable device endpoint, server distribution, package workflow, performance capture script/template; APK installed on Pixel 6 | Signed release, deployment runbook, recorded performance report |
| M6 | In progress / 进行中 | Knot `IdempotencyCache` and the `knot init` Android/Ktor scaffold are merged and rebuilt by CI; default output is `knot-app` | Versioned CS26 integration, more reusable modules, published template artifacts |

## Evidence / 证据

Local verification uses JDK 21 and the installed Android SDK. The current physical-device build is:

```text
./gradlew :server:test
./gradlew check :androidApp:assembleDebug
./gradlew :server:installDist
./gradlew :androidApp:assembleDebug -Pcs26BaseUrl=http://127.0.0.1:8080
$HOME/Library/Android/sdk/platform-tools/adb install -r androidApp/build/outputs/apk/debug/androidApp-debug.apk
```

Pull requests are required for both repositories. CS26 PR checks run the full JVM and Android checks; the tag/manual package workflow is intentionally separate to control the 2,000-minute organization budget.

本地验证使用 JDK 21 和已安装的 Android SDK。两个仓库都要求通过 PR 更新主分支；CS26 PR 执行完整 JVM/Android 检查，打包 workflow 只在标签或手动触发时运行，以控制组织每月 2000 分钟 Actions 预算。

## Next slice / 下一切片

Use `docs/PRODUCT-SPEC.md` as the product source of truth, then implement the social shell and plaza states. In parallel, collect M3 retry/receipt/reconnect evidence, capture a real-device performance record, and prepare the signed-package checklist. Keep real SMS/OAuth, OSS, push provider and deployment credentials explicitly deferred. Integrate a pinned Knot scaffold only after the CS26 evidence is repeatable.

下一步以 `docs/PRODUCT-SPEC.md` 作为产品依据，实现社交主框架和广场状态；同时补 M3 重试/回执/重连证据，采集真机性能记录并准备签名包清单。真实短信/OAuth、OSS、推送供应商和部署密钥继续明确搁置，不阻塞本地开发。CS26 证据可重复后，再接入固定版本的 Knot 脚手架并沉淀更多能力。