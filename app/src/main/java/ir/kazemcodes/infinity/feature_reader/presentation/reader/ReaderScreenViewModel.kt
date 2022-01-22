package ir.kazemcodes.infinity.feature_reader.presentation.reader

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ActivityInfo
import android.view.WindowManager
import android.webkit.WebView
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.zhuinden.simplestack.ScopedServices
import ir.kazemcodes.infinity.core.data.network.models.Source
import ir.kazemcodes.infinity.core.domain.models.Book
import ir.kazemcodes.infinity.core.domain.models.Chapter
import ir.kazemcodes.infinity.core.domain.models.FontType
import ir.kazemcodes.infinity.core.domain.repository.LocalBookRepository
import ir.kazemcodes.infinity.core.domain.repository.LocalChapterRepository
import ir.kazemcodes.infinity.core.domain.repository.RemoteRepository
import ir.kazemcodes.infinity.core.domain.use_cases.preferences.PreferencesUseCase
import ir.kazemcodes.infinity.core.presentation.theme.fonts
import ir.kazemcodes.infinity.core.presentation.theme.readerScreenBackgroundColors
import ir.kazemcodes.infinity.core.utils.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.jsoup.Jsoup
import uy.kohesive.injekt.injectLazy


class ReaderScreenViewModel(
    private val preferencesUseCase: PreferencesUseCase,
    private val source: Source,
    private val bookName: String,
    private val chapterName: String,
    private val chapterIndex: Int,
    private val remoteRepository: RemoteRepository,
    private val localBookRepository: LocalBookRepository,
    private val localChapterRepository: LocalChapterRepository,
) : ScopedServices.Registered {
    private val _state =
        mutableStateOf(ReaderScreenState(source = source, currentChapterIndex = chapterIndex))

    val state: State<ReaderScreenState> = _state

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onServiceRegistered() {
        val initState = state.value
        _state.value = state.value.copy(
            book = initState.book.copy(bookName = bookName),
            chapter = initState.chapter.copy(title = chapterName, bookName = bookName),
            source = source
        )
        getContent(chapter = state.value.chapter)
        readPreferences()
        getLocalChapters()
        getLocalBookByName()
    }


    private fun readPreferences() {
        readSelectedFontState()
        readFontSize()
        readBackgroundColor()
        readFontHeight()
        readParagraphDistance()
        readParagraphIndent()
    }

    fun onEvent(event: ReaderEvent) {
        when (event) {
            is ReaderEvent.ChangeBrightness -> {
                saveBrightness(event.brightness, event.context)
            }
            is ReaderEvent.ChangeFontSize -> {
                saveFontSize(event.fontSizeEvent)
            }
            is ReaderEvent.ChangeFont -> {
                saveFont(event.fontType)
            }
            is ReaderEvent.GetContent -> {
                getContent(event.chapter)
            }
            is ReaderEvent.ToggleReaderMode -> {
                toggleReaderMode(event.enable)
            }
        }
    }


    private fun toggleReaderMode(enable: Boolean? = null) {
        _state.value =
            state.value.copy(isReaderModeEnable = enable ?: !state.value.isReaderModeEnable,
                isMainBottomModeEnable = true,
                isSettingModeEnable = false)
    }

    fun toggleSettingMode(enable: Boolean, returnToMain: Boolean? = null) {
        if (returnToMain.isNull()) {
            _state.value =
                state.value.copy(isSettingModeEnable = enable, isMainBottomModeEnable = false)

        } else {
            _state.value =
                state.value.copy(isSettingModeEnable = false, isMainBottomModeEnable = true)
        }
    }

    fun getContent(chapter: Chapter) {
        _state.value = state.value.copy(chapter = chapter)
        getReadingContentLocally()
        if (state.value.book.inLibrary) {
            toggleLastReadAndUpdateChapterContent(chapter)
        }
    }

    private fun getLocalChapters() {
        localChapterRepository.getChapterByName(bookName = state.value.book.bookName, source.name)
            .onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        if (!result.data.isNullOrEmpty()) {
                            _state.value = state.value.copy(
                                chapters = result.data,
                                drawerChapters = if (state.value.isReversed) result.data.reversed() else result.data,
                                isChapterLoaded = true
                            )
                        }
                    }
                    is Resource.Error -> {
                    }
                    is Resource.Loading -> {
                    }
                }
            }.launchIn(coroutineScope)
    }

    private fun getReadingContentLocally() {
        localChapterRepository.getChapterByChapter(bookName = state.value.chapter.bookName ?: "",
            source = source.name,
            chapterTitle = state.value.chapter.title)
            .onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        if (result.data != null ) {
                            _state.value = state.value.copy(
                                chapter = result.data,
                                isLoading = false,
                                isLoaded = true,
                                error = ""
                            )
                            toggleLastReadAndUpdateChapterContent(state.value.chapter)
                            if (state.value.chapter.content.joinToString().isBlank() ) {
                                getReadingContentRemotely()
                            }
                        }
                    }
                    is Resource.Error -> {
                        getReadingContentRemotely()
                        _state.value =
                            state.value.copy(
                                error = result.message ?: "An Unknown Error Occurred",
                                isLoading = false,
                                isLoaded = false,
                            )
                    }
                    is Resource.Loading -> {
                        _state.value = state.value.copy(
                            isLoading = true,
                            error = "",
                            isLoaded = false,
                        )
                    }
                }

            }.launchIn(coroutineScope)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getFromWebView() {
        val webView by injectLazy<WebView>()
        coroutineScope.launch {
            _eventFlow.emit(UiEvent.ShowSnackbar(
                uiText = UiText.DynamicString("Trying to fetch chapter's content")
            ))

            val chapter = source.contentFromElementParse(Jsoup.parse(webView.getHtml()))
            if (!chapter.content.isNullOrEmpty() && state.value.isBookLoaded && state.value.isChapterLoaded && webView.originalUrl == state.value.chapter.link ) {
                _state.value = state.value.copy(isLoading = false, error = "", chapter = state.value.chapter.copy(content = chapter.content))
                toggleLastReadAndUpdateChapterContent(state.value.chapter.copy(content = chapter.content))

                _eventFlow.emit(UiEvent.ShowSnackbar(
                    uiText = UiText.DynamicString("${state.value.chapter.title} of ${state.value.chapter.bookName} was Fetched")
                ))
            } else {
                _eventFlow.emit(UiEvent.ShowSnackbar(
                    uiText = UiText.DynamicString("Failed to to get the content")
                ))
            }
        }

    }

    fun getReadingContentRemotely() {
            remoteRepository.getRemoteReadingContentUseCase(state.value.chapter, source = source)
                .onEach { result ->
                    when (result) {
                        is Resource.Success -> {
                            if (result.data != null) {
                                _state.value = state.value
                                    .copy(
                                        chapter = state.value.chapter.copy(content = result.data.content),
                                        isLoading = false,
                                        error = "",
                                        isLoaded = true,
                                    )

                                toggleLastReadAndUpdateChapterContent(state.value.chapter)
                            }
                        }
                        is Resource.Error -> {
                            _state.value =
                                state.value.copy(
                                    error = result.message ?: "An Unknown Error Occurred",
                                    isLoading = false,
                                    isLoaded = false,
                                )
                        }
                        is Resource.Loading -> {
                            _state.value = state.value.copy(
                                isLoading = true, error = "",
                                isLoaded = false,
                            )
                        }
                    }
                }.launchIn(coroutineScope)
    }
    @Suppress()
    private fun getLocalBookByName() {
        localBookRepository.getLocalBookByName(state.value.book.bookName, sourceName = source.name).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    if (result.data != null && result.data != Book.create()) {
                        _state.value = state.value.copy(
                            book = result.data,
                            isBookLoaded = true
                        )
                        localBookRepository.updateLocalBook(book = result.data.copy(lastRead = System.currentTimeMillis(), unread = !result.data.unread))
                    }
                }
                is Resource.Error -> {
                }
                is Resource.Loading -> {
                }
            }
        }.launchIn(coroutineScope)
    }

    private fun toggleLastReadAndUpdateChapterContent(chapter: Chapter) {
        coroutineScope.launch(Dispatchers.IO) {
            localChapterRepository.updateChapter(state.value.chapter)
            localChapterRepository.deleteLastReadChapter(state.value.book.bookName, source.name)
            localChapterRepository.setLastReadChapter(state.value.book.bookName,
                chapter.title,
                source = source.name)
        }
    }

    fun reverseChapters() {
        _state.value = state.value.copy(isReversed = !state.value.isReversed,
            drawerChapters = state.value.drawerChapters.reversed())
    }


    private fun readSelectedFontState() {
        _state.value = state.value.copy(font = preferencesUseCase.readSelectedFontStateUseCase())
    }

    fun readBrightness(context: Context) {
        val brightness = preferencesUseCase.readBrightnessStateUseCase()
        val activity = context.findAppCompatAcivity()!!
        val window = activity.window
        val layoutParams: WindowManager.LayoutParams = window.attributes
        layoutParams.screenBrightness = brightness
        window.attributes = layoutParams
        _state.value = state.value.copy(brightness = brightness)
    }

    private fun readFontSize() {
        _state.value = state.value.copy(fontSize = preferencesUseCase.readFontSizeStateUseCase())
    }

    private fun readParagraphDistance() {
        _state.value =
            state.value.copy(distanceBetweenParagraphs = preferencesUseCase.readParagraphDistanceUseCase())
    }

    private fun readParagraphIndent() {
        _state.value =
            state.value.copy(paragraphsIndent = preferencesUseCase.readParagraphIndentUseCase())
    }

    private fun readFontHeight() {
        _state.value = state.value.copy(lineHeight = preferencesUseCase.readFontHeightUseCase())
    }

    private fun readBackgroundColor() {
        val color = readerScreenBackgroundColors[preferencesUseCase.getBackgroundColorUseCase()]
        _state.value =
            state.value.copy(backgroundColor = color.color, textColor = color.onTextColor)
    }


    @SuppressLint("SourceLockedOrientationActivity")
    fun readOrientation(context: Context) {
        val activity = context.findAppCompatAcivity()!!
        when (preferencesUseCase.readOrientationUseCase()) {
            0 -> {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                _state.value = state.value.copy(orientation = Orientation.Portrait)
            }
            1 -> {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                _state.value = state.value.copy(orientation = Orientation.Landscape)
            }
        }
    }

    fun changeBackgroundColor(colorIndex: Int) {
        val color = readerScreenBackgroundColors[colorIndex]
        _state.value =
            state.value.copy(backgroundColor = color.color, textColor = color.onTextColor)
        preferencesUseCase.setBackgroundColorUseCase(colorIndex)
    }

    @SuppressLint("SourceLockedOrientationActivity")
    fun saveOrientation(context: Context) {
        val activity = context.findAppCompatAcivity()!!
        when (state.value.orientation) {
            is Orientation.Landscape -> {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                _state.value = state.value.copy(orientation = Orientation.Portrait)
                preferencesUseCase.saveOrientationUseCase(Orientation.Portrait.index)
            }
            is Orientation.Portrait -> {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                _state.value = state.value.copy(orientation = Orientation.Landscape)
                preferencesUseCase.saveOrientationUseCase(Orientation.Landscape.index)
            }
        }
    }

    fun saveFontHeight(isIncreased: Boolean) {
        val currentFontHeight = state.value.lineHeight
        if (isIncreased) {
            preferencesUseCase.saveFontHeightUseCase(currentFontHeight + 1)
            _state.value = state.value.copy(lineHeight = currentFontHeight + 1)

        } else if (currentFontHeight > 20 && !isIncreased) {
            preferencesUseCase.saveFontHeightUseCase(currentFontHeight - 1)
            _state.value = state.value.copy(lineHeight = currentFontHeight - 1)
        }
    }

    fun saveParagraphDistance(isIncreased: Boolean) {
        val currentDistance = state.value.distanceBetweenParagraphs
        if (isIncreased) {
            preferencesUseCase.saveParagraphDistanceUseCase(currentDistance + 1)
            _state.value = state.value.copy(distanceBetweenParagraphs = currentDistance + 1)

        } else if (currentDistance > 1 && !isIncreased) {
            preferencesUseCase.saveParagraphDistanceUseCase(currentDistance - 1)
            _state.value = state.value.copy(distanceBetweenParagraphs = currentDistance - 1)
        }
    }

    fun saveParagraphIndent(isIncreased: Boolean) {
        val paragraphsIndent = state.value.paragraphsIndent
        if (isIncreased) {
            preferencesUseCase.saveParagraphIndentUseCase(paragraphsIndent + 1)
            _state.value = state.value.copy(paragraphsIndent = paragraphsIndent + 1)

        } else if (paragraphsIndent > 1 && !isIncreased) {
            preferencesUseCase.saveParagraphIndentUseCase(paragraphsIndent - 1)
            _state.value = state.value.copy(paragraphsIndent = paragraphsIndent - 1)
        }
    }

    private fun saveFontSize(event: FontSizeEvent) {
        if (event == FontSizeEvent.Increase) {
            _state.value = state.value.copy(fontSize = state.value.fontSize + 1)
            preferencesUseCase.saveFontSizeStateUseCase(state.value.fontSize)
        } else {
            if (state.value.fontSize > 0) {
                _state.value = state.value.copy(fontSize = state.value.fontSize - 1)
                preferencesUseCase.saveFontSizeStateUseCase(state.value.fontSize)
            }
        }
    }

    private fun saveFont(fontType: FontType) {
        _state.value = state.value.copy(font = fontType)
        preferencesUseCase.saveSelectedFontStateUseCase(fonts.indexOf(fontType))
    }

    private fun saveBrightness(brightness: Float, context: Context) {
        val activity = context.findAppCompatAcivity()!!
        val window = activity.window
        _state.value = state.value.copy(brightness = brightness)
        val layoutParams: WindowManager.LayoutParams = window.attributes
        layoutParams.screenBrightness = brightness
        window.attributes = layoutParams

        preferencesUseCase.saveBrightnessStateUseCase(brightness)
    }


    /**
     * need a index, there is no need to confuse the index because the list reversed
     */
    fun updateChapterSliderIndex(index: Int) {
        _state.value = state.value.copy(currentChapterIndex = getIndexOfChapter(index))
    }

    /**
     * get the index pf chapter based on the reversed state
     */
    fun getIndexOfChapter(index: Int): Int {
        return if (state.value.isReversed) ((state.value.drawerChapters.size - 1) - index) else index
    }

    override fun onServiceUnregistered() {
        coroutineScope.cancel()
    }
}

