package com.knotkt.cs26.shared

import com.knotkt.cs26.contracts.FollowStateResponse
import com.knotkt.cs26.contracts.FollowingList
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.http.HttpHeaders

class FollowRepository(
    private val client: HttpClient,
    private val baseUrl: String,
) {
    suspend fun follow(accessToken: String, userId: String): FollowStateResponse = client
        .post("$baseUrl/users/$userId/follow") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }
        .body()

    suspend fun unfollow(accessToken: String, userId: String): FollowStateResponse = client
        .delete("$baseUrl/users/$userId/follow") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }
        .body()

    suspend fun list(accessToken: String): FollowingList = client
        .get("$baseUrl/me/following") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }
        .body()
}