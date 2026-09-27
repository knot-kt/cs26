package com.knotkt.cs26.shared

import com.knotkt.cs26.contracts.AuthSession
import com.knotkt.cs26.contracts.RequestCodeRequest
import com.knotkt.cs26.contracts.RequestCodeResponse
import com.knotkt.cs26.contracts.VerifyCodeRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class AuthRepository(
    private val client: HttpClient,
    private val baseUrl: String,
) {
    suspend fun requestCode(phone: String): RequestCodeResponse = client
        .post("$baseUrl/auth/code/request") {
            contentType(ContentType.Application.Json)
            setBody(RequestCodeRequest(phone))
        }
        .body()

    suspend fun verifyCode(phone: String, code: String): AuthSession = client
        .post("$baseUrl/auth/code/verify") {
            contentType(ContentType.Application.Json)
            setBody(VerifyCodeRequest(phone, code))
        }
        .body()
}
