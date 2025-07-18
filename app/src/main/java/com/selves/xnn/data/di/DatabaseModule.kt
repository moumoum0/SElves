package com.selves.xnn.data.di

import android.content.Context
import androidx.room.Room
import com.selves.xnn.data.AppDatabase
import com.selves.xnn.data.Converters
import com.selves.xnn.data.MemberPreferences
import com.selves.xnn.data.dao.ChatGroupDao
import com.selves.xnn.data.dao.DynamicDao
import com.selves.xnn.data.dao.MemberDao
import com.selves.xnn.data.dao.MessageDao
import com.selves.xnn.data.dao.MessageReadStatusDao
import com.selves.xnn.data.dao.TodoDao
import com.selves.xnn.data.dao.VoteDao
import com.selves.xnn.data.dao.SystemDao
import com.selves.xnn.data.dao.OnlineStatusDao
import com.selves.xnn.data.repository.ChatGroupRepository
import com.selves.xnn.data.repository.DynamicRepository
import com.selves.xnn.data.repository.MemberRepository
import com.selves.xnn.data.repository.MessageRepository
import com.selves.xnn.data.repository.MessageReadStatusRepository
import com.selves.xnn.data.repository.TodoRepository
import com.selves.xnn.data.repository.VoteRepository
import com.selves.xnn.data.repository.SystemRepository
import com.selves.xnn.data.repository.OnlineStatusRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "chat_database"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    @Singleton
    fun provideMemberDao(database: AppDatabase): MemberDao {
        return database.memberDao()
    }

    @Provides
    @Singleton
    fun provideChatGroupDao(database: AppDatabase): ChatGroupDao {
        return database.chatGroupDao()
    }

    @Provides
    @Singleton
    fun provideMessageDao(database: AppDatabase): MessageDao {
        return database.messageDao()
    }

    @Provides
    @Singleton
    fun provideMessageReadStatusDao(database: AppDatabase): MessageReadStatusDao {
        return database.messageReadStatusDao()
    }

    @Provides
    @Singleton
    fun provideTodoDao(database: AppDatabase): TodoDao {
        return database.todoDao()
    }

    @Provides
    @Singleton
    fun provideDynamicDao(database: AppDatabase): DynamicDao {
        return database.dynamicDao()
    }

    @Provides
    @Singleton
    fun provideVoteDao(database: AppDatabase): VoteDao {
        return database.voteDao()
    }

    @Provides
    @Singleton
    fun provideSystemDao(database: AppDatabase): SystemDao {
        return database.systemDao()
    }

    @Provides
    @Singleton
    fun provideOnlineStatusDao(database: AppDatabase): OnlineStatusDao {
        return database.onlineStatusDao()
    }

    @Provides
    @Singleton
    fun provideMemberRepository(database: AppDatabase): MemberRepository {
        return MemberRepository(database)
    }

    @Provides
    @Singleton
    fun provideChatGroupRepository(chatGroupDao: ChatGroupDao, memberDao: MemberDao): ChatGroupRepository {
        return ChatGroupRepository(chatGroupDao, memberDao)
    }

    @Provides
    @Singleton
    fun provideMessageRepository(messageDao: MessageDao): MessageRepository {
        return MessageRepository(messageDao)
    }

    @Provides
    @Singleton
    fun provideMessageReadStatusRepository(messageReadStatusDao: MessageReadStatusDao): MessageReadStatusRepository {
        return MessageReadStatusRepository(messageReadStatusDao)
    }

    @Provides
    @Singleton
    fun provideTodoRepository(todoDao: TodoDao): TodoRepository {
        return TodoRepository(todoDao)
    }

    @Provides
    @Singleton
    fun provideDynamicRepository(dynamicDao: DynamicDao): DynamicRepository {
        return DynamicRepository(dynamicDao)
    }

    @Provides
    @Singleton
    fun provideVoteRepository(voteDao: VoteDao): VoteRepository {
        return VoteRepository(voteDao)
    }

    @Provides
    @Singleton
    fun provideSystemRepository(database: AppDatabase): SystemRepository {
        return SystemRepository(database)
    }

    @Provides
    @Singleton
    fun provideOnlineStatusRepository(onlineStatusDao: OnlineStatusDao): OnlineStatusRepository {
        return OnlineStatusRepository(onlineStatusDao)
    }

    @Provides
    @Singleton
    fun provideMemberPreferences(@ApplicationContext context: Context): MemberPreferences {
        return MemberPreferences(context)
    }
} 