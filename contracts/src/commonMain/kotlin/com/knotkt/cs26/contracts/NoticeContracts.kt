package com.knotkt.cs26.contracts

import kotlinx.serialization.Serializable

@Serializable
data class CreateAnnouncementRequest(
    val title: String,
    val body: String,
    val deepLink: String? = null,
)

@Serializable
data class Notice(
    val id: String,
    val title: String,
    val body: String,
    val deepLink: String? = null,
    val createdAtEpochMillis: Long,
    val read: Boolean = false,
)

@Serializable
data class NoticePage(
    val items: List<Notice>,
)