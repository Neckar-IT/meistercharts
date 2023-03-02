package com.meistercharts.demojs.descriptors

import com.meistercharts.algorithms.layers.addClearBackground
import com.meistercharts.algorithms.layers.text.addText
import com.meistercharts.demo.ChartingDemo
import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import it.neckar.open.i18n.Locale
import kotlin.js.Date

/**
 *
 */
class LocalesJSDemoDescriptor : ChartingDemoDescriptor<Nothing> {
  override val name: String = "JS Locales"
  override val category: DemoCategory = DemoCategory.Platform

  override val description: String = """
    <h3>Date.toLocaleString()</h3>

    Demonstrates the results of JS formatting.

    <p>
    The two last locales will produce "out of range" errors.
    </p>

  """.trimIndent()

  override fun createDemo(configuration: PredefinedConfiguration<Nothing>?): ChartingDemo {
    return ChartingDemo {
      meistercharts {
        configure {
          layers.addClearBackground()

          layers.addText { _, _ ->
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
