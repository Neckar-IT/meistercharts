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
package com.meistercharts.demo

import com.meistercharts.algorithms.layers.CategoryLayouter
import com.meistercharts.algorithms.layers.DefaultCategoryLayouter
import com.meistercharts.canvas.DirtyReason
import com.meistercharts.canvas.LayerSupport
import com.meistercharts.charts.ThresholdsGestalt
import com.meistercharts.history.DecimalDataSeriesIndex
import com.meistercharts.history.DecimalDataSeriesIndexProvider
import com.meistercharts.history.EnumDataSeriesIndex
import com.meistercharts.history.EnumDataSeriesIndexProvider
import it.neckar.open.kotlin.lang.fastFor
import it.neckar.open.provider.DoublesProvider
import it.neckar.open.provider.MultiProvider
import it.neckar.open.provider.SizedProvider
import com.meistercharts.history.ReferenceEntryDataSeriesIndex
import com.meistercharts.history.ReferenceEntryDataSeriesIndexProvider

/**
 * Contains extension methods relevant for the demos
 */


/**
 * Casts the category layouter to [DefaultCategoryLayouter] and returns the style
 */
val CategoryLayouter.style: DefaultCategoryLayouter.Style
  get() {
    return (this as DefaultCategoryLayouter).style
  }

/**
 * Creates check boxes to select indices.
 * @param demo the demo to create the widgets for
 * @param sectionName a name for the section that contains the check boxes
 * @param indexSuffix text that is appended to the index
 * @param initial the initially selected indices
 * @param maxSize the maximum number of indices that can be selected
 * @param onChange function that is called whenever the selected indices change
 */
@DemoDeclaration
fun LayerSupport.configurableIndices(
  demo: @DemoDeclaration ChartingDemo,
  sectionName: String,
  indexSuffix: String,
  initial: List<Int>,
  maxSize: Int = initial.size.coerceAtLeast(3),
  onChange: (newIndices: List<Int>) -> Unit,
) {
  require(maxSize > 0) { "maxSize <$maxSize> does not make any sense" }

  val indices = initial.toMutableSet()
  demo.declare {
    section(sectionName)
  }

  maxSize.fastFor { index ->
    demo.configurableBoolean("${index + 1}. ${indexSuffix}") {
      value = indices.contains(index)
      onChange {
        if (it) {
          indices.add(index)
        } else {
          indices.remove(index)
        }
        onChange(indices.toList())
        markAsDirty(DirtyReason.UiStateChanged)
      }
    }
  }

}

/**
 * Removes all threshold-values and threshold-labels
 */
fun ThresholdsGestalt.clearThresholds() {
  configuration.thresholdValues = DoublesProvider.empty
  configuration.thresholdLabels = MultiProvider.empty()
}


/**
 * Converts the content to a mutable list
 */
fun DecimalDataSeriesIndexProvider.toMutableList(): MutableList<DecimalDataSeriesIndex> {
  return MutableList(this.size()) { index -> this.valueAt(index) }
}

fun DecimalDataSeriesIndexProvider.toList(): List<DecimalDataSeriesIndex> {
  return List(this.size()) { index -> this.valueAt(index) }
}

/**
 * Converts the content to a mutable list
 */
fun EnumDataSeriesIndexProvider.toMutableList(): MutableList<EnumDataSeriesIndex> {
  return MutableList(this.size()) { index -> this.valueAt(index) }
}

fun EnumDataSeriesIndexProvider.toList(): List<EnumDataSeriesIndex> {
  return List(this.size()) { index -> this.valueAt(index) }
}

fun ReferenceEntryDataSeriesIndexProvider.toList(): List<ReferenceEntryDataSeriesIndex> {
  return List(this.size()) { index -> this.valueAt(index) }
}

/**
 * Returns a [MutableList] that contains all elements of this [SizedProvider].
 *
 * Changes made to that list will *not* be reflected by this [SizedProvider]
 */
fun <T> SizedProvider<T>.toMutableList(): MutableList<T> {
  return MutableList(this.size()) { index -> this.valueAt(index) }
}

/**
 * Returns a [List] that contains all elements of this [SizedProvider]
 */
fun <T> SizedProvider<T>.toList(): List<T> {
  return List(this.size()) { index -> this.valueAt(index) }
}

/**
 * Tries to convert a multi provider to a list
 */
fun <IndexContext, T> MultiProvider<IndexContext, T>.toList(): List<T> {
  val first = valueAt(0)

  return buildList {
    add(first)

    var counter = 1
    while (true) {
      val currentValue = valueAt(counter)
      if (currentValue == first) {
        return@buildList
      }

      add(currentValue)
      counter++
    }
  }
}
