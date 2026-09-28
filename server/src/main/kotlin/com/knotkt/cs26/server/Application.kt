package com.knotkt.cs26.server

import com.knotkt.cs26.contracts.AuthError
import com.knotkt.cs26.contracts.ChatDeliveryStatus
import com.knotkt.cs26.contracts.ChatStreamEvent
import com.knotkt.cs26.contracts.CreatePostRequest
import com.knotkt.cs26.contracts.CreateCommentRequest
import com.knotkt.cs26.contracts.MessagePage
import com.knotkt.cs26.contracts.SendMessageRequest
import com.knotkt.cs26.contracts.CreateAnnouncementRequest
import com.knotkt.cs26.contracts.NoticePage
import com.knotkt.cs26.contracts.PostPage
import com.knotkt.cs26.contracts.MediaUploadResponse
import com.knotkt.cs26.contracts.RequestCodeRequest
import com.knotkt.cs26.contracts.VerifyCodeRequest
import io.ktor.http.HttpStatusCode
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.receive
import io.ktor.server.request.receiveMultipart
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.util.cio.toByteArray
import io.ktor.server.request.header
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.response.respondBytes
import io.ktor.server.routing.get
import io.ktor.server.routing.delete
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

fun Application.module() = module(
    authService = DatabaseConfig.fromEnvironment()
        ?.let { config -> InMemoryAuthService(PostgresAuthStore(config.dataSource())) }
        ?: InMemoryAuthService(),
)

