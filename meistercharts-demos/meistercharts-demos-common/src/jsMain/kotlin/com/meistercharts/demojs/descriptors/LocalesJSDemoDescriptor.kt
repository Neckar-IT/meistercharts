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
package com.meistercharts.demojs.descriptors

import kotlin.uuid.Uuid
import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.text.addTexts
import com.meistercharts.demo.MeisterchartsDemo
import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.DemoQuality
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.VariabilityType
import it.neckar.open.i18n.Locale
import kotlin.js.Date

/**
 *
 */
class LocalesJSDemoDescriptor : MeisterchartsDemoDescriptor<Nothing> {
  override val uuid: Uuid = Uuid.parse("508256ca-9d38-46ce-a21c-6e1e64164847")
  override val name: String = "JS Locales"
  override val category: DemoCategory = DemoCategory.Platform
  override val quality: DemoQuality = DemoQuality.High
  override val variabilityType: VariabilityType = VariabilityType.Stable

  override val description: String = """
    <h3>Date.toLocaleString()</h3>

    Demonstrates the results of JS formatting.

    <p>
    The two last locales will produce "out of range" errors.
    </p>

  """.trimIndent()

  override fun prepareDemo(configuration: PredefinedConfiguration<Nothing>?): MeisterchartsDemo {
    return MeisterchartsDemo(this) {
      meistercharts {
        configure {
          layers.addClearBackground()

          layers.addTexts { _, _ ->
            val date = Date(Date.UTC(2012, 11, 20, 3, 0, 0));
            listOf(
              "US: ${Locale.US.locale}: ${date.formatSafe(Locale.US.locale)}",
              "Germany: ${Locale.Germany.locale}: ${date.formatSafe(Locale.Germany.locale)}",
              "C: ${date.formatSafe("C")}",
              "Invalid Locale: ${date.formatSafe("INVALID_LOCALE")}",
            )
          }

        }
      }
    }
  }

  private fun Date.formatSafe(locale: String): String {
    return try {
      println("formatting for <$locale>")
      toLocaleString(locale)
    } catch (e: Throwable) {
      println("error: $e")
      e.toString()
    }
  }
}
