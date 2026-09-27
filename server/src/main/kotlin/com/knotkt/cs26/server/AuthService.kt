package com.knotkt.cs26.server

import com.knotkt.cs26.contracts.AuthSession
import com.knotkt.cs26.contracts.RequestCodeResponse
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.UUID

data class CodeRequestResult(
    val response: RequestCodeResponse,
    val retryAfterSeconds: Int = 0,
)

interface AuthService {
    fun requestCode(phone: String): CodeRequestResult?

    fun verifyCode(phone: String, code: String): AuthSession?

    fun logout(accessToken: String): Boolean
}

/**
 * Development-only authentication adapter. Replace it with the SMS-backed implementation before
 * enabling production authentication.
 */
class InMemoryAuthService(
    private val store: AuthStore = InMemoryAuthStore(),
) : AuthService {

    override fun requestCode(phone: String): CodeRequestResult? {
        val normalizedPhone = normalizePhone(phone)
        if (normalizedPhone.length < MIN_PHONE_DIGITS) return null

        val now = System.currentTimeMillis()
        val existing = store.findChallenge(normalizedPhone)
        if (existing != null && existing.resendAvailableAtMillis > now) {
            return CodeRequestResult(
                response = response(),
                retryAfterSeconds = ((existing.resendAvailableAtMillis - now) / 1_000).toInt().coerceAtLeast(1),
            )
        }

        store.saveChallenge(normalizedPhone, StoredChallenge(
            codeHash = hashCode(normalizedPhone, DEVELOPMENT_CODE),
            expiresAtMillis = System.currentTimeMillis() + CODE_TTL_MILLIS,
            resendAvailableAtMillis = now + RESEND_COOLDOWN_MILLIS,
            attemptsRemaining = MAX_ATTEMPTS,
        ))
        return CodeRequestResult(
            response = response(),
        )
    }

    override fun verifyCode(phone: String, code: String): AuthSession? {
        val normalizedPhone = normalizePhone(phone)
        val challenge = store.findChallenge(normalizedPhone) ?: return null
        if (challenge.expiresAtMillis < System.currentTimeMillis()) {
            store.removeChallenge(normalizedPhone)
            return null
        }
        val matches = MessageDigest.isEqual(
            challenge.codeHash,
            hashCode(normalizedPhone, code),
        )
        if (!matches) {
            val attemptsRemaining = store.decrementAttempts(normalizedPhone)
            if (attemptsRemaining != null && attemptsRemaining <= 0) store.removeChallenge(normalizedPhone)
            return null
        }

        store.removeChallenge(normalizedPhone)
        val session = AuthSession(
            userId = UUID.nameUUIDFromBytes(normalizedPhone.toByteArray()).toString(),
            accessToken = "dev-${UUID.randomUUID()}",
        )
        store.saveSession(normalizedPhone, session)
        return session
    }

    override fun logout(accessToken: String): Boolean = store.revokeSession(accessToken)

    private fun response() = RequestCodeResponse(
        expiresInSeconds = CODE_TTL_MILLIS.toInt() / 1_000,
        developmentCode = DEVELOPMENT_CODE,
    )

    private fun normalizePhone(phone: String): String = phone.filter(Char::isDigit)

    private fun hashCode(phone: String, code: String): ByteArray = MessageDigest
        .getInstance("SHA-256")
        .digest("$phone:$code".toByteArray(StandardCharsets.UTF_8))

    private companion object {
        const val DEVELOPMENT_CODE = "123456"
        const val CODE_TTL_MILLIS = 5 * 60 * 1_000L
        const val RESEND_COOLDOWN_MILLIS = 60 * 1_000L
        const val MAX_ATTEMPTS = 5
        const val MIN_PHONE_DIGITS = 8
    }
}
