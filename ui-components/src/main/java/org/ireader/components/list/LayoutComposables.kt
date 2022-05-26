package org.ireader.components.list

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.ireader.common_models.LayoutType
import org.ireader.common_models.entities.BookItem
import org.ireader.components.list.layouts.CompactGridLayoutComposable
import org.ireader.components.list.layouts.GridLayoutComposable
import org.ireader.components.list.layouts.LinearListDisplay
import org.ireader.core_api.source.Source

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LayoutComposable(
    books: List<BookItem> = emptyList(),
    onClick: (book: BookItem) -> Unit,
    onLongClick: (BookItem) -> Unit = {},
    selection: List<Long> = emptyList<Long>(),
    layout: LayoutType,
    scrollState: LazyListState,
    gridState: LazyGridState,
    source: Source? = null,
    isLocal: Boolean,
    goToLatestChapter: (book: BookItem) -> Unit = {},
    isLoading: Boolean = false,
    showGoToLastChapterBadge: Boolean = false,
    showUnreadBadge: Boolean = false,
    showReadBadge: Boolean = false,
    showInLibraryBadge:Boolean = false
) {
        when (layout) {
            is LayoutType.GridLayout -> {
                GridLayoutComposable(
                    books = books,
                    onClick = { book ->
                        onClick(book)
                    },
                    selection = selection,
                    onLongClick = { onLongClick(it) },
                    scrollState = gridState,
                    goToLatestChapter = { goToLatestChapter(it) },
                    isLoading = isLoading,
                    showGoToLastChapterBadge = showGoToLastChapterBadge,
                    modifier = Modifier,
                    showInLibraryBadge = showInLibraryBadge,
                    showReadBadge = showReadBadge,
                    showUnreadBadge = showUnreadBadge,
                )
            }
            is LayoutType.ListLayout -> {
                LinearListDisplay(
                    books = books, onClick = { book ->
                        onClick(book)
                    }, scrollState = scrollState,
                    isLocal = isLocal,
                    selection = selection,
                    onLongClick = { onLongClick(it) },
                    goToLatestChapter = { goToLatestChapter(it) },
                    isLoading = isLoading,

                    showGoToLastChapterBadge = showGoToLastChapterBadge,
                    showInLibraryBadge = showInLibraryBadge,
                    showReadBadge = showReadBadge,
                    showUnreadBadge = showUnreadBadge,
                )
            }
            is LayoutType.CompactGrid -> {
                CompactGridLayoutComposable(
                    books = books,
                    onClick = { book ->
                        onClick(book)
                    }, scrollState = gridState,
                    isLocal = isLocal,
                    selection = selection,
                    onLongClick = { onLongClick(it) },
                    goToLatestChapter = { goToLatestChapter(it) },
                    isLoading = isLoading,
                    showGoToLastChapterBadge = showGoToLastChapterBadge,
                    modifier = Modifier,
                    showInLibraryBadge = showInLibraryBadge,
                    showReadBadge = showReadBadge,
                    showUnreadBadge = showUnreadBadge,
                )
            }
        }
}
