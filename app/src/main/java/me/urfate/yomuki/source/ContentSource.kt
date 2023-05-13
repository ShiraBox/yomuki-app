package me.urfate.yomuki.source

import android.util.Log
import me.urfate.yomuki.model.Book
import me.urfate.yomuki.model.Chapter
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.IOException

abstract class ContentSource internal constructor(val name: String, val url: String, val iconUrl: String) {

    abstract suspend fun fetchBook(url: String): Book?
    abstract suspend fun search(query: String?): List<Book>
    abstract suspend fun fetchBookChapters(url: String): List<Chapter>
    abstract suspend fun fetchFirstChapter(url: String): Chapter?
    abstract suspend fun fetchChapterPages(url: String): List<String>
    abstract suspend fun fetchUpdatedBooks(): List<Book>
    abstract suspend fun fetchPopularBooks(): List<Book>
    abstract fun explicitGenres(): List<String>

    fun httpGET(url: String): Document? {
        val document: Document = try {
            Jsoup.connect(url)
                .ignoreHttpErrors(true)
                .followRedirects(true)
                .ignoreContentType(true)
                .userAgent(userAgent)
                .timeout(15000)
                .get()
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }

        val response = document.connection().response()
        if(response.statusCode() != 200) {
            Log.e("HttpGET", "Failed to GET $url: ${response.statusCode()} - ${response.statusMessage()}")
            return null
        }

        return document
    }

    fun httpGET(url: String, cookies: Map<String, String>, vararg headers: Map<String, String>): Document? {
        val document: Document = try {
            Jsoup.connect(url)
                .headers(*headers)
                .cookies(cookies)
                .ignoreHttpErrors(true)
                .followRedirects(true)
                .ignoreContentType(true)
                .userAgent(userAgent)
                .timeout(15000)
                .get()
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }

        val response = document.connection().response()
        if(response.statusCode() != 200) {
            Log.e("HttpGET", "Failed to GET $url: ${response.statusCode()} - ${response.statusMessage()}")
            return null
        }

        return document
    }

    fun httpGET(url: String, cookies: Map<String, String>): Document? {
        val document: Document = try {
            Jsoup.connect(url)
                .ignoreHttpErrors(true)
                .followRedirects(true)
                .ignoreContentType(true)
                .cookies(cookies)
                .userAgent(userAgent)
                .timeout(15000)
                .get()
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }

        val response = document.connection().response()
        if(response.statusCode() != 200) {
            Log.e("HttpGET", "Failed to GET $url: ${response.statusCode()} - ${response.statusMessage()}")
            return null
        }

        return document
    }

    fun httpPOST(url: String, cookies: Map<String, String>, vararg headers: Map<String, String>): Connection.Response? {
        val response: Connection.Response?
        try {
            response = Jsoup.connect(url)
                .headers(*headers)
                .cookies(cookies)
                .userAgent(userAgent)
                .method(Connection.Method.POST)
                .ignoreContentType(true)
                .timeout(15000)
                .execute()
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }

        if(response.statusCode() != 200) {
            Log.e("HttpPOST", "Failed to POST $url: ${response.statusCode()} - ${response.statusMessage()}")
            return null
        }

        return response
    }

    companion object {
        @JvmStatic
        val userAgent: String
            get() = "Mozilla/5.0 (Linux; Android 13; SM-A127F) AppleWebKit/537.36 " +
                    "(KHTML, like Gecko) Chrome/100.0.4896.127 Mobile Safari/537.36"
    }
}

private fun Connection.headers(vararg headers: Map<String, String>): Connection {
    headers.forEach {
        for ((key, value) in it) {
            header(key, value)
        }
    }
    return this
}
