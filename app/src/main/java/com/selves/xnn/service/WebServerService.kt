package com.selves.xnn.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.selves.xnn.MainActivity
import com.selves.xnn.R
import com.selves.xnn.data.repository.ChatGroupRepository
import com.selves.xnn.data.repository.DynamicRepository
import com.selves.xnn.data.repository.MemberDiaryRepository
import com.selves.xnn.data.repository.MemberRepository
import com.selves.xnn.data.repository.MessageRepository
import com.selves.xnn.data.repository.SystemRepository
import com.selves.xnn.data.repository.TodoRepository
import com.selves.xnn.model.Todo
import com.selves.xnn.model.TodoPriority
import dagger.hilt.android.AndroidEntryPoint
import io.ktor.http.*
import io.ktor.serialization.gson.*
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.net.Inet4Address
import java.net.NetworkInterface
import java.util.UUID
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@AndroidEntryPoint
class WebServerService : Service() {

    @Inject lateinit var memberRepository: MemberRepository
    @Inject lateinit var chatGroupRepository: ChatGroupRepository
    @Inject lateinit var messageRepository: MessageRepository
    @Inject lateinit var todoRepository: TodoRepository
    @Inject lateinit var memberDiaryRepository: MemberDiaryRepository
    @Inject lateinit var systemRepository: SystemRepository
    @Inject lateinit var dynamicRepository: DynamicRepository

