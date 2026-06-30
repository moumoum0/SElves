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
import com.selves.xnn.data.AppDatabase
import com.selves.xnn.data.MemberPreferences
import com.selves.xnn.data.Mappers.toDto
import com.selves.xnn.data.repository.ChatGroupRepository
import com.selves.xnn.data.repository.DynamicRepository
import com.selves.xnn.data.repository.MemberDiaryRepository
import com.selves.xnn.data.repository.MemberRepository
import com.selves.xnn.data.repository.MessageRepository
import com.selves.xnn.data.repository.SystemRepository
import com.selves.xnn.data.repository.TodoRepository
import com.selves.xnn.data.repository.VoteRepository
import com.selves.xnn.model.DynamicType
import com.selves.xnn.model.Todo
import com.selves.xnn.model.TodoPriority
import com.selves.xnn.data.entity.ChatGroupEntity
import dagger.hilt.android.AndroidEntryPoint
import io.ktor.http.*
import io.ktor.serialization.gson.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
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
import kotlinx.coroutines.runBlocking
import java.io.IOException
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
    @Inject lateinit var voteRepository: VoteRepository
    @Inject lateinit var database: AppDatabase
    @Inject lateinit var memberPreferences: MemberPreferences

    @Volatile
    private var server: EmbeddedServer<*, *>? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    companion object {
        const val SERVER_PORT = 8080
        private const val NOTIFICATION_ID = 2001
        private const val CHANNEL_ID = "web_server_channel"
        private const val WEB_ASSET_ROOT = "web"
        private const val TAG = "WebServerService"

        fun getLocalIpAddress(): String {
            // 优先取 wlan* 接口（Wi-Fi），避免返回移动数据/VPN/热点 IP
            try {
                val interfaces = NetworkInterface.getNetworkInterfaces()?.toList() ?: emptyList()
                val wifiAddr = interfaces
                    .filter { it.name.startsWith("wlan") && it.isUp && !it.isLoopback }
                    .flatMap { it.inetAddresses.toList() }
                    .filterIsInstance<Inet4Address>()
                    .firstOrNull { !it.isLoopbackAddress }
                if (wifiAddr != null) return wifiAddr.hostAddress ?: "127.0.0.1"

                // 回退：任意非回环 IPv4（排除移动数据接口 rmnet/ccmni）
                val fallbackAddr = interfaces
                    .filter { it.isUp && !it.isLoopback && !it.name.startsWith("rmnet") && !it.name.startsWith("ccmni") }
                    .flatMap { it.inetAddresses.toList() }
                    .filterIsInstance<Inet4Address>()
                    .firstOrNull { !it.isLoopbackAddress }
                if (fallbackAddr != null) return fallbackAddr.hostAddress ?: "127.0.0.1"
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
        val runningServer = server
        server = null
        serviceScope.launch {
            runningServer?.stop(1000, 5000)
            Log.i(TAG, "Web server stopped")
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startServer() {
        if (server != null) {
            serviceScope.launch {
                val ip = getLocalIpAddress()
                Log.i(TAG, "Web server already running: http://$ip:$SERVER_PORT")
                updateNotification("http://$ip:$SERVER_PORT")
            }
            return
        }

        try {
            val newServer = embeddedServer(CIO, port = SERVER_PORT) {
                installPlugins()
                setupRoutes()
            }
            server = newServer

            serviceScope.launch {
                try {
                    newServer.start(wait = false)
                    if (server === newServer) {
                        val ip = getLocalIpAddress()
                        Log.i(TAG, "Web server started: http://$ip:$SERVER_PORT")
                        updateNotification("http://$ip:$SERVER_PORT")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to start web server: ${e.message}", e)
                    if (server === newServer) {
                        server = null
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create web server: ${e.message}", e)
            server = null
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
        install(Authentication) {
            bearer("api-token") {
                authenticate { credential ->
                    val storedToken = runBlocking { memberPreferences.getWebApiToken() }
                    if (storedToken != null && credential.token == storedToken) {
                        UserIdPrincipal("web")
                    } else null
                }
            }
        }
    }

    private fun Application.setupRoutes() {
        routing {
            get("/") {
                if (!call.respondWebIndex()) {
                    call.respondApiLandingPage()
                }
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
                val entity = database.systemDao().getCurrentSystem().first()
                if (entity != null) {
                    call.respond(entity.toDto())
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "No system found"))
                }
            }

            // ===== 成员 =====
            get("/api/members") {
                val entities = database.memberDao().getAllMembersSync()
                    .filter { !it.isDeleted }
                call.respond(entities.map { it.toDto() })
            }

            get("/api/members/{id}") {
                val id = call.parameters["id"]
                    ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing id"))
                val entity = database.memberDao().getMemberById(id)
                if (entity != null && !entity.isDeleted) call.respond(entity.toDto())
                else call.respond(HttpStatusCode.NotFound, mapOf("error" to "Member not found"))
            }

            // ===== 群聊 =====
            get("/api/groups") {
                val entities = database.chatGroupDao().getAllGroupsSync()
                call.respond(entities.map { it.toDto(database.memberDao()) })
            }

            get("/api/groups/{groupId}") {
                val groupId = call.parameters["groupId"]
                    ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing groupId"))
                val entity = database.chatGroupDao().getGroupById(groupId)
                if (entity != null) call.respond(entity.toDto(database.memberDao()))
                else call.respond(HttpStatusCode.NotFound, mapOf("error" to "Group not found"))
            }

            get("/api/groups/{groupId}/messages") {
                val groupId = call.parameters["groupId"]
                    ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing groupId"))
                val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 50
                val entities = database.messageDao().getRecentMessagesByGroupId(groupId, limit).first()
                call.respond(entities.map { it.toDto() })
            }

            // ===== 待办 =====
            get("/api/todos") {
                val entities = database.todoDao().getAllTodos().first()
                call.respond(entities.map { it.toDto() })
            }

            get("/api/todos/{id}") {
                val id = call.parameters["id"]
                    ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing id"))
                val entity = database.todoDao().getTodoById(id)
                if (entity != null) call.respond(entity.toDto())
                else call.respond(HttpStatusCode.NotFound, mapOf("error" to "Todo not found"))
            }

            // ===== 动态 =====
            get("/api/dynamics") {
                val entities = database.dynamicDao().getAllDynamics().first()
                call.respond(entities.map { it.toDto() })
            }

            // ===== 日记 =====
            get("/api/diaries") {
                val memberId = call.request.queryParameters["memberId"]
                if (memberId != null) {
                    val entities = database.memberDiaryDao().getDiariesByMember(memberId).first()
                    call.respond(entities.map { it.toDto() })
                } else {
                    val entities = database.memberDiaryDao().getAllDiariesSync()
                    call.respond(entities.map { it.toDto() })
                }
            }

            // ===== 投票 =====
            get("/api/votes") {
                val userId = call.request.queryParameters["userId"]
                val voteEntities = database.voteDao().getAllVotesSync()
                val dtos = voteEntities.map { entity ->
                    val options = database.voteDao().getVoteOptionsSync(entity.id)
                    val userRecords = if (userId != null) database.voteDao().getUserVoteRecords(entity.id, userId) else emptyList()
                    val optionDtos = options.map { it.toDto(entity.totalVotes, userRecords.any { r -> r.optionId == it.id }) }
                    entity.toDto(optionDtos, userRecords.isNotEmpty())
                }
                call.respond(dtos)
            }

            get("/api/votes/{id}") {
                val id = call.parameters["id"]
                    ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing id"))
                val userId = call.request.queryParameters["userId"]
                val entity = database.voteDao().getVoteById(id)
                    ?: return@get call.respond(HttpStatusCode.NotFound, mapOf("error" to "Vote not found"))
                val options = database.voteDao().getVoteOptionsSync(id)
                val userRecords = if (userId != null) database.voteDao().getUserVoteRecords(id, userId) else emptyList()
                val optionDtos = options.map { it.toDto(entity.totalVotes, userRecords.any { r -> r.optionId == it.id }) }
                call.respond(entity.toDto(optionDtos, userRecords.isNotEmpty()))
            }

            get("/api/votes/{id}/records") {
                val id = call.parameters["id"]
                    ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing id"))
                val records = database.voteDao().getVoteRecords(id).first()
                call.respond(records.map { it.toDto() })
            }

            // ===== 动态评论 =====
            get("/api/dynamics/{id}/comments") {
                val id = call.parameters["id"]
                    ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing id"))
                val comments = database.dynamicDao().getCommentsByDynamicId(id).first()
                call.respond(comments.map { it.toDto() })
            }

            // ===== 需要鉴权的写入路由 =====
            authenticate("api-token") {
                // --- 消息 ---
                post("/api/groups/{groupId}/messages") {
                    val groupId = call.parameters["groupId"]
                        ?: return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing groupId"))
                    val body = call.receive<MessageCreateRequest>()
                    val message = com.selves.xnn.model.Message(
                        id = UUID.randomUUID().toString(),
                        senderId = body.senderId,
                        content = body.content,
                        timestamp = System.currentTimeMillis(),
                        type = if (body.type == "IMAGE") com.selves.xnn.model.MessageType.IMAGE else com.selves.xnn.model.MessageType.TEXT,
                        imagePath = null
                    )
                    messageRepository.saveMessage(message, groupId)
                    val dto = database.messageDao().getMessageById(message.id)?.toDto(groupId)
                    WebSocketManager.broadcast("MESSAGE_CREATED", dto ?: mapOf("id" to message.id, "groupId" to groupId))
                    call.respond(HttpStatusCode.Created, dto ?: mapOf("id" to message.id))
                }

                // --- 动态 ---
                post("/api/dynamics") {
                    val body = call.receive<DynamicCreateRequest>()
                    val id = dynamicRepository.createDynamic(
                        title = body.title,
                        content = body.content,
                        authorId = body.authorId,
                        authorName = body.authorName,
                        authorAvatar = body.authorAvatar,
                        type = DynamicType.valueOf(body.type ?: "TEXT"),
                        images = body.images ?: emptyList(),
                        tags = body.tags ?: emptyList()
                    )
                    val dto = database.dynamicDao().getDynamicById(id)?.toDto()
                    WebSocketManager.broadcast("DYNAMIC_CREATED", dto)
                    call.respond(HttpStatusCode.Created, dto ?: mapOf("id" to id))
                }

                put("/api/dynamics/{id}") {
                    val id = call.parameters["id"]
                        ?: return@put call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing id"))
                    val body = call.receive<DynamicUpdateRequest>()
                    dynamicRepository.updateDynamic(id, body.title, body.content, body.images, body.tags)
                    val dto = database.dynamicDao().getDynamicById(id)?.toDto()
                    WebSocketManager.broadcast("DYNAMIC_UPDATED", dto)
                    call.respond(dto ?: mapOf("id" to id))
                }

                delete("/api/dynamics/{id}") {
                    val id = call.parameters["id"]
                        ?: return@delete call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing id"))
                    dynamicRepository.deleteDynamic(id)
                    WebSocketManager.broadcast("DYNAMIC_DELETED", mapOf("id" to id))
                    call.respond(HttpStatusCode.NoContent)
                }

                post("/api/dynamics/{id}/like") {
                    val id = call.parameters["id"]
                        ?: return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing id"))
                    val body = call.receive<LikeRequest>()
                    val liked = dynamicRepository.toggleLike(id, body.userId)
                    val dto = database.dynamicDao().getDynamicById(id)?.toDto()
                    WebSocketManager.broadcast("DYNAMIC_LIKED", dto)
                    call.respond(mapOf("liked" to liked, "dynamic" to dto))
                }

                post("/api/dynamics/{id}/comments") {
                    val id = call.parameters["id"]
                        ?: return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing id"))
                    val body = call.receive<CommentCreateRequest>()
                    val commentId = dynamicRepository.addComment(
                        dynamicId = id,
                        content = body.content,
                        authorId = body.authorId,
                        authorName = body.authorName,
                        authorAvatar = body.authorAvatar,
                        parentCommentId = body.parentCommentId
                    )
                    val comments = database.dynamicDao().getCommentsByDynamicId(id).first()
                    val commentDto = comments.find { it.id == commentId }?.toDto()
                    WebSocketManager.broadcast("COMMENT_CREATED", commentDto)
                    call.respond(HttpStatusCode.Created, commentDto ?: mapOf("id" to commentId))
                }

                delete("/api/dynamics/{id}/comments/{cid}") {
                    val cid = call.parameters["cid"]
                        ?: return@delete call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing comment id"))
                    dynamicRepository.deleteComment(cid)
                    WebSocketManager.broadcast("COMMENT_DELETED", mapOf("id" to cid))
                    call.respond(HttpStatusCode.NoContent)
                }

                // --- 投票 ---
                post("/api/votes") {
                    val body = call.receive<VoteCreateRequest>()
                    val voteId = voteRepository.createVote(
                        title = body.title,
                        description = body.description ?: "",
                        authorId = body.authorId,
                        authorName = body.authorName,
                        authorAvatar = body.authorAvatar,
                        options = body.options,
                        allowMultipleChoice = body.allowMultipleChoice ?: false,
                        isAnonymous = body.isAnonymous ?: false
                    )
                    val entity = database.voteDao().getVoteById(voteId)
                    val options = database.voteDao().getVoteOptionsSync(voteId)
                    val dto = entity?.toDto(options.map { it.toDto(0) })
                    WebSocketManager.broadcast("VOTE_CREATED", dto)
                    call.respond(HttpStatusCode.Created, dto ?: mapOf("id" to voteId))
                }

                post("/api/votes/{id}/vote") {
                    val id = call.parameters["id"]
                        ?: return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing id"))
                    val body = call.receive<CastVoteRequest>()
                    val success = voteRepository.vote(id, body.optionIds, body.userId, body.userName, body.userAvatar)
                    if (!success) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Vote failed"))
                        return@post
                    }
                    val entity = database.voteDao().getVoteById(id)
                    val options = database.voteDao().getVoteOptionsSync(id)
                    val dto = entity?.toDto(options.map { it.toDto(entity.totalVotes) })
                    WebSocketManager.broadcast("VOTE_UPDATED", dto)
                    call.respond(dto ?: mapOf("id" to id))
                }

                put("/api/votes/{id}/end") {
                    val id = call.parameters["id"]
                        ?: return@put call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing id"))
                    voteRepository.endVote(id)
                    WebSocketManager.broadcast("VOTE_ENDED", mapOf("id" to id))
                    call.respond(mapOf("id" to id, "status" to "ENDED"))
                }

                delete("/api/votes/{id}") {
                    val id = call.parameters["id"]
                        ?: return@delete call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing id"))
                    voteRepository.deleteVote(id)
                    WebSocketManager.broadcast("VOTE_DELETED", mapOf("id" to id))
                    call.respond(HttpStatusCode.NoContent)
                }

                // --- 日记 ---
                post("/api/diaries") {
                    val body = call.receive<DiaryCreateRequest>()
                    val diary = com.selves.xnn.model.MemberDiary(
                        id = UUID.randomUUID().toString(),
                        memberId = body.memberId,
                        title = body.title,
                        content = body.content,
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                    memberDiaryRepository.upsertDiary(diary)
                    val dto = database.memberDiaryDao().getDiaryById(diary.id)?.toDto()
                    WebSocketManager.broadcast("DIARY_CREATED", dto)
                    call.respond(HttpStatusCode.Created, dto ?: mapOf("id" to diary.id))
                }

                put("/api/diaries/{id}") {
                    val id = call.parameters["id"]
                        ?: return@put call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing id"))
                    val body = call.receive<DiaryUpdateRequest>()
                    val existing = database.memberDiaryDao().getDiaryById(id)
                        ?: return@put call.respond(HttpStatusCode.NotFound, mapOf("error" to "Diary not found"))
                    val updated = existing.copy(
                        title = body.title ?: existing.title,
                        content = body.content ?: existing.content,
                        updatedAt = System.currentTimeMillis()
                    )
                    database.memberDiaryDao().updateDiary(updated)
                    val dto = updated.toDto()
                    WebSocketManager.broadcast("DIARY_UPDATED", dto)
                    call.respond(dto)
                }

                delete("/api/diaries/{id}") {
                    val id = call.parameters["id"]
                        ?: return@delete call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing id"))
                    database.memberDiaryDao().deleteDiaryById(id)
                    WebSocketManager.broadcast("DIARY_DELETED", mapOf("id" to id))
                    call.respond(HttpStatusCode.NoContent)
                }

                // --- 成员 ---
                post("/api/members") {
                    val body = call.receive<MemberCreateRequest>()
                    val member = com.selves.xnn.model.Member(
                        id = UUID.randomUUID().toString(),
                        name = body.name,
                        avatarUrl = null,
                        bio = body.bio ?: "",
                        pronouns = body.pronouns ?: ""
                    )
                    memberRepository.saveMember(member)
                    val dto = database.memberDao().getMemberById(member.id)?.toDto()
                    WebSocketManager.broadcast("MEMBER_CREATED", dto)
                    call.respond(HttpStatusCode.Created, dto ?: mapOf("id" to member.id))
                }

                put("/api/members/{id}") {
                    val id = call.parameters["id"]
                        ?: return@put call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing id"))
                    val body = call.receive<MemberUpdateRequest>()
                    val existing = database.memberDao().getMemberById(id)
                        ?: return@put call.respond(HttpStatusCode.NotFound, mapOf("error" to "Member not found"))
                    val updated = existing.copy(
                        name = body.name ?: existing.name,
                        bio = body.bio ?: existing.bio,
                        pronouns = body.pronouns ?: existing.pronouns
                    )
                    database.memberDao().updateMember(updated)
                    val dto = updated.toDto()
                    WebSocketManager.broadcast("MEMBER_UPDATED", dto)
                    call.respond(dto)
                }

                delete("/api/members/{id}") {
                    val id = call.parameters["id"]
                        ?: return@delete call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing id"))
                    val existing = database.memberDao().getMemberById(id)
                        ?: return@delete call.respond(HttpStatusCode.NotFound, mapOf("error" to "Member not found"))
                    database.memberDao().updateMember(existing.copy(isDeleted = true))
                    WebSocketManager.broadcast("MEMBER_DELETED", mapOf("id" to id))
                    call.respond(HttpStatusCode.NoContent)
                }

                // --- 群聊 ---
                post("/api/groups") {
                    val body = call.receive<GroupCreateRequest>()
                    val memberIdStr = (body.memberIds ?: emptyList()).joinToString(",")
                    val entity = ChatGroupEntity(
                        id = UUID.randomUUID().toString(),
                        name = body.name,
                        avatarUrl = null,
                        memberIds = memberIdStr,
                        ownerId = body.ownerId,
                        createdAt = System.currentTimeMillis()
                    )
                    database.chatGroupDao().insertGroup(entity)
                    val dto = database.chatGroupDao().getGroupById(entity.id)?.toDto(database.memberDao())
                    WebSocketManager.broadcast("GROUP_CREATED", dto)
                    call.respond(HttpStatusCode.Created, dto ?: mapOf("id" to entity.id))
                }

                put("/api/groups/{id}") {
                    val id = call.parameters["id"]
                        ?: return@put call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing id"))
                    val body = call.receive<GroupUpdateRequest>()
                    val existing = database.chatGroupDao().getGroupById(id)
                        ?: return@put call.respond(HttpStatusCode.NotFound, mapOf("error" to "Group not found"))
                    val updated = existing.copy(
                        name = body.name ?: existing.name
                    )
                    if (body.memberIds != null) {
                        database.chatGroupDao().updateGroup(updated.copy(memberIds = body.memberIds.joinToString(",")))
                    } else {
                        database.chatGroupDao().updateGroup(updated)
                    }
                    val dto = database.chatGroupDao().getGroupById(id)?.toDto(database.memberDao())
                    WebSocketManager.broadcast("GROUP_UPDATED", dto)
                    call.respond(dto ?: mapOf("id" to id))
                }

                delete("/api/groups/{id}") {
                    val id = call.parameters["id"]
                        ?: return@delete call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing id"))
                    chatGroupRepository.deleteGroupById(id)
                    WebSocketManager.broadcast("GROUP_DELETED", mapOf("id" to id))
                    call.respond(HttpStatusCode.NoContent)
                }

                // --- 系统 ---
                put("/api/system") {
                    val body = call.receive<SystemUpdateRequest>()
                    val existing = database.systemDao().getCurrentSystem().first()
                        ?: return@put call.respond(HttpStatusCode.NotFound, mapOf("error" to "No system found"))
                    val updated = existing.copy(
                        name = body.name ?: existing.name,
                        description = body.description ?: existing.description
                    )
                    database.systemDao().updateSystem(updated)
                    val dto = updated.toDto()
                    WebSocketManager.broadcast("SYSTEM_UPDATED", dto)
                    call.respond(dto)
                }

                // --- 待办 ---
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
                    val dto = database.todoDao().getTodoById(todo.id)?.toDto()
                    WebSocketManager.broadcast("TODO_CREATED", dto ?: todo)
                    call.respond(HttpStatusCode.Created, dto ?: mapOf("id" to todo.id))
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
                    val dto = database.todoDao().getTodoById(id)?.toDto()
                    WebSocketManager.broadcast("TODO_UPDATED", dto ?: updated)
                    call.respond(dto ?: mapOf("id" to id))
                }

                delete("/api/todos/{id}") {
                    val id = call.parameters["id"]
                        ?: return@delete call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing id"))
                    todoRepository.deleteTodoById(id)
                    WebSocketManager.broadcast("TODO_DELETED", mapOf("id" to id))
                    call.respond(HttpStatusCode.NoContent)
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

            get("{...}") {
                if (call.respondWebAssetOrIndex()) return@get
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "Web page not found"))
            }
        }
    }

    private suspend fun ApplicationCall.respondWebIndex(): Boolean {
        return respondWebAsset("index.html")
    }

    private suspend fun ApplicationCall.respondWebAssetOrIndex(): Boolean {
        val requestPath = request.path().trimStart('/')
        if (requestPath.isBlank() || requestPath.startsWith("api/") || requestPath == "ws") {
            return false
        }
        if (respondWebAsset(requestPath)) {
            return true
        }
        if (!requestPath.contains('.')) {
            return respondWebIndex()
        }
        return false
    }

    private suspend fun ApplicationCall.respondWebAsset(assetPath: String): Boolean {
        if (assetPath.contains("..")) return false
        val resolvedPath = "$WEB_ASSET_ROOT/$assetPath"
        return try {
            val bytes = assets.open(resolvedPath).use { input ->
                input.readBytes()
            }
            respondBytes(bytes, contentTypeFor(assetPath))
            true
        } catch (_: IOException) {
            false
        }
    }

    private fun contentTypeFor(assetPath: String): io.ktor.http.ContentType = when (assetPath.substringAfterLast('.', "").lowercase()) {
        "html" -> io.ktor.http.ContentType.Text.Html
        "js" -> io.ktor.http.ContentType.Application.JavaScript
        "css" -> io.ktor.http.ContentType.Text.CSS
        "json" -> io.ktor.http.ContentType.Application.Json
        "svg" -> io.ktor.http.ContentType.parse("image/svg+xml")
        "png" -> io.ktor.http.ContentType.Image.PNG
        "jpg", "jpeg" -> io.ktor.http.ContentType.Image.JPEG
        "ico" -> io.ktor.http.ContentType.parse("image/x-icon")
        "ttf" -> io.ktor.http.ContentType.parse("font/ttf")
        "otf" -> io.ktor.http.ContentType.parse("font/otf")
        "woff" -> io.ktor.http.ContentType.parse("font/woff")
        "woff2" -> io.ktor.http.ContentType.parse("font/woff2")
        else -> io.ktor.http.ContentType.Application.OctetStream
    }

    private suspend fun ApplicationCall.respondApiLandingPage() {
        respondText(
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

// ===== 消息 =====
data class MessageCreateRequest(
    val senderId: String,
    val content: String,
    val type: String = "TEXT"
)

// ===== 动态 =====
data class DynamicCreateRequest(
    val title: String,
    val content: String,
    val authorId: String,
    val authorName: String,
    val authorAvatar: String? = null,
    val type: String? = "TEXT",
    val images: List<String>? = null,
    val tags: List<String>? = null
)

data class DynamicUpdateRequest(
    val title: String? = null,
    val content: String? = null,
    val images: List<String>? = null,
    val tags: List<String>? = null
)

data class LikeRequest(val userId: String)

data class CommentCreateRequest(
    val content: String,
    val authorId: String,
    val authorName: String,
    val authorAvatar: String? = null,
    val parentCommentId: String? = null
)

// ===== 投票 =====
data class VoteCreateRequest(
    val title: String,
    val description: String? = null,
    val authorId: String,
    val authorName: String,
    val authorAvatar: String? = null,
    val options: List<String>,
    val allowMultipleChoice: Boolean? = false,
    val isAnonymous: Boolean? = false
)

data class CastVoteRequest(
    val userId: String,
    val userName: String,
    val userAvatar: String? = null,
    val optionIds: List<String>
)

// ===== 日记 =====
data class DiaryCreateRequest(
    val memberId: String,
    val title: String,
    val content: String
)

data class DiaryUpdateRequest(
    val title: String? = null,
    val content: String? = null
)

// ===== 成员 =====
data class MemberCreateRequest(
    val name: String,
    val bio: String? = null,
    val pronouns: String? = null
)

data class MemberUpdateRequest(
    val name: String? = null,
    val bio: String? = null,
    val pronouns: String? = null
)

// ===== 群聊 =====
data class GroupCreateRequest(
    val name: String,
    val ownerId: String,
    val memberIds: List<String>? = null
)

data class GroupUpdateRequest(
    val name: String? = null,
    val memberIds: List<String>? = null
)

// ===== 系统 =====
data class SystemUpdateRequest(
    val name: String? = null,
    val description: String? = null
)
