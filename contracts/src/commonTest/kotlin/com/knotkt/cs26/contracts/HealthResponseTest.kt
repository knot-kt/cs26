package com.knotkt.cs26.contracts

import kotlin.test.Test
import kotlin.test.assertEquals

class HealthResponseTest {
    @Test
    fun statusIsPreserved() {
        assertEquals("ok", HealthResponse("ok").status)
    }
}
