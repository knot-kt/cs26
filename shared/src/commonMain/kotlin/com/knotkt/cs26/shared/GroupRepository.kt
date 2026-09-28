package com.knotkt.cs26.shared

import com.knotkt.cs26.contracts.CreateGroupRequest
import com.knotkt.cs26.contracts.ConversationPage
import com.knotkt.cs26.contracts.GroupConversation
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType

class GroupRepository(
    private val client: HttpClient,
    private val baseUrl: String,
) {
    suspend fun list(accessToken: String): ConversationPage = client
        .get("$baseUrl/conversations") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }
        .body()

    suspend fun create(accessToken: String, name: String): GroupConversation = client
        .post("$baseUrl/groups") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
            contentType(ContentType.Application.Json)
            setBody(CreateGroupRequest(name))
        }
        .body()

    suspend fun accept(accessToken: String, conversationId: String): GroupConversation = client
        .post("$baseUrl/groups/$conversationId/accept") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }
        .body()
}