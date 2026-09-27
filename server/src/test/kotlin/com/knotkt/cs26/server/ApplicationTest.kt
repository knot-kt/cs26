package com.knotkt.cs26.server

import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals

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
    }
}
