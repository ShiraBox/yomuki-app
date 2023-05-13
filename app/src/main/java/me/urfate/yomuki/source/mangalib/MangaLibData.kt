package me.urfate.yomuki.source.mangalib

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SearchBookData(
    @SerialName("rus_name") val name: String,
    @SerialName("name") val altName: String,
    @SerialName("covers") val covers: CoversData,
    @SerialName("created_at") val issueYear: String,
    @SerialName("href") val url: String,

)

@Serializable
data class BookWrapperData(
    @SerialName("chapters") val chapters: ChaptersData
)

@Serializable
data class BookData(
    @SerialName("id") var id: Int,
    @SerialName("rus_name") var name: String,
    @SerialName("eng_name") var altName: String,
    @SerialName("slug") var slug: String,
    @SerialName("releaseDate") var releaseDate: String,
    @SerialName("summary") var description: String,
    @SerialName("rate_avg") var rateAvg: String,
    @SerialName("categories") var categories: List<CategoryData>,
    @SerialName("status") var status: StatusData,
    @SerialName("coverImage") var cover: String
)

@Serializable
data class StatusData(
    val id: Int
)

@Serializable
data class CategoryData(
    val name: String
)

@Serializable
data class ChaptersData(
    val list: List<ChapterData>
)

@Serializable
data class ChapterData(
    @SerialName("chapter_id") val id: Int,
    @SerialName("chapter_slug") val slug: String,
    @SerialName("chapter_name") val name: String = "",
    @SerialName("chapter_number") val number: String = "",
    @SerialName("chapter_volume") val volume: Int = 0,
    @SerialName("chapter_created_at") val createdAt: String,
)

@Serializable
data class PagesChapterData(
    @SerialName("page") val page: Int,
    @SerialName("media") val media: PagesMediaData,
    @SerialName("current") val current: PagesCurrentData,
    @SerialName("img") val img: PagesImgData,
    @SerialName("servers") val servers: PageServersData
)

@Serializable
data class PagesMediaData(
    @SerialName("id") val id: Int,
    @SerialName("slug") val slug: String,
    @SerialName("type") val type: Int
)

@Serializable
data class PagesImgData(
    @SerialName("url") val url: String,
    @SerialName("server") val server: String
)

@Serializable
data class PagesCurrentData(
    @SerialName("id") val id: Int,
    @SerialName("volume") val volume: Int,
    @SerialName("number") val number: String,
    @SerialName("index") val index: Int
)

@Serializable
data class PageServersData (
    @SerialName("main") val main: String,
    @SerialName("secondary") val secondary: String,
    @SerialName("compress") val compress: String,
    @SerialName("fourth") val fourth: String
)

@Serializable
data class PagesData(
    @SerialName("p") val page: Int,
    @SerialName("u") val img: String
)

@Serializable
data class CoversData(
    val default: String
)

@Serializable
data class WrapperData<T>(
    val items: T
)

@Serializable
data class WrapperListData<T>(
    val data: List<T>
)