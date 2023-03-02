package com.meistercharts.demo

import com.meistercharts.algorithms.painter.Color
import com.meistercharts.demo.DemoConfiguration
import com.meistercharts.demo.Section
import com.meistercharts.fx.toColor
import com.meistercharts.fx.toJavaFx
import it.neckar.open.kotlin.lang.round
import it.neckar.open.javafx.Components
import it.neckar.open.javafx.Components.hbox5
import it.neckar.open.javafx.Components.label
import it.neckar.open.javafx.map
import it.neckar.open.observable.ObservableObject
import it.neckar.open.observable.toNumber
import com.meistercharts.fx.binding.toJavaFx
import javafx.scene.control.ColorPicker
import javafx.scene.control.Label
import javafx.scene.control.Tooltip
import javafx.scene.layout.VBox
import kotlin.math.abs
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.round

class DemoConfigurationFX : DemoConfiguration {
  /**
   * The box that contains the configuration
   */
  val pane: VBox = Components.vbox5()

  override fun section(name: String, configure: Section.() -> Unit) {
    val element = Components.headline1(name)
    pane.children.add(element)

    val section = SectionFX(element)
    section.configure()
  }

  override fun <T : Enum<T>> comboBox(name: String, property: ObservableObject<T>, possibleValues: Array<T>) {
    val fxProperty = property.toJavaFx()
    val comboBox = Components.comboBox<T>(fxProperty, possibleValues)

    pane.children.add(hbox5(label(name), comboBox))
  }

  override fun <T> comboBox(name: String, property: ObservableObject<T>, possibleValues: List<T>, converter: (T) -> String) {
    val fxProperty = property.toJavaFx()
    val component = Components.choiceBox<T>(fxProperty, possibleValues, converter)

    pane.children.add(hbox5(label(name), component))
  }

  override fun slider(name: String, valueProperty: ObservableObject<Double>, min: Double, max: Double, step: Double?) {
    val fxProperty = valueProperty.toJavaFx()
    val diff = abs(max - min)
    val sliderStep = step ?: 10.0.pow(round(log10(diff * 0.01)))
    val slider = Components.slider(fxProperty, min, max, sliderStep)
    slider.isShowTickLabels = true
    slider.isSnapToTicks = false
    slider.isSnapToPixel = false

    pane.children.add(hbox5(label(name), slider, label(fxProperty.asString("%.2f"))))
  }

  override fun slider(name: String, valueProperty: ObservableObject<Int>, min: Int, max: Int, step: Int?) {
    val fxProperty = valueProperty.toNumber().toJavaFx()
    val sliderStep = (step?.toDouble() ?: ((max - min) / 100.0)).coerceAtLeast(1.0).round()
    val slider = Components.slider(fxProperty, min.toDouble(), max.toDouble(), sliderStep)
    slider.isShowTickLabels = true
    slider.blockIncrement = 1.0
    slider.isSnapToTicks = true

    pane.children.add(hbox5(label(name), slider, label(fxProperty.map { "${it.toInt()}" })))
  }

  override fun sliderNan(name: String, valueProperty: ObservableObject<Double>, isNanProperty: ObservableObject<Boolean>, min: Double, max: Double, step: Double?) {
    val fxProperty = valueProperty.toJavaFx()
    val diff = abs(max - min)
    val sliderStep = step ?: 10.0.pow(round(log10(diff * 0.01)))
    val slider = Components.slider(fxProperty, min, max, sliderStep)
    slider.isShowTickLabels = true
    slider.isSnapToTicks = false
    slider.isSnapToPixel = false

    val nanCheckBox = Components.checkBox("", isNanProperty.toJavaFx())
    nanCheckBox.tooltip = Tooltip("NaN")

    pane.children.add(hbox5(label(name), slider, nanCheckBox, label(fxProperty.asString("%.2f"))))
  }

  override fun checkBox(name: String, property: ObservableObject<Boolean>) {
    val fxProperty = property.toJavaFx()
    val checkBox = Components.checkBox(name, fxProperty)

    pane.children.add(hbox5(checkBox))
  }

  override fun button(name: String, action: () -> Unit) {
    pane.children.add(Components.button(name) { action() })
  }

  override fun colorPicker(name: String, valueProperty: ObservableObject<Color>, customColors: List<Color>) {
    val colorPicker = ColorPicker()
    colorPicker.customColors.setAll(customColors.map { it.toJavaFx() })
    colorPicker.value = valueProperty.value.toJavaFx()
    colorPicker.valueProperty().addListener { _, _, newValue ->
      valueProperty.value = newValue.toColor()
    }
    pane.children.add(hbox5(label(name), colorPicker))
  }
}

class SectionFX(val label: Label) : Section {}
