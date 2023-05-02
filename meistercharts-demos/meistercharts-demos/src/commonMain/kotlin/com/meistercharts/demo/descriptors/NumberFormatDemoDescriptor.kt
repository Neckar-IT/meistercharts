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
package com.meistercharts.demo.descriptors

import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.text.addText
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.canvas.FontDescriptorFragment
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.model.Direction
import com.meistercharts.model.DirectionBasedBasePointProvider
import it.neckar.open.formatting.decimalFormat
import it.neckar.open.formatting.decimalFormat1digit
import it.neckar.open.formatting.decimalFormat2digits
import it.neckar.open.formatting.intFormat
import it.neckar.open.formatting.percentageFormat
import it.neckar.open.formatting.percentageFormat2digits
import it.neckar.open.i18n.TextService

/**
 */
class NumberFormatDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "Number Format"
  override val description: String = "# How to format numbers"
  override val category: DemoCategory = DemoCategory.Text

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    val customDecimalFormat1 = decimalFormat(4, 3, 3, false)
    val customDecimalFormat2 = decimalFormat(4, 3, 2, true)

    return ChartingDemo {
      meistercharts {
        configure {
          layers.addClearBackground()
          layers.addText({ _: TextService, i18nConfiguration ->
            listOf(1.23456789, 1234.56789).flatMap {
              it.let { value ->
                listOf(
                  "value=$value, locale=${i18nConfiguration}",
                  "",
                  "decimalFormat1digit: ${decimalFormat1digit.format(value, i18nConfiguration)}",
                  "decimalFormat2digits: ${decimalFormat2digits.format(value, i18nConfiguration)}",
                  "percentageFormat: ${percentageFormat.format(value, i18nConfiguration)}",
                  "percentageFormat2digits: ${percentageFormat2digits.format(value, i18nConfiguration)}",
                  "intFormat: ${intFormat.format(value, i18nConfiguration)}",
                  "customDecimalFormat1: ${customDecimalFormat1.format(value, i18nConfiguration)}",
                  "customDecimalFormat2: ${customDecimalFormat2.format(value, i18nConfiguration)}",
                  "",
                )
              }
            }
          }) {
            anchorDirection = Direction.TopLeft
            anchorPointProvider = DirectionBasedBasePointProvider(anchorDirection)
            textColor = Color.black
            font = FontDescriptorFragment(14.0)
          }
        }
      }
    }
  }
}
