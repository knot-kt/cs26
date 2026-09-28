package com.knotkt.cs26.shared

import com.knotkt.cs26.contracts.Profile
import com.knotkt.cs26.contracts.UpdateProfileRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType

class ProfileRepository(
    private val client: HttpClient,
    private val baseUrl: String,
) {
    suspend fun get(accessToken: String): Profile = client
        .get("$baseUrl/me/profile") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }
        .body()

    suspend fun update(
        accessToken: String,
        nickname: String,
        interests: List<String>,
        anonymousByDefault: Boolean,
    ): Profile = client
        .patch("$baseUrl/me/profile") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
            contentType(ContentType.Application.Json)
            setBody(UpdateProfileRequest(nickname, interests, anonymousByDefault))
        }
        .body()
}