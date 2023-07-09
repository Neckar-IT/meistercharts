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
package com.meistercharts.maps

import com.meistercharts.geometry.Coordinates
import it.neckar.open.provider.CoordinatesProvider


/**
 * Creates a new [CoordinatesProvider] that returns the given values
 */
fun CoordinatesProvider.Companion.forValues(values: List<Coordinates>): CoordinatesProvider {
  return object : CoordinatesProvider {
    override fun size(): Int {
      return values.size
    }

    override fun xAt(index: Int): Double {
      return values[index].x
    }

    override fun yAt(index: Int): Double {
      return values[index].y
    }
  }
}
