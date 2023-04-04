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
package com.meistercharts.demo

import com.meistercharts.algorithms.ValueRange
import com.meistercharts.algorithms.layers.linechart.LineStyle
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.canvas.BorderRadius
import com.meistercharts.canvas.FontDescriptor
import com.meistercharts.canvas.FontDescriptorFragment
import com.meistercharts.canvas.FontFamily
import com.meistercharts.canvas.FontSize
import com.meistercharts.canvas.FontStyle
import com.meistercharts.canvas.FontVariant
import com.meistercharts.canvas.FontWeight
import com.meistercharts.canvas.LayerSupport
import com.meistercharts.canvas.MeisterChartBuilder
import com.meistercharts.model.Coordinates
import com.meistercharts.model.Insets
import com.meistercharts.model.Size
import com.meistercharts.provider.ColorProvider
import it.neckar.open.kotlin.lang.asProvider
import it.neckar.open.kotlin.lang.getAndSet
import it.neckar.open.provider.BooleanProvider
import it.neckar.open.provider.DoubleProvider
import it.neckar.open.formatting.CachedNumberFormat
import it.neckar.open.formatting.DecimalFormat
import it.neckar.open.formatting.DefaultCachedFormat
import it.neckar.open.formatting.decimalFormat
import it.neckar.open.observable.DependentObjects
import it.neckar.open.observable.ObservableBoolean
import it.neckar.open.observable.ObservableDouble
import it.neckar.open.observable.ObservableObject
import com.meistercharts.style.BoxStyle
import it.neckar.financial.currency.Money
import it.neckar.logging.LoggerFactory
import it.neckar.open.kotlin.lang.enumEntries
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.math.PI
import kotlin.reflect.KMutableProperty0

/**
 * Abstract base class for charting demos
 */
@DemoDeclaration
class ChartingDemo(demoConfig: ChartingDemo.() -> Unit) {
  /**
   * The dependent objects that hold the properties
   */
  val dependentObjects: DependentObjects = DependentObjects()

  /**
   * The chart configurations
   */
  val chartConfigs: MutableList<MeisterChartBuilder.() -> Unit> = mutableListOf()

  fun meistercharts(configuration: MeisterChartBuilder.() -> Unit) {
    this.chartConfigs.add(configuration)
  }

  /**
   * The declarations
   */
  val declarations: MutableList<DemoConfiguration.(LayerSupport) -> Unit> = mutableListOf()

  @DemoDeclaration
  fun declare(declaration: DemoConfiguration.(LayerSupport) -> Unit) {
    this.declarations.add(declaration)
  }

  init {
    demoConfig()
  }

  /**
   * Configures the chart builder
   */
  fun MeisterChartBuilder.configure() {
    chartConfigs.forEach {
      it()
    }

    //Remember the chart layer support
    configure {
      layerSupport = this
    }
  }

  /**
   * The current
   */
  private var layerSupport: LayerSupport? = null

  /**
   * Declares the demo configuration
   */
  fun declare(demoConfiguration: DemoConfiguration, layerSupport: LayerSupport) {
    declarations.forEach {
      demoConfiguration.it(layerSupport)
    }
  }

  /**
   * Keeps the given object
   */
  fun <T : Any> T.keep(): T {
    dependentObjects.addDependentObject(this)
    return this
  }

  /**
   * This method can be called and triggers a paint on the canvas.
   *
   * This is a helper method that allows to mark the chart as dirty - even if no [LayerSupport] is available in the current scope of the demo.
   */
  fun markAsDirty() {
    layerSupport?.markAsDirty()
  }

  companion object {
    val logger = LoggerFactory.getLogger("com.meistercharts.demo.ChartingDemo")
  }
}

@DemoDeclaration
fun ChartingDemo.section(
  sectionName: String,
) {
  declare {
    section(sectionName)
  }
}

@DemoDeclaration
fun ChartingDemo.button(
  actionName: String,
  action: () -> Unit,
) {
  contract {
    callsInPlace(action, InvocationKind.UNKNOWN)
  }

  declare {
    button(actionName, action)
  }
}

/**
 * Adds a configurable integer value
 */
@DemoDeclaration
fun ChartingDemo.configurableInt(
  propertyName: String,
  config: ConfigurableInt.() -> Unit = {},
) {
  contract {
    callsInPlace(config, InvocationKind.EXACTLY_ONCE)
  }

  val configurable = ConfigurableInt(propertyName)
    .also(config)
    .also {
      it.keep()
    }

  declare {
    slider(propertyName, configurable.valueProperty, configurable.min, configurable.max, configurable.step)
  }

  //Notify initially
  configurable.changed(configurable.valueProperty.value)
}

@DemoDeclaration
fun ChartingDemo.configurableInt(
  propertyName: String,
  property: KMutableProperty0<Int>,
  config: ConfigurableInt.() -> Unit = {},
) {
  contract {
    callsInPlace(config, InvocationKind.EXACTLY_ONCE)
  }

  configurableInt(propertyName) {
    onChange {
      property.set(it)
      markAsDirty()
    }
    value = property.get()

    config()

    validate(propertyName)
  }
}

/**
 * Adds a configurable double value
 */
@DemoDeclaration
fun ChartingDemo.configurableDouble(
  propertyName: String,
  initialValue: Double,
  callChangedInitially: Boolean = true,
  config: ConfigurableDouble.() -> Unit = {},
) {
  contract {
    callsInPlace(config, InvocationKind.EXACTLY_ONCE)
  }

  val configurable = ConfigurableDouble(propertyName, initialValue)
    .also(config)
    .also {
      it.keep()
    }

  declare {
    sliderNan(propertyName, configurable.valueProperty, configurable.isNanProperty, configurable.min, configurable.max, configurable.step)
  }

  //Notify initially
  if (callChangedInitially) {
    configurable.changed(configurable.valueProperty.value)
  }
}

@DemoDeclaration
fun ChartingDemo.configurableDouble(
  propertyName: String,
  property: ObservableObject<Double>,
  config: ConfigurableDouble.() -> Unit = {},
) {
  contract {
    callsInPlace(config, InvocationKind.EXACTLY_ONCE)
  }

  configurableDouble(propertyName, property.value) {
    onChange {
      property.value = it
      markAsDirty()
    }
    config()

    validate(propertyName)

    property.consume {
      value = it
    }
  }
}

