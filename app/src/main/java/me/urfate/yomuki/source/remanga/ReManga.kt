package me.urfate.yomuki.source.remanga

import android.net.Uri
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import me.urfate.yomuki.model.Book
import me.urfate.yomuki.model.Chapter
import me.urfate.yomuki.model.Status
import me.urfate.yomuki.source.ContentSource
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.safety.Safelist
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

class ReManga : ContentSource("ReManga", "https://remanga.org/", "https://remanga.org/apple-touch-icon.png") {

    private val baseURL = "https://api.remanga.org/"
    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }


    override suspend fun fetchBook(url: String): Book? {
        val dir = url.substringAfter("manga/")
        val response = httpGET("${baseURL}api/titles/$dir") ?: return null

        val book = json.decodeFromString<WrapperData<BookData>>(
            fixNestedQuotes(response.body().wholeText())
        ).content

        return Book(
            book.rusName, book.enName, stripHtml(book.description), "${this.url}${book.img.high}",
            book.issueYear.toString(), getStatus(book.status.id), ratingCalc(book.avgRating.toDouble()),
            url, book.genres?.map { genresData -> genresData.name; }
                ?.toMutableList()
                .also{ genresList -> if(book.isYaoi) genresList?.add("яой") } ?: emptyList(),
            emptyList(), -1, -1, this.url
        )
    }

    override suspend fun search(query: String?): List<Book> {
        val encodedQuery = Uri.encode(query)
        val response = httpGET("${baseURL}api/search/?query=$encodedQuery")
            ?: return emptyList()

        val books = json.decodeFromString<LibraryWrapperData<SearchBookData>>(
            response.body().wholeText())

        return books.content.map {
            Book(
                it.rusName, it.enName, "", "$url${it.img.high}", it.issueYear.toString(),
                Status.UNKNOWN, ratingCalc(it.avgRating.toDouble()), "$url/manga/${it.dir}",
                emptyList<String>().toMutableList()
                    .also { genresList -> if(it.isYaoi) genresList.add("яой") },
                emptyList(), -1, -1,
                this.url
            )
        }
    }

    override suspend fun fetchBookChapters(url: String): List<Chapter> {
        val dir = url.substringAfter("manga/")

        val bookResponse = httpGET("${baseURL}api/titles/$dir") ?: return emptyList()
        val bookData = json.decodeFromString<WrapperData<BookData>>(
            fixNestedQuotes(bookResponse.body().wholeText())
        ).content

        val branchId = bookData.branches.first().id
        val chaptersCount = bookData.branches.first().countChapters

        val chaptersData = mutableListOf<List<ChapterData>>()

        for(i in 1..(chaptersCount / 100 + 1)){
            val chaptersResponse = httpGET("${baseURL}api/titles/chapters/" +
                    "?page=$i&count=100&branch_id=$branchId")
                ?: return emptyList()
            chaptersData.add(json.decodeFromString<LibraryWrapperData<ChapterData>>(
                chaptersResponse.body().wholeText()
            ).content)
        }

        // For API v24 compatibility
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())

        return chaptersData.flatten().map {
            Chapter(it.name, it.volume, it.index,
                outputFormat.format(inputFormat.parse(it.uploadDate) as Date),
                "$this.url/manga/${dir}/ch${it.id}", emptyList()
            )
        }
    }

    override suspend fun fetchFirstChapter(url: String): Chapter {
        return fetchBookChapters(url).last()
    }

    override suspend fun fetchChapterPages(url: String): List<String> {
        val chapterId = url.substringAfter("/ch")

        val response = httpGET("${baseURL}api/titles/chapters/$chapterId")
            ?: return emptyList()

        val chapterData = json.decodeFromString<WrapperData<ChapterPagesData>>(
            response.body().wholeText()).content

        return chapterData.pages.flatten().map { it.img }
    }

    override suspend fun fetchUpdatedBooks(): List<Book> {
        val response = httpGET("${baseURL}api/search/catalog/?ordering=-chapter_date")
            ?: return emptyList()

        return unpackBooks(response)
    }

    override suspend fun fetchPopularBooks(): List<Book> {
        val response = httpGET("${baseURL}api/search/catalog/?ordering=-rating")
            ?: return emptyList()

        return unpackBooks(response)
    }

    override fun explicitGenres(): List<String> {
        return listOf(
            "эротика",
            "этти",
            "юри",
            "яой"
        )
    }

    private fun unpackBooks(response: Document): List<Book> {
        val books = json.decodeFromString<LibraryWrapperData<LibraryBookData>>(
            response.body().wholeText())

        return books.content.map {
            Book(
                it.rusName, it.enName, "", "$url${it.img.high}", it.issueYear.toString(),
                Status.UNKNOWN, ratingCalc(it.avgRating.toDouble()), "$url/manga/${it.dir}",
                it.genres?.map { genresData -> genresData.name; }
                    ?.toMutableList()
                    ?.also { genresList -> if(it.isYaoi) genresList.add("яой") } ?: emptyList(),
                emptyList(), -1, -1,
                this.url
            )
        }
    }

    private fun ratingCalc(rating: Double): Float {
        return (rating / 2).roundToInt().toFloat()
    }

    /**
     * Mostly description can contain HTML tags and other formatting things which
     * is not needed
     */
    private fun stripHtml(text: String): String {
        return Jsoup.clean(text, Safelist.none())
            .replace("&nbsp;"," ")
    }

    /**
     * In some cases API can return description with quotes inside which
     * causes JsonDecodingException and breaks parsing process
     */
    private fun fixNestedQuotes(str: String): String {
        val indexesToRemove = str.indices.filterIndexed { index, _ ->
            (index > 0 && index < str.lastIndex && str[index] == '\"' && str[index - 1].isWhitespace()) ||
                    (index > 0 && index < str.lastIndex - 1 && str[index] == '\"' && str[index + 1].isWhitespace())
        }

        return str.filterIndexed{index, _ -> !indexesToRemove.contains(index)}
    }

    private fun getStatus(status: Int): Status {
        return when (status){
            0 -> Status.COMPLETED
            1 -> Status.ONGOING
            2 -> Status.UNKNOWN
            3 -> Status.UNKNOWN
            4 -> Status.ONGOING
            5 -> Status.COMPLETED
            else -> Status.UNKNOWN
        }
    }
}