    private var server: EmbeddedServer<*, *>? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    companion object {
        const val SERVER_PORT = 8080
        private const val NOTIFICATION_ID = 2001
        private const val CHANNEL_ID = "web_server_channel"
        private const val TAG = "WebServerService"

        fun getLocalIpAddress(): String {
            try {
                val interfaces = NetworkInterface.getNetworkInterfaces()
                for (intf in interfaces) {
                    val addrs = intf.inetAddresses
                    for (addr in addrs) {
                        if (!addr.isLoopbackAddress && addr is Inet4Address) {
                            return addr.hostAddress ?: "127.0.0.1"
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to get local IP: ${e.message}")
            }
            return "127.0.0.1"
        }

        fun start(context: Context) {
            val intent = Intent(context, WebServerService::class.java)
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, WebServerService::class.java)
            context.stopService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, buildNotification())
        startServer()
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        server?.stop(1000, 5000)
        server = null
        Log.i(TAG, "Web server stopped")
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startServer() {
        try {
            server = embeddedServer(CIO, port = SERVER_PORT) {
                installPlugins()
                setupRoutes()
            }
            serviceScope.launch {
                try {
                    server!!.start(wait = false)
                    val ip = getLocalIpAddress()
                    Log.i(TAG, "Web server started: http://$ip:$SERVER_PORT")
                    updateNotification("http://$ip:$SERVER_PORT")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to start web server: ${e.message}", e)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create web server: ${e.message}", e)
        }
    }

    private fun Application.installPlugins() {
        install(CORS) {
            anyHost()
            allowMethod(HttpMethod.Get)
            allowMethod(HttpMethod.Post)
            allowMethod(HttpMethod.Put)
            allowMethod(HttpMethod.Delete)
            allowMethod(HttpMethod.Options)
            allowHeader(HttpHeaders.ContentType)
            allowHeader(HttpHeaders.Authorization)
        }
        install(ContentNegotiation) {
            gson {
                setPrettyPrinting()
                serializeNulls()
            }
        }
        install(WebSockets) {
            pingPeriod = 30.seconds
            timeout = 15.seconds
            maxFrameSize = Long.MAX_VALUE
            masking = false
        }
        install(StatusPages) {
            exception<Throwable> { call, cause ->
                Log.e(TAG, "Route error: ${cause.message}", cause)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to (cause.message ?: "Internal server error"))
                )
            }
        }
    }

    private fun Application.setupRoutes() {
        routing {
            // ===== 根路径 / =====
            get("/") {
                call.respondText(
                    contentType = io.ktor.http.ContentType.Text.Html,
                    text = """
                        <!DOCTYPE html>
                        <html lang="zh">
                        <head>
                          <meta charset="UTF-8">
                          <meta name="viewport" content="width=device-width,initial-scale=1">
                          <title>Selves API</title>
                          <style>
                            body{font-family:sans-serif;max-width:480px;margin:60px auto;padding:0 16px;color:#333}
                            h1{font-size:1.6rem;margin-bottom:4px}
                            p{color:#666;margin-bottom:24px}
                            a{display:block;padding:12px 16px;background:#6750A4;color:#fff;border-radius:8px;text-decoration:none;margin-bottom:8px}
                            a:hover{opacity:.85}
                          </style>
                        </head>
                        <body>
                          <h1>Selves API</h1>
                          <p>服务器正在运行，端口 $SERVER_PORT</p>
                          <a href="/api/status">GET /api/status</a>
                          <a href="/api/members">GET /api/members</a>
                          <a href="/api/groups">GET /api/groups</a>
                          <a href="/api/todos">GET /api/todos</a>
                          <a href="/api/dynamics">GET /api/dynamics</a>
                          <a href="/api/diaries">GET /api/diaries</a>
                        </body>
                        </html>
                    """.trimIndent()
                )
            }

            // ===== 状态 =====
            get("/api/status") {
                call.respond(mapOf(
                    "status" to "ok",
                    "version" to "1.0",
                    "port" to SERVER_PORT,
                    "connectedClients" to WebSocketManager.connectedCount
                ))
            }

            // ===== 系统信息 =====
            get("/api/system") {
                val system = systemRepository.getCurrentSystem().first()
                if (system != null) {
                    call.respond(system)
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "No system found"))
                }
            }

            // ===== 成员 =====
            get("/api/members") {
                val members = memberRepository.getAllMembers().first()
                call.respond(members)
            }

            get("/api/members/{id}") {
                val id = call.parameters["id"]
                    ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing id"))
                val member = memberRepository.getMemberById(id)
                if (member != null) call.respond(member)
                else call.respond(HttpStatusCode.NotFound, mapOf("error" to "Member not found"))
            }

            // ===== 群聊 =====
            get("/api/groups") {
                val groups = chatGroupRepository.getAllGroups().first()
                call.respond(groups)
            }

            get("/api/groups/{groupId}") {
                val groupId = call.parameters["groupId"]
                    ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing groupId"))
                val group = chatGroupRepository.getGroupById(groupId)
                if (group != null) call.respond(group)
                else call.respond(HttpStatusCode.NotFound, mapOf("error" to "Group not found"))
            }

            get("/api/groups/{groupId}/messages") {
                val groupId = call.parameters["groupId"]
                    ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing groupId"))
                val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 50
                val messages = messageRepository.getRecentGroupMessages(groupId, limit).first()
                call.respond(messages)
            }

            // ===== 待办 =====
            get("/api/todos") {
                val todos = todoRepository.getAllTodos().first()
                call.respond(todos)
            }

            get("/api/todos/{id}") {
                val id = call.parameters["id"]
                    ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing id"))
                val todo = todoRepository.getTodoById(id)
                if (todo != null) call.respond(todo)
                else call.respond(HttpStatusCode.NotFound, mapOf("error" to "Todo not found"))
            }

            post("/api/todos") {
                val body = call.receive<TodoCreateRequest>()
                val todo = Todo(
                    id = UUID.randomUUID().toString(),
                    title = body.title,
                    description = body.description ?: "",
                    createdBy = body.createdBy,
                    priority = when (body.priority) {
                        0 -> TodoPriority.LOW
                        2 -> TodoPriority.HIGH
                        else -> TodoPriority.NORMAL
                    }
                )
                todoRepository.saveTodo(todo)
                WebSocketManager.broadcast("TODO_CREATED", todo)
                call.respond(HttpStatusCode.Created, todo)
            }

            put("/api/todos/{id}") {
                val id = call.parameters["id"]
                    ?: return@put call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing id"))
                val existing = todoRepository.getTodoById(id)
                    ?: return@put call.respond(HttpStatusCode.NotFound, mapOf("error" to "Todo not found"))
                val body = call.receive<TodoUpdateRequest>()
                val updated = existing.copy(
                    title = body.title ?: existing.title,
                    description = body.description ?: existing.description,
                    isCompleted = body.isCompleted ?: existing.isCompleted,
                    completedAt = if (body.isCompleted == true && !existing.isCompleted)
                        System.currentTimeMillis() else existing.completedAt
                )
                todoRepository.updateTodo(updated)
                WebSocketManager.broadcast("TODO_UPDATED", updated)
                call.respond(updated)
            }

            delete("/api/todos/{id}") {
                val id = call.parameters["id"]
                    ?: return@delete call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing id"))
                todoRepository.deleteTodoById(id)
                WebSocketManager.broadcast("TODO_DELETED", mapOf("id" to id))
                call.respond(HttpStatusCode.NoContent)
            }

            // ===== 动态 =====
            get("/api/dynamics") {
                val dynamics = dynamicRepository.getAllDynamics().first()
                call.respond(dynamics)
            }

            // ===== 日记 =====
            get("/api/diaries") {
                val memberId = call.request.queryParameters["memberId"]
                if (memberId != null) {
                    val diaries = memberDiaryRepository.getDiariesByMember(memberId).first()
                    call.respond(diaries)
                } else {
                    val diaries = memberDiaryRepository.getAllDiaries()
                    call.respond(diaries)
                }
            }

            // ===== WebSocket 实时推送 =====
            webSocket("/ws") {
                WebSocketManager.addSession(this)
                Log.d(TAG, "WS client connected, total: ${WebSocketManager.connectedCount}")
                try {
                    for (frame in incoming) {
                        // 当前仅接收，不处理客户端消息
                    }
                } catch (e: Exception) {
                    Log.d(TAG, "WS client error: ${e.message}")
                } finally {
                    WebSocketManager.removeSession(this)
                    Log.d(TAG, "WS client disconnected, total: ${WebSocketManager.connectedCount}")
                }
            }
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Web 访问服务",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "保持 Web 访问服务在后台运行"
            setShowBadge(false)
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun buildNotification(url: String = "启动中...") =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Selves Web 访问已开启")
            .setContentText(url)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setSilent(true)
            .setContentIntent(
                PendingIntent.getActivity(
                    this, 0,
                    Intent(this, MainActivity::class.java),
                    PendingIntent.FLAG_IMMUTABLE
                )
            )
            .build()

    private fun updateNotification(url: String) {
        val nm = getSystemService(NotificationManager::class.java)
        nm.notify(NOTIFICATION_ID, buildNotification(url))
    }
}

data class TodoCreateRequest(
    val title: String,
    val description: String? = null,
    val createdBy: String,
    val priority: Int = 1
)

data class TodoUpdateRequest(
    val title: String? = null,
    val description: String? = null,
    val isCompleted: Boolean? = null
)
