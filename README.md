# CS26

基于 Kotlin Multiplatform、Compose Multiplatform 和 Ktor 的校园社交应用。

CS 表达计算机专业背景，26 纪念作者 26 岁本科毕业。应用名称统一为 **CS26**，仓库和标识使用 `cs26`。

## 项目目标

完成「基于 Android Studio 的校园社交 APP 开发（原生开发）」毕业设计，交付可以运行、部署和验收的 Android 应用与服务端；同时用真实业务验证 [Knot](https://github.com/knot-kt/knot) 脚手架设计。

Knot 是逐步沉淀的基座，CS26 是完整产品。当前已完成 M0.5 工程骨架、Compose 启动页和 health endpoint 纵向验证，以及 M1 本地假短信登录切片；真实短信尚未接入。

## 计划功能

- 手机验证码注册与登录、至少一种第三方登录、退出和会话管理。
- 拍照、选图、文字编辑、话题标签及动态发布。
- 一对一文字、图片、语音聊天，历史记录与断线恢复。
- 校园通知发布、通知中心、未读状态和通知跳转。
- 用户资料、动态详情、评论点赞等配套流程。

Android 是完整验收端；iOS 和 macOS Desktop 按计划验证共享能力。AI Agent 是后续扩展，不能影响基础功能交付。

## 文档入口

- [需求、范围与验收](docs/REQUIREMENTS.md)
- [实施架构与开发路线](docs/IMPLEMENTATION.md)
- [交付状态 / Delivery status](docs/STATUS.md)
- [开发流程 / Development Workflow](docs/DEVELOPMENT.md)
- [贡献指南 / Contributing](CONTRIBUTING.md)

## 技术选型

| 领域 | CS26 选型 |
| --- | --- |
| 客户端 UI | Compose Multiplatform |
| 客户端网络 | Ktor Client |
| 服务端 | Ktor Server |
| 服务端数据库 | PostgreSQL |
| 客户端数据库 | SQLite + SQLDelight |
| 在线通知 | Ktor + PostgreSQL + WebSocket |
| 后台推送 | 自托管 ntfy |
| Android 通知展示 | Android Notification API |
| 业务测试 | kotlin.test + Kotest |
| Android UI 测试 | Compose UI Test + AndroidX Test |
| 内存与性能 | Android Studio Profiler、Heap Dump、Perfetto、StrictMode |
| AI Agent | Ktor 服务端 + Koog |
| 文件存储 | 阿里云 OSS |
| 短信 | 阿里云短信服务 |

所有手写业务、服务端和测试代码使用 Kotlin。AndroidX Test 只作为 Android UI 测试基础设施；ntfy 作为自托管后台推送网关；Koog 作为服务端可选 Agent，不阻塞核心业务。

通知链路固定为：Ktor 通知中心负责业务和权限，PostgreSQL 保存权威记录，WebSocket 负责在线通知，ntfy 负责后台推送，Android Notification API 负责本地展示。后台推送的实际可达范围必须通过 Android 真机测试记录。

## 运行与部署

M0 本地验证命令：

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
export ANDROID_HOME="$HOME/Library/Android/sdk"
./gradlew check :androidApp:assembleDebug
./gradlew :server:run
```

服务端 health endpoint 为 `GET /health`，开发认证接口为 `POST /auth/code/request`、`POST /auth/code/verify` 和 Bearer Token 退出接口 `POST /auth/logout`。固定开发验证码为 `123456`，适配器已验证过期、尝试次数、重复发送冷却和会话撤销边界。Android 启动页已使用 Compose 渲染，并通过 Ktor Client 检查本机服务；模拟器访问宿主机使用 `10.0.2.2`。开发认证接口不能用于生产；生产部署尚未开始，任何示例配置都不得包含真实密钥。

服务端默认使用内存存储；设置 `CS26_DATABASE_URL`、`CS26_DATABASE_USER` 和 `CS26_DATABASE_PASSWORD` 后使用 PostgreSQL 存储。启动前先执行 `server/src/main/resources/db/migration/V1__auth.sql`，凭据只放在本地环境或 GitHub Environment。

## 毕设与长期维护

毕业设计需求和证据保存在本仓库；可复用能力及通用工程约定逐步沉淀到 Knot。正式开源发布前确定许可证。