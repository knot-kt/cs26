# Chat Reliability / 聊天可靠性证据

- Requirement / 需求: `CHAT-RETRY-01`
- Build / 构建: local HEAD after `ChatStreamEvent` receipt implementation
- Environment / 环境: JVM Ktor test application with in-memory auth and chat store
- Command / 命令: `JAVA_HOME=/opt/homebrew/opt/openjdk@21 ./gradlew :server:test --no-daemon --console=plain`
- Result / 结果: `pass`
- Device smoke / 真机 smoke: Pixel 6 connected to the local server through `adb reverse tcp:8080 tcp:8080`; Chat tab reached `Connected` and accepted a text message.

## Observed / 实际

`websocketRetryReturnsReceiptWithoutDuplicatingHistory` sends the same `clientMessageId` twice over WebSocket. The server emits a message event and a receipt for each attempt; both receipts point to the same server `messageId`, and the history endpoint contains one `clientMessageId` entry.

客户端通过 WebSocket 重发相同 `clientMessageId`。服务端两次都返回事件和 receipt，但两个 receipt 指向同一个服务端消息；历史接口只保留一条消息。

The Android client keeps unacknowledged requests in `ChatState.pendingMessages`, retries them after reconnect, removes them on receipt, and reports the last delivery status in the Chat tab.

The intentional server-stop/server-restart check and two-device delivery check are still open; this record does not claim those scenarios are complete.

Android 客户端把未确认请求保存在 `ChatState.pendingMessages`，重连后重发，收到 receipt 后移除，并在聊天页显示最后一次投递状态。

服务端停止/恢复和双设备投递检查仍未完成；本记录不把这两项标记为已完成。