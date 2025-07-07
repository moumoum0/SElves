package com.example.myapplication.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.myapplication.data.dao.ChatGroupDao
import com.example.myapplication.data.dao.MemberDao
import com.example.myapplication.data.dao.MessageDao
import com.example.myapplication.data.dao.MessageReadStatusDao
import com.example.myapplication.data.entity.ChatGroupEntity
import com.example.myapplication.data.entity.MemberEntity
import com.example.myapplication.data.entity.MessageEntity
import com.example.myapplication.data.entity.MessageReadStatusEntity
import android.util.Log

@Database(
    entities = [
        MemberEntity::class,
        ChatGroupEntity::class,
        MessageEntity::class,
        MessageReadStatusEntity::class
    ],
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun memberDao(): MemberDao
    abstract fun chatGroupDao(): ChatGroupDao
    abstract fun messageDao(): MessageDao
    abstract fun messageReadStatusDao(): MessageReadStatusDao

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

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "chat_database"
                )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
} 