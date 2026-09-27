package com.knotkt.cs26.shared

import com.knotkt.cs26.contracts.ChatMessage
import com.knotkt.cs26.contracts.MessagePage
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders

class ChatRepository(
    private val client: HttpClient,
    private val baseUrl: String,
) {
    suspend fun history(accessToken: String, conversationId: String): List<ChatMessage> = client
        .get("$baseUrl/conversations/$conversationId/messages") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }
        .body<MessagePage>()
        .items
}