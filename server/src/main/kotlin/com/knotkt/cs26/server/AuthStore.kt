package com.knotkt.cs26.server

import com.knotkt.cs26.contracts.AuthSession
import java.util.concurrent.ConcurrentHashMap

data class StoredChallenge(
    val codeHash: ByteArray,
    val expiresAtMillis: Long,
    val resendAvailableAtMillis: Long,
    val attemptsRemaining: Int,
)

interface AuthStore {
    fun findChallenge(phone: String): StoredChallenge?

    fun saveChallenge(phone: String, challenge: StoredChallenge)

    fun removeChallenge(phone: String)

    fun decrementAttempts(phone: String): Int?

    fun saveSession(phone: String, session: AuthSession)

    fun revokeSession(accessToken: String): Boolean
}

class InMemoryAuthStore : AuthStore {
    private val challenges = ConcurrentHashMap<String, StoredChallenge>()
    private val sessions = ConcurrentHashMap<String, AuthSession>()

    override fun findChallenge(phone: String): StoredChallenge? = challenges[phone]

    override fun saveChallenge(phone: String, challenge: StoredChallenge) {
        challenges[phone] = challenge
    }

    override fun removeChallenge(phone: String) {
        challenges.remove(phone)
    }

    override fun decrementAttempts(phone: String): Int? {
        var remaining: Int? = null
        challenges.computeIfPresent(phone) { _, challenge ->
            val updated = challenge.copy(attemptsRemaining = challenge.attemptsRemaining - 1)
            remaining = updated.attemptsRemaining
            updated
        }
        return remaining
    }

    override fun saveSession(phone: String, session: AuthSession) {
        sessions[session.accessToken] = session
    }

    override fun revokeSession(accessToken: String): Boolean = sessions.remove(accessToken) != null
}
