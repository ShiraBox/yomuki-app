package me.urfate.yomuki.source.mangalib

data class AuthData(
    val csrfToken: String,
    val cookies: Map<String, String>
)