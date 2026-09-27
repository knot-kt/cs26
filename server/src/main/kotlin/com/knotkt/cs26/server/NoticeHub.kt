package com.knotkt.cs26.server

import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.websocket.Frame
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap

class NoticeHub {
    private val connections = ConcurrentHashMap<String, MutableSet<DefaultWebSocketServerSession>>()
    private val lock = Mutex()

    suspend fun join(userId: String, session: DefaultWebSocketServerSession) {
        lock.withLock { connections.computeIfAbsent(userId) { mutableSetOf() }.add(session) }
    }

    suspend fun leave(userId: String, session: DefaultWebSocketServerSession) {
        lock.withLock {
            connections[userId]?.let { sessions ->
                sessions.remove(session)
                if (sessions.isEmpty()) connections.remove(userId)
            }
        }
    }

    suspend fun broadcast(notice: String) {
        val sessions = lock.withLock { connections.values.flatMap { it.toList() } }
        sessions.forEach { session ->
            try {
                session.send(Frame.Text(notice))
            } catch (_: Throwable) {
                // Disconnected sessions are removed by their stream finally block.
            }
        }
    }
}