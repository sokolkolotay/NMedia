package ru.netology.nmedia.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dao.PostRemoteKeyDao
import ru.netology.nmedia.db.AppDb

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): AppDb =
        Room.databaseBuilder(
            context,
            AppDb::class.java,
            "app.db"
        )
            .addMigrations(
                AppDb.MIGRATION_6_7,
                AppDb.MIGRATION_7_8,
                AppDb.MIGRATION_8_9,
            )
            .fallbackToDestructiveMigration()
            .allowMainThreadQueries()
            .build()

    @Provides
    fun providePostDao(
        database: AppDb
    ): PostDao =
        database.postDao()

    @Provides
    fun providePostRemoteKeyDao(
        database: AppDb
    ): PostRemoteKeyDao =
        database.postRemoteKeyDao()
}
