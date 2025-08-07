package com.selves.xnn.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.selves.xnn.data.dao.ChatGroupDao
import com.selves.xnn.data.dao.DynamicDao
import com.selves.xnn.data.dao.MemberDao
import com.selves.xnn.data.dao.MessageDao
import com.selves.xnn.data.dao.MessageReadStatusDao
import com.selves.xnn.data.dao.TodoDao
import com.selves.xnn.data.dao.VoteDao
import com.selves.xnn.data.dao.SystemDao
import com.selves.xnn.data.dao.OnlineStatusDao
import com.selves.xnn.data.entity.ChatGroupEntity
import com.selves.xnn.data.entity.DynamicEntity
import com.selves.xnn.data.entity.DynamicCommentEntity
import com.selves.xnn.data.entity.DynamicLikeEntity
import com.selves.xnn.data.entity.MemberEntity
import com.selves.xnn.data.entity.MessageEntity
import com.selves.xnn.data.entity.MessageReadStatusEntity
import com.selves.xnn.data.entity.TodoEntity
import com.selves.xnn.data.entity.VoteEntity
import com.selves.xnn.data.entity.VoteOptionEntity
import com.selves.xnn.data.entity.VoteRecordEntity
import com.selves.xnn.data.entity.SystemEntity
import com.selves.xnn.data.entity.OnlineStatusEntity
import android.util.Log

