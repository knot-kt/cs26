# CS26 Product Specification / CS26 产品规格

Status: Proposed product baseline / 状态：产品基线

This document is the product source of truth for CS26. Implementation tasks, UI changes, API contracts, tests, and acceptance evidence should refer to sections in this document. A separate long-running goal is not required; the document and `docs/STATUS.md` define the sequence.

本文档是 CS26 的产品依据。后续实现、界面调整、接口契约、测试和验收证据都应引用本文档章节；开发顺序以本文档和 `docs/STATUS.md` 为准，不再依赖复杂的长期 goal。

## 1. Product Definition / 产品定位

CS26 is a low-pressure, pseudonymous social community for students. It helps people express themselves, discover shared interests, find other students, exchange useful information, and solve everyday needs such as finding lost items or selling used goods.

CS26 是面向学生的低压力、半匿名、兴趣驱动社交社区。它服务于表达、发现同好、认识他人、交换信息和解决日常问题，包括吐槽、求助、失物招领、活动协作和闲置交易。

The product loop is:

```text
Join -> choose a social identity -> browse the plaza -> publish or respond
     -> follow or message people -> receive reminders -> return to the plaza
```

产品闭环为：加入社区、建立社交身份、浏览广场、发布或回应内容、关注或联系他人、接收提醒、回到广场。

## 2. Product Principles / 产品原则

- **Peer-first / 学生优先**: the visible product is for student-to-student interaction. Platform administrators only handle safety and moderation.
- **Pseudonymous by default / 默认半匿名**: a nickname and avatar are enough for normal social use. Phone numbers, real names, student numbers, departments, and classes are never public profile fields.
- **Low pressure / 低压力**: following is one-way; message requests can be accepted or declined; users can post anonymously when appropriate.
- **Useful content / 内容有用**: topics include `吐槽`, `求助`, `学习`, `活动`, `失物`, `兴趣`, and `闲置`.
- **Small private conversations / 小范围交流**: direct messages and small groups handle deeper conversations. Public content remains in the plaza.

Student eligibility may use an invite code, student email, or a future provider adapter. Eligibility data stays private and does not become a public identity field. Anonymous posts remain traceable to the platform for reports and abuse handling.

## 3. Information Architecture / 信息架构

The signed-in shell has four bottom-navigation destinations:

| Tab | Purpose / 目的 | Required behavior / 必须行为 |
| --- | --- | --- |
| **Plaza / 广场** | Discover and publish content | Topic filters, chronological feed, search, post detail, comments, reactions, follow, compose |
| **Messages / 消息** | Private conversations | Direct chats, group chats, message requests, unread counts, retry and reconnect states |
| **Reminders / 提醒** | Activity and system feedback | Likes, comments, mentions, follows, message invites, system announcements, deep links |
| **Me / 我** | Personal space and controls | Nickname, avatar, interests, posts, drafts, privacy, blocked users, settings, sign out |

User-facing labels should use `广场`, `消息`, `提醒`, and `我`. `Campus`, `Activity`, raw user IDs, health checks, development codes, server URLs, and debug controls do not appear in the normal social shell.

## 4. Content and Social Graph / 内容与关系

All public content uses one post model with a type field instead of separate products:

- normal post, rant, question/help, event, lost-and-found, interest, and used item;
- text and up to nine media attachments;
- topic tags, comments, reactions, delete-own-post, report, and block;
- optional anonymous publishing;
- feed filters for latest, following, and topics.

Used-item posts add only `title`, `price`, `condition`, `pickupArea`, and `status` (`FOR_SALE`, `RESERVED`, `SOLD`). Contact happens through messages; CS26 does not process payment or expose an exact address.

The social graph uses one-way `Follow`. People discovery searches nicknames and interests, and may show shared topics or recent participation. Phone lookup and public real-world identity lookup are excluded.

## 5. Messages and Group Chats / 消息与群聊

The message model supports `DIRECT` and `GROUP` conversations. The first release supports groups with a hard server-side limit of **20 members including the owner**. The limit is configurable as `CS26_MAX_GROUP_MEMBERS=20`; the client cannot bypass it.

Group entry is invite-based or created from a post. Groups are not globally searchable. Invitees accept before joining.

Group features:

- group name, avatar, owner, member list, invite, remove, leave, mute, and report;
- text, image, and voice messages;
- mentions, unread count, retry, history sync, idempotency, receipts, and reconnect;
- no public channels, payment rooms, group files, bots, or per-member read receipts in the first release.

The service must reject member 21, concurrent invitations that exceed 20, messages from removed members, duplicate client message IDs, and stale sessions.

## 6. Current Code Baseline / 当前代码基线

| Area | Current implementation / 当前实现 | Product gap / 产品缺口 |
| --- | --- | --- |
| `contracts` | Auth, posts, media, chat, notices contracts | Profile, follow, reports, blocks, groups, and post types need contracts |
| `shared` | Repositories and state for auth, posts, chat, notices; `Cs26App.kt` is still one vertical debug-oriented screen | Split into auth flow, social shell, plaza, composer, conversation list/detail, reminders, and profile |
| `androidApp` | Picker, camera capture, image upload, audio recording/playback, reconnect callbacks | Production social navigation, polished states, group UI, profile and privacy controls |
| `server` | Ktor auth, post, chat, notice endpoints; in-memory defaults and PostgreSQL auth adapter | Persistent post/chat/notice stores, follow/group/report permissions, pagination and detail endpoints |
| `Knot` | `knot init` generates a rebuildable Android/Ktor `knot-app`; `IdempotencyCache` is reusable | Versioned CS26 integration and additional stable modules |

