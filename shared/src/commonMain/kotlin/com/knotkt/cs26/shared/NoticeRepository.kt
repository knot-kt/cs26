package com.knotkt.cs26.shared

import com.knotkt.cs26.contracts.Notice
import com.knotkt.cs26.contracts.NoticePage
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.http.HttpHeaders

class NoticeRepository(
    private val client: HttpClient,
    private val baseUrl: String,
) {
    suspend fun list(accessToken: String): List<Notice> = client
        .get("$baseUrl/notifications") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }
        .body<NoticePage>()
        .items

    suspend fun markRead(accessToken: String, noticeId: String) {
        client.post("$baseUrl/notifications/$noticeId/read") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }
    }
}