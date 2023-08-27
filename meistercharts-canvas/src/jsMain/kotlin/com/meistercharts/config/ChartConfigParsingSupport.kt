package com.meistercharts.config

import com.meistercharts.canvas.ChartSupport
import com.meistercharts.js.MeisterChartClasses
import com.meistercharts.js.MeisterchartJS
import forEach
import it.neckar.commons.kotlin.js.config.JsonScriptTagSupport
import it.neckar.logging.Logger
import it.neckar.logging.LoggerFactory
import it.neckar.logging.debug
import it.neckar.logging.trace
import kotlinx.serialization.json.Json
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLScriptElement

/**
 * Supports parsing of JSON data from script tags
 */
class ChartConfigParsingSupport(
  /**
   * The JSON parser used to parse the JSON data
   */
  json: Json = Json,
) {

  val jsonScriptTagSupport: JsonScriptTagSupport = JsonScriptTagSupport(json)

  /**
   * Parses the JSON data from a script tag with the given id.
   *
   * This can be used to find script tags that might be anywhere in the document.
   */
  inline fun <reified T> parseChartsConfig(id: String): T {
    return jsonScriptTagSupport.parseTagById(id)
  }

  /**
   * Returns the configuration for the chart represented by the provided [chartSupport].
   *
   * This method can be used to find the (first) script tag within the canvas holder with the class [MeisterChartClasses.config]
   */
  inline fun <reified T> parseChartsConfig(chartSupport: ChartSupport): T {
    val jsonContent = getJsonContent(chartSupport)
    return jsonScriptTagSupport.decode(jsonContent)
  }

  /**
   * Returns the JSON configuration (unparsed) for the chart represented by the provided [chartSupport].
   */
  fun getJsonContent(chartSupport: ChartSupport): String {
    val meisterchart = chartSupport.meisterchart as MeisterchartJS
    logger.debug { "Looking for configuration script tag under ${meisterchart.holder}" }

    meisterchart.holder.childNodes.forEach {
      when (it) {
        is HTMLElement -> {
        }

        else -> {
          println("Not a HTML element: $it")
        }
      }

      logger.trace { "Found a child node: $it NodeName: [${it.nodeName}] ${it::class}" }

      if (it.nodeName == "SCRIPT") {
        val scriptElement = it as HTMLScriptElement
        require(scriptElement.type == "application/json") { "The script element must have the type 'application/json'" }
        require(scriptElement.className.contains(MeisterChartClasses.config)) { "The script element must have the class '${MeisterChartClasses.config}'" }

        return scriptElement.text

      } else {
        println("*NOT* a Script tag: ${it.nodeName}")
      }
    }

    throw IllegalStateException("No script tag found for chart with id [${chartSupport.chartId}]")
  }

  companion object {
    private val logger: Logger = LoggerFactory.getLogger("com.meistercharts.config.ChartConfigParsingSupport")
  }
}
