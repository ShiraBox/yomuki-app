package me.urfate.yomuki.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity
class BookEntity {
    @PrimaryKey(autoGenerate = true)
    var id = 0

    @JvmField
    @ColumnInfo(name = "name")
    var name: String

    @JvmField
    @ColumnInfo(name = "description")
    var description: String

    @JvmField
    @ColumnInfo(name = "release_year")
    var releaseYear: String

    @JvmField
    @ColumnInfo(name = "url")
    var url: String

    @JvmField
    @ColumnInfo(name = "cover_url")
    var coverUrl: String

    @JvmField
    @ColumnInfo(name = "favorite")
    var favorite: Int

    @ColumnInfo(name = "reading_chapter_url")
    var readingChapterUrl: String

    @ColumnInfo(name = "reading_chapter_page")
    var readingChapterPage: Int

    @ColumnInfo(name = "reading_chapter_volume")
    var readingChapterVolume: Int

    @ColumnInfo(name = "reading_chapter_number")
    var readingChapterNumber: Int

    @JvmField
    @ColumnInfo(name = "source")
    var source: String

    constructor(id: Int, name: String, description: String, releaseYear: String, url: String, coverUrl: String, favorite: Int, readingChapterUrl: String, readingChapterPage: Int, readingChapterVolume: Int, readingChapterNumber: Int, source: String) {
        this.id = id
        this.name = name
        this.description = description
        this.releaseYear = releaseYear
        this.url = url
        this.coverUrl = coverUrl
        this.favorite = favorite
        this.readingChapterUrl = readingChapterUrl
        this.readingChapterPage = readingChapterPage
        this.readingChapterVolume = readingChapterVolume
        this.readingChapterNumber = readingChapterNumber
        this.source = source
    }

    @Ignore
    constructor(name: String, description: String, releaseYear: String, url: String, coverUrl: String, favorite: Int, readingChapterUrl: String, readingChapterPage: Int, readingChapterVolume: Int, readingChapterNumber: Int, source: String) {
        this.name = name
        this.description = description
        this.releaseYear = releaseYear
        this.url = url
        this.coverUrl = coverUrl
        this.favorite = favorite
        this.readingChapterUrl = readingChapterUrl
        this.readingChapterPage = readingChapterPage
        this.readingChapterVolume = readingChapterVolume
        this.readingChapterNumber = readingChapterNumber
        this.source = source
    }
}