package me.urfate.yomuki.model

data class Chapter(val name: String, val volume: Int, val chapterNumber: Int,
                   val releaseDate: String, val chapterUrl: String, val pages: List<String>)