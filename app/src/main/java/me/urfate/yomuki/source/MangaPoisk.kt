package me.urfate.yomuki.source

import android.net.Uri
import me.urfate.yomuki.model.Book
import me.urfate.yomuki.model.Chapter
import me.urfate.yomuki.model.Status
import org.apache.commons.lang3.StringUtils
import org.jsoup.nodes.Element
import java.util.*
import java.util.function.Consumer

class MangaPoisk : ContentSource("MangaPoisk", "https://mangapoisk.ru", "https://mangapoisk.ru/favicon.ico") {

    override suspend fun fetchBook(url: String): Book? {
        val document = httpGET(url) ?: return null

        // Define book parameters
        val title = document.select("body > div.container.manga-container > div > article.order-0.card.shadow-sm.bg-black.rounded-0.w-100.col-xl-8 > div > h1 > span.post-name").text()
        val altTitle = document.select("body > div.container.manga-container > div > article.order-0.card.shadow-sm.bg-black.rounded-0.w-100.col-xl-8 > div > div.row > div.col-md-7.col-lg-8 > h2.post-name-jp.h5").text()
        val coverUrl = document.select("body > div.container.manga-container > div > article.order-0.card.shadow-sm.bg-black.rounded-0.w-100.col-xl-8 > div > div.row > div.col-md-5.col-lg-4.text-center > img").attr("src")
        val releaseYear = document.select("body > div.container.manga-container > div > article.order-0.card.shadow-sm.bg-black.rounded-0.w-100.col-xl-8 > div > div.row > div.col-md-7.col-lg-8 > div > span:nth-child(13) > a").text()
        val statusString = document.select("body > div.container.manga-container > div > article.order-0.card.shadow-sm.bg-black.rounded-0.w-100.col-xl-8 > div > div.row > div.col-md-7.col-lg-8 > div > span:nth-child(8)").text().replace(".+: ".toRegex(), "")
        val rating = Scanner(document.select("body > div.container.manga-container > div > article.order-0.card.shadow-sm.bg-black.rounded-0.w-100.col-xl-8 > div > div.row > div.col-md-7.col-lg-8 > div > div.row.my-2 > div.ratingResults.mr-3 > b").text()).useDelimiter("\\D+").nextFloat()
        val genres: MutableList<String> = ArrayList()
        val chapters: List<Chapter> = ArrayList()
        val description: String? = try {
            document.select("div.manga-description.entry").first()!!.text()
        } catch (e: NullPointerException) {
            ""
        }

        val genresElements = document.select("body > div.container.manga-container > div > article.order-0.card.shadow-sm.bg-black.rounded-0.w-100.col-xl-8 > div > div.row > div.col-md-7.col-lg-8 > div > span:nth-child(11) > a")
        genresElements.forEach(Consumer { genreElement: Element -> genres.add(StringUtils.capitalize(genreElement.text())) })
        val status = if (statusString.equals("Выпускается", ignoreCase = true)) Status.ONGOING else Status.COMPLETED

        return Book(title, altTitle, description!!, coverUrl, releaseYear, status, rating, url, genres, chapters, 1, 1,
            this.url
        )
    }

    override suspend fun search(query: String?): List<Book> {
        val encodedQuery = Uri.encode(query)
        val document = httpGET("$url/search?q=$encodedQuery") ?: return ArrayList()

        val searchBooksElements = document.select("body > div.container.index-container" +
                " > div > div > div.flex-container.row.align-items-start.justify-content-center")
        val books: MutableList<Book> = ArrayList()

        for (element in searchBooksElements.select("article")) {
            val title = element.select("div > a > p.card-title.js-card-title").text()
            val description = element.select("div > a > p.card-text").text()
            var coverUrl = element.select("a.px-1.py-1 > img").attr("src")
            val url = url + element.select("a").attr("href")
            val releaseYear = Scanner(element.select("div > a > ul.card-numbers.m-0.p-0 " +
                    "> li:nth-child(2) > span").text()).useDelimiter("\\D+")
                    .nextInt().toString()
            val scanner = Scanner(element.select("ul > li > a").text())
                .useDelimiter("\\D+")
            val volume = if (scanner.hasNextInt()) scanner.nextInt() else 1
            val chapter = if (scanner.hasNextInt()) scanner.nextInt() else 1
            if (coverUrl.contains("data:image/svg+xml")) {
                coverUrl = element.select("a.px-1.py-1 > img").attr("data-src")
            }

            books.add(Book(title, "", description, coverUrl, releaseYear, Status.UNKNOWN,
                5.0f, url, ArrayList(), ArrayList(), volume, chapter, this.url
            ))
        }

        return books
    }

    override suspend fun fetchBookChapters(url: String): List<Chapter> {
        return fetchBookChaptersRequest(url, -1)
    }

