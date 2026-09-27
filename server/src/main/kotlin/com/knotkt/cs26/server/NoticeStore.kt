package com.knotkt.cs26.server

import com.knotkt.cs26.contracts.Notice
import java.util.UUID
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.ConcurrentHashMap

interface NoticeStore {
    fun publish(title: String, body: String, deepLink: String?): Notice

    fun list(userId: String, limit: Int): List<Notice>

    fun markRead(userId: String, noticeId: String): Boolean
}

class InMemoryNoticeStore : NoticeStore {
    private val notices = CopyOnWriteArrayList<Notice>()
    private val reads = ConcurrentHashMap.newKeySet<String>()

    override fun publish(title: String, body: String, deepLink: String?): Notice = Notice(
        id = UUID.randomUUID().toString(),
        title = title,
        body = body,
        deepLink = deepLink,
        createdAtEpochMillis = System.currentTimeMillis(),
    ).also(notices::add)

    override fun list(userId: String, limit: Int): List<Notice> = notices
        .asReversed()
        .take(limit)
        .map { it.copy(read = "$userId:${it.id}" in reads) }

    override fun markRead(userId: String, noticeId: String): Boolean {
        if (notices.none { it.id == noticeId }) return false
        reads.add("$userId:$noticeId")
        return true
    }
}