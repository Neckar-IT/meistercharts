/*
 * Copyright 2023 Neckar IT GmbH, Mössingen, Germany
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.meistercharts.demo.descriptors.zeitstrahl

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

/**
 * Reads lanes, the parts of an entry and periodization sections from `zeitstrahl-poc-server` (`internal/personal/zeitstrahl/zeitstrahl-poc-server`).
 */
class ZeitstrahlPocClient(
  private val httpClient: HttpClient,
  private val baseUrl: String,
) {
  suspend fun getLaneEntries(lane: ZeitstrahlPocLane, from: Double, to: Double, zoomLevel: ZoomLevel, limit: LaneLimit): List<ZeitstrahlPocEntry> {
    return httpClient.get("$baseUrl/api/lanes/${lane.node}/entries") {
      parameter("from", from)
      parameter("to", to)
      parameter("z", zoomLevel.value)
      parameter("limit", limit.value)
    }.body<List<ZeitstrahlPocEntry>>()
  }

  /** The direct parts of [entry] overlapping [from] to [to], most important first. */
  suspend fun getParts(entry: EntryId, from: Double, to: Double, limit: LaneLimit): List<ZeitstrahlPocEntry> {
    return httpClient.get("$baseUrl/api/entries/$entry/parts") {
      parameter("from", from)
      parameter("to", to)
      parameter("limit", limit.value)
    }.body<List<ZeitstrahlPocEntry>>()
  }

  suspend fun getSections(periodizationId: PeriodizationId, from: Double, to: Double): List<ZeitstrahlPocSection> {
    return httpClient.get("$baseUrl/api/periodizations/$periodizationId/sections") {
      parameter("from", from)
      parameter("to", to)
    }.body<List<ZeitstrahlPocSection>>()
  }
}
