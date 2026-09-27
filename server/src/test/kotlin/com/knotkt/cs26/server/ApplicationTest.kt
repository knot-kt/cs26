package com.knotkt.cs26.server

import io.ktor.client.request.get
import io.ktor.client.request.delete
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import com.knotkt.cs26.contracts.MediaKind

class ApplicationTest {
    @Test
    fun healthEndpointReturnsOk() = testApplication {
        application { module() }

        val response = client.get("/health")

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("ok", response.bodyAsText())
    }

    @Test
    fun developmentAuthReturnsSessionAfterCodeVerification() = testApplication {
        application { module(InMemoryAuthService()) }

        val requestCode = client.post("/auth/code/request") {
            contentType(ContentType.Application.Json)
            setBody("""{"phone":"+8613800138000"}""")
        }

        assertEquals(HttpStatusCode.OK, requestCode.status)
        assertEquals(true, requestCode.bodyAsText().contains("123456"))

        val verifyCode = client.post("/auth/code/verify") {
            contentType(ContentType.Application.Json)
            setBody("""{"phone":"+8613800138000","code":"123456"}""")
        }

        assertEquals(HttpStatusCode.OK, verifyCode.status)
        assertEquals(true, verifyCode.bodyAsText().contains("accessToken"))

        val accessToken = Regex("\\\"accessToken\\\":\\\"([^\\\"]+)\\\"")
            .find(verifyCode.bodyAsText())
            ?.groupValues
            ?.get(1)
        check(accessToken != null)

        val currentSession = client.get("/auth/session") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }
        assertEquals(HttpStatusCode.OK, currentSession.status)
        assertEquals(true, currentSession.bodyAsText().contains(accessToken))

        val logout = client.post("/auth/logout") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }
        assertEquals(HttpStatusCode.NoContent, logout.status)

        val secondLogout = client.post("/auth/logout") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }
        assertEquals(HttpStatusCode.Unauthorized, secondLogout.status)

        val expiredSession = client.get("/auth/session") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }
        assertEquals(HttpStatusCode.Unauthorized, expiredSession.status)
    }

    @Test
    fun developmentAuthAppliesCooldownAndAttemptLimit() {
        val service = InMemoryAuthService()
        val phone = "+8613800138000"

        assertEquals(0, service.requestCode(phone)?.retryAfterSeconds)
        assertEquals(true, (service.requestCode(phone)?.retryAfterSeconds ?: 0) > 0)
        repeat(5) { assertNull(service.verifyCode(phone, "000000")) }
        assertNull(service.verifyCode(phone, "123456"))
    }

    @Test
    fun authenticatedUserCanCreateAndListPosts() = testApplication {
        val authService = InMemoryAuthService()
        application { module(authService) }
        authService.requestCode("+8613800138000")
        val session = checkNotNull(authService.verifyCode("+8613800138000", "123456"))

        val create = client.post("/posts") {
            header(HttpHeaders.Authorization, "Bearer ${session.accessToken}")
            contentType(ContentType.Application.Json)
            setBody("""{"content":"first post","attachments":[{"kind":"IMAGE","objectKey":"posts/one.jpg","mimeType":"image/jpeg","sizeBytes":128}]}""")
        }
        assertEquals(HttpStatusCode.Created, create.status)
        assertEquals(true, create.bodyAsText().contains(session.userId))
        assertEquals(true, create.bodyAsText().contains("posts/one.jpg"))

        val list = client.get("/posts") {
            header(HttpHeaders.Authorization, "Bearer ${session.accessToken}")
        }
        assertEquals(HttpStatusCode.OK, list.status)
        assertEquals(true, list.bodyAsText().contains("first post"))
    }

    @Test
    fun postsRequireAnActiveSession() = testApplication {
        application { module(InMemoryAuthService()) }

        val response = client.get("/posts")

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun ownerCanDeleteAndUsersCanLikeAndComment() = testApplication {
        val authService = InMemoryAuthService()
        application { module(authService) }
        authService.requestCode("+8613800138000")
        val session = checkNotNull(authService.verifyCode("+8613800138000", "123456"))
        val created = client.post("/posts") {
            header(HttpHeaders.Authorization, "Bearer ${session.accessToken}")
            contentType(ContentType.Application.Json)
            setBody("""{"content":"interactive post"}""")
        }
        val postId = Regex("\\\"id\\\":\\\"([^\\\"]+)\\\"").find(created.bodyAsText())!!.groupValues[1]

        assertEquals(HttpStatusCode.NoContent, client.post("/posts/$postId/like") {
            header(HttpHeaders.Authorization, "Bearer ${session.accessToken}")
        }.status)
        val comment = client.post("/posts/$postId/comments") {
            header(HttpHeaders.Authorization, "Bearer ${session.accessToken}")
            contentType(ContentType.Application.Json)
            setBody("""{"content":"nice"}""")
        }
        assertEquals(HttpStatusCode.Created, comment.status)
        assertEquals(HttpStatusCode.NoContent, client.delete("/posts/$postId") {
            header(HttpHeaders.Authorization, "Bearer ${session.accessToken}")
        }.status)
    }
}