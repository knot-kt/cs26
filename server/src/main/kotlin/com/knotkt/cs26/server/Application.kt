package com.knotkt.cs26.server

import com.knotkt.cs26.contracts.AuthError
import com.knotkt.cs26.contracts.RequestCodeRequest
import com.knotkt.cs26.contracts.VerifyCodeRequest
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing

fun Application.module() = module(InMemoryAuthService())

fun Application.module(authService: AuthService) {
    install(ContentNegotiation) {
        json()
    }

    routing {
        get("/health") {
            call.respondText("ok")
        }
        post("/auth/code/request") {
            val request = call.receive<RequestCodeRequest>()
            val response = authService.requestCode(request.phone)
            if (response == null) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    AuthError("invalid_phone", "phone must contain at least 8 digits"),
                )
            } else {
                call.respond(response)
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
    }
}

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = { module() }).start(wait = true)
}
