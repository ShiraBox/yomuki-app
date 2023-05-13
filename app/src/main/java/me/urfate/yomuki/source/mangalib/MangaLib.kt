package me.urfate.yomuki.source.mangalib

import android.net.Uri
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import me.urfate.yomuki.model.Book
import me.urfate.yomuki.model.Chapter
import me.urfate.yomuki.model.Status
import me.urfate.yomuki.source.ContentSource
import org.apache.commons.lang3.StringUtils
import org.jsoup.Connection
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt


class MangaLib : ContentSource("MangaLib", "https://mangalib.me", "https://mangalib.me/icons/android-icon-192x192.png") {

    private var authData: AuthData = AuthData("", emptyMap())
    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }
    private val forumUrl = "https://lib.social"

    override suspend fun fetchBook(url: String): Book? {
        val document = httpGET("${forumUrl}/manga-short-info?" +
                "slug=${url.substringAfter(".me/")}") ?: return null

        val bookData = json.decodeFromString<BookData>(document.body().wholeText())

        return Book(
            bookData.name, bookData.altName, bookData.description, bookData.cover,
            bookData.releaseDate, getStatus(bookData.status.id),
            ratingCalc(bookData.rateAvg.toDouble()), url, bookData.categories.map { it.name },
            emptyList(), -1, -1, this.url
        )
    }

    override suspend fun search(query: String?): List<Book> {
        val encodedQuery = Uri.encode(query)

        prepare()
        val response = httpPOST("$url/filterlist?page=1&name=$encodedQuery",
            authData.cookies,
            mapOf("Content-Type" to "application/json"),
            mapOf("Accept" to "application/json, text/plain, */*"),
            mapOf("x-csrf-token" to authData.csrfToken)
        ) ?: return emptyList()

        return unpackBooks(response)
    }

    override suspend fun fetchBookChapters(url: String): List<Chapter> {
        val document = httpGET(url, authData.cookies,
            mapOf("Accept" to "text/html,application/xhtml+xml,application/xml;" +
                    "q=0.9,image/webp,image/apng,*/*;" +
                    "q=0.8,application/signed-exchange;v=b3;q=0.9\""),
            mapOf("Referer" to this.url)) ?: return emptyList()

        var dataString: String? = document.toString()
        dataString = StringUtils.substringAfter(dataString, "window.__DATA__ = ")
        dataString = StringUtils.substringBefore(dataString, "window._SITE_COLOR_")
        dataString = StringUtils.substringBeforeLast(dataString, ";")

        val chaptersData = json.decodeFromString<BookWrapperData>(dataString).chapters

        // For API v24 compatibility
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())

        return chaptersData.list.map {
            Chapter(
                it.name, it.volume, it.number.toDouble().roundToInt(),
                outputFormat.format(inputFormat.parse(it.createdAt) as Date),
                "$url/v${it.volume}/c${it.number}", emptyList()
            )
        }
    }

    override suspend fun fetchChapterPages(url: String): List<String> {

        val document = httpGET(url, authData.cookies) ?: return ArrayList()

        val chapterInfo = document
            .select("script:containsData(window.__info)")
            .first()
            ?.html()
            ?.split("window.__info = ")
            ?.last()
            ?.trim()
            ?.split(";")
            ?.first() ?: return emptyList()

        val pagesInfo = document
            .select("script:containsData(window.__pg)")
            .first()
            ?.html()
            ?.trim()
            ?.removePrefix("window.__pg = ")
            ?.removeSuffix(";") ?: return emptyList()

        val chapterData = json.decodeFromString<PagesChapterData>(chapterInfo)
        val pagesData = json.decodeFromString<List<PagesData>>(pagesInfo)

        return pagesData.map {
            getServer(chapterData.img.server, chapterData.servers) +
                    "${chapterData.img.url}${it.img}"
        }
    }

    override suspend fun fetchFirstChapter(url: String): Chapter {
        val chapters = fetchBookChapters(url)
        return chapters[chapters.size - 1]
    }

    override suspend fun fetchUpdatedBooks(): List<Book> {
        prepare()
        val response = httpPOST("$url/filterlist?dir=desc&sort=last_chapter_at&page=1",
            authData.cookies, mapOf("Content-Type" to "application/json"),
            mapOf("Accept" to "application/json, text/plain, */*"),
            mapOf("x-csrf-token" to authData.csrfToken)) ?: return ArrayList()

        return unpackBooks(response)
    }

    override suspend fun fetchPopularBooks(): List<Book> {
        prepare()
        val response = httpPOST("$url/filterlist?dir=desc&sort=views&page=1",
            authData.cookies, mapOf("Content-Type" to "application/json"),
            mapOf("Accept" to "application/json, text/plain, */*"),
            mapOf("x-csrf-token" to authData.csrfToken)) ?: return ArrayList()

        return unpackBooks(response)
    }

    override fun explicitGenres(): List<String> {
        return listOf("Яой", "Бара", "Юри", "Эротика")
    }

    private suspend fun prepare() {
        authData.csrfToken.ifEmpty { authData = fetchAuthData() }
    }

    private fun unpackBooks(response: Connection.Response): List<Book> {
        val booksData = json.decodeFromString<WrapperData<WrapperListData<SearchBookData>>>(
            response.body()).items.data

        return booksData.map {
            Book(
                it.name, it.altName, "", it.covers.default, it.issueYear, Status.UNKNOWN,
                5f, it.url, emptyList(), emptyList(), -1, -1, this.url
            )
        }
    }

    private fun getStatus(status: Int): Status {
        return when (status){
            1 -> Status.ONGOING
            2 -> Status.COMPLETED
            3 -> Status.ONGOING
            4 -> Status.COMPLETED
            5 -> Status.COMPLETED
            else -> Status.UNKNOWN
        }
    }

    private fun ratingCalc(rating: Double): Float {
        return (rating / 2).roundToInt().toFloat()
    }

    private fun getServer(string: String, data: PageServersData): String {
        return when(string){
            "main" -> data.main
            "secondary" -> data.secondary
            "compress" -> data.compress
            "fourth" -> data.fourth
            else -> ""
        }
    }

    private fun fetchAuthData(): AuthData {
        val mainPage = httpGET(url) ?: return AuthData("", emptyMap())

        return AuthData(mainPage.select("meta[name=_token]")[0].attr("content"),
            mainPage.connection().response().cookies())
    }
}