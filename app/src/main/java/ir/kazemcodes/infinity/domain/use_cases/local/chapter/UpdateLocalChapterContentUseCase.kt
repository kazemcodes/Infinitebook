package ir.kazemcodes.infinity.domain.use_cases.local.chapter

import ir.kazemcodes.infinity.domain.models.remote.Chapter
import ir.kazemcodes.infinity.domain.repository.Repository
import ir.kazemcodes.infinity.domain.utils.InvalidBookException
import timber.log.Timber
import javax.inject.Inject

class UpdateLocalChapterContentUseCase @Inject constructor(
    private val repository: Repository,
) {

    @Throws(InvalidBookException::class)
    suspend operator fun invoke(chapter: Chapter) {
        try {
            Timber.d("Timber: UpdateLocalChapterContentUseCase was Called")
            repository.localChapterRepository.updateChapter(
                chapterEntity = chapter.toChapterEntity()
            )
            Timber.d("Timber: GetLocalBookByNameUseCase was Finished Successfully")
        } catch (e: Exception) {
            Timber.e("Timber: InsertLocalChapterContentUseCase: " + e.localizedMessage)
        }

    }
}