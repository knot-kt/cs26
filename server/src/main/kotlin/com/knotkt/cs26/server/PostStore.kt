package com.knotkt.cs26.server

import com.knotkt.cs26.contracts.MediaAttachment
import com.knotkt.cs26.contracts.Post
import java.util.UUID
import java.util.concurrent.CopyOnWriteArrayList

interface PostStore {
    fun create(authorId: String, content: String, attachments: List<MediaAttachment>): Post

    fun list(limit: Int): List<Post>
}

class InMemoryPostStore : PostStore {
    private val posts = CopyOnWriteArrayList<Post>()

    override fun create(authorId: String, content: String, attachments: List<MediaAttachment>): Post = Post(
        id = UUID.randomUUID().toString(),
        authorId = authorId,
        content = content,
        createdAtEpochMillis = System.currentTimeMillis(),
        attachments = attachments,
    ).also(posts::add)

    override fun list(limit: Int): List<Post> = posts
        .asReversed()
        .take(limit)
}