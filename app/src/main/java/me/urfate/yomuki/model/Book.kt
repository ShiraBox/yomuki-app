package me.urfate.yomuki.model

data class Book(val title: String, val altTitle: String, val description: String,
                val coverUrl: String, val releaseDate: String, val status: Status,
                val rating: Float, val bookUrl: String, val genres: List<String>,
                var chapters: List<Chapter>, val volume: Int, val chapter: Int, val source: String)