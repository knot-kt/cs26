package com.knotkt.cs26.server

import com.knotkt.cs26.contracts.ChatMessage
import com.knotkt.cs26.contracts.MediaAttachment
import java.util.UUID
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.ConcurrentHashMap

interface ChatStore {
    fun send(
        conversationId: String,
        senderId: String,
        clientMessageId: String,
        content: String,
        attachments: List<MediaAttachment>,
    ): ChatMessage

    fun list(conversationId: String, limit: Int): List<ChatMessage>
}

class InMemoryChatStore : ChatStore {
    private val messages = CopyOnWriteArrayList<ChatMessage>()
    private val byClientId = ConcurrentHashMap<String, ChatMessage>()

    override fun send(
        conversationId: String,
        senderId: String,
        clientMessageId: String,
        content: String,
        attachments: List<MediaAttachment>,
    ): ChatMessage {
        val key = "$senderId:$conversationId:$clientMessageId"
        return byClientId.computeIfAbsent(key) {
            ChatMessage(
                id = UUID.randomUUID().toString(),
                conversationId = conversationId,
                clientMessageId = clientMessageId,
                senderId = senderId,
                content = content,
                createdAtEpochMillis = System.currentTimeMillis(),
                attachments = attachments,
            ).also(messages::add)
        }
    }

    override fun list(conversationId: String, limit: Int): List<ChatMessage> = messages
        .asReversed()
        .filter { it.conversationId == conversationId }
        .take(limit)
        .reversed()
}