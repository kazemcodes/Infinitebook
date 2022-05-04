

package org.ireader.core_api.source

import org.ireader.core_api.source.model.ChapterInfo
import org.ireader.core_api.source.model.Command
import org.ireader.core_api.source.model.MangaInfo
import org.ireader.core_api.source.model.Page

/**
 * A basic interface for creating a source. It could be an online source, a local source, etc...
 */
interface Source {

    /**
     * Id for the source. Must be unique.
     */
    val id: Long

    /**
     * Name of the source.
     */
    val name: String

    // TODO remove CatalogSource?
    val lang: String

    /**
     * Returns an observable with the updated details for a manga.
     *
     * @param manga the manga to update.
     */
    @Deprecated("This api is deprecated")
    suspend fun getMangaDetails(manga: MangaInfo): MangaInfo

    /**
     * Returns an observable with the updated details for a manga.
     *
     * @param manga the manga to update.
     */
    suspend fun getMangaDetails(manga: MangaInfo,commands: List<Command<*>>): MangaInfo

    /**
     * Returns an observable with all the available chapters for a manga.
     *
     * @param manga the manga to update.
     * @param commands the list of commands
     */
    suspend fun getChapterList(manga: MangaInfo,commands: List<Command<*>>): List<ChapterInfo>


    /**
     * Returns an observable with all the available chapters for a manga.
     *
     * @param manga the manga to update.
     * @param commands the list of commands
     */
    @Deprecated("This api is deprecated")
    suspend fun getChapterList(manga: MangaInfo): List<ChapterInfo>

    /**
     * Returns an observable with the list of pages a chapter has.
     *
     * @param chapter the chapter.
     */
    @Deprecated("This api is deprecated")
    suspend fun getPageList(chapter: ChapterInfo): List<Page>

    /**
     * Returns an observable with the list of pages a chapter has.
     *
     * @param chapter the chapter.
     */
    suspend fun getPageList(chapter: ChapterInfo,commands: List<Command<*>>): List<Page>

    /**
     * Returns a regex used to determine chapter information.
     *
     * @return empty regex will run default parser.
     */
    fun getRegex(): Regex {
        return Regex("")
    }

}