fun Application.module(
    authService: AuthService,
    postStore: PostStore = InMemoryPostStore(),
    mediaStorage: MediaStorage = LocalMediaStorage(),
    chatStore: ChatStore = InMemoryChatStore(),
    chatHub: ChatHub = ChatHub(),
    noticeStore: NoticeStore = InMemoryNoticeStore(),
    noticeHub: NoticeHub = NoticeHub(),
    profileStore: ProfileStore = InMemoryProfileStore(),
    pushPublisher: PushPublisher = PushPublisher.fromEnvironment(),
) {
    install(ContentNegotiation) {
        json()
    }
    install(WebSockets)

    routing {
        get("/health") {
            call.respondText("ok")
        }
        post("/auth/code/request") {
            val request = call.receive<RequestCodeRequest>()
            val result = authService.requestCode(request.phone)
            if (result == null) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    AuthError("invalid_phone", "phone must contain at least 8 digits"),
                )
            } else if (result.retryAfterSeconds > 0) {
                call.respond(
                    HttpStatusCode.TooManyRequests,
                    AuthError("rate_limited", "retry after ${result.retryAfterSeconds} seconds"),
                )
            } else {
                call.respond(result.response)
            }
        }
        post("/auth/code/verify") {
            val request = call.receive<VerifyCodeRequest>()
            val session = authService.verifyCode(request.phone, request.code)
            if (session == null) {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    AuthError("invalid_code", "code is invalid or expired"),
                )
            } else {
                call.respond(session)
            }
        }
        post("/auth/logout") {
            val token = call.request.header(HttpHeaders.Authorization)
                ?.removePrefix("Bearer ")
                ?.takeIf { it.isNotBlank() }
            if (token == null || !authService.logout(token)) {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    AuthError("invalid_session", "session is invalid or expired"),
                )
            } else {
                call.respond(HttpStatusCode.NoContent)
            }
        }
        get("/auth/session") {
            val token = call.request.header(HttpHeaders.Authorization)
                ?.removePrefix("Bearer ")
                ?.takeIf { it.isNotBlank() }
            val session = token?.let(authService::findSession)
            if (session == null) {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    AuthError("invalid_session", "session is invalid or expired"),
                )
            } else {
                call.respond(session)
            }
        }
        get("/me/profile") {
            val token = bearerToken(call.request.header(HttpHeaders.Authorization))
            val session = token?.let(authService::findSession)
            if (session == null) {
                call.respond(HttpStatusCode.Unauthorized, AuthError("invalid_session", "session is invalid or expired"))
            } else {
                call.respond(profileStore.get(session.userId))
            }
        }
        patch("/me/profile") {
            val token = bearerToken(call.request.header(HttpHeaders.Authorization))
            val session = token?.let(authService::findSession)
            if (session == null) {
                call.respond(HttpStatusCode.Unauthorized, AuthError("invalid_session", "session is invalid or expired"))
                return@patch
            }
            val request = call.receive<com.knotkt.cs26.contracts.UpdateProfileRequest>()
            val nickname = request.nickname.trim()
            val interests = request.interests
                .map(String::trim)
                .filter(String::isNotEmpty)
                .distinct()
            if (nickname.length !in 1..32 || interests.size > 10 || interests.any { it.length > 24 }) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    AuthError("invalid_profile", "nickname must be 1-32 characters and interests must contain at most 10 items"),
                )
                return@patch
            }
            call.respond(profileStore.update(session.userId, nickname, interests, request.anonymousByDefault))
        }
        post("/posts") {
            val token = bearerToken(call.request.header(HttpHeaders.Authorization))
            val session = token?.let(authService::findSession)
            if (session == null) {
                call.respond(HttpStatusCode.Unauthorized, AuthError("invalid_session", "session is invalid or expired"))
                return@post
            }
            val request = call.receive<CreatePostRequest>()
            val content = request.content.trim()
            val attachments = request.attachments
            if (attachments.size > 9 || attachments.any { it.objectKey.isBlank() || it.sizeBytes < 0 }) {
                call.respond(HttpStatusCode.BadRequest, AuthError("invalid_media", "attachments are invalid"))
                return@post
            }
            if ((content.isEmpty() && attachments.isEmpty()) || content.length > 2_000) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    AuthError("invalid_content", "content must contain 1-2000 characters or include an attachment"),
                )
                return@post
            }
            call.respond(HttpStatusCode.Created, postStore.create(session.userId, content, attachments, request.type))
        }
        get("/posts") {
            val token = bearerToken(call.request.header(HttpHeaders.Authorization))
            if (token == null || authService.findSession(token) == null) {
                call.respond(HttpStatusCode.Unauthorized, AuthError("invalid_session", "session is invalid or expired"))
                return@get
            }
            val limit = call.request.queryParameters["limit"]?.toIntOrNull()?.coerceIn(1, 50) ?: 20
            val before = call.request.queryParameters["before"]?.toLongOrNull()
            val items = postStore.list(limit + 1, authService.findSession(token)?.userId.orEmpty(), before)
            val pageItems = items.take(limit)
            val nextCursor = if (items.size > limit) {
                pageItems.lastOrNull()?.createdAtEpochMillis?.toString()
            } else {
                null
            }
            call.respond(PostPage(pageItems, nextCursor))
        }
        delete("/posts/{id}") {
            val token = bearerToken(call.request.header(HttpHeaders.Authorization))
            val session = token?.let(authService::findSession)
            val id = call.parameters["id"]
            if (session == null || id == null || !postStore.delete(id, session.userId)) {
                call.respond(HttpStatusCode.NotFound, AuthError("post_not_found", "post is missing or not owned by this user"))
            } else {
                call.respond(HttpStatusCode.NoContent)
            }
        }
        post("/posts/{id}/like") {
            val token = bearerToken(call.request.header(HttpHeaders.Authorization))
            val session = token?.let(authService::findSession)
            val id = call.parameters["id"]
            if (session == null || id == null || !postStore.toggleLike(id, session.userId)) {
                call.respond(HttpStatusCode.NotFound, AuthError("post_not_found", "post does not exist"))
            } else {
                call.respond(HttpStatusCode.NoContent)
            }
        }
        post("/posts/{id}/comments") {
            val token = bearerToken(call.request.header(HttpHeaders.Authorization))
            val session = token?.let(authService::findSession)
            val id = call.parameters["id"]
            val request = call.receive<CreateCommentRequest>()
            val content = request.content.trim()
            if (session == null) {
                call.respond(HttpStatusCode.Unauthorized, AuthError("invalid_session", "session is invalid or expired"))
            } else if (id == null || content.isEmpty() || content.length > 1_000) {
                call.respond(HttpStatusCode.BadRequest, AuthError("invalid_content", "comment must contain 1-1000 characters"))
            } else {
                val comment = postStore.addComment(id, session.userId, content)
                if (comment == null) call.respond(HttpStatusCode.NotFound, AuthError("post_not_found", "post does not exist"))
                else call.respond(HttpStatusCode.Created, comment)
            }
        }
        get("/posts/{id}/comments") {
            val token = bearerToken(call.request.header(HttpHeaders.Authorization))
            if (token == null || authService.findSession(token) == null) {
                call.respond(HttpStatusCode.Unauthorized, AuthError("invalid_session", "session is invalid or expired"))
            } else {
                val id = call.parameters["id"]
                if (id == null) call.respond(HttpStatusCode.NotFound, AuthError("post_not_found", "post does not exist"))
                else call.respond(postStore.comments(id))
            }
        }
        post("/media/upload") {
            val token = bearerToken(call.request.header(HttpHeaders.Authorization))
            if (token == null || authService.findSession(token) == null) {
                call.respond(HttpStatusCode.Unauthorized, AuthError("invalid_session", "session is invalid or expired"))
                return@post
            }
            var bytes: ByteArray? = null
            var mimeType = "application/octet-stream"
            call.receiveMultipart().forEachPart { part ->
                if (part is PartData.FileItem && bytes == null) {
                    mimeType = part.contentType?.toString() ?: mimeType
                    bytes = part.provider().toByteArray()
                }
                part.dispose.invoke()
            }
            val payload = bytes
            if (payload == null || payload.isEmpty() || payload.size > 10 * 1024 * 1024) {
                call.respond(HttpStatusCode.BadRequest, AuthError("invalid_media", "file must be between 1 byte and 10 MiB"))
                return@post
            }
            val stored = mediaStorage.store(payload, mimeType)
            call.respond(MediaUploadResponse(stored.objectKey, stored.mimeType, stored.sizeBytes))
        }
        get("/media/{path...}") {
            val token = bearerToken(call.request.header(HttpHeaders.Authorization))
            if (token == null || authService.findSession(token) == null) {
                call.respond(HttpStatusCode.Unauthorized, AuthError("invalid_session", "session is invalid or expired"))
                return@get
            }
            val objectKey = "uploads/" + call.parameters.getAll("path").orEmpty().joinToString("/")
            val media = mediaStorage.load(objectKey)
            if (media == null) call.respond(HttpStatusCode.NotFound, AuthError("media_not_found", "media is missing"))
            else call.respondBytes(media.bytes, io.ktor.http.ContentType.parse(media.mimeType))
        }
        post("/conversations/{id}/messages") {
            val token = bearerToken(call.request.header(HttpHeaders.Authorization))
            val session = token?.let(authService::findSession)
            val conversationId = call.parameters["id"]
            val request = call.receive<SendMessageRequest>()
            val content = request.content.trim()
            if (session == null) {
                call.respond(HttpStatusCode.Unauthorized, AuthError("invalid_session", "session is invalid or expired"))
            } else if (conversationId.isNullOrBlank() || request.clientMessageId.isBlank() ||
                (content.isEmpty() && request.attachments.isEmpty()) || content.length > 4_000
            ) {
                call.respond(HttpStatusCode.BadRequest, AuthError("invalid_message", "message fields are invalid"))
            } else if (request.attachments.size > 9 || request.attachments.any { it.objectKey.isBlank() || it.sizeBytes < 0 }) {
                call.respond(HttpStatusCode.BadRequest, AuthError("invalid_media", "attachments are invalid"))
            } else {
                call.respond(
                    HttpStatusCode.Created,
                    chatStore.send(conversationId, session.userId, request.clientMessageId, content, request.attachments),
                )
            }
        }
        get("/conversations/{id}/messages") {
            val token = bearerToken(call.request.header(HttpHeaders.Authorization))
            if (token == null || authService.findSession(token) == null) {
                call.respond(HttpStatusCode.Unauthorized, AuthError("invalid_session", "session is invalid or expired"))
            } else {
                val conversationId = call.parameters["id"]
                if (conversationId.isNullOrBlank()) {
                    call.respond(HttpStatusCode.BadRequest, AuthError("invalid_conversation", "conversation id is required"))
                } else {
                    val limit = call.request.queryParameters["limit"]?.toIntOrNull()?.coerceIn(1, 100) ?: 50
                    call.respond(MessagePage(chatStore.list(conversationId, limit)))
                }
            }
        }
        post("/announcements") {
            val token = bearerToken(call.request.header(HttpHeaders.Authorization))
            if (token == null || authService.findSession(token) == null) {
                call.respond(HttpStatusCode.Unauthorized, AuthError("invalid_session", "session is invalid or expired"))
                return@post
            }
            val request = call.receive<CreateAnnouncementRequest>()
            val title = request.title.trim()
            val body = request.body.trim()
            if (title.isEmpty() || title.length > 200 || body.isEmpty() || body.length > 10_000) {
                call.respond(HttpStatusCode.BadRequest, AuthError("invalid_notice", "announcement fields are invalid"))
            } else {
                val notice = noticeStore.publish(title, body, request.deepLink)
                noticeHub.broadcast(Json.encodeToString(notice))
                try {
                    pushPublisher.publish(notice)
                } catch (_: Throwable) {
                    // Background delivery must not roll back a persisted announcement.
                }
                call.respond(HttpStatusCode.Created, notice)
            }
        }
        get("/notifications") {
            val token = bearerToken(call.request.header(HttpHeaders.Authorization))
            val session = token?.let(authService::findSession)
            if (session == null) {
                call.respond(HttpStatusCode.Unauthorized, AuthError("invalid_session", "session is invalid or expired"))
            } else {
                val limit = call.request.queryParameters["limit"]?.toIntOrNull()?.coerceIn(1, 100) ?: 50
                call.respond(NoticePage(noticeStore.list(session.userId, limit)))
            }
        }
        post("/notifications/{id}/read") {
            val token = bearerToken(call.request.header(HttpHeaders.Authorization))
            val session = token?.let(authService::findSession)
            val noticeId = call.parameters["id"]
            if (session == null) {
                call.respond(HttpStatusCode.Unauthorized, AuthError("invalid_session", "session is invalid or expired"))
            } else if (noticeId == null || !noticeStore.markRead(session.userId, noticeId)) {
                call.respond(HttpStatusCode.NotFound, AuthError("notice_not_found", "notification does not exist"))
            } else {
                call.respond(HttpStatusCode.NoContent)
            }
        }
        webSocket("/notifications/stream") {
            val token = bearerToken(call.request.header(HttpHeaders.Authorization))
            val session = token?.let(authService::findSession)
            if (session == null) {
                close(io.ktor.websocket.CloseReason(io.ktor.websocket.CloseReason.Codes.VIOLATED_POLICY, "invalid session"))
                return@webSocket
            }
            noticeHub.join(session.userId, this)
            try {
                for (frame in incoming) {
                    if (frame is Frame.Close) break
                }
            } finally {
                noticeHub.leave(session.userId, this)
            }
        }
        webSocket("/conversations/{id}/stream") {
            val token = bearerToken(call.request.header(HttpHeaders.Authorization))
            val session = token?.let(authService::findSession)
            val conversationId = call.parameters["id"]
            if (session == null || conversationId.isNullOrBlank()) {
                close(io.ktor.websocket.CloseReason(io.ktor.websocket.CloseReason.Codes.VIOLATED_POLICY, "invalid session or conversation"))
                return@webSocket
            }
            chatHub.join(conversationId, this)
            try {
                for (frame in incoming) {
                    if (frame is Frame.Text) {
                        val request = runCatching { Json.decodeFromString<SendMessageRequest>(frame.readText()) }.getOrNull()
                        val content = request?.content?.trim().orEmpty()
                        if (request == null ||
                            (content.isEmpty() && request.attachments.isEmpty()) ||
                            content.length > 4_000 || request.clientMessageId.isBlank()
                        ) continue
                        val message = chatStore.send(conversationId, session.userId, request.clientMessageId, content, request.attachments)
                        chatHub.broadcast(
                            conversationId,
                            Json.encodeToString<ChatStreamEvent>(ChatStreamEvent.Message(message)),
                        )
                        send(
                            Frame.Text(
                                Json.encodeToString<ChatStreamEvent>(
                                    ChatStreamEvent.Receipt(
                                        clientMessageId = message.clientMessageId,
                                        messageId = message.id,
                                        status = ChatDeliveryStatus.ACCEPTED,
                                    ),
                                ),
                            ),
                        )
                    }
                }
            } finally {
                chatHub.leave(conversationId, this)
            }
        }
    }
}

private fun bearerToken(header: String?): String? = header
    ?.removePrefix("Bearer ")
    ?.takeIf { it.isNotBlank() }

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = { module() }).start(wait = true)
}