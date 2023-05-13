package me.urfate.yomuki.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room.databaseBuilder
import androidx.room.RoomDatabase
import me.urfate.yomuki.db.dao.BooksDao
import me.urfate.yomuki.db.dao.SourceDao
import me.urfate.yomuki.db.entity.BookEntity
import me.urfate.yomuki.db.entity.SourceEntity

@Database(entities = [SourceEntity::class, BookEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sourceDao(): SourceDao?
    abstract fun booksDao(): BooksDao?

    companion object {
        private const val DATABASE_NAME = "yomuki_db"
        private var instance: AppDatabase? = null

        @JvmStatic
        fun getInstance(context: Context?): AppDatabase? {
            if (instance == null) {
                instance =
                    databaseBuilder(context!!, AppDatabase::class.java, DATABASE_NAME).build()
            }
            return instance
        }
    }
}