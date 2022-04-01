package org.ireader.domain.use_cases.local

import org.ireader.domain.use_cases.local.book_usecases.*
import org.ireader.infinity.core.domain.use_cases.local.book_usecases.GetBooksByQueryPagingSource
import javax.inject.Inject

data class LocalGetBookUseCases @Inject constructor(
    val subscribeBookById: SubscribeBookById,
    val findBookById: FindBookById,
    val SubscribeInLibraryBooks: SubscribeInLibraryBooks,
    val findAllInLibraryBooks: FindAllInLibraryBooks,
    val getBooksByQueryByPagination: GetBooksByQueryByPagination,
    val getBooksByQueryPagingSource: GetBooksByQueryPagingSource,
    val getAllExploredBookPagingSource: GetAllExploredBookPagingSource,
    val getAllExploredBookPagingData: GetAllExploredBookPagingData,
    val findBookByKey: FindBookByKey,
    val findBooksByKey: FindBooksByKey,
)