@DemoDeclaration
fun ChartingDemo.configurableDouble(
  propertyName: String,
  property: KMutableProperty0<Double>,
  config: ConfigurableDouble.() -> Unit = {},
) {
  contract {
    callsInPlace(config, InvocationKind.EXACTLY_ONCE)
  }

  configurableDouble(propertyName, property.get()) {
    onChange {
      property.set(it)
      markAsDirty()
    }

    config()

    validate(propertyName)
  }
}

@DemoDeclaration
fun ChartingDemo.configurableDoubleNullable(
  propertyName: String,
  property: KMutableProperty0<Double?>,
  fallbackValue: Double,
  config: ConfigurableDouble.() -> Unit = {},
) {
  contract {
    callsInPlace(config, InvocationKind.EXACTLY_ONCE)
  }

  configurableDouble(propertyName, property.get() ?: fallbackValue) {
    onChange {
      property.set(it)
      markAsDirty()
    }

    config()

    validate(propertyName)
  }
}

@DemoDeclaration
fun ChartingDemo.configurableMoney(
  propertyName: String,
  property: KMutableProperty0<Money>,
  config: ConfigurableDouble.() -> Unit = {},
) {
  contract {
    callsInPlace(config, InvocationKind.EXACTLY_ONCE)
  }

  return configurableDouble(propertyName, property.get().euros) {
    onChange {
      property.set(Money.euros(it))
      markAsDirty()
    }

    config()
  }
}

@DemoDeclaration
fun ChartingDemo.configurableDouble(
  propertyName: String,
  property: KMutableProperty0<Double?>,
  fallbackValue: Double,
  config: ConfigurableDouble.() -> Unit = {},
) {
  contract {
    callsInPlace(config, InvocationKind.EXACTLY_ONCE)
  }

  configurableDouble(propertyName, property.get() ?: fallbackValue) {
    onChange {
      property.set(it)
      markAsDirty()
    }

    config()

    validate(propertyName)
  }
}

@DemoDeclaration
fun ChartingDemo.configurableDoubleProvider(
  propertyName: String,
  property: KMutableProperty0<DoubleProvider>,
  config: ConfigurableDouble.() -> Unit = {},
) {
  contract {
    callsInPlace(config, InvocationKind.EXACTLY_ONCE)
  }

  configurableDouble(propertyName, property.get().invoke()) {
    onChange {
      property.set(DoubleProvider { it })
      markAsDirty()
    }

    config()
    validate(propertyName)
  }
}

/**
 * Configurable rad that has max/min set to +/- 2*PI
 */
@DemoDeclaration
fun ChartingDemo.configurableRad(
  propertyName: String,
  initialValue: Double,
  config: ConfigurableDouble.() -> Unit = {},
) {
  contract {
    callsInPlace(config, InvocationKind.EXACTLY_ONCE)
  }

  configurableDouble(propertyName, initialValue) {
    min = -PI * 2
    max = PI * 2
    config()
  }
}

@DemoDeclaration
fun ChartingDemo.configurableRad(
  propertyName: String,
  property: KMutableProperty0<Double>,
  config: ConfigurableDouble.() -> Unit = {},
) {
  contract {
    callsInPlace(config, InvocationKind.EXACTLY_ONCE)
  }

  configurableRad(propertyName, property.get()) {
    onChange {
      property.set(it)
      markAsDirty()
    }

    config()
  }
}

@DemoDeclaration
fun ChartingDemo.configurableDouble(
  propertyName: String,
  property: ObservableDouble,
  config: ConfigurableDouble.() -> Unit = {},
) {
  contract {
    callsInPlace(config, InvocationKind.EXACTLY_ONCE)
  }

  configurableDouble(propertyName, property.value) {
    onChange {
      property.value = it
      markAsDirty()
    }

    config()

    property.consume {
      value = it
    }
  }
}

@DemoDeclaration
fun ChartingDemo.configurableDouble(
  property: KMutableProperty0<Double>,
  config: ConfigurableDouble.() -> Unit = {},
) {
  contract {
    callsInPlace(config, InvocationKind.EXACTLY_ONCE)
  }

  return configurableDouble(property.name, property, config)
}

@DemoDeclaration
fun ChartingDemo.configurableInsets(
  propertyName: String,
  initialValue: Insets,
  config: ConfigurableInsets.() -> Unit = {},
) {
  contract {
    callsInPlace(config, InvocationKind.EXACTLY_ONCE)
  }

  val configurable = ConfigurableInsets(propertyName, initialValue)
    .also(config)
    .also {
      it.keep()
    }

  configurableDouble(propertyName, configurable.all) {
    min = configurable.min
    max = configurable.max

    onChange {
      configurable.all = it
      markAsDirty()
    }
  }
}

@DemoDeclaration
fun ChartingDemo.configurableInsetsSeparate(
  propertyName: String,
  property: ObservableObject<Insets>,
  //TODO change to ConfigurableDouble (!?)
  config: ConfigurableInsets.() -> Unit = {},
) {
  contract {
    callsInPlace(config, InvocationKind.EXACTLY_ONCE)
  }

  //Contains the value - is only used to extract the configuration (min/max)
  val configurableInsets = ConfigurableInsets(propertyName, property.value)
    .also(config)

  configurableDouble("$propertyName Top", property.value.top) {
    min = configurableInsets.min
    max = configurableInsets.max

    property.consume {
      value = it.top
    }

    onChange {
      property.value = property.value.withTop(it)
    }
  }

  configurableDouble("$propertyName Right", property.value.right) {
    min = configurableInsets.min
    max = configurableInsets.max

    property.consume {
      value = it.right
    }

    onChange {
      property.value = property.value.withRight(it)
    }
  }

  configurableDouble("$propertyName Bottom", property.value.bottom) {
    min = configurableInsets.min
    max = configurableInsets.max

    property.consume {
      value = it.bottom
    }

    onChange {
      property.value = property.value.withBottom(it)
    }

  }

  configurableDouble("$propertyName Left", property.value.left) {
    min = configurableInsets.min
    max = configurableInsets.max

    property.consume {
      value = it.left
    }

    onChange {
      property.value = property.value.withLeft(it)
    }
  }
}

