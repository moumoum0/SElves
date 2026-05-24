package com.selves.xnn.data

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.selves.xnn.data.entity.ChatGroupEntity
import com.selves.xnn.data.entity.MemberDiaryEntity
import com.selves.xnn.data.entity.MemberEntity
import com.selves.xnn.data.entity.MemberGroupEntity
import com.selves.xnn.data.entity.MessageEntity
import com.selves.xnn.data.entity.OnlineStatusEntity
import com.selves.xnn.data.entity.SystemEntity
import com.selves.xnn.data.entity.VoteEntity
import com.selves.xnn.data.entity.VoteOptionEntity
import com.selves.xnn.data.entity.VoteRecordEntity
import com.selves.xnn.model.VoteStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

enum class ImportMode {
    OVERWRITE,
    MERGE
}

sealed class SpImportResult {
    data class Success(val memberCount: Int, val groupCount: Int, val messageCount: Int, val diaryCount: Int = 0) : SpImportResult()
    data class Error(val message: String) : SpImportResult()
}

@Singleton
class SimplyPluralImportService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: AppDatabase
) {
    private val TAG = "SpImportService"
    private val gson = Gson()

    suspend fun importFromUri(
        uri: Uri,
        mode: ImportMode,
        onProgress: suspend (Float, String) -> Unit = { _, _ -> }
    ): SpImportResult = withContext(Dispatchers.IO) {
        try {
            onProgress(0.05f, "读取文件…")
            Log.i(TAG, "[1/8] 读取文件: $uri")
            val jsonBytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: return@withContext SpImportResult.Error("无法打开文件")
            val jsonString = jsonBytes.toString(Charsets.UTF_8)
            Log.d(TAG, "文件大小: ${jsonBytes.size / 1024} KB")

            onProgress(0.10f, "解析 JSON…")
            Log.i(TAG, "[2/8] 解析 JSON")
            val root = try {
                JsonParser.parseString(jsonString).asJsonObject
            } catch (e: Exception) {
                Log.e(TAG, "JSON 解析失败", e)
                return@withContext SpImportResult.Error("JSON 解析失败: ${e.message}")
            }
            val topKeys = root.keySet().toList()
            Log.d(TAG, "JSON 顶层字段: $topKeys")
            Log.d(TAG, "  users: ${root.getAsJsonArray("users")?.size() ?: 0} 条")
            Log.d(TAG, "  members: ${root.getAsJsonArray("members")?.size() ?: 0} 条")
            Log.d(TAG, "  groups: ${root.getAsJsonArray("groups")?.size() ?: 0} 条")
            Log.d(TAG, "  frontHistory: ${root.getAsJsonArray("frontHistory")?.size() ?: 0} 条")
            Log.d(TAG, "  channels: ${root.getAsJsonArray("channels")?.size() ?: 0} 条")
            Log.d(TAG, "  chatMessages: ${root.getAsJsonArray("chatMessages")?.size() ?: 0} 条")
            Log.d(TAG, "  polls: ${root.getAsJsonArray("polls")?.size() ?: 0} 条")
            Log.d(TAG, "  notes: ${root.getAsJsonArray("notes")?.size() ?: 0} 条")

            if (mode == ImportMode.OVERWRITE) {
                onProgress(0.15f, "清除现有数据…")
                Log.i(TAG, "[OVERWRITE] 清除现有数据")
                clearAll()
                Log.d(TAG, "[OVERWRITE] 清除完成")
            } else {
                Log.i(TAG, "[MERGE] 合并模式，保留现有数据")
            }

            onProgress(0.20f, "导入系统信息…")
            Log.i(TAG, "[3/8] 导入系统信息")
            val systemId = importSystem(root)
            Log.d(TAG, "系统 ID: $systemId")

            onProgress(0.30f, "导入成员…")
            Log.i(TAG, "[4/8] 导入成员")
            val memberIdMap = importMembers(root)
            val memberCount = memberIdMap.size
            Log.i(TAG, "成员导入完成: $memberCount 个")

            onProgress(0.38f, "导入日记…")
            val diaryCount = importNotes(root, memberIdMap)
            Log.i(TAG, "日记导入完成: $diaryCount 条")

            onProgress(0.45f, "导入分组…")
            Log.i(TAG, "[5/8] 导入分组")
            val groupCount = importGroups(root, memberIdMap)
            Log.i(TAG, "分组导入完成: $groupCount 个")

            onProgress(0.55f, "导入 Front 历史…")
            Log.i(TAG, "[6/8] 导入 Front 历史")
            val frontCount = importFrontHistory(root, memberIdMap)
            Log.i(TAG, "Front 历史导入完成: $frontCount 条")

            onProgress(0.65f, "导入聊天频道…")
            Log.i(TAG, "[7/8] 导入聊天频道 & 消息")
            val channelCount = importChannels(root, memberIdMap, systemId)
            Log.d(TAG, "频道导入: $channelCount 个")

            onProgress(0.75f, "导入聊天消息…")
            val messageCount = importChatMessages(root, memberIdMap)
            Log.d(TAG, "消息导入: $messageCount 条")

            onProgress(0.88f, "导入投票…")
            Log.i(TAG, "[8/8] 导入投票")
            val pollCount = importPolls(root, memberIdMap)
            Log.i(TAG, "投票导入完成: $pollCount 个")

            onProgress(1.0f, "导入完成")
            Log.i(TAG, "======= 导入完成 =======")
            Log.i(TAG, "  成员: $memberCount  日记: $diaryCount  分组: $groupCount  Front历史: $frontCount")
            Log.i(TAG, "  频道: $channelCount  消息: $messageCount  投票: $pollCount")
            SpImportResult.Success(memberCount, groupCount, messageCount, diaryCount)
        } catch (e: Exception) {
            Log.e(TAG, "导入失败: ${e.message}", e)
            SpImportResult.Error(e.message ?: "未知错误")
        }
    }

    // ──────────────────────────────────────────────────────
    // 清除
    // ──────────────────────────────────────────────────────

    private suspend fun clearAll() {
        Log.d(TAG, "  删除 voteRecords…")
        database.voteDao().deleteAllVoteRecords()
        Log.d(TAG, "  删除 voteOptions…")
        database.voteDao().deleteAllVoteOptions()
        Log.d(TAG, "  删除 votes…")
        database.voteDao().deleteAllVotes()
        Log.d(TAG, "  删除 messages…")
        database.messageDao().deleteAll()
        Log.d(TAG, "  删除 chatGroups…")
        database.chatGroupDao().deleteAll()
        Log.d(TAG, "  删除 onlineStatus…")
        database.onlineStatusDao().deleteAll()
        Log.d(TAG, "  删除 memberDiaries…")
        database.memberDiaryDao().deleteAll()
        Log.d(TAG, "  删除 memberGroups…")
        database.memberGroupDao().deleteAll()
        Log.d(TAG, "  删除 members…")
        database.memberDao().deleteAll()
        Log.d(TAG, "  删除 systems…")
        database.systemDao().deleteAll()
    }

    // ──────────────────────────────────────────────────────
    // 系统
    // ──────────────────────────────────────────────────────

    private suspend fun importSystem(root: JsonObject): String {
        val usersArr = root.getAsJsonArray("users") ?: run {
            Log.w(TAG, "  JSON 中无 users 字段，跳过系统导入")
            return ""
        }
        if (usersArr.size() == 0) {
            Log.w(TAG, "  users 数组为空，跳过系统导入")
            return ""
        }
        val user = usersArr[0].asJsonObject
        val id = user.getString("_id") ?: user.getString("id") ?: UUID.randomUUID().toString()
        val name = user.getString("username") ?: user.getString("name") ?: "Imported System"
        val desc = user.getString("desc") ?: ""
        Log.d(TAG, "  系统: name=$name  id=$id  desc长度=${desc.length}")
        val entity = SystemEntity(
            id = id,
            name = name,
            description = desc,
            avatarUrl = null
        )
        database.systemDao().insertSystem(entity)
        return id
    }

    // ──────────────────────────────────────────────────────
    // 成员
    // ──────────────────────────────────────────────────────

    private suspend fun importMembers(root: JsonObject): Map<String, String> {
        val arr = root.getAsJsonArray("members") ?: run {
            Log.w(TAG, "  JSON 中无 members 字段")
            return emptyMap()
        }
        val idMap = mutableMapOf<String, String>()
        var skippedArchived = 0
        var skippedNoId = 0
        var skippedNoName = 0

        Log.d(TAG, "  原始成员数: ${arr.size()}")
        for (elem in arr) {
            val obj = elem.asJsonObject
            val (id, data) = if (obj.has("content")) {
                (obj.getString("id") ?: obj.getString("_id") ?: run { skippedNoId++; null }) to obj.getAsJsonObject("content")
            } else {
                (obj.getString("_id") ?: obj.getString("id") ?: run { skippedNoId++; null }) to obj
            }
            if (id == null) continue
            if (data.getBool("archived") == true) { skippedArchived++; Log.d(TAG, "  跳过已归档成员 id=$id"); continue }
            val name = data.getString("name") ?: run { skippedNoName++; null }
            if (name == null) continue
            val bio = data.getString("desc") ?: ""
            val pronouns = data.getString("pronouns") ?: ""
            Log.d(TAG, "  导入成员: $name (id=$id, pronouns=$pronouns, bio长度=${bio.length})")
            val entity = MemberEntity(
                id = id,
                name = name,
                avatarUrl = null,
                bio = bio,
                pronouns = pronouns,
                groups = emptyList()
            )
            database.memberDao().insertMember(entity)
            idMap[id] = name
        }
        if (skippedArchived > 0) Log.d(TAG, "  跳过已归档: $skippedArchived 个")
        if (skippedNoId > 0)     Log.w(TAG, "  跳过无ID: $skippedNoId 个")
        if (skippedNoName > 0)   Log.w(TAG, "  跳过无名称: $skippedNoName 个")
        return idMap
    }

    private suspend fun importNotes(root: JsonObject, memberIdMap: Map<String, String>): Int {
        val arr = root.getAsJsonArray("notes") ?: run {
            Log.d(TAG, "  JSON 中无 notes 字段，跳过")
            return 0
        }
        Log.d(TAG, "  原始日记数: ${arr.size()}")
        var count = 0
        var skippedUnknownMember = 0
        for (elem in arr) {
            if (!elem.isJsonObject) continue
            val obj = elem.asJsonObject
            val memberId = obj.getString("member") ?: continue
            if (memberId !in memberIdMap) { skippedUnknownMember++; continue }
            val id = obj.getString("_id") ?: obj.getString("id") ?: UUID.randomUUID().toString()
            val title = obj.getString("title") ?: ""
            val content = obj.getString("note") ?: obj.getString("content") ?: ""
            val createdAt = obj.getLong("date") ?: obj.getLong("createdAt") ?: obj.getLong("lastOperationTime") ?: System.currentTimeMillis()
            val updatedAt = obj.getLong("lastOperationTime") ?: obj.getLong("updatedAt") ?: createdAt
            database.memberDiaryDao().upsertDiary(
                MemberDiaryEntity(
                    id = id,
                    memberId = memberId,
                    title = title,
                    content = content,
                    createdAt = createdAt,
                    updatedAt = updatedAt
                )
            )
            count++
        }
        if (skippedUnknownMember > 0) Log.w(TAG, "  跳过未知成员日记: $skippedUnknownMember 条")
        return count
    }

    // ──────────────────────────────────────────────────────
    // 分组
    // ──────────────────────────────────────────────────────

    private suspend fun importGroups(root: JsonObject, memberIdMap: Map<String, String>): Int {
        val arr = root.getAsJsonArray("groups") ?: run {
            Log.d(TAG, "  JSON 中无 groups 字段，跳过")
            return 0
        }
        if (arr.size() == 0) { Log.d(TAG, "  groups 为空，跳过"); return 0 }
        Log.d(TAG, "  原始分组数: ${arr.size()}")

        // 第一遍：建立 _id → name 映射表
        val groupIdToName = mutableMapOf<String, String>()
        for (elem in arr) {
            val obj = elem.asJsonObject
            val id = obj.getString("_id") ?: continue
            val name = obj.getString("name") ?: continue
            groupIdToName[id] = name
        }

        // 建立成员 ID → 分组名列表 映射（反转）
        val memberGroupsMap = mutableMapOf<String, MutableList<String>>()
        for (elem in arr) {
            val obj = elem.asJsonObject
            val groupName = obj.getString("name") ?: continue
            val members = obj.getAsJsonArray("members") ?: continue
            for (mElem in members) {
                val memberId = mElem.asString ?: continue
                if (memberId in memberIdMap) {
                    memberGroupsMap.getOrPut(memberId) { mutableListOf() }.add(groupName)
                }
            }
        }

        // 写入分组实体
        var count = 0
        for (elem in arr) {
            val obj = elem.asJsonObject
            val name = obj.getString("name") ?: continue
            val desc = obj.getString("desc") ?: ""
            val parentRaw = obj.getString("parent")
            val parentName = if (parentRaw == null || parentRaw == "root") null
            else groupIdToName[parentRaw]
            Log.d(TAG, "  导入分组: $name (parent=$parentName)")
            database.memberGroupDao().upsertGroup(
                MemberGroupEntity(name = name, description = desc, parentName = parentName)
            )
            count++
        }

        // 更新成员的分组字段
        Log.d(TAG, "  更新成员分组归属: ${memberGroupsMap.size} 个成员")
        for ((memberId, groupNames) in memberGroupsMap) {
            val existing = database.memberDao().getMemberById(memberId) ?: continue
            Log.d(TAG, "  成员 ${memberIdMap[memberId]} 归属分组: $groupNames")
            database.memberDao().insertMember(existing.copy(groups = groupNames))
        }

        return count
    }

    // ──────────────────────────────────────────────────────
    // Front 历史
    // ──────────────────────────────────────────────────────

    private suspend fun importFrontHistory(root: JsonObject, memberIdMap: Map<String, String>): Int {
        val arr = root.getAsJsonArray("frontHistory") ?: run {
            Log.d(TAG, "  JSON 中无 frontHistory 字段，跳过")
            return 0
        }
        Log.d(TAG, "  原始 front 记录数: ${arr.size()}")
        var count = 0
        var skippedCustom = 0
        var skippedUnknownMember = 0
        for (elem in arr) {
            val obj = elem.asJsonObject
            if (obj.getBool("custom") == true) { skippedCustom++; continue }
            val memberId = obj.getString("member") ?: continue
            if (memberId !in memberIdMap) { skippedUnknownMember++; continue }
            val startTime = obj.getLong("startTime") ?: continue
            val live = obj.getBool("live") ?: false
            val endTime = if (live) null else obj.getLong("endTime")
            val duration = if (endTime != null) endTime - startTime else 0L
            database.onlineStatusDao().insertOnlineStatus(
                OnlineStatusEntity(
                    id = 0,
                    memberId = memberId,
                    loginTime = startTime,
                    logoutTime = endTime,
                    duration = duration
                )
            )
            count++
        }
        if (skippedCustom > 0)        Log.d(TAG, "  跳过 custom=true 记录: $skippedCustom 条")
        if (skippedUnknownMember > 0) Log.w(TAG, "  跳过未知成员记录: $skippedUnknownMember 条")
        return count
    }

    // ──────────────────────────────────────────────────────
    // 聊天频道 & 消息
    // ──────────────────────────────────────────────────────

    private suspend fun importChannels(root: JsonObject, memberIdMap: Map<String, String>, systemId: String): Int {
        val arr = root.getAsJsonArray("channels") ?: run {
            Log.d(TAG, "  JSON 中无 channels 字段，跳过")
            return 0
        }
        Log.d(TAG, "  原始频道数: ${arr.size()}")
        val allMemberIds = memberIdMap.keys.joinToString(",")
        val ownerId = memberIdMap.keys.firstOrNull() ?: systemId
        var count = 0
        for (elem in arr) {
            val obj = elem.asJsonObject
            val id = obj.getString("_id") ?: continue
            val name = obj.getString("name") ?: continue
            val createdAt = obj.getLong("lastOperationTime") ?: System.currentTimeMillis()
            Log.d(TAG, "  导入频道: $name (id=$id)")
            database.chatGroupDao().insertGroup(
                ChatGroupEntity(
                    id = id,
                    name = name,
                    avatarUrl = null,
                    memberIds = allMemberIds,
                    ownerId = ownerId,
                    createdAt = createdAt
                )
            )
            count++
        }
        return count
    }

    private suspend fun importChatMessages(root: JsonObject, memberIdMap: Map<String, String>): Int {
        val arr = root.getAsJsonArray("chatMessages") ?: run {
            Log.d(TAG, "  JSON 中无 chatMessages 字段，跳过")
            return 0
        }
        Log.d(TAG, "  原始消息数: ${arr.size()}")
        var count = 0
        var skippedUnknownWriter = 0
        for (elem in arr) {
            val obj = elem.asJsonObject
            val id = obj.getString("_id") ?: continue
            val writer = obj.getString("writer") ?: continue
            if (writer !in memberIdMap) { skippedUnknownWriter++; continue }
            val channelId = obj.getString("channel") ?: continue
            val message = obj.getString("message") ?: ""
            val writtenAt = obj.getLong("writtenAt") ?: System.currentTimeMillis()
            database.messageDao().insertMessage(
                MessageEntity(
                    id = id,
                    groupId = channelId,
                    senderId = writer,
                    content = message,
                    timestamp = writtenAt,
                    type = 0
                )
            )
            count++
        }
        if (skippedUnknownWriter > 0) Log.w(TAG, "  跳过未知发送者消息: $skippedUnknownWriter 条")
        return count
    }

    // ──────────────────────────────────────────────────────
    // 投票
    // ──────────────────────────────────────────────────────

    private suspend fun importPolls(root: JsonObject, memberIdMap: Map<String, String>): Int {
        val arr = root.getAsJsonArray("polls") ?: run {
            Log.d(TAG, "  JSON 中无 polls 字段，跳过")
            return 0
        }
        Log.d(TAG, "  原始投票数: ${arr.size()}")
        val now = LocalDateTime.now()
        var pollCount = 0
        for (elem in arr) {
            val obj = if (elem.isJsonObject) elem.asJsonObject else continue
            val voteId = obj.getString("_id") ?: continue
            val title = obj.getString("name") ?: continue
            val desc = obj.getString("desc") ?: ""
            val endTimeMs = obj.getLong("endTime")
            val endTimeLocal = endTimeMs?.toLocalDateTime()

            val votes = obj.getAsJsonArray("votes")
            val firstVoterId = votes?.firstOrNull { it.isJsonObject }?.asJsonObject?.getString("id")
            val authorId = if (firstVoterId != null && firstVoterId in memberIdMap) firstVoterId
            else memberIdMap.keys.firstOrNull()
            if (authorId == null) {
                Log.w(TAG, "  无法确定投票作者，跳过投票: $title")
                continue
            }

            val authorName = memberIdMap[authorId] ?: ""
            val totalVotes = votes?.size() ?: 0
            val status = if (endTimeLocal != null && endTimeLocal.isBefore(now)) VoteStatus.ENDED else VoteStatus.ACTIVE
            val optionCount = obj.getAsJsonArray("options")?.size() ?: 0
            Log.d(TAG, "  导入投票: \"$title\" (id=$voteId, 选项=$optionCount, 票数=$totalVotes, 状态=$status)")

            database.voteDao().insertVote(
                VoteEntity(
                    id = voteId,
                    title = title,
                    description = desc,
                    authorId = authorId,
                    authorName = authorName,
                    authorAvatar = null,
                    createdAt = now,
                    endTime = endTimeLocal,
                    status = status,
                    totalVotes = totalVotes
                )
            )

            pollCount++

            // 选项
            val options = obj.getAsJsonArray("options") ?: continue
            val optionIdMap = mutableMapOf<String, String>()
            for ((idx, optElem) in options.withIndex()) {
                if (!optElem.isJsonObject) continue
                val optObj = optElem.asJsonObject
                val optName = optObj.getString("name") ?: continue
                val optId = UUID.randomUUID().toString()
                optionIdMap[optName] = optId
                Log.d(TAG, "    选项[$idx]: \"$optName\" (id=$optId)")
                database.voteDao().insertVoteOption(
                    VoteOptionEntity(
                        id = optId,
                        voteId = voteId,
                        content = optName,
                        voteCount = 0,
                        orderIndex = idx
                    )
                )
            }

            // 投票记录
            if (votes == null) continue
            var recordCount = 0
            var skippedVoteRecord = 0
            for (vElem in votes) {
                if (!vElem.isJsonObject) continue
                val vObj = vElem.asJsonObject
                val userId = vObj.getString("id") ?: continue
                if (userId !in memberIdMap) { skippedVoteRecord++; continue }
                val votedOption = vObj.getString("vote") ?: continue
                val optionId = optionIdMap[votedOption]
                if (optionId == null) {
                    Log.w(TAG, "    投票记录选项未找到: vote=\"$votedOption\"")
                    continue
                }
                Log.d(TAG, "    投票记录: ${memberIdMap[userId]} → \"$votedOption\"")
                database.voteDao().insertVoteRecord(
                    VoteRecordEntity(
                        id = UUID.randomUUID().toString(),
                        voteId = voteId,
                        optionId = optionId,
                        userId = userId,
                        userName = memberIdMap[userId] ?: "",
                        userAvatar = null,
                        votedAt = now
                    )
                )
                database.voteDao().updateVoteOptionCount(
                    optionId,
                    database.voteDao().getOptionVoteCount(optionId)
                )
                recordCount++
            }
            if (skippedVoteRecord > 0) Log.w(TAG, "    跳过未知成员投票记录: $skippedVoteRecord 条")
            Log.d(TAG, "    投票记录写入: $recordCount 条")
        }
        return pollCount
    }

    // ──────────────────────────────────────────────────────
    // 扩展工具
    // ──────────────────────────────────────────────────────

    private fun JsonObject.getString(key: String): String? =
        if (has(key) && !get(key).isJsonNull) get(key).asString else null

    private fun JsonObject.getBool(key: String): Boolean? =
        if (has(key) && !get(key).isJsonNull) get(key).asBoolean else null

    private fun JsonObject.getLong(key: String): Long? =
        if (has(key) && !get(key).isJsonNull) get(key).asLong else null

    private fun Long.toLocalDateTime(): LocalDateTime =
        Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDateTime()
}
