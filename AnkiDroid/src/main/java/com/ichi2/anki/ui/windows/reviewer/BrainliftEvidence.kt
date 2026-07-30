// SPDX-License-Identifier: GPL-3.0-or-later

package com.ichi2.anki.ui.windows.reviewer

import anki.stats.BrainliftEvidenceScore
import anki.stats.BrainliftScoreSnapshotResponse
import anki.stats.BrainliftTopic
import anki.stats.brainliftTopic
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

val DEFAULT_MCAT_TOPICS: List<BrainliftTopic> =
    listOf(
        "Biochemistry" to "mcat::biochemistry",
        "Biology" to "mcat::biology",
        "General Chemistry" to "mcat::general-chemistry",
        "Organic Chemistry" to "mcat::organic-chemistry",
        "Physics" to "mcat::physics",
        "Psychology and Sociology" to "mcat::psychology-sociology",
        "Critical Analysis and Reasoning" to "mcat::cars",
    ).map { (topicName, topicTag) ->
        brainliftTopic {
            name = topicName
            tag = topicTag
        }
    }

fun interface BrainliftEvidenceLoader {
    suspend fun load(): BrainliftScoreSnapshotResponse
}

enum class BrainliftSignal {
    MEMORY,
    PERFORMANCE,
    READINESS,
}

data class BrainliftScoreRow(
    val signal: BrainliftSignal,
    val available: Boolean,
    val value: String,
    val range: String?,
    val detail: String,
    val coverage: String,
    val confidence: String,
    val updated: String,
)

data class BrainliftPanelState(
    val rows: List<BrainliftScoreRow>,
    val backendUnavailable: Boolean = false,
) {
    companion object {
        fun loading(): BrainliftPanelState =
            BrainliftPanelState(
                rows = BrainliftSignal.entries.map { BrainliftEvidencePresenter.loading(it) },
            )
    }
}

class BrainliftEvidenceStateHolder(
    private val loader: BrainliftEvidenceLoader,
) {
    private val mutableState = MutableStateFlow(BrainliftPanelState.loading())
    val state: StateFlow<BrainliftPanelState> = mutableState.asStateFlow()

    suspend fun refresh() {
        mutableState.value =
            try {
                BrainliftEvidencePresenter.present(loader.load())
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                BrainliftEvidencePresenter.backendUnavailable()
            }
    }
}

object BrainliftEvidencePresenter {
    private val updatedFormatter =
        DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm 'UTC'")
            .withZone(ZoneOffset.UTC)

    fun present(snapshot: BrainliftScoreSnapshotResponse): BrainliftPanelState =
        BrainliftPanelState(
            rows =
                listOf(
                    scoreRow(BrainliftSignal.MEMORY, snapshot.memory),
                    scoreRow(BrainliftSignal.PERFORMANCE, snapshot.performance),
                    scoreRow(BrainliftSignal.READINESS, snapshot.readiness),
                ),
        )

    fun backendUnavailable(): BrainliftPanelState =
        BrainliftPanelState(
            rows =
                BrainliftSignal.entries.map { signal ->
                    BrainliftScoreRow(
                        signal = signal,
                        available = false,
                        value = "Evidence temporarily unavailable",
                        range = null,
                        detail = "Study and review remain available.",
                        coverage = "0%",
                        confidence = "none",
                        updated = "No update",
                    )
                },
            backendUnavailable = true,
        )

    fun loading(signal: BrainliftSignal): BrainliftScoreRow =
        BrainliftScoreRow(
            signal = signal,
            available = false,
            value = "Loading evidence",
            range = null,
            detail = "Collection-wide evidence is loading.",
            coverage = "0%",
            confidence = "none",
            updated = "No update",
        )

    private fun scoreRow(
        signal: BrainliftSignal,
        score: BrainliftEvidenceScore,
    ): BrainliftScoreRow {
        val available = score.availability == BrainliftEvidenceScore.Availability.AVAILABLE
        val isMcat = score.scale == BrainliftEvidenceScore.Scale.MCAT
        return BrainliftScoreRow(
            signal = signal,
            available = available,
            value =
                if (available) {
                    if (isMcat) score.estimate.roundToInt().toString() else percent(score.estimate)
                } else {
                    "Not enough evidence"
                },
            range =
                if (available) {
                    if (isMcat) {
                        "${score.range.lower.roundToInt()}-${score.range.upper.roundToInt()}"
                    } else {
                        "${(score.range.lower * 100).roundToInt()}-${(score.range.upper * 100).roundToInt()}%"
                    }
                } else {
                    null
                },
            detail =
                if (available) {
                    availableDetail(score)
                } else {
                    abstentionDetail(score)
                },
            coverage = percent(score.coverage),
            confidence = score.confidence.name.lowercase(),
            updated = updatedText(score.updatedAtSecs),
        )
    }

    private fun availableDetail(score: BrainliftEvidenceScore): String =
        (
            listOf("${score.successfulReviews}/${score.ratedReviews} successful reviews") +
                score.reasonsList.map { reasonText(score, it) }
        ).joinToString(" · ")

    private fun abstentionDetail(score: BrainliftEvidenceScore): String {
        val reasons = score.reasonsList.map { reasonText(score, it) }
        return if (reasons.isEmpty()) {
            "Waiting for enough rated review evidence"
        } else {
            reasons.joinToString(" · ")
        }
    }

    private fun reasonText(
        score: BrainliftEvidenceScore,
        reason: String,
    ): String =
        when {
            reason == "no_qualifying_reviews" -> "No qualifying rated reviews yet"
            reason.startsWith("minimum_rated_reviews_not_met:") ->
                "Waiting for rated reviews (${score.ratedReviews}/${reason.substringAfter(":")})"
            reason.startsWith("joint_topic_coverage_below:") -> {
                val minimum = reason.substringAfter(":").toDoubleOrNull() ?: 0.0
                "Waiting for joint topic coverage (${percent(score.coverage)}/${percent(minimum)})"
            }
            reason == "memory_unavailable" -> "Waiting for Memory evidence"
            reason == "performance_unavailable" -> "Waiting for held-out Performance evidence"
            reason == "memory_from_ordinary_rated_reviews" -> "Source: ordinary rated reviews"
            reason == "performance_from_held_out_rated_reviews" -> "Source: held-out rated reviews"
            reason == "readiness_combines_memory_and_held_out_performance" ->
                "Source: Memory and held-out Performance"
            else -> reason
        }

    private fun percent(value: Double): String = "${(value * 100).roundToInt()}%"

    private fun updatedText(updatedAtSecs: Long): String =
        if (updatedAtSecs == 0L) {
            "No rated reviews yet"
        } else {
            "Updated ${updatedFormatter.format(Instant.ofEpochSecond(updatedAtSecs))}"
        }
}
