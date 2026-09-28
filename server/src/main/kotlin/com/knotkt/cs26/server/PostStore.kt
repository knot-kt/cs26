package com.knotkt.cs26.server

import com.knotkt.cs26.contracts.Comment
import com.knotkt.cs26.contracts.MediaAttachment
import com.knotkt.cs26.contracts.Post
import com.knotkt.cs26.contracts.PostType
import java.util.UUID
import java.util.concurrent.CopyOnWriteArrayList

interface PostStore {
    fun create(
        authorId: String,
        content: String,
        attachments: List<MediaAttachment>,
        type: PostType = PostType.GENERAL,
    ): Post

    fun list(limit: Int, viewerId: String, beforeEpochMillis: Long? = null): List<Post>

    fun delete(postId: String, authorId: String): Boolean

    fun toggleLike(postId: String, userId: String): Boolean

    fun addComment(postId: String, authorId: String, content: String): Comment?

    fun comments(postId: String): List<Comment>
}

class InMemoryPostStore : PostStore {
    private val posts = CopyOnWriteArrayList<Post>()
    private val likes = java.util.concurrent.ConcurrentHashMap<String, MutableSet<String>>()
    private val comments = java.util.concurrent.ConcurrentHashMap<String, MutableList<Comment>>()

    override fun create(
        authorId: String,
        content: String,
        attachments: List<MediaAttachment>,
        type: PostType,
    ): Post = Post(
        id = UUID.randomUUID().toString(),
        authorId = authorId,
        content = content,
        createdAtEpochMillis = System.currentTimeMillis(),
        attachments = attachments,
        type = type,
    ).also(posts::add)

    override fun list(limit: Int, viewerId: String, beforeEpochMillis: Long?): List<Post> = posts
        .asReversed()
        .asSequence()
        .filter { beforeEpochMillis == null || it.createdAtEpochMillis < beforeEpochMillis }
        .take(limit)
        .map { post -> post.withStats(viewerId) }
        .toList()

    override fun delete(postId: String, authorId: String): Boolean {
        val post = posts.firstOrNull { it.id == postId && it.authorId == authorId } ?: return false
        return posts.remove(post).also {
            if (it) {
                likes.remove(postId)
                comments.remove(postId)
            }
        }
    }

    override fun toggleLike(postId: String, userId: String): Boolean {
        if (posts.none { it.id == postId }) return false
        val postLikes = likes.computeIfAbsent(postId) { java.util.concurrent.ConcurrentHashMap.newKeySet() }
        if (!postLikes.add(userId)) postLikes.remove(userId)
        return true
    }

    override fun addComment(postId: String, authorId: String, content: String): Comment? {
        if (posts.none { it.id == postId }) return null
        val comment = Comment(UUID.randomUUID().toString(), postId, authorId, content, System.currentTimeMillis())
        comments.computeIfAbsent(postId) { CopyOnWriteArrayList() }.add(comment)
        return comment
    }

    override fun comments(postId: String): List<Comment> = comments[postId].orEmpty().toList()

    private fun Post.withStats(viewerId: String): Post {
        val postLikes = likes[id].orEmpty()
        return copy(
            likeCount = postLikes.size,
            commentCount = comments[id]?.size ?: 0,
            likedByViewer = viewerId in postLikes,
        )
    }
}