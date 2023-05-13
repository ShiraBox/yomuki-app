package me.urfate.yomuki.source

import me.urfate.yomuki.source.mangalib.MangaLib
import me.urfate.yomuki.source.remanga.ReManga

class SourceManager {

    private val sources: List<ContentSource> = listOf(
        ReManga(),
        MangaLib(),
        MangaPoisk()
    )

    fun sources(): List<ContentSource> {
        return sources
    }

    fun fromUrl(name: String): ContentSource? {
        return sources.firstOrNull { name.contains(it.name, true) }
    }

    companion object {
        val instance = SourceManager()
    }
}