package org.ireader.presentation.feature_library.presentation


import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.rememberPagerState
import org.ireader.core.utils.Constants
import org.ireader.core.utils.UiText
import org.ireader.core_ui.ui.EmptyScreen
import org.ireader.core_ui.ui.LoadingScreen
import org.ireader.presentation.feature_library.presentation.components.BottomTabComposable
import org.ireader.presentation.feature_library.presentation.components.LayoutComposable
import org.ireader.presentation.feature_library.presentation.viewmodel.LibraryViewModel
import org.ireader.presentation.ui.BookDetailScreenSpec
import org.ireader.presentation.ui.ReaderScreenSpec


@ExperimentalPagerApi
@ExperimentalAnimationApi
@OptIn(ExperimentalMaterialApi::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun LibraryScreen(
    modifier: Modifier = Modifier,
    navController: NavController = rememberNavController(),
    vm: LibraryViewModel = hiltViewModel(),
) {


    val coroutineScope = rememberCoroutineScope()


    val pagerState = rememberPagerState()
    val bottomSheetState =
        rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)

    val gridState = rememberLazyGridState()
    val lazyListState = rememberLazyListState()

    LaunchedEffect(key1 = true) {
        vm.getLibraryBooks()
    }

    ModalBottomSheetLayout(
        modifier = Modifier.systemBarsPadding(),
        sheetContent = {
            Box(modifier.defaultMinSize(minHeight = 1.dp)) {
                BottomTabComposable(
                    viewModel = vm,
                    pagerState = pagerState,
                    navController = navController,
                    scope = coroutineScope)

            }
        },
        sheetState = bottomSheetState,
        sheetBackgroundColor = MaterialTheme.colors.background,
        sheetContentColor = MaterialTheme.colors.onBackground,
    ) {
        Column(modifier = Modifier
            .fillMaxSize()
        ) {
            LibraryScreenTopBar(navController = navController,
                vm = vm,
                coroutineScope = coroutineScope,
                bottomSheetState = bottomSheetState)
            Box(modifier = Modifier
                .fillMaxSize()) {
                Crossfade(targetState = Pair(vm.isLoading, vm.isEmpty)) { (isLoading, isEmpty) ->
                    when {
                        isLoading -> LoadingScreen()
                        isEmpty -> EmptyScreen(UiText.DynamicString("There is no book is Library, you can add books in the Explore screen."))
                        else -> LayoutComposable(
                            books = vm.books,
                            layout = vm.layout,
                            navController = navController,
                            isLocal = true,
                            gridState = gridState,
                            scrollState = lazyListState,
                            goToLatestChapter = { book ->
                                navController.navigate(
                                    ReaderScreenSpec.buildRoute(
                                        bookId = book.id,
                                        sourceId = book.sourceId,
                                        chapterId = Constants.LAST_CHAPTER
                                    )
                                )
                            },
                            onBookTap = { book ->
                                navController.navigate(
                                    route = BookDetailScreenSpec.buildRoute(
                                        sourceId = book.sourceId,
                                        bookId = book.id)
                                )
                            },
                            histories = vm.histories
                        )
                    }
                }
            }
        }


    }

}





