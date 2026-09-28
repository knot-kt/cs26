package com.knotkt.cs26.server

import java.util.concurrent.ConcurrentHashMap

interface FollowStore {
    fun setFollowing(followerId: String, followedId: String, following: Boolean): Boolean

    fun isFollowing(followerId: String, followedId: String): Boolean

    fun listFollowing(followerId: String): List<String>
}

class InMemoryFollowStore : FollowStore {
    private val following = ConcurrentHashMap<String, MutableSet<String>>()

    override fun setFollowing(followerId: String, followedId: String, following: Boolean): Boolean {
        if (followerId == followedId) return false
        val followed = this.following.computeIfAbsent(followerId) { ConcurrentHashMap.newKeySet() }
        if (following) followed.add(followedId) else followed.remove(followedId)
        return true
    }

    override fun isFollowing(followerId: String, followedId: String): Boolean =
        following[followerId]?.contains(followedId) == true

    override fun listFollowing(followerId: String): List<String> =
        following[followerId].orEmpty().toList().sorted()
}