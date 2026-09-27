package com.knotkt.cs26.server

import com.knotkt.cs26.contracts.AuthSession
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.sql.Connection
import java.sql.ResultSet
import java.util.UUID
import javax.sql.DataSource

/** PostgreSQL implementation for the AuthStore boundary. Calls must run off the Ktor event loop. */
class PostgresAuthStore(
    private val dataSource: DataSource,
) : AuthStore {
    override fun findChallenge(phone: String): StoredChallenge? = dataSource.connection.use { connection ->
        connection.prepareStatement(
            """
            SELECT code_hash, expires_at, resend_available_at, attempts_remaining
            FROM auth_code_challenges
            WHERE phone_e164 = ?
            ORDER BY expires_at DESC
            LIMIT 1
            """.trimIndent(),
        ).use { statement ->
            statement.setString(1, phone)
            statement.executeQuery().use { result ->
                if (!result.next()) null else result.toChallenge()
            }
        }
    }

    override fun saveChallenge(phone: String, challenge: StoredChallenge) {
        dataSource.connection.use { connection ->
            connection.transaction {
                prepareStatement("DELETE FROM auth_code_challenges WHERE phone_e164 = ?").use { statement ->
                    statement.setString(1, phone)
                    statement.executeUpdate()
                }
                prepareStatement(
                    """
                    INSERT INTO auth_code_challenges
                        (id, phone_e164, code_hash, expires_at, resend_available_at, attempts_remaining)
                    VALUES (?, ?, ?, ?, ?, ?)
                    """.trimIndent(),
                ).use { statement ->
                    statement.setObject(1, UUID.randomUUID())
                    statement.setString(2, phone)
                    statement.setBytes(3, challenge.codeHash)
                    statement.setTimestamp(4, java.sql.Timestamp(challenge.expiresAtMillis))
                    statement.setTimestamp(5, java.sql.Timestamp(challenge.resendAvailableAtMillis))
                    statement.setInt(6, challenge.attemptsRemaining)
                    statement.executeUpdate()
                }
            }
        }
    }

    override fun removeChallenge(phone: String) {
        dataSource.connection.use { connection ->
            connection.prepareStatement("DELETE FROM auth_code_challenges WHERE phone_e164 = ?").use { statement ->
                statement.setString(1, phone)
                statement.executeUpdate()
            }
        }
    }

    override fun decrementAttempts(phone: String): Int? = dataSource.connection.use { connection ->
        connection.prepareStatement(
            """
            UPDATE auth_code_challenges
            SET attempts_remaining = attempts_remaining - 1
            WHERE phone_e164 = ? AND attempts_remaining > 0
            RETURNING attempts_remaining
            """.trimIndent(),
        ).use { statement ->
            statement.setString(1, phone)
            statement.executeQuery().use { result ->
                if (result.next()) result.getInt(1) else null
            }
        }
    }

    override fun saveSession(phone: String, session: AuthSession) {
        dataSource.connection.use { connection ->
            connection.transaction {
                val userId = UUID.fromString(session.userId)
                prepareStatement(
                    """
                    INSERT INTO auth_users (id, phone_e164)
                    VALUES (?, ?)
                    ON CONFLICT (phone_e164) DO NOTHING
                    """.trimIndent(),
                ).use { statement ->
                    statement.setObject(1, userId)
                    statement.setString(2, phone)
                    statement.executeUpdate()
                }
                prepareStatement(
                    """
                    INSERT INTO auth_sessions (access_token_hash, user_id, expires_at)
                    VALUES (?, ?, CURRENT_TIMESTAMP + INTERVAL '30 days')
                    """.trimIndent(),
                ).use { statement ->
                    statement.setBytes(1, hashToken(session.accessToken))
                    statement.setObject(2, userId)
                    statement.executeUpdate()
                }
            }
        }
    }

    override fun revokeSession(accessToken: String): Boolean = dataSource.connection.use { connection ->
        connection.prepareStatement(
            """
            UPDATE auth_sessions
            SET revoked_at = CURRENT_TIMESTAMP
            WHERE access_token_hash = ? AND revoked_at IS NULL AND expires_at > CURRENT_TIMESTAMP
            """.trimIndent(),
        ).use { statement ->
            statement.setBytes(1, hashToken(accessToken))
            statement.executeUpdate() > 0
        }
    }

    private fun ResultSet.toChallenge(): StoredChallenge = StoredChallenge(
        codeHash = getBytes("code_hash"),
        expiresAtMillis = getTimestamp("expires_at").time,
        resendAvailableAtMillis = getTimestamp("resend_available_at").time,
        attemptsRemaining = getInt("attempts_remaining"),
    )

    private fun hashToken(accessToken: String): ByteArray = MessageDigest
        .getInstance("SHA-256")
        .digest(accessToken.toByteArray(StandardCharsets.UTF_8))
}

private fun Connection.transaction(block: Connection.() -> Unit) {
    val previousAutoCommit = autoCommit
    autoCommit = false
    try {
        block()
        commit()
    } catch (error: Throwable) {
        rollback()
        throw error
    } finally {
        autoCommit = previousAutoCommit
    }
}