@DemoDeclaration
fun ChartingDemo.configurableInsets(
  propertyName: String,
  property: KMutableProperty0<Insets>,
  config: ConfigurableInsets.() -> Unit = {},
) {
  contract {
    callsInPlace(config, InvocationKind.EXACTLY_ONCE)
  }

  configurableInsets(propertyName, property.get()) {
    onChange {
      property.set(it)
      markAsDirty()
    }
    config()
  }
}

@DemoDeclaration
fun ChartingDemo.configurableInsets(
  propertyName: String,
  property: ObservableObject<Insets>,
  //TODO mabye change to ConfigurableDouble
  config: ConfigurableInsets.() -> Unit = {},
) {
  contract {
    callsInPlace(config, InvocationKind.EXACTLY_ONCE)
  }

  val configurableInsets = ConfigurableInsets(propertyName, property.value)
    .also(config)

  configurableDouble("$propertyName Insets", configurableInsets.all) {
    min = configurableInsets.min
    max = configurableInsets.max

    property.consume {
      value = it.top
    }

    onChange {
      property.value = Insets.of(it)
    }
  }
}

@DemoDeclaration
fun ChartingDemo.configurableInsetsSeparate(
  propertyName: String,
  initialValue: Insets = Insets.empty,
  config: ConfigurableInsets.() -> Unit = {},
) {
  contract {
    callsInPlace(config, InvocationKind.EXACTLY_ONCE)
  }

  val configurable = ConfigurableInsets(propertyName, initialValue)
    .also(config)
    .also {
      it.keep()
    }

  configurableDouble("$propertyName Top", configurable.top) {
    min = configurable.min
    max = configurable.max
    onChange {
      configurable.top = it
    }
  }
  configurableDouble("$propertyName Right", configurable.right) {
    min = configurable.min
    max = configurable.max
    onChange {
      configurable.right = it
    }
  }
  configurableDouble("$propertyName Bottom", configurable.bottom) {
    min = configurable.min
    max = configurable.max
    onChange {
      configurable.bottom = it
    }
  }
  configurableDouble("$propertyName Left", configurable.left) {
    min = configurable.min
    max = configurable.max
    onChange {
      configurable.left = it
    }
  }
}

@DemoDeclaration
fun ChartingDemo.configurableSizeSeparate(
  propertyName: String,
  property: KMutableProperty0<Size>,
  config: ConfigurableDouble.() -> Unit = {},
) {
  contract {
    callsInPlace(config, InvocationKind.EXACTLY_ONCE)
  }

  val initialSize = property.get()

  configurableDouble("$propertyName width", initialSize.width) {
    max = 200.0
    config()

    onChange {
      property.set(property.get().withWidth(it))
      markAsDirty()
    }
  }
  configurableDouble("$propertyName height", initialSize.height) {
    max = 200.0
    config()

    onChange {
      property.set(property.get().withHeight(it))
      markAsDirty()
    }
  }
}

@DemoDeclaration
fun ChartingDemo.configurableInsetsProviderSeparate(
  propertyName: String,
  property: KMutableProperty0<() -> Insets>,
  config: ConfigurableInsets.() -> Unit = {},
) {
  contract {
    callsInPlace(config, InvocationKind.EXACTLY_ONCE)
  }

  val observableObject = ObservableObject<Insets>(property.get()())
  observableObject.consume {
    property.set { it }
    markAsDirty()
  }

  configurableInsetsSeparate(propertyName, observableObject, config)
}

@DemoDeclaration
fun ChartingDemo.configurableInsetsProvider(
  propertyName: String,
  property: KMutableProperty0<() -> Insets>,
  config: ConfigurableInsets.() -> Unit = {},
) {
  contract {
    callsInPlace(config, InvocationKind.EXACTLY_ONCE)
  }

  val observableObject = ObservableObject(property.get()())
  observableObject.consume {
    property.set { it }
    markAsDirty()
  }

  configurableInsets(propertyName, observableObject, config)
}

@DemoDeclaration
fun ChartingDemo.configurableInsetsSeparate(
  propertyName: String,
  property: KMutableProperty0<Insets>,
  config: ConfigurableDouble.() -> Unit = {},
) {
  contract {
    callsInPlace(config, InvocationKind.EXACTLY_ONCE)
  }

  val initial = property.get()

  configurableDouble("$propertyName left", initial.left) {
    max = 200.0
    config()

    onChange {
      property.set(property.get().withLeft(it))
      markAsDirty()
    }
  }
  configurableDouble("$propertyName top", initial.top) {
    max = 200.0
    config()

    onChange {
      property.set(property.get().withTop(it))
      markAsDirty()
    }
  }
  configurableDouble("$propertyName right", initial.right) {
    max = 200.0
    config()

    onChange {
      property.set(property.get().withRight(it))
      markAsDirty()
    }
  }
  configurableDouble("$propertyName bottom", initial.bottom) {
    max = 200.0
    config()

    onChange {
      property.set(property.get().withBottom(it))
      markAsDirty()
    }
  }
}

@DemoDeclaration
fun ChartingDemo.configurableValueRange(
  propertyName: String,
  property: KMutableProperty0<ValueRange>,
  config: ConfigurableDouble.() -> Unit = {},
) {
  contract {
    callsInPlace(config, InvocationKind.EXACTLY_ONCE)
  }

  val initial = property.get()

  configurableDouble("$propertyName Start", initial.start) {
    max = 200.0
    min = -50.0

    config()

    onChange {
      property.set(property.get().withStart(it))
      markAsDirty()
    }
  }

  configurableDouble("$propertyName End", initial.end) {
    max = 200.0
    min = -50.0

    config()

    onChange {
      property.set(property.get().withEnd(it))
      markAsDirty()
    }
  }
}

