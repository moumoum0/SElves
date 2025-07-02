package com.example.myapplication.data.di

import android.content.Context
import androidx.room.Room
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.Converters
import com.example.myapplication.data.MemberPreferences
import com.example.myapplication.data.dao.ChatGroupDao
import com.example.myapplication.data.dao.MemberDao
import com.example.myapplication.data.dao.MessageDao
import com.example.myapplication.data.dao.MessageReadStatusDao
import com.example.myapplication.data.repository.ChatGroupRepository
import com.example.myapplication.data.repository.MemberRepository
import com.example.myapplication.data.repository.MessageRepository
import com.example.myapplication.data.repository.MessageReadStatusRepository
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
    fun provideMemberPreferences(@ApplicationContext context: Context): MemberPreferences {
        return MemberPreferences(context)
    }
} 