@Database(
    entities = [
        MemberEntity::class,
        ChatGroupEntity::class,
        MessageEntity::class,
        MessageReadStatusEntity::class,
        TodoEntity::class,
        DynamicEntity::class,
        DynamicCommentEntity::class,
        DynamicLikeEntity::class,
        VoteEntity::class,
        VoteOptionEntity::class,
        VoteRecordEntity::class,
        SystemEntity::class,
        OnlineStatusEntity::class
    ],
    version = 11,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun memberDao(): MemberDao
    abstract fun chatGroupDao(): ChatGroupDao
    abstract fun messageDao(): MessageDao
    abstract fun messageReadStatusDao(): MessageReadStatusDao
    abstract fun todoDao(): TodoDao
    abstract fun dynamicDao(): DynamicDao
    abstract fun voteDao(): VoteDao
    abstract fun systemDao(): SystemDao
    abstract fun onlineStatusDao(): OnlineStatusDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                try {
                    database.execSQL(
                        "CREATE TABLE IF NOT EXISTS messages_new (" +
                                "id TEXT PRIMARY KEY NOT NULL, " +
                                "groupId TEXT NOT NULL, " +
                                "senderId TEXT NOT NULL, " +
                                "content TEXT NOT NULL, " +
                                "timestamp INTEGER NOT NULL, " +
                                "type INTEGER NOT NULL DEFAULT 0, " +
                                "FOREIGN KEY (senderId) REFERENCES members(id) ON DELETE NO ACTION)"
                    )

                    database.execSQL(
                        "INSERT INTO messages_new (id, groupId, senderId, content, timestamp, type) " +
                                "SELECT id, groupId, senderId, content, timestamp, type FROM messages"
                    )

                    database.execSQL("DROP TABLE messages")

                    database.execSQL("ALTER TABLE messages_new RENAME TO messages")

                    database.execSQL("CREATE INDEX IF NOT EXISTS index_messages_groupId ON messages(groupId)")
                    database.execSQL("CREATE INDEX IF NOT EXISTS index_messages_senderId ON messages(senderId)")

                    Log.d("AppDatabase", "数据库迁移 1->2 完成：消息表外键关系已更新")
                } catch (e: Exception) {
                    Log.e("AppDatabase", "数据库迁移失败: ${e.message}", e)
                }
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                try {
                    // 创建消息已读状态表
                    database.execSQL(
                        "CREATE TABLE IF NOT EXISTS message_read_status (" +
                                "id TEXT PRIMARY KEY NOT NULL, " +
                                "messageId TEXT NOT NULL, " +
                                "memberId TEXT NOT NULL, " +
                                "readAt INTEGER NOT NULL, " +
                                "FOREIGN KEY (messageId) REFERENCES messages(id) ON DELETE CASCADE, " +
                                "FOREIGN KEY (memberId) REFERENCES members(id) ON DELETE CASCADE)"
                    )

                    // 创建索引
                    database.execSQL("CREATE INDEX IF NOT EXISTS index_message_read_status_messageId ON message_read_status(messageId)")
                    database.execSQL("CREATE INDEX IF NOT EXISTS index_message_read_status_memberId ON message_read_status(memberId)")
                    database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_message_read_status_messageId_memberId ON message_read_status(messageId, memberId)")

                    Log.d("AppDatabase", "数据库迁移 2->3 完成：消息已读状态表已创建")
                } catch (e: Exception) {
                    Log.e("AppDatabase", "数据库迁移失败: ${e.message}", e)
                }
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                try {
                    // 添加imagePath字段到messages表
                    database.execSQL("ALTER TABLE messages ADD COLUMN imagePath TEXT")
                    
                    Log.d("AppDatabase", "数据库迁移 3->4 完成：消息表已添加imagePath字段")
                } catch (e: Exception) {
                    Log.e("AppDatabase", "数据库迁移失败: ${e.message}", e)
                }
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                try {
                    // 创建待办事项表
                    database.execSQL(
                        "CREATE TABLE IF NOT EXISTS todos (" +
                                "id TEXT PRIMARY KEY NOT NULL, " +
                                "title TEXT NOT NULL, " +
                                "description TEXT NOT NULL DEFAULT '', " +
                                "isCompleted INTEGER NOT NULL DEFAULT 0, " +
                                "createdAt INTEGER NOT NULL, " +
                                "completedAt INTEGER, " +
                                "priority INTEGER NOT NULL DEFAULT 1, " +
                                "createdBy TEXT NOT NULL, " +
                                "FOREIGN KEY (createdBy) REFERENCES members(id) ON DELETE CASCADE)"
                    )

                    // 创建索引
                    database.execSQL("CREATE INDEX IF NOT EXISTS index_todos_createdBy ON todos(createdBy)")

                    Log.d("AppDatabase", "数据库迁移 4->5 完成：待办事项表已创建")
                } catch (e: Exception) {
                    Log.e("AppDatabase", "数据库迁移失败: ${e.message}", e)
                }
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                try {
                    // 创建动态表
                    database.execSQL(
                        "CREATE TABLE IF NOT EXISTS dynamics (" +
                                "id TEXT PRIMARY KEY NOT NULL, " +
                                "title TEXT NOT NULL, " +
                                "content TEXT NOT NULL, " +
                                "authorId TEXT NOT NULL, " +
                                "authorName TEXT NOT NULL, " +
                                "authorAvatar TEXT, " +
                                "createdAt INTEGER NOT NULL, " +
                                "updatedAt INTEGER NOT NULL, " +
                                "type TEXT NOT NULL, " +
                                "images TEXT NOT NULL DEFAULT '', " +
                                "likeCount INTEGER NOT NULL DEFAULT 0, " +
                                "commentCount INTEGER NOT NULL DEFAULT 0, " +
                                "tags TEXT NOT NULL DEFAULT '', " +
                                "FOREIGN KEY (authorId) REFERENCES members(id) ON DELETE CASCADE)"
                    )

                    // 创建动态评论表
                    database.execSQL(
                        "CREATE TABLE IF NOT EXISTS dynamic_comments (" +
                                "id TEXT PRIMARY KEY NOT NULL, " +
                                "dynamicId TEXT NOT NULL, " +
                                "content TEXT NOT NULL, " +
                                "authorId TEXT NOT NULL, " +
                                "authorName TEXT NOT NULL, " +
                                "authorAvatar TEXT, " +
                                "createdAt INTEGER NOT NULL, " +
                                "parentCommentId TEXT, " +
                                "FOREIGN KEY (dynamicId) REFERENCES dynamics(id) ON DELETE CASCADE, " +
                                "FOREIGN KEY (authorId) REFERENCES members(id) ON DELETE CASCADE)"
                    )

                    // 创建动态点赞表
                    database.execSQL(
                        "CREATE TABLE IF NOT EXISTS dynamic_likes (" +
                                "id TEXT PRIMARY KEY NOT NULL, " +
                                "dynamicId TEXT NOT NULL, " +
                                "userId TEXT NOT NULL, " +
                                "createdAt INTEGER NOT NULL, " +
                                "FOREIGN KEY (dynamicId) REFERENCES dynamics(id) ON DELETE CASCADE, " +
                                "FOREIGN KEY (userId) REFERENCES members(id) ON DELETE CASCADE)"
                    )

                    // 创建索引
                    database.execSQL("CREATE INDEX IF NOT EXISTS index_dynamics_authorId ON dynamics(authorId)")
                    database.execSQL("CREATE INDEX IF NOT EXISTS index_dynamics_type ON dynamics(type)")
                    database.execSQL("CREATE INDEX IF NOT EXISTS index_dynamics_createdAt ON dynamics(createdAt)")
                    
                    database.execSQL("CREATE INDEX IF NOT EXISTS index_dynamic_comments_dynamicId ON dynamic_comments(dynamicId)")
                    database.execSQL("CREATE INDEX IF NOT EXISTS index_dynamic_comments_authorId ON dynamic_comments(authorId)")
                    database.execSQL("CREATE INDEX IF NOT EXISTS index_dynamic_comments_parentCommentId ON dynamic_comments(parentCommentId)")
                    
                    database.execSQL("CREATE INDEX IF NOT EXISTS index_dynamic_likes_dynamicId ON dynamic_likes(dynamicId)")
                    database.execSQL("CREATE INDEX IF NOT EXISTS index_dynamic_likes_userId ON dynamic_likes(userId)")
                    database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_dynamic_likes_dynamicId_userId ON dynamic_likes(dynamicId, userId)")

                    Log.d("AppDatabase", "数据库迁移 5->6 完成：动态相关表已创建")
                } catch (e: Exception) {
                    Log.e("AppDatabase", "数据库迁移失败: ${e.message}", e)
                }
            }
        }

        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                try {
                    // 创建投票表
                    database.execSQL(
                        "CREATE TABLE IF NOT EXISTS votes (" +
                                "id TEXT PRIMARY KEY NOT NULL, " +
                                "title TEXT NOT NULL, " +
                                "description TEXT NOT NULL, " +
                                "authorId TEXT NOT NULL, " +
                                "authorName TEXT NOT NULL, " +
                                "authorAvatar TEXT, " +
                                "createdAt INTEGER NOT NULL, " +
                                "endTime INTEGER, " +
                                "status TEXT NOT NULL, " +
                                "allowMultipleChoice INTEGER NOT NULL DEFAULT 0, " +
                                "isAnonymous INTEGER NOT NULL DEFAULT 0, " +
                                "totalVotes INTEGER NOT NULL DEFAULT 0, " +
                                "FOREIGN KEY (authorId) REFERENCES members(id) ON DELETE CASCADE)"
                    )

                    // 创建投票选项表
                    database.execSQL(
                        "CREATE TABLE IF NOT EXISTS vote_options (" +
                                "id TEXT PRIMARY KEY NOT NULL, " +
                                "voteId TEXT NOT NULL, " +
                                "content TEXT NOT NULL, " +
                                "voteCount INTEGER NOT NULL DEFAULT 0, " +
                                "orderIndex INTEGER NOT NULL DEFAULT 0, " +
                                "FOREIGN KEY (voteId) REFERENCES votes(id) ON DELETE CASCADE)"
                    )

                    // 创建投票记录表
                    database.execSQL(
                        "CREATE TABLE IF NOT EXISTS vote_records (" +
                                "id TEXT PRIMARY KEY NOT NULL, " +
                                "voteId TEXT NOT NULL, " +
                                "optionId TEXT NOT NULL, " +
                                "userId TEXT NOT NULL, " +
                                "userName TEXT NOT NULL, " +
                                "userAvatar TEXT, " +
                                "votedAt INTEGER NOT NULL, " +
                                "FOREIGN KEY (voteId) REFERENCES votes(id) ON DELETE CASCADE, " +
                                "FOREIGN KEY (optionId) REFERENCES vote_options(id) ON DELETE CASCADE, " +
                                "FOREIGN KEY (userId) REFERENCES members(id) ON DELETE CASCADE)"
                    )

                    // 创建索引
                    database.execSQL("CREATE INDEX IF NOT EXISTS index_votes_authorId ON votes(authorId)")
                    database.execSQL("CREATE INDEX IF NOT EXISTS index_vote_options_voteId ON vote_options(voteId)")
                    database.execSQL("CREATE INDEX IF NOT EXISTS index_vote_records_voteId ON vote_records(voteId)")
                    database.execSQL("CREATE INDEX IF NOT EXISTS index_vote_records_optionId ON vote_records(optionId)")
                    database.execSQL("CREATE INDEX IF NOT EXISTS index_vote_records_userId ON vote_records(userId)")
                    database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_vote_records_unique ON vote_records(voteId, userId, optionId)")

                    Log.d("AppDatabase", "数据库迁移 6->7 完成：投票相关表已创建")
                } catch (e: Exception) {
                    Log.e("AppDatabase", "数据库迁移失败: ${e.message}", e)
                }
            }
        }

        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                try {
                    // 创建系统表
                    database.execSQL(
                        "CREATE TABLE IF NOT EXISTS systems (" +
                                "id TEXT PRIMARY KEY NOT NULL, " +
                                "name TEXT NOT NULL, " +
                                "avatarUrl TEXT, " +
                                "description TEXT NOT NULL DEFAULT '', " +
                                "createdAt INTEGER NOT NULL, " +
                                "updatedAt INTEGER NOT NULL)"
                    )

                    Log.d("AppDatabase", "数据库迁移 7->8 完成：系统表已创建")
                } catch (e: Exception) {
                    Log.e("AppDatabase", "数据库迁移失败: ${e.message}", e)
                }
            }
        }

        private val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(database: SupportSQLiteDatabase) {
                try {
                    // 创建在线状态表
                    database.execSQL(
                        "CREATE TABLE IF NOT EXISTS online_status (" +
                                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                                "memberId TEXT NOT NULL, " +
                                "loginTime INTEGER NOT NULL, " +
                                "logoutTime INTEGER, " +
                                "duration INTEGER NOT NULL DEFAULT 0, " +
                                "FOREIGN KEY (memberId) REFERENCES members(id) ON DELETE CASCADE)"
                    )

                    // 创建索引
                    database.execSQL("CREATE INDEX IF NOT EXISTS index_online_status_memberId ON online_status(memberId)")

                    Log.d("AppDatabase", "数据库迁移 8->9 完成：在线状态表已创建")
                } catch (e: Exception) {
                    Log.e("AppDatabase", "数据库迁移失败: ${e.message}", e)
                }
            }
        }

        private val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(database: SupportSQLiteDatabase) {
                try {
                    // 为chat_groups表添加avatarUrl字段
                    database.execSQL("ALTER TABLE chat_groups ADD COLUMN avatarUrl TEXT")
                    
                    Log.d("AppDatabase", "数据库迁移 9->10 完成：chat_groups表已添加avatarUrl字段")
                } catch (e: Exception) {
                    Log.e("AppDatabase", "数据库迁移失败: ${e.message}", e)
                    throw e // 重新抛出异常，让Room知道迁移失败
                }
            }
        }

        private val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(database: SupportSQLiteDatabase) {
                try {
                    // 删除systems表的description字段
                    // SQLite不支持直接删除列，需要重建表
                    database.execSQL(
                        "CREATE TABLE systems_new (" +
                                "id TEXT PRIMARY KEY NOT NULL, " +
                                "name TEXT NOT NULL, " +
                                "avatarUrl TEXT, " +
                                "createdAt INTEGER NOT NULL, " +
                                "updatedAt INTEGER NOT NULL)"
                    )
                    
                    // 复制数据（不包括description字段）
                    database.execSQL(
                        "INSERT INTO systems_new (id, name, avatarUrl, createdAt, updatedAt) " +
                                "SELECT id, name, avatarUrl, createdAt, updatedAt FROM systems"
                    )
                    
                    // 删除旧表
                    database.execSQL("DROP TABLE systems")
                    
                    // 重命名新表
                    database.execSQL("ALTER TABLE systems_new RENAME TO systems")
                    
                    Log.d("AppDatabase", "数据库迁移 10->11 完成：systems表已删除description字段")
                } catch (e: Exception) {
                    Log.e("AppDatabase", "数据库迁移失败: ${e.message}", e)
                    throw e // 重新抛出异常，让Room知道迁移失败
                }
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "chat_database"
                )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11)
                // 移除 .fallbackToDestructiveMigration() 以防止数据丢失
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
} 