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
package com.meistercharts.label

import com.meistercharts.annotations.Domain
import com.meistercharts.annotations.Window
import it.neckar.geometry.Coordinates
import it.neckar.open.collections.fastForEach
import it.neckar.open.unit.other.px

/**
 * Wrapper for layouted labels
 */
@Deprecated("No longer used")
data class LayoutedLabels(
  @Domain

  val labels: List<LayoutedLabel>
) {

  /**
   * Returns the label model at the given location
   */
  @Domain
  fun findLabel(@Window location: Coordinates): LayoutedLabel? {
    return labels
      .asSequence()
      .firstOrNull {
        it.bounds.contains(location)
      }
  }

  @Domain
  fun findLabel(@Window @px y: Double): LayoutedLabel? {
    return labels
      .asSequence()
      .firstOrNull {
        it.containsActual(y)
      }
  }

  /**
   * Creates a paint result with updated paint bounds that "reverts" the given translation
   */
  fun fixTranslation(tx: Double, ty: Double) {
    labels.fastForEach {
      it.moveBounds(tx, ty)
    }
  }
}
