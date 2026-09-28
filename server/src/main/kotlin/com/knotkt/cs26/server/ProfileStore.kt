package com.knotkt.cs26.server

import com.knotkt.cs26.contracts.Profile
import java.util.concurrent.ConcurrentHashMap

interface ProfileStore {
    fun get(userId: String): Profile

    fun update(
        userId: String,
        nickname: String,
        interests: List<String>,
        anonymousByDefault: Boolean,
    ): Profile
}

class InMemoryProfileStore : ProfileStore {
    private val profiles = ConcurrentHashMap<String, Profile>()

    override fun get(userId: String): Profile = profiles.computeIfAbsent(userId) {
        Profile(userId = userId, nickname = "同学", anonymousByDefault = true)
    }

    override fun update(
        userId: String,
        nickname: String,
        interests: List<String>,
        anonymousByDefault: Boolean,
    ): Profile = Profile(
        userId = userId,
        nickname = nickname,
        interests = interests,
        anonymousByDefault = anonymousByDefault,
    ).also { profiles[userId] = it }
}