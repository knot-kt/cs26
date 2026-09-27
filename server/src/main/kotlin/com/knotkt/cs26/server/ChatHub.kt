package com.knotkt.cs26.server

import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.websocket.Frame
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap

class ChatHub {
    private val connections = ConcurrentHashMap<String, MutableSet<DefaultWebSocketServerSession>>()
    private val lock = Mutex()

    suspend fun join(conversationId: String, session: DefaultWebSocketServerSession) {
        lock.withLock {
            connections.computeIfAbsent(conversationId) { mutableSetOf() }.add(session)
        }
    }

    suspend fun leave(conversationId: String, session: DefaultWebSocketServerSession) {
        lock.withLock {
            connections[conversationId]?.let { sessions ->
                sessions.remove(session)
                if (sessions.isEmpty()) connections.remove(conversationId)
            }
        }
    }

    suspend fun broadcast(conversationId: String, payload: String) {
        val sessions = lock.withLock { connections[conversationId].orEmpty().toList() }
        sessions.forEach { session ->
            try {
                session.send(Frame.Text(payload))
            } catch (_: Throwable) {
                // A disconnected session is removed by its own finally block.
            }
        }
    }
}