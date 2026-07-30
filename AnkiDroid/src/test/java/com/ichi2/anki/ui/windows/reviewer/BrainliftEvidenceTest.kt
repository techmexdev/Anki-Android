// SPDX-License-Identifier: GPL-3.0-or-later

package com.ichi2.anki.ui.windows.reviewer

import anki.stats.BrainliftEvidenceScore
import anki.stats.BrainliftScoreRange
import anki.stats.BrainliftScoreSnapshotResponse
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BrainliftEvidenceTest {
    @Test
    fun `presenter keeps memory performance and readiness separate`() {
        val snapshot =
            BrainliftScoreSnapshotResponse
                .newBuilder()
                .setMemory(availableProbability(0.82, 0.75, 0.88))
                .setPerformance(availableProbability(0.61, 0.50, 0.71))
                .setReadiness(availableMcat(508.0, 503.0, 512.0))
                .build()

        val panel = BrainliftEvidencePresenter.present(snapshot)

        assertEquals(listOf(BrainliftSignal.MEMORY, BrainliftSignal.PERFORMANCE, BrainliftSignal.READINESS), panel.rows.map { it.signal })
        assertEquals("82%", panel.rows[0].value)
        assertEquals("61%", panel.rows[1].value)
        assertEquals("508", panel.rows[2].value)
        assertEquals("503-512", panel.rows[2].range)
        assertFalse(panel.backendUnavailable)
    }

    @Test
    fun `readiness abstention preserves the backend reason`() {
        val readiness =
            BrainliftEvidenceScore
                .newBuilder()
                .setAvailability(BrainliftEvidenceScore.Availability.ABSTAINED)
                .setCoverage(0.43)
                .setConfidence(BrainliftEvidenceScore.Confidence.NONE)
                .setRatedReviews(4)
                .addReasons("performance_unavailable")
                .build()
        val snapshot =
            BrainliftScoreSnapshotResponse
                .newBuilder()
                .setReadiness(readiness)
                .build()

        val readinessRow = BrainliftEvidencePresenter.present(snapshot).rows[2]

        assertFalse(readinessRow.available)
        assertEquals("Not enough evidence", readinessRow.value)
        assertEquals("Waiting for held-out Performance evidence", readinessRow.detail)
        assertEquals("43%", readinessRow.coverage)
    }

    @Test
    fun `state holder converts a backend error to safe unavailable evidence`() =
        runTest {
            val holder =
                BrainliftEvidenceStateHolder(
                    BrainliftEvidenceLoader { error("backend unavailable") },
                )

            holder.refresh()

            assertTrue(holder.state.value.backendUnavailable)
            assertEquals(3, holder.state.value.rows.size)
            assertTrue(
                holder.state.value.rows
                    .all { it.value == "Evidence temporarily unavailable" },
            )
            assertTrue(
                holder.state.value.rows
                    .all { it.detail == "Study and review remain available." },
            )
        }

    private fun availableProbability(
        estimate: Double,
        lower: Double,
        upper: Double,
    ): BrainliftEvidenceScore = availableScore(BrainliftEvidenceScore.Scale.PROBABILITY, estimate, lower, upper)

    private fun availableMcat(
        estimate: Double,
        lower: Double,
        upper: Double,
    ): BrainliftEvidenceScore = availableScore(BrainliftEvidenceScore.Scale.MCAT, estimate, lower, upper)

    private fun availableScore(
        scale: BrainliftEvidenceScore.Scale,
        estimate: Double,
        lower: Double,
        upper: Double,
    ): BrainliftEvidenceScore =
        BrainliftEvidenceScore
            .newBuilder()
            .setAvailability(BrainliftEvidenceScore.Availability.AVAILABLE)
            .setScale(scale)
            .setEstimate(estimate)
            .setRange(
                BrainliftScoreRange
                    .newBuilder()
                    .setLower(lower)
                    .setUpper(upper),
            ).setCoverage(0.86)
            .setConfidence(BrainliftEvidenceScore.Confidence.HIGH)
            .setUpdatedAtSecs(1_785_364_800)
            .setRatedReviews(20)
            .setSuccessfulReviews(18)
            .build()
}
