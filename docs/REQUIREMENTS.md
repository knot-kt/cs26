# CS26 需求、范围与验收

状态：设计基线；M0.5 工程骨架和 health endpoint 已实现，M1 本地假短信登录切片进行中，真实业务需求尚未完成。原任务书作为验收依据；替代技术方案需要导师认可，当前不假定认可已经取得。

## 已确定的实现技术

```text
客户端：Compose Multiplatform + Ktor Client + SQLite
服务端：Ktor Server + PostgreSQL
在线通知：Ktor + PostgreSQL + WebSocket
后台推送：自托管 ntfy
测试：kotlin.test + Kotest；Android UI 使用 Compose UI Test + AndroidX Test
内存与性能：Android Studio Profiler、Heap Dump、Perfetto、StrictMode
Agent：Ktor 服务端 + Koog
```

ntfy 只负责后台推送，Ktor 通知中心负责通知记录、权限、未读状态和跳转数据；Koog 只负责可选的服务端 AI 能力。

## 必须完成的用户流程

| 编号 | 能力 | 最低验收标准 |
| --- | --- | --- |
| AUTH-01 | 手机注册和登录 | 真实短信验证；过期、错误、重复发送限制；登录后可退出 |
| AUTH-02 | 第三方登录 | 至少一种真实授权流程；服务端验证身份并绑定账户 |
| FEED-01 | 动态发布 | 拍照或选图、文字、标签、上传、发布成功后可查看 |
| FEED-02 | 动态浏览 | 分页、详情、作者信息；错误与空状态；本人可删除 |
| FEED-03 | 社交互动 | 点赞、评论、取消操作及基本访问权限 |
| CHAT-01 | 文字聊天 | 两个真实用户双向收发、历史查询、未读计数 |
| CHAT-02 | 图片和语音 | OSS 上传、图片预览、语音录制及播放、失败重试 |
| CHAT-03 | 可靠性 | 消息幂等、确认回执、重连与缺失消息补拉 |
| NOTICE-01 | 校园通知 | 有权限账号发布，用户查看列表、详情和已读状态 |
| NOTICE-02 | 通知送达 | 在线实时通知，离线保存并补拉；后台推送能力单独验收 |
| PROFILE-01 | 个人资料 | 昵称、头像、本人动态和基本设置 |
| QUALITY-01 | 测试与优化 | 自动化测试及启动、内存的可复现实验记录 |

“语音发送”指录音文件消息，不包含实时语音通话。第三方登录提供商取决于可申请的开发者资质，应在开始实现前选定。

## 原任务书映射

| 原要求 | 拟定实现 | 尚需确认的差异 |
| --- | --- | --- |
| Android Studio / Kotlin / 原生开发 | Android Kotlin + Compose，KMP 共享逻辑 | 是否接受共享 CMP UI 的表述 |
| Node.js / Express | Ktor Server | 替代服务端技术栈 |
| MongoDB | PostgreSQL | 替代服务端数据库 |
| Socket 聊天 | Ktor WebSocket | 是否接受 WebSocket；不等同于裸 TCP Socket |
| JPush 校园推送 | Ktor 通知中心 + PostgreSQL + WebSocket + 自托管 ntfy | ntfy 的 Android 接收链路需要真机验证 |
| 阿里云 OSS | 服务端签发预签名 URL 或 STS | 保留真实上传功能 |
| JUnit | Kotlin 编写测试，kotlin.test / Kotest；AndroidX 运行器 | JVM 和 Android 测试仍可能使用 JUnit 基础设施 |
| LeakCanary | Android Studio Profiler、Heap Dump、Perfetto、StrictMode | 需确认任务书接受这些分析工具 |
| 40+ Activity/Fragment | 单 Activity + Compose 导航页面 | 不能直接宣称页面数量等同组件数量 |
| 10+ 自定义 View | 可复用自定义 Compose 组件 | 需接受组件统计口径替代 |
| 客户端 5000+、后端 1500+、测试优化 1000+ 行 | 实际源码统计 | 确认注释、配置、共享代码及脚本的计数规则 |
| 25+ 接口 | 按真实业务定义 REST 和 WebSocket 契约 | 明确事件是否计入接口数量 |

不得用“现代技术”作为忽略书面要求的理由。形成导师认可的需求基线后，记录日期和调整内容。

## 优先级

P0：Android 核心流程、Ktor 服务端、PostgreSQL、真实短信和 OSS、至少一种第三方登录、测试与验收记录。

P1：ntfy 后台系统推送验证、交互与性能完善、Knot 能力提取、iOS / Desktop 共享流程演示。若任务书要求离线推送，ntfy 真机链路升为 P0。

P2：Koog 校园问答或内容辅助、第二个示例、完善公开脚手架发布。

## 证据清单

开发中建立 `docs/evidence/`，记录需求编号、对应版本、测试步骤、结果和截图。性能结果注明设备、系统、构建类型、样本数量及测量方法，保留优化前后原始结果。不提前填造指标或测试通过数量。

最终交付包括：Android 安装包、服务端部署说明、数据库迁移、配置模板、API 契约、源码和统计、测试报告、性能记录、演示脚本及论文材料。iOS / Desktop 不可用于替代 Android 基本功能验收。