@DemoDeclaration
fun ChartingDemo.configurableValueRangeProvider(
  propertyName: String,
  property: KMutableProperty0<() -> ValueRange>,
  config: ConfigurableDouble.() -> Unit = {},
) {
  contract {
    callsInPlace(config, InvocationKind.EXACTLY_ONCE)
  }

  val initial = property.get().invoke()

  configurableDouble("$propertyName Start", initial.start) {
    max = 200.0
    min = -50.0

    config()

    onChange {
      property.set(property.get().invoke().withStart(it).asProvider())
      markAsDirty()
    }
  }

  configurableDouble("$propertyName End", initial.end) {
    max = 200.0
    min = -50.0

    config()

    onChange {
      property.set(property.get().invoke().withEnd(it).asProvider())
      markAsDirty()
    }
  }
}

private fun ValueRange.withStart(newStart: Double): ValueRange {
  return when {
    this.isLinear -> ValueRange.linear(newStart.coerceAtMost(end - 0.1), end)
    this.isLogarithmic -> ValueRange.logarithmic(newStart.coerceAtMost(end - 0.1), end)
    else -> throw IllegalStateException("Unsupported value range <$this>")
  }
}

private fun ValueRange.withEnd(newEnd: Double): ValueRange {
  return when {
    this.isLinear -> ValueRange.linear(start, newEnd.coerceAtLeast(start + 0.1))
    this.isLogarithmic -> ValueRange.logarithmic(start, newEnd.coerceAtLeast(start + 0.1))
    else -> throw IllegalStateException("Unsupported value range <$this>")
  }
}

@DemoDeclaration
fun ChartingDemo.configurableRange(
  propertyName: String,
  property: KMutableProperty0<IntRange>,
  config: ConfigurableInt.() -> Unit = {},
) {
  contract {
    callsInPlace(config, InvocationKind.EXACTLY_ONCE)
  }

  val initial = property.get()

  configurableInt("$propertyName Start") {
    min = 50
    max = 200

    config()

    onChange {
      property.set(property.get().withStart(it))
      markAsDirty()
    }

    value = initial.start
  }

  configurableInt("$propertyName End") {
    min = -50
    max = 200

    config()
    value = initial.last

    onChange {
      property.set(property.get().withEnd(it))
      markAsDirty()
    }
  }
}

private fun IntRange.withStart(newStart: Int): IntRange {
  return IntRange(newStart, endInclusive)
}

private fun IntRange.withEnd(newEnd: Int): IntRange {
  return IntRange(start, newEnd)
}

@DemoDeclaration
fun ChartingDemo.configurableCoordinatesSeparate(
  propertyName: String,
  property: KMutableProperty0<Coordinates>,
  config: ConfigurableDouble.() -> Unit = {},
) {
  contract {
    callsInPlace(config, InvocationKind.EXACTLY_ONCE)
  }

  val initialSize = property.get()

  configurableDouble("$propertyName x", initialSize.x) {
    max = 500.0
    config()

    onChange {
      property.set(property.get().withX(it))
      markAsDirty()
    }
  }
  configurableDouble("$propertyName y", initialSize.y) {
    max = 500.0
    config()

    onChange {
      property.set(property.get().withY(it))
      markAsDirty()
    }
  }
}

@DemoDeclaration
fun ChartingDemo.configurableBoolean(
  propertyName: String,
  config: ConfigurableBoolean.() -> Unit = {},
) {
  contract {
    callsInPlace(config, InvocationKind.EXACTLY_ONCE)
  }

  val configurable = ConfigurableBoolean(propertyName, false)
    .also(config)
    .also {
      it.keep()
    }

  declare {
    checkBox(propertyName, configurable.valueProperty)
  }

  //Notify initially
  configurable.changed(configurable.valueProperty.value)
}

@DemoDeclaration
fun ChartingDemo.configurableBooleanProvider(
  propertyName: String,
  property: KMutableProperty0<BooleanProvider>,
  config: ConfigurableBoolean.() -> Unit = {},
) {
  contract {
    callsInPlace(config, InvocationKind.EXACTLY_ONCE)
  }

  configurableBoolean(propertyName, property.get().invoke()) {
    onChange {
      property.set(BooleanProvider { it })
      markAsDirty()
    }

    config()
  }
}

/**
 * Adds a configurable boolean value
 */
@DemoDeclaration
fun ChartingDemo.configurableBoolean(
  propertyName: String,
  initialValue: Boolean,
  callChangedInitially: Boolean = true,
  config: ConfigurableBoolean.() -> Unit = {},
) {
  contract {
    callsInPlace(config, InvocationKind.EXACTLY_ONCE)
  }

  val configurable = ConfigurableBoolean(propertyName, initialValue)
    .also(config)
    .also {
      it.keep()
    }

  declare {
    checkBox(propertyName, configurable.valueProperty)
  }

  //Notify initially
  if (callChangedInitially) {
    configurable.changed(configurable.valueProperty.value)
  }
}


@DemoDeclaration
fun ChartingDemo.configurableBoolean(
  propertyName: String,
  property: KMutableProperty0<Boolean>,
  config: ConfigurableBoolean.() -> Unit = {},
) {
  contract {
    callsInPlace(config, InvocationKind.EXACTLY_ONCE)
  }

  configurableBoolean(propertyName) {
    onChange {
      property.set(it)
      markAsDirty()
    }

    value = property.get()

    config()
  }
}

@DemoDeclaration
fun ChartingDemo.configurableBoolean(
  propertyName: String,
  property: ObservableBoolean,
  config: ConfigurableBoolean.() -> Unit = {},
) {
  contract {
    callsInPlace(config, InvocationKind.EXACTLY_ONCE)
  }

  configurableBoolean(propertyName) {
    onChange {
      property.value = it
      markAsDirty()
    }

    value = property.get()

    config()
  }
}

@DemoDeclaration
inline fun <reified T : Enum<T>> ChartingDemo.configurableEnum(
  propertyName: String,
  property: ObservableObject<T>,
  possibleValues: List<T> = enumEntries(),
  crossinline config: ConfigurableEnum<T>.() -> Unit = {},
) {
  contract {
    callsInPlace(config, InvocationKind.EXACTLY_ONCE)
  }

  configurableEnum(propertyName, property.value, possibleValues) {
    onChange {
      property.value = it
      markAsDirty()
    }
    config()

    property.consume {
      value = it
    }
  }
}