    private fun fetchBookChaptersRequest(url: String, page: Int): List<Chapter> {
        val document = httpGET("$url/chaptersList?infinite=1&page=$page") ?: return ArrayList()

        val chapters: MutableList<Chapter> = ArrayList()

        val chaptersElements = document.select("body > div.scroll-area > ul.chapter-list-container.post-footer.list-group")
        val pagination = document.select("body > div.chapters-infinite-pagination > nav > ul > li")
        val maxPage = if (pagination.size - 2 < 0) 1 else pagination.size - 2

        if(page == -1){
            var pages = 1
            while(pages <= maxPage){
                chapters.addAll(fetchBookChaptersRequest(url, pages))
                pages++
            }
            return chapters
        }

        chaptersElements.select("li").forEach { element ->
            val scanner = Scanner(element.select("a > span.chapter-title").text())
                .useDelimiter("\\D+")
            val chapterVolume = scanner.nextInt()
            val chapterNumber: Int = try {
                scanner.nextInt()
            } catch (e: NoSuchElementException) {
                -1
            }
            element.select("a").select("span.chapter-title").remove()
            val chapterTitle = element.select("a").text()
            val chapterReleaseDate = element.select("span.chapter-date").text()
            val chapterUrl = this.url + element.select("a").attr("href")
            chapters.add(Chapter(chapterTitle, chapterVolume, chapterNumber, chapterReleaseDate,
                chapterUrl, ArrayList()))
        }
        return chapters
    }

    override suspend fun fetchFirstChapter(url: String): Chapter {
        val chapters = fetchBookChaptersRequest(url, 1)
        return chapters[0]
    }

    override suspend fun fetchChapterPages(url: String): List<String> {
        val document = httpGET(url) ?: return ArrayList()

        val pagesUrls: MutableList<String> = ArrayList()
        val pages = document.select("body > div.container-fluid.chapter-container > div.mt-1.d-flex.flex-column.align-items-center.chapter-images")

        pages.select("img").forEach {
            pagesUrls.add(it.attr("data-src").ifEmpty { it.attr("src") })
        }

        return pagesUrls
    }

    override suspend fun fetchUpdatedBooks(): List<Book> {
        val document = httpGET("$url/manga?sortBy=-last_chapter_at") ?: return ArrayList()

        // Fetch only updated books
        val updatedBooksElements = document.select("body > div.container.index-container > div > div > div > div.flex-container.row.align-items-start.justify-content-center.flex-wrap")
        val books: MutableList<Book> = ArrayList()
        for (element in updatedBooksElements.select("article")) {
            val title = element.select("div > a > p.card-title.js-card-title").text()
            val description = element.select("div > a > p.card-text").text()
            val genres = element.select("div > a > ul.card-genres > li.card-genres-item").toList().map { it.text() }
            var coverUrl = element.select("a.px-1.py-1 > img").attr("src")
            val url = url + element.select("a").attr("href")
            val releaseYearScanner = Scanner(element.select("div > a > ul.card-numbers.m-0.p-0 > li:nth-child(2) > span").text()).useDelimiter("\\D+")
            val releaseYear = if (releaseYearScanner.hasNextInt()) releaseYearScanner.nextInt().toString() else "1970"
            val scanner = Scanner(element.select("ul > li > a").text()).useDelimiter("\\D+")
            val volume = scanner.nextInt()
            val chapter = scanner.nextInt()
            if (coverUrl.contains("data:image/svg+xml")) {
                coverUrl = element.select("a.px-1.py-1 > img").attr("data-src")
            }
            books.add(Book(title, "", description, coverUrl, releaseYear, Status.UNKNOWN,
                5.0f, url, genres, ArrayList(), volume, chapter, this.url
            ))
        }

        return books
    }

    override suspend fun fetchPopularBooks(): List<Book> {
        val document = httpGET("$url/manga?sortBy=popular") ?: return ArrayList()

        // Fetch only popular books
        val popularBooksElements = document.select("body > div.container.index-container > div > div > div > div.flex-container.row.align-items-start.justify-content-center.flex-wrap")
        val books: MutableList<Book> = ArrayList()
        for (element in popularBooksElements.select("article")) {
            val title = element.select("div > a > p.card-title.js-card-title").text()
            val description = element.select("div > a > p.card-text").text()
            val genres = element.select("div > a > ul.card-genres > li.card-genres-item").toList().map { it.text() }
            var coverUrl = element.select("a.px-1.py-1 > img").attr("src")
            val url = url + element.select("a").attr("href")
            val releaseYear = Scanner(element.select("div > a > ul.card-numbers.m-0.p-0 > li:nth-child(2) > span").text()).useDelimiter("\\D+").nextInt().toString()
            val rating = Scanner(element.select("div > a > ul.card-numbers.m-0.p-0 > li:nth-child(3) > span").text()).useDelimiter("\\D+").nextFloat()
            if (coverUrl.contains("data:image/svg+xml")) {
                coverUrl = element.select("a.px-1.py-1 > img").attr("data-src")
            }
            books.add(Book(title, "", description, coverUrl, releaseYear, Status.UNKNOWN,
                rating, url, genres, ArrayList(), 0, 0, this.url
            ))
        }

        return books
    }

    override fun explicitGenres(): List<String> {
        return listOf("Яой", "Бара", "Юри", "Эротика")
    }
}