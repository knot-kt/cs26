# CS26 实施架构与开发路线

## 起步结构

M0 已建立 `androidApp`、`shared`、`contracts` 和 `server` 四个模块，并验证 Android debug 构建、共享测试、服务端 `/health` 测试。M0.5 将 Compose 启动页和 Ktor Client 健康检查接通；业务页面按后续切片实现。

M0 now contains the four modules `androidApp`, `shared`, `contracts`, and `server`, with Android debug assembly, shared tests, and the server `/health` test verified. M0.5 connects the Compose launcher screen to a Ktor Client health check; business pages follow in later slices.

建议先保持少量职责明确的模块，命名在工程创建时固定。完整技术基线见 Knot 的 [STACK.md](https://github.com/knot-kt/knot/blob/main/docs/STACK.md)：

```text
androidApp/     Android 启动、权限及 SDK 配置
shared/         共享 UI、状态、Repository、网络及本地存储
contracts/      客户端与服务端的传输契约、WebSocket 协议
server/         Ktor 路由、业务服务、数据库及云服务适配
iosApp/         iOS 宿主和签名配置，按阶段加入
desktopApp/     桌面入口，按阶段加入
docs/          需求、决策、开发和验收证据
```

业务先在模块内部按 auth、feed、chat、notice、profile 分包。真正需要独立编译或复用时再拆 feature 模块，避免工程启动阶段出现几十个空模块。

客户端采用 Compose → 状态持有者 → Repository → 网络/SQLite。服务端采用路由 → 业务服务 → 数据访问和云服务适配器。PostgreSQL 是权威数据，SQLite 是缓存、草稿和待发送队列；客户端不能直接访问 PostgreSQL。

## 首批技术验证

1. 锁定兼容的 Kotlin、CMP、Ktor、AGP、Gradle 和 JDK 版本。
2. Android 页面调用 Ktor 健康接口，服务端读写 PostgreSQL。
3. 验证 `commonTest` 的 `kotlin.test + Kotest`、服务端 Kotest 和 Android Compose UI Test + AndroidX Test。
4. 使用 SQLDelight 验证 SQLite schema、迁移、查询和 KMP 目标平台驱动。
5. 如使用 Exposed JDBC，验证事务调度和连接池，避免在主线程或事件线程执行阻塞查询。

选定结果写入 ADR 和版本目录，后续不无故更换工具链。

## 关键业务设计

认证：服务端负责验证码校验、第三方身份校验、账号绑定和 Token 签发。验证码哈希保存并设置期限、尝试次数和频率限制；Access Token 短期有效，Refresh Token 可撤销。凭证通过平台安全存储接口管理，不把明文 Token 简单当作普通偏好设置。

媒体：服务端创建上传记录、限制对象路径和文件类型，签发短期上传权限。上传完成后确认归属和有效性，再关联动态或消息。图片与语音放 OSS，数据库保存对象标识和元数据；私聊媒体采用受控访问，不保存永久公开地址。

聊天：协议包含协议版本、客户端消息 ID、服务端消息 ID、会话 ID、类型和序号。服务端校验会话成员，幂等入库后确认；客户端以状态区分发送中、已保存和失败。重复投递不产生重复消息，按服务端顺序补拉；不能承诺网络“恰好一次”投递。

通知：保存通知与用户接收记录，权限控制发布，在线下发事件，客户端补拉未读。使用事件 ID 去重。推送只用于提示或触发同步，数据库才是权威记录。需要可靠重试时采用事务 outbox，初期无需引入消息队列。

## 推送决策

已确定的通知方案为 Ktor 通知中心 + PostgreSQL + WebSocket + 自托管 ntfy。Ktor 是业务通知的唯一入口，PostgreSQL 是权威记录，WebSocket 负责在线下发，ntfy 是后台推送网关，Android Notification API 负责本地展示。该方案不能把 WebSocket 的在线送达等同于后台系统推送。

ntfy 服务端为 Go，不属于 Kotlin 源码；它作为已选定的外部基础设施运行。仅部署 ntfy 服务端不会自动唤醒 CS26。Android 需实现 UnifiedPush 接入并安装 distributor，或采用其他明确验证过的客户端集成方案；不能把 ntfy APP 自己收到通知当作 CS26 完成集成。

自托管 ntfy 的 Android 常驻接收通常涉及前台服务和设备后台策略；iOS 即时远程通知仍涉及 APNs，现成 ntfy iOS 客户端的转发链路也不是 CS26 的直接推送实现。先在目标设备做锁屏、后台、进程回收和强制停止测试，明确可达范围后再定案。强制停止不承诺立即可达。

CS26 使用 ntfy 的 HTTP 发布接口；Android 端采用 ntfy / UnifiedPush 接收适配，不依赖用户另外打开 ntfy 客户端作为产品功能。启用认证、主题访问控制和 HTTPS，不把随机主题名当作权限机制；推送内容尽量只含事件标识。

## 分阶段交付

| 阶段 | 可演示成果 | 进入下一阶段的条件 |
| --- | --- | --- |
| M0 需求与原型 | 工具链、基础页面、接口和数据库连通 | 差异清单明确；最小测试可运行 |
| M1 认证 | 两个用户完成短信和第三方登录 | 真实授权及退出/失效测试通过 |
| M2 动态 | 真实拍照、选图、OSS 上传、发布和互动 | 完整发布流程及权限可验证 |
| M3 聊天 | 文字、图片、录音收发与历史同步 | 重连、重试、幂等验证通过 |
| M4 通知 | 公告管理、未读同步及通知跳转 | 后台推送结论与验收要求一致 |
| M5 优化与交付 | 安装包、部署、测试和性能报告 | Android 核心需求全部闭环 |
| M6 基座沉淀 | Knot 可复用能力、多端演示 | CS26 功能保持通过且示例可重建 |

Koog 作为 Ktor 服务端独立 Agent 模块，在核心功能完成后加入话题推荐、校园问答或内容辅助。模型密钥只在服务端，Agent 失败不阻塞登录、发布、聊天和通知。

## 验证重点

- 业务：过期验证码、越权读写、重复提交、分页、消息乱序和重复事件。
- 设备：权限拒绝、旋转、后台切换、退出登录、网络断开、录音中断。
- 性能：release 或 benchmark 构建测量启动，Heap Dump 分析引用链和内存趋势。
- 工具边界：Profiler 看分配与堆，Perfetto 看时序，StrictMode 看违规；均不等同自动泄漏检测。
- 部署：空库迁移、重启恢复、外部服务失败、密钥配置、备份恢复。

## 官方参考

- [Android 架构建议](https://developer.android.com/topic/architecture/recommendations)
- [Compose UI 测试](https://developer.android.com/develop/ui/compose/testing)
- [Android 后台任务限制](https://developer.android.com/develop/background-work/background-tasks)
- [Room KMP](https://developer.android.com/kotlin/multiplatform/room)
- [ntfy 手机接收与 UnifiedPush](https://docs.ntfy.sh/subscribe/phone/)
- [ntfy 服务端配置](https://docs.ntfy.sh/config/)
