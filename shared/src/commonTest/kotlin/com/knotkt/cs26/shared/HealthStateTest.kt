package com.knotkt.cs26.shared

import com.knotkt.cs26.contracts.HealthResponse
import kotlin.test.Test
import kotlin.test.assertEquals

class HealthStateTest {
    @Test
    fun storesHealthResponse() {
        assertEquals("ok", HealthState(HealthResponse("ok")).response?.status)
    }

    @Test
    fun loadingDefaultsToFalse() {
        assertEquals(false, HealthState().isLoading)
    }
}