/**
 * Adds a configurable enum value
 */
@DemoDeclaration
inline fun <reified T : Enum<T>> ChartingDemo.configurableEnum(
  propertyName: String,
  property: KMutableProperty0<T>,
  possibleValues: List<T> = enumEntries(),
  crossinline config: ConfigurableEnum<T>.() -> Unit = {},
) {
  contract {
    callsInPlace(config, InvocationKind.EXACTLY_ONCE)
  }

  configurableEnum(propertyName, property.get(), possibleValues) {
    onChange {
      property.set(it)
      markAsDirty()
    }

    config()
  }
}

@DemoDeclaration
inline fun <reified T : Enum<T>> ChartingDemo.configurableEnumProvider(
  propertyName: String,
  property: KMutableProperty0<() -> T>,
  possibleValues: List<T> = enumEntries(),
  crossinline config: ConfigurableEnum<T>.() -> Unit = {},
) {
  contract {
    callsInPlace(config, InvocationKind.EXACTLY_ONCE)
  }

  configurableEnum(propertyName, property.get()(), possibleValues) {
    onChange { newValue ->
      property.set { newValue }
      markAsDirty()
    }
    config()
  }
}

/**
 * Adds a configurable enum value
 */
@DemoDeclaration
inline fun <reified T : Enum<T>> ChartingDemo.configurableEnum(
  propertyName: String,
  initial: T,
  possibleValues: List<T> = enumEntries(),
  config: ConfigurableEnum<T>.() -> Unit = {},
) {
  contract {
    callsInPlace(config, InvocationKind.EXACTLY_ONCE)
  }

  val configurable = ConfigurableEnum(propertyName, initial)
    .also(config)
    .also {
      it.keep()
    }

  declare {
    comboBox(propertyName, configurable.valueProperty, possibleValues)
  }

  //Notify initially
  configurable.changed(configurable.valueProperty.value)
}

@DemoDeclaration
fun <T> ChartingDemo.configurableListWithProperty(
  propertyName: String,
  property: KMutableProperty0<T>,
  possibleValues: List<T>,
  config: ConfigurableList<T>.() -> Unit = {},
) {
  contract {
    callsInPlace(config, InvocationKind.EXACTLY_ONCE)
  }

  configurableList(propertyName, property.get(), possibleValues) {
    onChange {
      property.set(it)
      markAsDirty()
    }

    config()
  }
}

@DemoDeclaration
fun <T> ChartingDemo.configurableList(
  propertyName: String,
  initial: T,
  possibleValues: List<T>,
  config: ConfigurableList<T>.() -> Unit = {},
) {
  contract {
    callsInPlace(config, InvocationKind.EXACTLY_ONCE)
  }

  if (initial is KMutableProperty0<*>) {
    throw IllegalArgumentException("Did you mean to call configurableListWithProperty instead?")
  }

  val configurable = ConfigurableList(propertyName, initial)
    .also(config)
    .also {
      it.keep()
    }

  val updatedPossibleValues = if (possibleValues.contains(configurable.value).not()) {
    possibleValues.toMutableList().also {
      it.add(0, configurable.value)
    }
  } else {
    possibleValues
  }

  declare {
    comboBox(propertyName, configurable.valueProperty, updatedPossibleValues, configurable.converter)
  }

  //Notify initially
  configurable.changed(configurable.valueProperty.value)
}

@DemoDeclaration
fun ChartingDemo.configurableSize(
  propertyName: String = "Image Size",
  initialValue: Size,
  possibleValues: List<Size> = predefinedSizes,
  config: ConfigurableList<Size>.() -> Unit,
) {
  contract {
    callsInPlace(config, InvocationKind.EXACTLY_ONCE)
  }

  configurableList(propertyName, initialValue, possibleValues) {
    converter = {
      "${it.width}"
    }
    config()
  }
}

@DemoDeclaration
fun ChartingDemo.configurableSize(
  propertyName: String = "Size",
  property: KMutableProperty0<Size>,
  possibleValues: List<Size> = predefinedSizes,
  config: ConfigurableList<Size>.() -> Unit = {},
) {
  contract {
    callsInPlace(config, InvocationKind.EXACTLY_ONCE)
  }

  configurableList(propertyName, property.get(), possibleValues = possibleValues) {
    converter = {
      "${it.width}"
    }

    onChange {
      property.set(it)
      markAsDirty()
    }

    config()
  }
}

@DemoDeclaration
fun ChartingDemo.configurableSize(
  propertyName: String = "Size",
  property: ObservableObject<Size>,
  possibleValues: List<Size> = predefinedSizes,
  config: ConfigurableList<Size>.() -> Unit = {},
) {
  contract {
    callsInPlace(config, InvocationKind.EXACTLY_ONCE)
  }

  configurableList(propertyName, property.get(), possibleValues) {
    converter = {
      "${it.width}"
    }

    onChange {
      property.value = it
      markAsDirty()
    }

    config()
  }
}

/**
 * Returns the predefined sizes
 */
val predefinedSizes: List<Size> = listOf(Size.PX_16, Size.PX_24, Size.PX_30, Size.PX_40, Size.PX_50, Size.PX_60, Size.PX_90, Size.PX_120)

/**
 * Adds a configurable color picker
 */
@DemoDeclaration
fun ChartingDemo.configurableColorPicker(
  propertyName: String,
  initialValue: Color,
  config: ConfigurableColor.() -> Unit,
) {
  contract {
    callsInPlace(config, InvocationKind.EXACTLY_ONCE)
  }

  val configurable = ConfigurableColor(propertyName, initialValue)
    .also(config)
    .also {
      it.keep()
    }

  declare {
    colorPicker(
      propertyName,
      configurable.valueProperty,
      listOf(
        Color.orange,
        Color.transparent,
        Color.black50percent,
        Color.gray,
        Color.red,
        Color.white,
        Color.black
      )
    )
  }

  //Notify initially
  configurable.changed(configurable.valueProperty.value)
}

/**
 * Configures a color provider
 */
