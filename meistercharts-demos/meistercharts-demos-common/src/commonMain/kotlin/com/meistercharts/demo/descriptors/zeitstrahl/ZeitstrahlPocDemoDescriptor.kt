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

import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.canvas.DirtyReason
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import com.meistercharts.demo.button
import com.meistercharts.demo.section
import io.ktor.client.plugins.HttpTimeout
import it.neckar.ktor.client.createHttpClient
import it.neckar.open.time.nowMillis
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlin.math.min
import kotlin.uuid.Uuid

/**
 * Proof of concept of zeitstrahl: the entries of the local MongoDB on a deep time axis from the Big Bang down to one day.
 * Needs `zeitstrahl-poc-server` on port 8095, see `internal/personal/zeitstrahl/README.md`.
 */
class ZeitstrahlPocDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("ba1a35b2-b0dd-491f-aaaf-821d477db5b8")
  override val name: String = "Zeitstrahl PoC"
  override val description: String = "Entries of the local zeitstrahl MongoDB from the Big Bang down to one day; needs zeitstrahl-poc-server on port 8095"
  override val category: DemoCategory = DemoCategory.ShowCase
  override val quality: DemoQuality = DemoQuality.Low
  override val variabilityType: VariabilityType = VariabilityType.Unstable

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      val viewport = DeepTimeViewport(presentSeconds = nowMillis() / 1000.0)
      val rowScale = RowScale()

      meistercharts {
        disableZoomAndTranslation()

        val httpClient = createHttpClient("ZeitstrahlPocDemo") {
          // The concept allows 50 ms per lane request; 5 s marks a hanging server as failure.
          install(HttpTimeout) {
            requestTimeoutMillis = 5_000
          }
        }
        val coroutineScope = CoroutineScope(SupervisorJob())
        onDispose {
          coroutineScope.cancel()
          httpClient.close()
        }
        val client = ZeitstrahlPocClient(httpClient, "http://localhost:8095")
        val repaint = { markAsDirty(DirtyReason.DataUpdated) }

        // The loader asks for twice the window, so the margins share the limit with the visible entries; the parts of an open node have a limit of their own.
        val limitPerLane = LaneLimit(min(LaneLimit.MaxValue, MaxEntriesOnScreen / lanes.size))
        val createLaneLoader = { lane: ZeitstrahlPocLane ->
          WindowLoader(coroutineScope, { request: WindowRequest<ZoomLevel> -> client.getLaneEntries(lane, request.from, request.to, request.key, limitPerLane) }, repaint)
        }
        val createPartsLoader = {
          WindowLoader(coroutineScope, { request: WindowRequest<EntryId> -> client.getParts(request.key, request.from, request.to, limitPerLane) }, repaint)
        }
        val sectionLoader = WindowLoader(coroutineScope, { request -> client.getSections(request.key, request.from, request.to) }, repaint)

        configure {
          layers.addClearBackground()
          layers.addLayer(DeepTimeViewportInteractionLayer(viewport, rowScale))
          layers.addLayer(DeepTimeAxisLayer(viewport))
          layers.addLayer(ZeitstrahlPocLanesLayer(viewport, rowScale, lanes, createLaneLoader, createPartsLoader))
          layers.addLayer(PeriodStripLayer(viewport, sectionLoader))
        }
      }

      section("Sprung")
      jumps(viewport).forEach { jump ->
        button(jump.label) {
          viewport.showWindow(jump.from, jump.to)
          markAsDirty(DirtyReason.UserInteraction)
        }
      }
    }
  }

  /** The examples of `KONZEPT.md` and further windows down to one day. */
  private fun jumps(viewport: DeepTimeViewport): List<ZeitstrahlPocJump> {
    fun day(year: Long, month: Int, day: Int): Double = ProlepticGregorianCalendar.secondsOf(CivilDate(year, month, day))
    fun millionYearsAgo(millionYears: Double): Double = DeepTimeFormat.secondsOfYearsBeforePresent(millionYears * 1e6)
    return listOf(
      ZeitstrahlPocJump("Urknall bis heute", DeepTimeViewport.BigBangSeconds, viewport.presentSeconds),
      ZeitstrahlPocJump("Kreide-Paläogen-Grenze", millionYearsAgo(73.0), millionYearsAgo(57.0)),
      ZeitstrahlPocJump("Letzte 10.000 Jahre", millionYearsAgo(0.01), viewport.presentSeconds),
      ZeitstrahlPocJump("Antike", day(-800, 1, 1), day(500, 1, 1)),
      ZeitstrahlPocJump("Dreißigjähriger Krieg", day(1600, 1, 1), day(1670, 1, 1)),
      ZeitstrahlPocJump("Französische Revolution", day(1785, 1, 1), day(1800, 1, 1)),
      ZeitstrahlPocJump("Terrorherrschaft", day(1793, 7, 1), day(1794, 9, 1)),
      ZeitstrahlPocJump("14. Juli 1789", day(1789, 7, 14), day(1789, 7, 15)),
      ZeitstrahlPocJump("20. Jahrhundert", day(1900, 1, 1), day(2000, 1, 1)),
      ZeitstrahlPocJump("20. Juli 1969", day(1969, 7, 20), day(1969, 7, 21)),
    )
  }

  private companion object {
    /** The limit of `KONZEPT.md` for the whole screen. */
    const val MaxEntriesOnScreen: Int = 500

    val lanes: List<ZeitstrahlPocLane> = listOf(
      ZeitstrahlPocLane(CategoryNode("cosmos"), "Kosmos"),
      ZeitstrahlPocLane(CategoryNode("earth.geology"), "Geologie"),
      ZeitstrahlPocLane(CategoryNode("earth.nature.fauna"), "Fauna"),
      ZeitstrahlPocLane(CategoryNode("earth.nature.fauna.humans.politics"), "Politik"),
      ZeitstrahlPocLane(CategoryNode("earth.nature.fauna.humans.science"), "Wissenschaft"),
      ZeitstrahlPocLane(CategoryNode("earth.nature.fauna.humans.technology"), "Technik"),
      ZeitstrahlPocLane(CategoryNode("earth.nature.fauna.humans.culture"), "Kultur"),
    )
  }
}
