package ru.netology.nmedia.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.entity.PostEntity

@Database(entities = [PostEntity::class], version = 1)
abstract class AppDb : RoomDatabase() {
    abstract fun postDao(): PostDao

    companion object {
        @Volatile
        private var instance: AppDb? = null

        fun getInstance(context: Context): AppDb {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context) = Room
            .databaseBuilder(context, AppDb::class.java, "app.db")
            .allowMainThreadQueries()
            .build()
    }
}


//class DbHelper(context: Context, dbVersion: Int, dbName: String, private val DDLs: Array<String>) :
//    SQLiteOpenHelper(context, dbName, null, dbVersion) {
//    override fun onCreate(db: SQLiteDatabase) {
//        DDLs.forEach {
//            db.execSQL(it)
//        }
//    }
//
//    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
//        TODO("Not yet implemented")
//    }
//
//    override fun onDowngrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
//        TODO("Not yet implemented")
//    }
//}