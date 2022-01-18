package ir.kazemcodes.infinity.core.data.network.models

import ir.kazemcodes.infinity.core.domain.models.Book
import ir.kazemcodes.infinity.core.domain.models.Chapter


interface Source {

    val id: Long

    val lang: String

    val name: String

    val baseUrl: String


    val supportsLatest: Boolean

    val supportsMostPopular: Boolean

    val supportSearch: Boolean

    /**
     * This is false when the content is viewed in web view not in  the app
     */
    val supportContentAppView: Boolean


    /**
     * Returns a page with a list of latest books updates and weather is has next page or not.
     *
     * @param page the page number to retrieve.
     */
    suspend fun fetchLatest(page: Int): BooksPage


    /**
     * Returns a page with a list of popular books updates and weather is has next page or not.
     *
     * @param page the page number to retrieve.
     */
    suspend fun fetchPopular(page: Int): BooksPage

    /**
     * Returns a page with a list of manga.
     *
     * @param page the page number to retrieve.
     * @param query the search query.
     */
    suspend fun fetchSearch(page: Int, query: String): BooksPage


    /**
     * Returns a page with a list of latest manga updates and weather is has next page or not.
     *
     * @param chapter the chapter that it contents is going to be retrieve.
     */
    suspend fun fetchContent(chapter: Chapter): ChapterPage


    /**
     * Returns a book with Complete information.
     *
     * @param book a book that contain need to be contain a bookName and a link
     */
    suspend fun fetchBook(book: Book): BookPage

    /**
     * Returns a list of chapter.
     *
     * @param book a book that contain need to be contain a bookName and a link
     */
    suspend fun fetchChapters(book: Book, page: Int): ChaptersPage


}

