package com.knotkt.cs26.shared

import com.knotkt.cs26.contracts.BlockStateResponse
import com.knotkt.cs26.contracts.BlockedUsers
import com.knotkt.cs26.contracts.CreateReportRequest
import com.knotkt.cs26.contracts.ReportReason
import com.knotkt.cs26.contracts.ReportReceipt
import com.knotkt.cs26.contracts.ReportTargetType
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType

class SafetyRepository(
    private val client: HttpClient,
    private val baseUrl: String,
) {
    suspend fun reportPost(accessToken: String, postId: String, reason: ReportReason = ReportReason.OTHER): ReportReceipt =
        report(accessToken, ReportTargetType.POST, postId, reason)

    suspend fun reportUser(accessToken: String, userId: String, reason: ReportReason = ReportReason.OTHER): ReportReceipt =
        report(accessToken, ReportTargetType.USER, userId, reason)

    suspend fun block(accessToken: String, userId: String): BlockStateResponse = client
        .post("$baseUrl/users/$userId/block") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }
        .body()

    suspend fun unblock(accessToken: String, userId: String): BlockStateResponse = client
        .delete("$baseUrl/users/$userId/block") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }
        .body()

    suspend fun listBlocked(accessToken: String): BlockedUsers = client
        .get("$baseUrl/me/blocks") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }
        .body()

    private suspend fun report(
        accessToken: String,
        targetType: ReportTargetType,
        targetId: String,
        reason: ReportReason,
    ): ReportReceipt = client
        .post("$baseUrl/reports") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
            contentType(ContentType.Application.Json)
            setBody(CreateReportRequest(targetType, targetId, reason))
        }
        .body()
}