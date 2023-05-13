package me.urfate.yomuki.db.dao

import androidx.room.*
import me.urfate.yomuki.db.entity.SourceEntity

@Dao
interface SourceDao {
    @get:Query("SELECT * FROM SourceEntity")
    val all: List<SourceEntity?>?

    @Query("SELECT * FROM SourceEntity WHERE is_default IS 0")
    suspend fun findAllNotDefault(): List<SourceEntity?>?

    @Query("SELECT * FROM SourceEntity WHERE id IN (:sourcesIds)")
    suspend fun loadAllByIds(sourcesIds: IntArray?): List<SourceEntity?>?

    @Query("SELECT * FROM SourceEntity WHERE url LIKE :sourceUrl LIMIT 1")
    suspend fun findByUrl(sourceUrl: String?): SourceEntity?

    @Query("SELECT * FROM SourceEntity WHERE is_default IS 1 LIMIT 1")
    suspend fun findDefault(): SourceEntity?

    @Update
    suspend fun update(vararg source: SourceEntity)

    @Insert
    suspend fun insertAll(vararg sources: SourceEntity?)

    @Delete
    suspend fun delete(source: SourceEntity?)
}