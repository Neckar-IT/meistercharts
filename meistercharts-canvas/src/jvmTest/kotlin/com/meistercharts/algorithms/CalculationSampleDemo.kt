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
package com.meistercharts.algorithms

import com.meistercharts.algorithms.impl.DefaultChartState
import com.meistercharts.annotations.ContentArea
import com.meistercharts.annotations.Domain
import com.meistercharts.annotations.DomainRelative
import com.meistercharts.model.Distance
import com.meistercharts.model.Size
import org.junit.jupiter.api.Test
import java.text.NumberFormat

/**
 * Shows how the calculations are done
 *
 */
class CalculationSampleDemo {
  @Test
  @Throws(Exception::class)
  internal fun testIt() {

    //
    //The domain values
    //

    //The value range describes the range that is relevant for this domain value
    //In this example we are interested in values from 100_000 to 200_000
    @Domain val valueRange = ValueRange.linear(100_000.0, 200_000.0)

    //The current domain value (e.g. measured)
    @Domain val domainValue = 142000.0

    //Translate to a domain relative value. Describes the percentage within the value range
    @DomainRelative val domainRelative = valueRange.toDomainRelative(domainValue)
    println("domainRelative = ${NumberFormat.getPercentInstance().format(domainRelative)}")


    //
    // The pixel values
    //
    val chartState = DefaultChartState()
    chartState.contentAreaSize = Size(800.0, 600.0)

    println("content area size: ${chartState.contentAreaSize}")

    val chartCalculator = ChartCalculator(chartState)


    //Convert to unzoomed, untranslated content area pixel size
    @ContentArea val contentAreaY = chartCalculator.domainRelative2contentAreaY(domainRelative)
    println("contentAreaY = $contentAreaY px")

    @ContentArea val zoomedY = chartCalculator.window2zoomedY(contentAreaY)
    println("zoomedY = $zoomedY px")

    @ContentArea val windowY = chartCalculator.zoomed2windowY(contentAreaY)
    println("windowY = $windowY px")


    //
    // Apply translation (panning)
    //
    println("------------- Applying translation -------------")

    chartState.windowTranslation = Distance.of(0.0, 100.0)

    @ContentArea val windowYAfterTranslation = chartCalculator.domainRelative2windowY(domainRelative)
    println("windowY with translation = $windowYAfterTranslation px")

  }
}