@DemoDeclaration
fun ChartingDemo.configurableColorPickerProvider(
  propertyName: String,
  property: KMutableProperty0<ColorProvider>,
  config: ConfigurableColor.() -> Unit = {},
) {
  contract {
    callsInPlace(config, InvocationKind.EXACTLY_ONCE)
  }

  //Use the current value of the provider
  configurableColorPicker(propertyName, property.get()()) {
    onChange {
      property.set { it }
      markAsDirty()
    }

    config()
  }
}

@DemoDeclaration
fun ChartingDemo.configurableColorPickerProviderNullable(
  propertyName: String,
  property: KMutableProperty0<() -> Color?>,
  config: ConfigurableColor.() -> Unit = {},
) {
  contract {
    callsInPlace(config, InvocationKind.EXACTLY_ONCE)
  }

  //Use the current value of the provider
  configurableColorPicker(propertyName, property.get().invoke() ?: Color.transparent) {
    onChange {
      property.set { it }
      markAsDirty()
    }

    config()
  }
}

@DemoDeclaration
fun ChartingDemo.configurableColorPicker(
  propertyName: String,
  property: KMutableProperty0<Color>,
  config: ConfigurableColor.() -> Unit = {},
) {
  contract {
    callsInPlace(config, InvocationKind.EXACTLY_ONCE)
  }

  configurableColorPicker(propertyName, property.get()) {
    onChange {
      property.set(it)
      markAsDirty()
    }

    config()
  }
}

/**
 * Converts transparent to null
 */
@DemoDeclaration
fun ChartingDemo.configurableColorPickerNullable(
  propertyName: String,
  property: KMutableProperty0<Color?>,
  config: ConfigurableColor.() -> Unit = {},
) {
  contract {
    callsInPlace(config, InvocationKind.EXACTLY_ONCE)
  }

  configurableColorPicker(propertyName, property.get() ?: Color.transparent) {
    onChange {
      val nullableColor = if (it == Color.transparent) null else it
      property.set(nullableColor)
      markAsDirty()
    }

    config()
  }
}

@DemoDeclaration
fun ChartingDemo.configurableColorPicker(
  propertyName: String,
  property: KMutableProperty0<Color?>,
  /**
   * The color that is used if the property contains null
   */
  fallbackColor: Color,
  config: ConfigurableColor.() -> Unit = {},
) {
  contract {
    callsInPlace(config, InvocationKind.EXACTLY_ONCE)
  }

  configurableColorPicker(propertyName, property.get() ?: fallbackColor) {
    onChange {
      property.set(it)
      markAsDirty()
    }

    config()
  }
}

@DemoDeclaration
fun ChartingDemo.configurableColor(
  propertyName: String,
  property: KMutableProperty0<Color>,
  config: ConfigurableList<Color>.() -> Unit = {},
) {
  contract {
    callsInPlace(config, InvocationKind.EXACTLY_ONCE)
  }

  configurableColor(propertyName, property.get()) {
    onChange {
      property.set(it)
      markAsDirty()
    }

    config()
  }
}

@DemoDeclaration
fun ChartingDemo.configurableColor(
  propertyName: String,
  initialValue: Color,
  config: ConfigurableList<Color>.() -> Unit,
) {
  contract {
    callsInPlace(config, InvocationKind.EXACTLY_ONCE)
  }

  configurableList(
    propertyName = propertyName,
    initial = initialValue,
    possibleValues = listOf(
      Color.orange, Color.transparent, Color.black50percent, Color.gray, Color.red, Color.white, Color.black
    ), config
  )
}

@DemoDeclaration
fun ChartingDemo.configurableColorNullable(
  propertyName: String,
  initialValue: Color?,
  config: ConfigurableList<Color?>.() -> Unit,
) {
  contract {
    callsInPlace(config, InvocationKind.EXACTLY_ONCE)
  }

  configurableList(
    propertyName, initialValue, listOf(
      null, Color.orange, Color.transparent, Color.black50percent, Color.gray, Color.red, Color.white, Color.black
    ), config
  )
}

@DemoDeclaration
fun ChartingDemo.configurableColorNullable(
  propertyName: String,
  property: KMutableProperty0<Color?>,
  config: ConfigurableList<Color?>.() -> Unit = {},
) {
  contract {
    callsInPlace(config, InvocationKind.EXACTLY_ONCE)
  }

  configurableColorNullable(propertyName, property.get()) {
    onChange {
      property.set(it)
      markAsDirty()
    }

    config()
  }
}


@DemoDeclaration
fun ChartingDemo.configurableDecimals(
  label: String = "Decimal places",
  property: KMutableProperty0<CachedNumberFormat>,
  config: ConfigurableInt.() -> Unit = {},
) {
  configurableInt(label) {
    val currentTickFormat = property.get()
    max = 7

    config()

    onChange {
      property.set(decimalFormat(it))
      markAsDirty()
    }

    value = currentTickFormat.extractGuessedDecimals(2)
  }
}

@DemoDeclaration
fun ChartingDemo.configurableDecimalsFormat(
  label: String = "Decimal places",
  property: KMutableProperty0<CachedNumberFormat>,
) {
  return configurableDecimals(label, property)
}

/**
 * Tries to guess the decimal places
 */
fun CachedNumberFormat.extractGuessedDecimals(fallback: Int = 2): Int {
  val defaultCachedFormat = this as? DefaultCachedFormat ?: return fallback

  val decimalFormat = defaultCachedFormat.format as? DecimalFormat ?: return fallback
  return decimalFormat.maximumFractionDigits
}

@DemoDeclaration
fun ChartingDemo.configurableFont(
  label: String = "Font",
  property: KMutableProperty0<FontDescriptorFragment>,
  config: ConfigurableFont.() -> Unit = {},
) {
  contract {
    callsInPlace(config, InvocationKind.EXACTLY_ONCE)
  }

  configurableFont(label, property.get().withDefaultValues()) {
    onChange {
      property.set(it)
      markAsDirty()
    }

    config()
  }
}

