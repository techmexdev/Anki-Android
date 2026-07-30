/*
 * Copyright (c) 2022 Ankitects Pty Ltd <http://apps.ankiweb.net>
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.ichi2.anki.libanki.stats

import anki.stats.BrainliftScoreSnapshotResponse
import anki.stats.BrainliftTopic
import com.ichi2.anki.libanki.Collection

// These take and return bytes that the frontend TypeScript code will encode/decode.
fun Collection.cardStatsRaw(input: ByteArray): ByteArray = backend.cardStatsRaw(input)

fun Collection.graphsRaw(input: ByteArray): ByteArray = backend.graphsRaw(input)

fun Collection.getGraphPreferencesRaw(): ByteArray {
    val prefs =
        backend
            .getGraphPreferences()
            .toBuilder()
            .setBrowserLinksSupported(false)
            .build()
    return prefs.toByteArray()
}

fun Collection.setGraphPreferencesRaw(input: ByteArray): ByteArray = backend.setGraphPreferencesRaw(input)

/**
 * Returns backend-owned Brainlift evidence derived from syncable collection state.
 *
 * Callers must invoke this off the main thread. The query is read-only and does
 * not implement any scoring or scheduling logic in Kotlin.
 */
fun Collection.brainliftScoreSnapshot(topics: Iterable<BrainliftTopic>): BrainliftScoreSnapshotResponse =
    backend.brainliftScoreSnapshot(topics)
