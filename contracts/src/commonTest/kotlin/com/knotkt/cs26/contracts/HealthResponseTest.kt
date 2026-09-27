package com.knotkt.cs26.contracts

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.serialization.json.Json

class HealthResponseTest {
    @Test
    fun statusIsPreserved() {
        assertEquals("ok", HealthResponse("ok").status)
    }

    @Test
    fun chatReceiptRoundTripsAsAStreamEvent() {
        val event: ChatStreamEvent = ChatStreamEvent.Receipt(
            clientMessageId = "client-1",
            messageId = "server-1",
            status = ChatDeliveryStatus.ACCEPTED,
        )

        val decoded = Json.decodeFromString<ChatStreamEvent>(Json.encodeToString(event))

        assertEquals(event, decoded)
    }
}