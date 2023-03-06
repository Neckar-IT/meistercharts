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
package com.meistercharts.algorithms.layers

import com.meistercharts.algorithms.axis.AxisEndConfiguration
import it.neckar.open.collections.fastMapDouble
import it.neckar.unit.conversion.Converter

/**
 * A tick provider that converts the values before calculating the ticks.
 * This class can be used to show ticks for other units (e.g. feet in stead of meters)
 */
class ConvertingTickProvider(
  /**
   * The delegate that is called with the converted values
   */
  val delegate: TickProvider,

  /**
   * Converter that converts the value
   */
  val converter: Converter,
) : TickProvider {

  override fun getTicks(lowerValue: Double, upperValue: Double, maxTickCount: Int, minTickDistance: Double, axisEndConfiguration: AxisEndConfiguration): DoubleArray {
    val lowerValueConverted = converter.convertValue(lowerValue)
    val upperValueConverted = converter.convertValue(upperValue)

    val ticks = delegate.getTicks(lowerValueConverted, upperValueConverted, maxTickCount, minTickDistance, axisEndConfiguration)
    //Convert back!

    return ticks.fastMapDouble {
      converter.reverseValue(it)
    }
  }
}

/**
 * A tick provider that uses the given factor
 */
fun TickProvider.withFactor(factor: Double): ConvertingTickProvider {
  return withConversion(Converter.withFactor(factor))
}

/**
 * Wraps a tick provider using the provided converter
 */
fun TickProvider.withConversion(converter: Converter): ConvertingTickProvider {
  return ConvertingTickProvider(this, converter)
}

