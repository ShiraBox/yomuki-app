package me.urfate.yomuki.db.dao

import androidx.room.*
import me.urfate.yomuki.db.entity.BookEntity

@Dao
interface BooksDao {
    @get:Query("SELECT * FROM BookEntity")
    val all: List<BookEntity?>?

    @Query("SELECT * FROM BookEntity WHERE id IN (:booksIds)")
    suspend fun loadAllByIds(booksIds: IntArray?): List<BookEntity?>?

    @Query("SELECT * FROM BookEntity WHERE url LIKE :bookUrl LIMIT 1")
    suspend fun findByUrl(bookUrl: String?): BookEntity?

    @Query("SELECT * FROM BookEntity WHERE source LIKE :sourceUrl LIMIT 1")
    suspend fun findBySource(sourceUrl: String?): BookEntity?

    @Query("SELECT * FROM BookEntity WHERE favorite IS 1")
    suspend fun findFavorites(): List<BookEntity?>?

    @Insert
    suspend fun insertAll(vararg bookEntities: BookEntity?)

    @Update
    suspend fun updateBook(bookEntity: BookEntity?)

    @Delete
    suspend fun delete(BookEntity: BookEntity?)
}