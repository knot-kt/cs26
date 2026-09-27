package com.knotkt.cs26.server

import com.knotkt.cs26.contracts.AuthSession
import com.knotkt.cs26.contracts.RequestCodeResponse
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

interface AuthService {
    fun requestCode(phone: String): RequestCodeResponse?

    fun verifyCode(phone: String, code: String): AuthSession?
}

/**
 * Development-only authentication adapter. Replace it with the SMS-backed implementation before
 * enabling production authentication.
 */
class InMemoryAuthService : AuthService {
    private data class Challenge(val expiresAtMillis: Long)

    private val challenges = ConcurrentHashMap<String, Challenge>()

    override fun requestCode(phone: String): RequestCodeResponse? {
        if (!isValidPhone(phone)) return null

        challenges[phone] = Challenge(
            expiresAtMillis = System.currentTimeMillis() + CODE_TTL_MILLIS,
        )
        return RequestCodeResponse(
            expiresInSeconds = CODE_TTL_MILLIS.toInt() / 1_000,
            developmentCode = DEVELOPMENT_CODE,
        )
    }

    override fun verifyCode(phone: String, code: String): AuthSession? {
        val challenge = challenges[phone] ?: return null
        if (challenge.expiresAtMillis < System.currentTimeMillis() || code != DEVELOPMENT_CODE) {
            return null
        }

        challenges.remove(phone)
        return AuthSession(
            userId = "dev-${phone.filter(Char::isDigit)}",
            accessToken = "dev-${UUID.randomUUID()}",
        )
    }

    private fun isValidPhone(phone: String): Boolean = phone.filter(Char::isDigit).length >= 8

    private companion object {
        const val DEVELOPMENT_CODE = "123456"
        const val CODE_TTL_MILLIS = 5 * 60 * 1_000L
    }
}
