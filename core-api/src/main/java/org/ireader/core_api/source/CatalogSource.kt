

package org.ireader.core_api.source

import org.ireader.core_api.source.model.Command
import org.ireader.core_api.source.model.CommandList
import org.ireader.core_api.source.model.FilterList
import org.ireader.core_api.source.model.Listing
import org.ireader.core_api.source.model.MangasPageInfo

interface CatalogSource : Source {

    override val lang: String

    suspend fun getMangaList(sort: Listing?, page: Int): MangasPageInfo
    suspend fun getMangaList(sort: Listing?, page: Int,commands: List<Command<*>>): MangasPageInfo

    suspend fun getMangaList(filters: FilterList, page: Int): MangasPageInfo
    suspend fun getMangaList(filters: FilterList, page: Int,commands: List<Command<*>>): MangasPageInfo

    fun getListings(): List<Listing>

    fun getFilters(): FilterList

    fun getCommands(): CommandList
}
