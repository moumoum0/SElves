package com.selves.xnn.service

import com.google.gson.Gson
import io.ktor.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.CopyOnWriteArraySet

object WebSocketManager {

    private val sessions = CopyOnWriteArraySet<WebSocketSession>()
    private val gson = Gson()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    val connectedCount: Int get() = sessions.size

    fun addSession(session: WebSocketSession) {
        sessions.add(session)
    }

    fun removeSession(session: WebSocketSession) {
        sessions.remove(session)
    }

    fun broadcast(eventType: String, data: Any? = null) {
        val json = gson.toJson(mapOf("type" to eventType, "data" to data))
        scope.launch {
            val dead = mutableListOf<WebSocketSession>()
            for (session in sessions) {
                try {
                    session.send(Frame.Text(json))
                } catch (e: Exception) {
                    dead.add(session)
                }
            }
            sessions.removeAll(dead.toSet())
        }
    }
}