M0 is complete. M1 local authentication is usable. M2 feed/media/interactions are in progress. M3 direct chat reliability is in progress. M4 reminders are in progress. M5 has a debug APK and performance tooling. M6 has the scaffold CLI and requires versioned integration. See [`docs/STATUS.md`](STATUS.md) for evidence and deferred items.

## 7. Delivery Order and Acceptance / 开发顺序与验收

1. **Social shell**: separate auth from the signed-in shell; implement the four tabs and remove debug content from normal navigation.
2. **Plaza**: implement typed post cards, detail, pagination, composer sheet, drafts, upload states, comments, reactions, follow, report, and block.
3. **People and used items**: add nickname/interest search and used-item fields on the existing post model.
4. **Messages**: finish direct conversations, then add 20-member groups, invite acceptance, roles, mute, leave, and server-side limits.
5. **Reminders and profile**: add deep links, unread synchronization, privacy controls, and personal content.
6. **Evidence and delivery**: run real-device reconnect/two-device checks, record performance, prepare signing/deployment documents, and pin the Knot version.

Each slice is complete only when the local build, relevant tests, UI state evidence, and PR checks pass. Loading, empty, offline, permission-denied, upload-failure, session-expired, and retry states are part of acceptance.

## 8. Deferred Integrations and Workflow / 延后集成与流程

Real SMS, third-party OAuth, secure Android token storage, OSS production buckets, background push providers, production secrets, and deployment remain deferred adapters. They must not block local development or the product shell.

Ktor remains the actual server technology. Koog is an optional later extension and cannot delay core product delivery. Documentation-only PRs should skip the expensive JVM/Android job. Code PRs run the smallest verified check set, and packaging/deployment workflows stay manual or tag-triggered to protect the organization-wide 2,000-minute GitHub Actions budget.

所有外部供应商和密钥继续通过 adapter 与配置模板保留。真实服务接入前，不能把本地假适配器的结果写成生产验收结论。

## 9. Koog Agent Boundary / Koog Agent 边界

Koog is an optional server-side agent layer for safety and content assistance. It reduces moderation and publishing effort while keeping the core social product deterministic. Posts, messages, permissions, storage, delivery, and notifications must work when no model key is configured.

Koog 是服务端可选的 Agent 层，负责降低审核和内容整理成本，不能成为社交核心链路的前置条件。动态、聊天、权限、存储、送达和通知在没有模型密钥时也必须正常工作。

### Recommended use cases / 推荐用途

| Priority | Workflow | Agent output / Agent 输出 |
| --- | --- | --- |
| P1 | Moderation triage / 审核分流 | Risk score, categories, evidence summary, and `ALLOW` / `REVIEW` suggestion for a human moderation queue |
| P1 | Publish assistance / 发布辅助 | Suggested topics, normalized used-item fields, and a draft preview that the user must confirm |
| P2 | On-demand digest / 用户主动总结 | A short summary of a selected post comment thread or group-chat range after the user taps `总结` |
| P2 | Lost-and-found matching / 失物匹配 | Candidate matches based on user-approved post text and images; the agent never contacts users automatically |

The first Koog slice should be moderation triage plus publish assistance. Deterministic rules handle length, rate limits, blocked terms, attachment limits, and obvious abuse. Koog may recommend review; it must not silently ban a user, delete a post, or make a final safety decision. A moderator or explicit policy rule confirms destructive actions.

首个 Koog 切片实现“审核分流 + 发布辅助”。长度、频率、附件数量、明确的违规词和权限仍由确定性规则负责。Koog 可以建议进入人工审核，但不能自动封禁、静默删除或独自作出最终安全判断。

### Technical boundary / 技术边界

Keep the agent behind a server interface such as `AgentGateway`, with `NoopAgentGateway` for local development and `KoogAgentGateway` for a configured provider. Agent jobs are asynchronous and idempotent; a timeout, provider failure, malformed output, or missing key falls back to the original product flow.

模型调用放在服务端 `AgentGateway` 后面，本地使用 `NoopAgentGateway`，配置供应商后才启用 `KoogAgentGateway`。任务异步且幂等；超时、供应商失败、输出格式错误或缺少密钥都必须回退到原始业务流程。

Do not send phone numbers, access tokens, exact addresses, hidden moderation metadata, or private chats to a model by default. User-triggered group summaries must state the selected time range and should not be stored unless the user saves them. Redact personal data before a request, keep provider keys server-side, and record only model version, workflow, latency, and decision metadata.

Koog acceptance requires structured-output tests, redaction tests, timeout/fallback tests, a small curated moderation fixture set, and a human override path. CI must never call a paid model; use deterministic fixtures and the no-op adapter instead.