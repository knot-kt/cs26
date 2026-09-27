package com.knotkt.cs26.shared

import com.knotkt.cs26.contracts.HealthResponse

data class HealthState(
    val response: HealthResponse? = null,
    val error: String? = null,
    val isLoading: Boolean = false,
)
