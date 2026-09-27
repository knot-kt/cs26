# Delivery Status / 交付状态

Updated: 2026-09-27

## M0-M6 / 阶段状态

| Milestone | Status | Verified scope / 已验证范围 | Remaining / 剩余 |
| --- | --- | --- | --- |
| M0 | Complete / 完成 | Kotlin, Compose, Ktor, PostgreSQL migrations, CI and PR gate | Final task-sheet difference confirmation |
| M1 | Foundation / 基础完成 | Local SMS flow, session lookup/revocation, PostgreSQL adapter | Real SMS, third-party auth, device credential storage |
| M2 | In progress / 进行中 | Text feed, image picker, local upload, media metadata, like, comment, owner delete | OSS adapter, camera capture, Android interaction screens |
| M3 | In progress / 进行中 | Message history, client-ID idempotency, WebSocket server/client, chat media metadata | Audio capture/playback, device reconnect evidence |
| M4 | In progress / 进行中 | Announcement records, unread/read sync, deep links, realtime stream, Android list | ntfy/UnifiedPush background delivery evidence |
| M5 | Foundation / 基础完成 | Debug APK build, server distribution, package workflow, evidence template | Signed release, deployment runbook, performance measurements |
| M6 | Preparation / 准备中 | Knot extraction gate and cross-repository policy | First reusable module and second rebuildable example |

## Evidence / 证据

Local verification uses JDK 21 and the installed Android SDK:

```text
./gradlew :server:test
./gradlew check :androidApp:assembleDebug
./gradlew :server:installDist
```

Pull requests are required for both repositories. CS26 PR checks run the full JVM and Android checks; the tag/manual package workflow is intentionally separate to control the 2,000-minute organization budget.

本地验证使用 JDK 21 和已安装的 Android SDK。两个仓库都要求通过 PR 更新主分支；CS26 PR 执行完整 JVM/Android 检查，打包 workflow 只在标签或手动触发时运行，以控制组织每月 2000 分钟 Actions 预算。

## Next slice / 下一切片

Complete M3 media delivery and M4 background delivery evidence, then implement signed packaging and performance records before extracting stable boundaries to Knot.

先完成 M3 媒体消息和 M4 后台送达证据，再做签名安装包与性能记录，最后把稳定边界提取到 Knot。