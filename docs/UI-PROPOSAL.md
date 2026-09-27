# Campus Social UI Proposal / 校园社交界面方案

> Superseded by [`PRODUCT-SPEC.md`](PRODUCT-SPEC.md). This file is retained as the earlier design discussion; the product specification is the source of truth for current UI names, identity rules, and feature scope.
>
> 本文档已由 [`PRODUCT-SPEC.md`](PRODUCT-SPEC.md) 取代，仅保留早期设计讨论。当前 UI 名称、身份规则和功能范围以产品规格为准。

## Product Position / 产品定位

CS26 should feel like a campus community, not a collection of API test panels. The first screen after sign-in is a useful feed with people, campus context, and a clear publish action. Authentication, health checks, and debug values stay outside the main social surface.

CS26 的登录后首页应该是可浏览、可发布的校园社区，而不是接口测试控件集合。健康检查、开发验证码和调试信息只保留在开发状态或设置中，不进入主社交界面。

## Information Architecture / 信息架构

Use four bottom-navigation destinations:

| Tab | User job / 用户任务 | Primary content / 主内容 |
| --- | --- | --- |
| Campus / 校园 | Browse and publish campus life | Feed, campus filters, topics, compose FAB |
| Messages / 消息 | Continue conversations | Conversation list, unread counts, chat detail |
| Notices / 通知 | Read trusted official information | Pinned announcements, unread state, deep links |
| Me / 我的 | Manage identity and personal activity | Avatar, school profile, my posts, settings |

The current `Activity`, `Chat`, `Notices`, and `Me` names can map to this structure, but Chat must open a conversation list before a conversation detail. The feed must not be a second full-screen form below authentication.

## Campus Feed / 校园动态

The top bar shows the current campus and a compact avatar. A filter row provides `For you`, `Following`, and campus topics such as `Lost & Found`, `Study`, `Clubs`, and `Events`. Each post is a compact item with avatar, display name, college/class metadata, relative time, text, media grid, topic chips, and a single action row for like, comment, share, and overflow.

发布使用 bottom sheet: text editor, topic picker, image picker, camera, and draft state. Empty, loading, upload progress, retry, and permission-denied states are explicit. Pagination and post detail are required before calling the feed complete.

## Visual Direction / 视觉方向

Use the icon language already established: deep navy `#14345F`, warm yellow for the `26` accent, cool blue for links, green for online/success, and neutral white/gray surfaces. Replace the default purple Material theme. Use 8dp spacing, 8dp surface corners, readable 16sp body text, and familiar icons for compose, camera, gallery, send, and more. Keep cards dense enough for scanning and avoid decorative gradients.

## Chat, Notices, Profile / 聊天、通知、个人

- Chat: list conversations first; detail uses bubbles, date separators, attachment preview, recording state, retry banner, and delivery status. Offline and reconnecting states remain visible without blocking navigation.
- Notices: distinguish official announcements with a source label, pin important items, show unread badges, and open deep links into the relevant screen.
- Me: show avatar, nickname, college/class, post count, my posts, drafts, notification settings, and sign-out. Do not expose raw user IDs in the normal view.

## Delivery Sequence / 实施顺序

1. Split authentication from the social shell and introduce the bottom navigation state.
2. Rebuild Campus as a real feed with cards, pagination, detail, and compose sheet.
3. Add conversation list and unread counts, then polish chat detail and media states.
4. Add notice detail and profile data; keep provider-backed delivery deferred behind adapters.

Acceptance screenshots should cover signed-out, empty feed, populated feed, compose/upload failure, chat reconnecting, unread notice, and profile states on the Pixel 6.