@DemoDeclaration
fun ChartingDemo.configurableFontProvider(
  label: String = "Font",
  property: KMutableProperty0<() -> FontDescriptorFragment>,
  config: ConfigurableFont.() -> Unit = {},
) {
  contract {
    callsInPlace(config, InvocationKind.EXACTLY_ONCE)
  }

  configurableFont(label, property.get().invoke().withDefaultValues()) {
    onChange {
      property.set(it.asProvider())
      markAsDirty()
    }

    config()
  }
}

/**
 * Contains some predefined line styles
 */
val predefinedLineStyles: List<LineStyle> = listOf(LineStyle.Continuous, LineStyle.Dotted, LineStyle.SmallDashes, LineStyle.LargeDashes)

fun ChartingDemo.configurableLineStyle(label: String = "Line Style", property: KMutableProperty0<LineStyle>, config: ConfigurableList<LineStyle>.() -> Unit = {}) {
  contract {
    callsInPlace(config, InvocationKind.EXACTLY_ONCE)
  }

  configurableListWithProperty(label, property, predefinedLineStyles) {
    this.converter = {
      "${it.color} | ${it.lineWidth} | ${it.dashes}"
    }

    this.config()
  }
}

@DemoDeclaration
fun ChartingDemo.configurableBoxStyle(
  propertyName: String = "Box Style",
  property: KMutableProperty0<BoxStyle>,
  paddingSeparate: Boolean = false,
) {

  val initialValue = property.get()

  section(propertyName)

  configurableColorNullable("Fill", initialValue.fill) {
    onChange { newValue ->
      property.getAndSet {
        it.copy(fill = newValue)
      }
      markAsDirty()
    }
  }

  configurableColorNullable("Border", initialValue.borderColor) {
    onChange { newValue ->
      property.getAndSet {
        it.copy(borderColor = newValue)
      }
      markAsDirty()
    }
  }

  if (paddingSeparate) {
    configurableInsetsSeparate("Padding") {
      value = initialValue.padding

      onChange { newValue ->
        property.getAndSet {
          it.copy(padding = newValue)
        }
        markAsDirty()
      }
    }
  } else {
    configurableInsets("Padding", initialValue.padding) {
      onChange { newValue ->
        property.getAndSet {
          it.copy(padding = newValue)
        }
        markAsDirty()
      }
    }
  }


  configurableDouble("Border width", initialValue.borderWidth) {
    max = 10.0

    onChange { newValue ->
      property.getAndSet {
        it.copy(borderWidth = newValue)
      }
      markAsDirty()
    }
  }

  configurableDouble("Border Radii", initialValue.radii?.topLeft ?: 0.0) {
    max = 40.0

    onChange { newValue ->
      property.getAndSet {
        it.copy(radii = BorderRadius.of(newValue))
      }
      markAsDirty()
    }
  }
}

/**
 * Appends a font settings config to the demo
 */
@DemoDeclaration
fun ChartingDemo.configurableFont(
  propertyName: String = "Font",
  initialValue: FontDescriptorFragment,
  config: ConfigurableFont.() -> Unit = {},
) {
  contract {
    callsInPlace(config, InvocationKind.EXACTLY_ONCE)
  }

  val configurableFont = ConfigurableFont(propertyName).keep()
    .also {
      it.applyValues(initialValue)
    }
    .also(config)

  declare {
    section(propertyName) {}
  }

  configurableDouble("Font Size", configurableFont.fontSizeProperty.value) {
    min = 1.0
    max = configurableFont.maxFontSize

    onChange {
      configurableFont.fontSizeProperty.value = it
    }
  }

  configurableList(
    "Family", configurableFont.fontFamilyProperty.value, listOf(
      FontFamily.SansSerif, FontFamily.Serif, FontFamily.Monospace,
      FontFamily("Arial"),
      FontFamily("Comic Sans MS"),
      FontFamily("Courier New"),
      FontFamily("Oswald"),
      FontFamily("Open Sans "),
      FontFamily("Segoe UI"),
      FontFamily("Ubuntu")
    )
  ) {
    converter = {
      it.family
    }

    onChange {
      configurableFont.fontFamilyProperty.value = it
    }
  }

  configurableList(
    "Weight", configurableFont.fontWeightProperty.value.weight, listOf(
      FontWeight.Thin.weight,
      FontWeight.ExtraLight.weight,
      FontWeight.Light.weight,
      FontWeight.Regular.weight,
      FontWeight.Medium.weight,
      FontWeight.SemiBold.weight,
      FontWeight.Bold.weight,
      FontWeight.ExtraBold.weight,
      FontWeight.Black.weight
    )
  ) {
    onChange {
      configurableFont.fontWeightProperty.value = FontWeight(it)
    }
  }

  configurableEnum("Style", configurableFont.fontStyleProperty.value, FontStyle.entries) {
    onChange {
      configurableFont.fontStyleProperty.value = it
    }
  }

  configurableEnum("Variant", configurableFont.fontVariantProperty.value, FontVariant.entries) {
    onChange {
      configurableFont.fontVariantProperty.value = it
    }
  }
}

/**
 * Abstract base class for configurable
 */
abstract class AbstractConfigurable<T>(val propertyName: String) {
  var onChange: MutableList<(T) -> Unit> = mutableListOf()

  /**
   * Call this method for every change
   */
  fun changed(newValue: T) {
    if (true) {
      if (onChange.isEmpty()) {
        ChartingDemo.logger.debug("No onChange event registered for $propertyName! This is probably a bug! Maybe you wanted to use a property instead: object::prop")
      }

    } else {
      //TODO reactivate later!
      require(onChange.isEmpty()) {
        "No onChange event registered for $propertyName! This is probably a bug! Maybe you wanted to use a property instead: object::prop"
      }
    }

    onChange.forEach {
      it(newValue)
    }
  }

  /**
   * [action] will be called for each change
   */
  fun onChange(action: (T) -> Unit) {
    onChange.add(action)
  }
}

abstract class AbstractConfigurableWithValue<T>(propertyName: String, initial: T) : AbstractConfigurable<T>(propertyName) {
  val valueProperty: ObservableObject<T> = ObservableObject(initial)
  var value: T by valueProperty

  init {
    valueProperty.consume {
      changed(it)
    }
  }

}

/**
 * Configuration for a double
 */
