package com.knotkt.cs26.server

import com.knotkt.cs26.contracts.AuthError
import com.knotkt.cs26.contracts.CreatePostRequest
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
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing

fun Application.module() = module(
    authService = DatabaseConfig.fromEnvironment()
        ?.let { config -> InMemoryAuthService(PostgresAuthStore(config.dataSource())) }
        ?: InMemoryAuthService(),
)

fun Application.module(
    authService: AuthService,
    postStore: PostStore = InMemoryPostStore(),
    mediaStorage: MediaStorage = LocalMediaStorage(),
) {
    install(ContentNegotiation) {
        json()
    }

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
        post("/posts") {
            val token = bearerToken(call.request.header(HttpHeaders.Authorization))
            val session = token?.let(authService::findSession)
            if (session == null) {
                call.respond(HttpStatusCode.Unauthorized, AuthError("invalid_session", "session is invalid or expired"))
                return@post
            }
            val request = call.receive<CreatePostRequest>()
            val content = request.content.trim()
            if (content.isEmpty() || content.length > 2_000) {
                call.respond(HttpStatusCode.BadRequest, AuthError("invalid_content", "content must contain 1-2000 characters"))
                return@post
            }
            val attachments = request.attachments
            if (attachments.size > 9 || attachments.any { it.objectKey.isBlank() || it.sizeBytes < 0 }) {
                call.respond(HttpStatusCode.BadRequest, AuthError("invalid_media", "attachments are invalid"))
                return@post
            }
            call.respond(HttpStatusCode.Created, postStore.create(session.userId, content, attachments))
        }
        get("/posts") {
            val token = bearerToken(call.request.header(HttpHeaders.Authorization))
            if (token == null || authService.findSession(token) == null) {
                call.respond(HttpStatusCode.Unauthorized, AuthError("invalid_session", "session is invalid or expired"))
                return@get
            }
            val limit = call.request.queryParameters["limit"]?.toIntOrNull()?.coerceIn(1, 50) ?: 20
            call.respond(PostPage(postStore.list(limit)))
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
    }
}

private fun bearerToken(header: String?): String? = header
    ?.removePrefix("Bearer ")
    ?.takeIf { it.isNotBlank() }

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = { module() }).start(wait = true)
}