package com.knotkt.cs26.contracts

import kotlinx.serialization.Serializable

@Serializable
enum class ReportTargetType {
    POST,
    USER,
}

@Serializable
enum class ReportReason {
    SPAM,
    HARASSMENT,
    HATE,
    SEXUAL_CONTENT,
    SCAM,
    OTHER,
}

@Serializable
data class CreateReportRequest(
    val targetType: ReportTargetType,
    val targetId: String,
    val reason: ReportReason = ReportReason.OTHER,
    val details: String = "",
)

@Serializable
data class ReportReceipt(
    val reportId: String,
    val targetType: ReportTargetType,
    val targetId: String,
)

@Serializable
data class BlockStateResponse(
    val userId: String,
    val blocked: Boolean,
)

@Serializable
data class BlockedUsers(
    val userIds: List<String>,
)