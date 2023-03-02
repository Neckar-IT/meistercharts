package com.meistercharts.demojs

import com.meistercharts.algorithms.painter.Color
import com.meistercharts.demo.DemoConfiguration
import com.meistercharts.demo.Section
import it.neckar.open.kotlin.lang.round
import it.neckar.open.observable.ObservableObject
import it.neckar.open.observable.toDouble
import kotlinx.browser.document
import org.w3c.dom.HTMLTableElement
import kotlin.math.abs
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.round


/**
 * Creates HTML elements in accordance with a demo configuration.
 *
 * @param table the table the generated elements are appended to
 *
 */
class DemoConfigurationJS(private val table: HTMLTableElement) : DemoConfiguration {

  override fun section(name: String, configure: Section.() -> Unit) {
    table.singleColumnRow(document.headline3(name))
    SectionJS().configure()
  }

  override fun <T : Enum<T>> comboBox(name: String, property: ObservableObject<T>, possibleValues: Array<T>) {
    table.twoColumnsRow(
      document.label(name),
      document.comboBox(property, possibleValues)
    )
  }

  override fun <T> comboBox(name: String, valueProperty: ObservableObject<T>, possibleValues: List<T>, converter: (T) -> String) {
    table.twoColumnsRow(
      document.label(name),
      document.comboBox(valueProperty, possibleValues, converter)
    )
  }

  override fun slider(name: String, valueProperty: ObservableObject<Double>, min: Double, max: Double, step: Double?) {
    val diff = abs(max - min)
    val sliderStep = step ?: 10.0.pow(round(log10(diff * 0.01)))
    table.twoColumnsRow(
      document.label(name),
      document.slider(valueProperty, min, max, sliderStep)
    )
  }

  override fun slider(name: String, valueProperty: ObservableObject<Int>, min: Int, max: Int, step: Int?) {
    val sliderStep = (step?.toDouble() ?: ((max - min) / 100.0)).coerceAtLeast(1.0).round()
    table.twoColumnsRow(
      document.label(name),
      document.slider(valueProperty.toDouble(), min.toDouble(), max.toDouble(), sliderStep)
    )
  }

  override fun sliderNan(name: String, valueProperty: ObservableObject<Double>, isNanProperty: ObservableObject<Boolean>, min: Double, max: Double, step: Double?) {
    val diff = abs(max - min)
    val sliderStep = step ?: 10.0.pow(round(log10(diff * 0.01)))
    table.threeColumnsRow(
      document.label(name),
      document.slider(valueProperty, min, max, sliderStep),
      document.checkBox(isNanProperty),
    )
  }

  override fun checkBox(name: String, property: ObservableObject<Boolean>) {
    table.singleColumnRow(document.checkBox(property, name))
  }

  override fun button(name: String, action: () -> Unit) {
    table.singleColumnRow(document.button(name, action))
  }

  override fun colorPicker(name: String, valueProperty: ObservableObject<Color>, customColors: List<Color>) {
    table.twoColumnsRow(
      document.label(name),
      document.colorPicker(valueProperty)
    )
  }
}

class SectionJS : Section
