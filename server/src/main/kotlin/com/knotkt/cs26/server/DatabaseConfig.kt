package com.knotkt.cs26.server

import org.postgresql.ds.PGSimpleDataSource
import javax.sql.DataSource

data class DatabaseConfig(
    val jdbcUrl: String,
    val username: String,
    val password: String,
) {
    fun dataSource(): DataSource = PGSimpleDataSource().also { source ->
        source.setURL(jdbcUrl)
        source.user = username
        source.password = password
    }

    companion object {
        fun fromEnvironment(environment: Map<String, String> = System.getenv()): DatabaseConfig? {
            val jdbcUrl = environment["CS26_DATABASE_URL"] ?: return null
            return DatabaseConfig(
                jdbcUrl = jdbcUrl,
                username = environment["CS26_DATABASE_USER"] ?: "cs26",
                password = environment["CS26_DATABASE_PASSWORD"] ?: "",
            )
        }
    }
}
