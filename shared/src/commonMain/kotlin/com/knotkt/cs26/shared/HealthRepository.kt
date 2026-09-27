package com.knotkt.cs26.shared

import com.knotkt.cs26.contracts.HealthResponse
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText

class HealthRepository(
    private val client: HttpClient,
    private val baseUrl: String,
) {
    suspend fun check(): HealthResponse {
        val status = client.get("$baseUrl/health").bodyAsText()
        return HealthResponse(status = status)
    }
}
