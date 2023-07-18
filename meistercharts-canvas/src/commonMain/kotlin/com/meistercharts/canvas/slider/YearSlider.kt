/**
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
package com.meistercharts.canvas.slider

import it.neckar.open.kotlin.lang.abs
import it.neckar.open.provider.DoublesProvider
import it.neckar.open.provider.MultiProvider
import it.neckar.open.provider.SizedProvider
import it.neckar.open.provider.fastForEachIndexed
import it.neckar.open.unit.other.Sorted
import it.neckar.open.unit.other.pct
import korlibs.time.Year
import kotlin.jvm.JvmInline

/**
 * Slider that supports years
 */
class YearSlider(
  /**
   * The years that are shown
   */
  val years: @Sorted SizedProvider<Year>,
  additionalSliderConfiguration: Slider.Configuration.() -> Unit = {},
) {

  /**
   * The selected year index.
   */
  var selectedYearIndex: YearIndex = YearIndex(0)

  /**
   * The delta between the first and last year
   * Returns 0 if there are no years
   */
  private val yearsDelta: Int
    get() {
      if (years.isEmpty()) {
        return 0
      }

      return years.last() - years.first()
    }


  /**
   * Returns the best year for the provided position
   */
  private fun findClosestYearIndex(position: @pct Double): YearIndex {
    if (years.isEmpty()) {
      return YearIndex.None
    }

    var bestYearIndex = YearIndex.None
    var bestDistance: Double = Double.NaN

    years.fastForEachIndexed { index, candidate ->
      val distance = (position - yearToPercentage(candidate)).abs()

      if (distance < bestDistance || bestYearIndex == YearIndex.None) {
        bestYearIndex = YearIndex(index)
        bestDistance = distance
      }
    }

    return bestYearIndex
  }

  /**
   * Converts a year to a percentage
   */
  fun yearToPercentage(year: Year): @pct Double {
    if (years.isEmpty() || yearsDelta == 0) {
      return 0.0
    }

    val firstYear = years.first()

    val deltaToFirstYear = year - firstYear
    return 1.0 / yearsDelta * deltaToFirstYear
  }

  val slider: Slider = Slider(
    Slider.Configuration(
      width = { 300.0 },
      handlePosition = {
        if (selectedYearIndex == YearIndex.None) {
          0.0
        } else {
          yearToPercentage(years.valueAt(selectedYearIndex.value))
        }
      },

      handlePositionChanged = { newPosition: @pct Double ->
        val closestYearIndex = findClosestYearIndex(newPosition)

        selectedYearIndex = closestYearIndex
        //markAsDirty(DirtyReason.UserInteraction)
      },
    )
  ) {
    tickLocations = object : DoublesProvider {
      override fun size(): Int {
        return years.size()
      }

      override fun valueAt(index: Int): @pct Double {
        val year = years.valueAt(index)
        return yearToPercentage(year)
      }
    }

    ticksLabels = MultiProvider { index ->
      val year = years.valueAt(index)
      year.year.toString()
    }

    additionalSliderConfiguration()
  }

  @JvmInline
  value class YearIndex(val value: Int) {
    override fun toString(): String {
      return value.toString()
    }

    companion object {
      val None: YearIndex = YearIndex(-1)
    }
  }
}
