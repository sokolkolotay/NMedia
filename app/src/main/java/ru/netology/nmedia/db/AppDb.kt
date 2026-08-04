package ru.netology.nmedia.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.PostRemoteKeyEntity
import ru.netology.nmedia.dao.PostRemoteKeyDao

@Database(
    entities = [PostEntity::class, PostRemoteKeyEntity::class],
    version = 10,
    exportSchema = false,
)
abstract class AppDb : RoomDatabase() {
    abstract fun postDao(): PostDao
    abstract fun postRemoteKeyDao(): PostRemoteKeyDao

    companion object {
        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE PostEntity " +
                        "ADD COLUMN ownedByMe INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE PostEntity " +
                        "ADD COLUMN authorId INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE PostEntity_new (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "authorId INTEGER NOT NULL, " +
                        "author TEXT NOT NULL, " +
                        "content TEXT NOT NULL, " +
                        "published INTEGER NOT NULL, " +
                        "likedByMe INTEGER NOT NULL, " +
                        "ownedByMe INTEGER NOT NULL, " +
                        "likes INTEGER NOT NULL, " +
                        "isSynced INTEGER NOT NULL, " +
                        "hidden INTEGER NOT NULL, " +
                        "attachmentUrl TEXT, " +
                        "attachmentType TEXT)"
                )
                db.execSQL(
                    "INSERT INTO PostEntity_new " +
                        "SELECT id, authorId, author, content, " +
                        "CAST(published AS INTEGER), likedByMe, ownedByMe, " +
                        "likes, isSynced, hidden, attachmentUrl, attachmentType " +
                        "FROM PostEntity"
                )
                db.execSQL("DROP TABLE PostEntity")
                db.execSQL("ALTER TABLE PostEntity_new RENAME TO PostEntity")
            }
        }

        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE PostEntity " +
                        "ADD COLUMN authorAvatar TEXT"
                )
                // Старый серверный кеш не содержит avatar и относится к другому
                // набору данных. Локальные несинхронизированные посты сохраняем.
                db.execSQL("DELETE FROM PostEntity WHERE isSynced = 1")
                db.execSQL("DELETE FROM PostRemoteKeyEntity")
            }
        }

        @Volatile
        private var instance: AppDb? = null

        fun getInstance(context: Context): AppDb {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(context, AppDb::class.java, "app.db")
                .addMigrations(
                    MIGRATION_6_7,
                    MIGRATION_7_8,
                    MIGRATION_8_9,
                    MIGRATION_9_10,
                )
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries()
                .build()
    }
}