class ConfigurableDouble(propertyName: String, initialValue: Double) : AbstractConfigurableWithValue<Double>(propertyName, initialValue) {
  var min: Double = 0.0
    set(value) {
      if (value > this.value) {
        ChartingDemo.logger.warn("WARNING. Min <$value> too low for current value: <${this.value}>")
      }

      field = value
    }

  var max: Double = 1.0
    set(value) {
      if (value < this.value) {
        ChartingDemo.logger.warn("WARNING. Max <$value> too low for current value: <${this.value}>")
      }

      field = value
    }

  /**
   * This is a hint for the control that alters this [ConfigurableDouble] at how much the current value should be adjusted.
   */
  var step: Double? = null


  /**
   * The last *known* value that has not been NaN.
   * Is used to reset when disabling the NaN checkbox
   */
  private var lastValueBeforeNan: Double = 0.0

  val isNanProperty: ObservableBoolean = ObservableBoolean(value.isNaN()).also { isNanProperty ->
    isNanProperty.consume { newIsNan ->
      if (newIsNan) {
        value = Double.NaN
      } else if (value.isNaN()) {
        value = lastValueBeforeNan
      }
    }

    valueProperty.consumeImmediately { newValue ->
      if (newValue.isFinite()) {
        lastValueBeforeNan = newValue
      }
      isNanProperty.value = newValue.isNaN()
    }
  }
  var isNan: Boolean by isNanProperty

  internal fun validate(propertyName: String) {
    if (value.isFinite().not()) {
      return
    }

    check(value >= min) { "$propertyName: value <$value> is less than min <$min>" }
    check(value <= max) { "$propertyName: value <$value> is greater than max <$max>" }
  }
}

class ConfigurableBoolean(propertyName: String, initialValue: Boolean) : AbstractConfigurableWithValue<Boolean>(propertyName, initialValue) {
}

class ConfigurableInsets(propertyName: String, initialValue: Insets) : AbstractConfigurableWithValue<Insets>(propertyName, initialValue) {
  var min: Double = 0.0
  var max: Double = 100.0

  fun setFromInsets(value: Insets) {
    top = value.top
    left = value.left
    right = value.right
    bottom = value.bottom
  }

  /**
   * Returns the *LEFT* value as placeholder for all.
   * Sets all a new value to *all* sides
   */
  var all: Double
    get() = value.left
    set(value) {
      this.value = Insets.of(value)
    }

  var top: Double
    get() = value.top
    set(value) {
      this.value = this.value.copy(top = value)
    }

  var left: Double
    get() = value.left
    set(value) {
      this.value = this.value.copy(left = value)
    }

  var right: Double
    get() = value.right
    set(value) {
      this.value = this.value.copy(right = value)
    }

  var bottom: Double
    get() = value.bottom
    set(value) {
      this.value = this.value.copy(bottom = value)
    }
}

class ConfigurableInt(propertyName: String) : AbstractConfigurableWithValue<Int>(propertyName, 0) {
  var min: Int = 0
  var max: Int = 10

  /**
   * This is a hint for the control that alters this [ConfigurableInt] at how much the current value should be adjusted.
   */
  var step: Int? = null

  internal fun validate(propertyName: String) {
    check(value >= min) { "$propertyName: value <$value> is less than min <$min>" }
    check(value <= max) { "$propertyName: value <$value> is greater than max <$max>" }
  }
}

class ConfigurableEnum<T : Enum<T>>(propertyName: String, initial: T) : AbstractConfigurableWithValue<T>(propertyName, initial) {
}

class ConfigurableList<T>(propertyName: String, initial: T) : AbstractConfigurableWithValue<T>(propertyName, initial) {
  var converter: (T) -> String = {
    it?.toString() ?: "-"
  }

  fun converter(converter: (T) -> String) {
    this.converter = converter
  }
}

class ConfigurableColor(propertyName: String, initialValue: Color) : AbstractConfigurableWithValue<Color>(propertyName, initialValue) {
}

class ConfigurableFont(propertyName: String) : AbstractConfigurable<FontDescriptor>(propertyName) {
  val maxFontSize: Double = 256.0

  val fontFamilyProperty: ObservableObject<FontFamily> = ObservableObject(FontFamily.SansSerif)
  val fontSizeProperty: ObservableDouble = ObservableDouble(16.0)
  val fontWeightProperty: ObservableObject<FontWeight> = ObservableObject(FontWeight.Normal)
  val fontStyleProperty: ObservableObject<FontStyle> = ObservableObject(FontStyle.Normal)
  val fontVariantProperty: ObservableObject<FontVariant> = ObservableObject(FontVariant.Normal)

  var value: FontDescriptor
    get() = toFontDescriptor()
    set(value) {
      fontFamilyProperty.value = value.family
      fontSizeProperty.value = value.size.size
      fontWeightProperty.value = value.weight
      fontStyleProperty.value = value.style
      fontVariantProperty.value = value.variant
    }

  fun toFontDescriptor(): FontDescriptor {
    return FontDescriptor(fontFamilyProperty.get(), FontSize(fontSizeProperty.get()), fontWeightProperty.get(), fontStyleProperty.get(), fontVariantProperty.get())
  }

  fun applyValues(source: FontDescriptorFragment) {
    source.family?.let {
      fontFamilyProperty.value = it
    }
    source.size?.let {
      fontSizeProperty.value = it.size
    }
    source.weight?.let {
      fontWeightProperty.value = it
    }
    source.style?.let {
      fontStyleProperty.value = it
    }
    source.variant?.let {
      fontVariantProperty.value = it
    }
  }

  init {
    //The action that is executed whenever a font value is updated
    val action: (newValue: Any) -> Unit = {
      changed(toFontDescriptor())
    }

    fontFamilyProperty.consume(action = action)
    fontSizeProperty.consume(action = action)
    fontWeightProperty.consume(action = action)
    fontStyleProperty.consume(action = action)
    fontVariantProperty.consumeImmediately(action) //Immediately
  }
}


@Target(AnnotationTarget.FUNCTION, AnnotationTarget.TYPE, AnnotationTarget.CLASS)
@DslMarker
annotation class DemoDeclaration
