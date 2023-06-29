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
package com.meistercharts.whatsat

import com.meistercharts.annotations.Window
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.ChartSupport
import com.meistercharts.geometry.Coordinates
import it.neckar.open.unit.other.Inclusive
import kotlin.jvm.JvmInline


/**
 * A resolver that resolves the information about what is located at a given location
 *
 */
typealias WhatsAtResolver = (where: @Window Coordinates, precision: WhatsAtSupport.Precision, chartSupport: ChartSupport) -> List<WhatsAtResultElement<*>>

/**
 * Provides information about "what" is at a given location.
 *
 * Layers should register a resolver using [registerResolverAsFirst].
 * These resolvers are then used to identify
 */
class WhatsAtSupport {

  /**
   * Queries the location
   */
  fun whatsAt(location: @Window Coordinates, precision: Precision, chartSupport: ChartSupport): WhatsAtResult? {
    val resultElements = resolvers.flatMap { resolver ->
      resolver(location, precision, chartSupport)
    }

    if (resultElements.isEmpty()) {
      //Nothing found - return null
      return null
    }

    return WhatsAtResult(location, precision, resultElements)
  }

  /**
   * Registers a whatsAt resolver.
   *
   * The new resolver is registered as first resolver.
   */
  fun registerResolverAsFirst(resolver: WhatsAtResolver) {
    resolvers.add(0, resolver)
  }

  /**
   * The resolvers. These are stored in *reverse* order of the layers.
   * Therefore, resolver provided by top layers are asked first!
   */
  private val resolvers: MutableList<WhatsAtResolver> = mutableListOf()

  sealed interface Precision {
    /**
     * Returns true if the given distance matches this precision
     */
    fun matches(distance: @Zoomed Double): Boolean
  }

  /**
   * Returns only exact results for the location
   */
  data object Exact : Precision {
    override fun matches(distance: @Zoomed Double): Boolean {
      return distance == 0.0
    }

  }

  /**
   * Returns the nearest result (which might be far away!)
   */
  data object Nearest : Precision {
    override fun matches(distance: @Zoomed Double): Boolean {
      return true
    }

  }

  /**
   * Returns the closest element within the given max distance.
   * If no element can be found within the given distance, no result is returned
   */
  @JvmInline
  value class CloseTo(val maxDistance: @Zoomed @Inclusive Double) : Precision {
    override fun matches(distance: Double): Boolean {
      return distance <= maxDistance
    }

    override fun toString(): String {
      return "CloseTo(maxDistance=$maxDistance)"
    }

    companion object {
      val VeryClose: CloseTo = CloseTo(5.0)
      val CloseTo: CloseTo = CloseTo(10.0)
    }
  }
}
