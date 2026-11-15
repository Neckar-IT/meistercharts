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
@file:Suppress("RETURN_VALUE_NOT_USED") // Demo-scope subscriptions tied to the demo lifetime

package com.meistercharts.demo

import com.meistercharts.algorithms.layers.linechart.LineStyle
import com.meistercharts.canvas.DirtyReason
import com.meistercharts.canvas.MeisterchartBuilder
import com.meistercharts.canvas.layer.LayerSupport
import com.meistercharts.color.Color
import com.meistercharts.color.ColorProvider
import com.meistercharts.color.ColorProviderNullable
import com.meistercharts.color.get
import com.meistercharts.font.FontDescriptor
import com.meistercharts.font.FontDescriptorFragment
import com.meistercharts.font.FontFamily
import com.meistercharts.font.FontSize
import com.meistercharts.font.FontStyle
import com.meistercharts.font.FontVariant
import com.meistercharts.font.FontWeight
import com.meistercharts.font.GenericFontFamily
import com.meistercharts.model.BorderRadius
import com.meistercharts.model.Insets
import com.meistercharts.range.ValueRange
import com.meistercharts.style.BoxStyle
import it.neckar.financial.currency.Money
import it.neckar.geometry.Coordinates
import it.neckar.geometry.Size
import it.neckar.logging.Logger
import it.neckar.logging.LoggerFactory
import it.neckar.open.formatting.CachedNumberFormat
import it.neckar.open.formatting.DecimalFormat
import it.neckar.open.formatting.DefaultCachedFormat
import it.neckar.open.formatting.decimalFormat
import it.neckar.open.kotlin.lang.asProvider
import it.neckar.open.kotlin.lang.getAndSet
import it.neckar.open.observable.DependentObjects
import it.neckar.open.observable.ObservableBoolean
import it.neckar.open.observable.ObservableDouble
import it.neckar.open.observable.ObservableObject
import it.neckar.open.provider.BooleanProvider
import it.neckar.open.provider.DoubleProvider
import it.neckar.open.unit.si.ms
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.jvm.JvmName
import kotlin.math.PI
import kotlin.reflect.KMutableProperty0
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.DurationUnit

/*
 * The `configurable*` demo DSL: extension functions on MeisterchartsDemo that add one interactive
 * control for a property to the demo's configuration pane.
 *
 * Naming axes for the value kinds:
 * - plain (e.g. configurableColor / configurableDouble) binds a mutable property directly.
 * - `Provider` binds a lambda/provider-typed property.
 * - `Nullable` allows a null value.
 * - `ColorPicker` uses the colour-picker widget (vs. a plain colour by name/hex).
 * - `Separate` exposes each component (e.g. the insets/size sides) as its own control.
 *
 * The backing model classes live in ConfigurableModel.kt.
 */

/**
 * Adds a configurable integer value
 */
@DemoDeclaration
fun MeisterchartsDemo.configurableInt(
  propertyName: String,
  initialValue: Int = 0,
  config: ConfigurableInt.() -> Unit = {},
) {
  contract {
    callsInPlace(config, InvocationKind.EXACTLY_ONCE)
  }

  val configurable = ConfigurableInt(propertyName, initialValue)
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
@Deprecated("Use configurable", ReplaceWith("configurable(propertyName, property, config)"))
fun MeisterchartsDemo.configurableInt(
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
fun MeisterchartsDemo.configurableDouble(
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
@Deprecated("Use configurable", ReplaceWith("configurable(propertyName, property, config)"))
fun MeisterchartsDemo.configurableDouble(
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
@Deprecated("Use configurable", ReplaceWith("configurable(propertyName, property, config)"))
fun MeisterchartsDemo.configurableDouble(
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
@Deprecated("Use configurable", ReplaceWith("configurable(propertyName, property, fallbackValue, config)"))
fun MeisterchartsDemo.configurableDoubleNullable(
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
@Deprecated("Use configurable", ReplaceWith("configurable(propertyName, property, config)"))
fun MeisterchartsDemo.configurableMoney(
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
@Deprecated("Use configurable", ReplaceWith("configurable(propertyName, property, fallbackValue, config)"))
fun MeisterchartsDemo.configurableDouble(
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
@Deprecated("Use configurable", ReplaceWith("configurable(propertyName, property, config)"))
fun MeisterchartsDemo.configurableDoubleProvider(
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
 * Adds a configurable duration value
 */
@DemoDeclaration
fun MeisterchartsDemo.configurableDuration(
  propertyName: String,
  initialValue: @ms Duration,
  callChangedInitially: Boolean = true,
  config: ConfigurableDouble.() -> Unit = {},
) {
  contract {
    callsInPlace(config, InvocationKind.EXACTLY_ONCE)
  }

  val configurable = ConfigurableDouble(propertyName, initialValue.toDouble(DurationUnit.MILLISECONDS))
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
@Deprecated("Use configurable", ReplaceWith("configurable(propertyName, property, config)"))
fun MeisterchartsDemo.configurableDuration(
  propertyName: String,
  property: KMutableProperty0<@ms Duration>,
  config: ConfigurableDouble.() -> Unit = {},
) {
  contract {
    callsInPlace(config, InvocationKind.EXACTLY_ONCE)
  }

  configurableDouble(propertyName, property.get().toDouble(DurationUnit.MILLISECONDS)) {
    onChange {
      property.set(it.milliseconds)
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
fun MeisterchartsDemo.configurableRad(
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
fun MeisterchartsDemo.configurableRad(
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
@Deprecated("Use configurable", ReplaceWith("configurable(propertyName, property, config)"))
fun MeisterchartsDemo.configurableDouble(
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
@JvmName("configurableDoublePropertyLabellessUnified")
@Suppress("DEPRECATION")
fun MeisterchartsDemo.configurable(
  property: KMutableProperty0<Double>,
  config: ConfigurableDouble.() -> Unit = {},
) = configurableDouble(property, config)

@DemoDeclaration
@Deprecated("Use configurable", ReplaceWith("configurable(property, config)"))
fun MeisterchartsDemo.configurableDouble(
  property: KMutableProperty0<Double>,
  config: ConfigurableDouble.() -> Unit = {},
) {
  contract {
    callsInPlace(config, InvocationKind.EXACTLY_ONCE)
  }

  return configurableDouble(property.name, property, config)
}


@DemoDeclaration
fun MeisterchartsDemo.configurableInsets(
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
@Deprecated("Use configurableSeparate", ReplaceWith("configurableSeparate(propertyName, property, config)"))
fun MeisterchartsDemo.configurableInsetsSeparate(
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
@Deprecated("Use configurable", ReplaceWith("configurable(propertyName, property, config)"))
fun MeisterchartsDemo.configurableInsets(
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
@Deprecated("Use configurable", ReplaceWith("configurable(propertyName, property, config)"))
fun MeisterchartsDemo.configurableInsets(
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
fun MeisterchartsDemo.configurableInsetsSeparate(
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
@JvmName("configurableStringPropertyLabellessUnified")
@Suppress("DEPRECATION")
fun MeisterchartsDemo.configurable(
  property: KMutableProperty0<String>,
  config: ConfigurableString.() -> Unit = {},
) = configurableString(property, config)

@DemoDeclaration
@Deprecated("Use configurable", ReplaceWith("configurable(property, config)"))
fun MeisterchartsDemo.configurableString(
  property: KMutableProperty0<String>,
  config: ConfigurableString.() -> Unit = {},
) {
  contract {
    callsInPlace(config, InvocationKind.EXACTLY_ONCE)
  }

  return configurableString(property.name, property, config)
}

@DemoDeclaration
@Deprecated("Use configurable", ReplaceWith("configurable(propertyName, property, config)"))
fun MeisterchartsDemo.configurableString(
  propertyName: String,
  property: KMutableProperty0<String>,
  config: ConfigurableString.() -> Unit = {},
) {
  contract {
    callsInPlace(config, InvocationKind.EXACTLY_ONCE)
  }

  configurableString(propertyName, property.get()) {
    onChange {
      property.set(it)
      markAsDirty()
    }

    config()
  }
}

@DemoDeclaration
fun MeisterchartsDemo.configurableString(
  propertyName: String,
  initialValue: String,
  callChangedInitially: Boolean = true,
  config: ConfigurableString.() -> Unit = {},
) {
  contract {
    callsInPlace(config, InvocationKind.EXACTLY_ONCE)
  }

  val configurable = ConfigurableString(propertyName, initialValue)
    .also(config)
    .also {
      it.keep()
    }

  declare {
    val property = configurable.valueProperty
    textInput(propertyName, property)
  }

  //Notify initially
  if (callChangedInitially) {
    configurable.changed(configurable.valueProperty.value)
  }
}


@DemoDeclaration
@Deprecated("Use configurableSeparate", ReplaceWith("configurableSeparate(propertyName, property, config)"))
fun MeisterchartsDemo.configurableSizeSeparate(
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
@Deprecated("Use configurableSeparate", ReplaceWith("configurableSeparate(propertyName, property, config)"))
@Suppress("DEPRECATION")
fun MeisterchartsDemo.configurableInsetsProviderSeparate(
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
@Deprecated("Use configurable", ReplaceWith("configurable(propertyName, property, config)"))
@Suppress("DEPRECATION")
fun MeisterchartsDemo.configurableInsetsProvider(
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
@Deprecated("Use configurableSeparate", ReplaceWith("configurableSeparate(propertyName, property, config)"))
fun MeisterchartsDemo.configurableInsetsSeparate(
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
@Deprecated("Use configurable", ReplaceWith("configurable(propertyName, property, config)"))
fun MeisterchartsDemo.configurableValueRange(
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
@Deprecated("Use configurable", ReplaceWith("configurable(propertyName, property, config)"))
fun MeisterchartsDemo.configurableValueRangeProvider(
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
@Deprecated("Use configurable", ReplaceWith("configurable(propertyName, property, config)"))
fun MeisterchartsDemo.configurableRange(
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
@Deprecated("Use configurableSeparate", ReplaceWith("configurableSeparate(propertyName, property, config)"))
fun MeisterchartsDemo.configurableCoordinatesSeparate(
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
fun MeisterchartsDemo.configurableBoolean(
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
@Deprecated("Use configurable", ReplaceWith("configurable(propertyName, property, config)"))
fun MeisterchartsDemo.configurableBooleanProvider(
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
fun MeisterchartsDemo.configurableBoolean(
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
@Deprecated("Use configurable", ReplaceWith("configurable(propertyName, property, config)"))
fun MeisterchartsDemo.configurableBoolean(
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
@Deprecated("Use configurable", ReplaceWith("configurable(propertyName, property, config)"))
fun MeisterchartsDemo.configurableBoolean(
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
@Deprecated("Use configurable", ReplaceWith("configurable(propertyName, property, possibleValues, config)"))
inline fun <reified T : Enum<T>> MeisterchartsDemo.configurableEnum(
  propertyName: String,
  property: ObservableObject<T>,
  possibleValues: List<T> = kotlin.enums.enumEntries<T>().toList(),
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
@Deprecated("Use configurable", ReplaceWith("configurable(propertyName, property, possibleValues, config)"))
inline fun <reified T : Enum<T>> MeisterchartsDemo.configurableEnum(
  propertyName: String,
  property: KMutableProperty0<T>,
  possibleValues: List<T> = kotlin.enums.enumEntries<T>().toList(),
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
inline fun <reified T : Enum<T>> MeisterchartsDemo.configurableEnumProvider(
  propertyName: String,
  property: KMutableProperty0<() -> T>,
  possibleValues: List<T> = kotlin.enums.enumEntries<T>().toList(),
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
inline fun <reified T : Enum<T>> MeisterchartsDemo.configurableEnum(
  propertyName: String,
  initial: T,
  possibleValues: List<T> = kotlin.enums.enumEntries<T>().toList(),
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
fun <T> MeisterchartsDemo.configurableListWithProperty(
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
fun <T> MeisterchartsDemo.configurableList(
  propertyName: String,
  initial: T,
  possibleValues: List<T>,
  config: ConfigurableList<T>.() -> Unit = {},
) {
  contract {
    callsInPlace(config, InvocationKind.EXACTLY_ONCE)
  }

  if (initial is KMutableProperty0<*>) {
    throw IllegalArgumentException("[${this.demoDescriptor.name}] Did you mean to call configurableListWithProperty instead? for $propertyName")
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
fun MeisterchartsDemo.configurableSize(
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
@Deprecated("Use configurable", ReplaceWith("configurable(propertyName, property, possibleValues, config)"))
fun MeisterchartsDemo.configurableSize(
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
@Deprecated("Use configurable", ReplaceWith("configurable(propertyName, property, possibleValues, config)"))
fun MeisterchartsDemo.configurableSize(
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
fun MeisterchartsDemo.configurableColorPicker(
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

//TODO inline?
@DemoDeclaration
@Deprecated("Use configurable", ReplaceWith("configurable(propertyName, property, config)"))
fun MeisterchartsDemo.configurableColorPickerProviderNullable(
  propertyName: String,
  property: KMutableProperty0<ColorProviderNullable>,
  config: ConfigurableColor.() -> Unit = {},
) {
  configurableColorPicker(propertyName, property.get().get() ?: Color.transparent()) {
    onChange { newColor ->
      val nullableColor = if (newColor == Color.transparent.get()) null else newColor
      property.set(nullableColor.asProvider())
      markAsDirty()
    }

    config()
  }
}

@DemoDeclaration
@Deprecated("Use configurable", ReplaceWith("configurable(propertyName, property, config)"))
fun MeisterchartsDemo.configurableColorPicker(
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

@DemoDeclaration
@Deprecated("Use configurable", ReplaceWith("configurable(propertyName, property, config)"))
fun MeisterchartsDemo.configurableColorPickerProvider(
  propertyName: String,
  property: KMutableProperty0<ColorProvider>,
  config: ConfigurableColor.() -> Unit = {},
) {
  contract {
    callsInPlace(config, InvocationKind.EXACTLY_ONCE)
  }

  configurableColorPicker(propertyName, property.get().invoke()) {
    onChange {
      property.set(it.asProvider())
      markAsDirty()
    }

    config()
  }
}

/**
 * Converts transparent to null
 */
@DemoDeclaration
@Deprecated("Use configurable", ReplaceWith("configurable(propertyName, property, config)"))
fun MeisterchartsDemo.configurableColorPickerNullable(
  propertyName: String,
  property: KMutableProperty0<Color?>,
  config: ConfigurableColor.() -> Unit = {},
) {
  configurableColorPicker(propertyName, property.get() ?: Color.transparent()) {
    onChange { newColor ->
      val nullableColor = if (newColor == Color.transparent) null else newColor
      property.set(nullableColor)
      markAsDirty()
    }

    config()
  }
}

@DemoDeclaration
fun MeisterchartsDemo.configurableColorPicker(
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
fun MeisterchartsDemo.configurableColor(
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
fun MeisterchartsDemo.configurableColorProvider(
  propertyName: String,
  property: KMutableProperty0<ColorProvider>,
  config: ConfigurableList<Color>.() -> Unit = {},
) {
  contract {
    callsInPlace(config, InvocationKind.EXACTLY_ONCE)
  }

  configurableColor(propertyName, property.get().invoke()) {
    onChange {
      property.set(it.asProvider())
      markAsDirty()
    }

    config()
  }
}

@DemoDeclaration
fun MeisterchartsDemo.configurableColor(
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
      Color.orange(), Color.transparent(), Color.black50percent(), Color.gray(), Color.red(), Color.white(), Color.black()
    ), config
  )
}

@DemoDeclaration
fun MeisterchartsDemo.configurableColorNullable(
  propertyName: String,
  initialValue: Color?,
  config: ConfigurableList<Color?>.() -> Unit,
) {
  contract {
    callsInPlace(config, InvocationKind.EXACTLY_ONCE)
  }

  configurableList(
    propertyName, initialValue, listOf(
      null, Color.orange(), Color.transparent(), Color.black50percent(), Color.gray(), Color.red(), Color.white(), Color.black()
    ), config
  )
}

@DemoDeclaration
fun MeisterchartsDemo.configurableColorNullable(
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
fun MeisterchartsDemo.configurableColorProviderNullable(
  propertyName: String,
  property: KMutableProperty0<ColorProviderNullable>,
  config: ConfigurableList<Color?>.() -> Unit = {},
) {
  contract {
    callsInPlace(config, InvocationKind.EXACTLY_ONCE)
  }

  configurableColorNullable(propertyName, property.get().get()) {
    onChange {
      property.set(it.asProvider())
      markAsDirty()
    }

    config()
  }
}


@DemoDeclaration
@Deprecated("Use configurable", ReplaceWith("configurable(label, property, config)"))
fun MeisterchartsDemo.configurableDecimals(
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
fun MeisterchartsDemo.configurableDecimalsFormat(
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
@Deprecated("Use configurable", ReplaceWith("configurable(label, property, config)"))
fun MeisterchartsDemo.configurableFont(
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
@Deprecated("Use configurable", ReplaceWith("configurable(label, property, config)"))
fun MeisterchartsDemo.configurableFontProvider(
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

@Deprecated("Use configurable", ReplaceWith("configurable(label, property, config)"))
fun MeisterchartsDemo.configurableLineStyle(label: String = "Line Style", property: KMutableProperty0<LineStyle>, config: ConfigurableList<LineStyle>.() -> Unit = {}) {
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
@Deprecated("Use configurable", ReplaceWith("configurable(propertyName, property, paddingSeparate)"))
fun MeisterchartsDemo.configurableBoxStyle(
  propertyName: String = "Box Style",
  property: KMutableProperty0<BoxStyle>,
  paddingSeparate: Boolean = false,
) {

  val initialValue = property.get()

  section(propertyName)

  configurableColorNullable("Fill", initialValue.fill.get()) {
    onChange { newValue ->
      property.getAndSet {
        it.copy(fill = newValue.asProvider())
      }
      markAsDirty()
    }
  }

  configurableColorNullable("Border", initialValue.borderColor.get()) {
    onChange { newValue ->
      property.getAndSet {
        it.copy(borderColor = newValue.asProvider())
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
fun MeisterchartsDemo.configurableFont(
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
    section(propertyName)
  }

  configurableDouble("Font Size", configurableFont.fontSizeProperty.value) {
    min = 1.0
    max = configurableFont.maxFontSize

    onChange {
      configurableFont.fontSizeProperty.value = it
    }
  }

  configurableList(
    "Family",
    initial = configurableFont.fontFamilyProperty.value,
    possibleValues = buildList {
      add(null)
      addAll(FontFamily.entries)

      add(FontFamily("Arial"))
      add(FontFamily("Comic Sans MS"))
      add(FontFamily("Courier New"))
      add(FontFamily("Segoe UI"))
      add(FontFamily("Ubuntu"))
    }

  ) {
    converter = {
      it?.family ?: "-"
    }

    onChange {
      configurableFont.fontFamilyProperty.value = it
    }
  }

  configurableEnum(
    "Generic Family",
    configurableFont.fontGenericFamilyProperty.value,
  ) {
    onChange {
      configurableFont.fontGenericFamilyProperty.value = it
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

/*
 * ============================================================================
 * Unified property-based demo control API
 * ============================================================================
 *
 * `configurable(label, property)` picks the interactive control from the
 * property's type: the property/observable type disambiguates the overload at
 * the call site. `configurableSeparate(label, property)` exposes each component
 * of a compound value (insets sides, size width/height, coordinates x/y) as its
 * own control.
 *
 * These overloads are additive. The legacy value-based `configurableXxx(label,
 * value)` overloads are kept as a fallback for call sites that bind a plain
 * value instead of a property or observable.
 *
 * Each overload carries a distinct `@JvmName`: the property/observable generic
 * argument erases to the same JVM signature (every `KMutableProperty0<…>`
 * erases to `KMutableProperty0`, every `ObservableObject<…>` to
 * `ObservableObject`), which would otherwise clash on the JVM.
 */

@DemoDeclaration
@JvmName("configurableIntUnified")
@Suppress("DEPRECATION")
fun MeisterchartsDemo.configurable(
  propertyName: String,
  property: KMutableProperty0<Int>,
  config: ConfigurableInt.() -> Unit = {},
) = configurableInt(propertyName, property, config)

@DemoDeclaration
@JvmName("configurableDoubleObservableObjectUnified")
@Suppress("DEPRECATION")
fun MeisterchartsDemo.configurable(
  propertyName: String,
  property: ObservableObject<Double>,
  config: ConfigurableDouble.() -> Unit = {},
) = configurableDouble(propertyName, property, config)

@DemoDeclaration
@JvmName("configurableDoublePropertyUnified")
@Suppress("DEPRECATION")
fun MeisterchartsDemo.configurable(
  propertyName: String,
  property: KMutableProperty0<Double>,
  config: ConfigurableDouble.() -> Unit = {},
) = configurableDouble(propertyName, property, config)

@DemoDeclaration
@JvmName("configurableDoubleObservableUnified")
@Suppress("DEPRECATION")
fun MeisterchartsDemo.configurable(
  propertyName: String,
  property: ObservableDouble,
  config: ConfigurableDouble.() -> Unit = {},
) = configurableDouble(propertyName, property, config)

//Collision: configurableDoubleNullable(String, KMutableProperty0<Double?>, Double, config) and
//configurableDouble(String, KMutableProperty0<Double?>, Double, config) share this exact source
//signature. They are collapsed into this single unified overload (forwarding to configurableDouble).
@DemoDeclaration
@JvmName("configurableDoubleNullableUnified")
@Suppress("DEPRECATION")
fun MeisterchartsDemo.configurable(
  propertyName: String,
  property: KMutableProperty0<Double?>,
  fallbackValue: Double,
  config: ConfigurableDouble.() -> Unit = {},
) = configurableDouble(propertyName, property, fallbackValue, config)

@DemoDeclaration
@JvmName("configurableMoneyUnified")
@Suppress("DEPRECATION")
fun MeisterchartsDemo.configurable(
  propertyName: String,
  property: KMutableProperty0<Money>,
  config: ConfigurableDouble.() -> Unit = {},
) = configurableMoney(propertyName, property, config)

@DemoDeclaration
@JvmName("configurableDoubleProviderUnified")
@Suppress("DEPRECATION")
fun MeisterchartsDemo.configurable(
  propertyName: String,
  property: KMutableProperty0<DoubleProvider>,
  config: ConfigurableDouble.() -> Unit = {},
) = configurableDoubleProvider(propertyName, property, config)

@DemoDeclaration
@JvmName("configurableDurationUnified")
@Suppress("DEPRECATION")
fun MeisterchartsDemo.configurable(
  propertyName: String,
  property: KMutableProperty0<@ms Duration>,
  config: ConfigurableDouble.() -> Unit = {},
) = configurableDuration(propertyName, property, config)

@DemoDeclaration
@JvmName("configurableInsetsPropertyUnified")
@Suppress("DEPRECATION")
fun MeisterchartsDemo.configurable(
  propertyName: String,
  property: KMutableProperty0<Insets>,
  config: ConfigurableInsets.() -> Unit = {},
) = configurableInsets(propertyName, property, config)

@DemoDeclaration
@JvmName("configurableInsetsObservableUnified")
@Suppress("DEPRECATION")
fun MeisterchartsDemo.configurable(
  propertyName: String,
  property: ObservableObject<Insets>,
  config: ConfigurableInsets.() -> Unit = {},
) = configurableInsets(propertyName, property, config)

@DemoDeclaration
@JvmName("configurableInsetsProviderUnified")
@Suppress("DEPRECATION")
fun MeisterchartsDemo.configurable(
  propertyName: String,
  property: KMutableProperty0<() -> Insets>,
  config: ConfigurableInsets.() -> Unit = {},
) = configurableInsetsProvider(propertyName, property, config)

@DemoDeclaration
@JvmName("configurableStringUnified")
@Suppress("DEPRECATION")
fun MeisterchartsDemo.configurable(
  propertyName: String,
  property: KMutableProperty0<String>,
  config: ConfigurableString.() -> Unit = {},
) = configurableString(propertyName, property, config)

@DemoDeclaration
@JvmName("configurableValueRangeUnified")
@Suppress("DEPRECATION")
fun MeisterchartsDemo.configurable(
  propertyName: String,
  property: KMutableProperty0<ValueRange>,
  config: ConfigurableDouble.() -> Unit = {},
) = configurableValueRange(propertyName, property, config)

@DemoDeclaration
@JvmName("configurableValueRangeProviderUnified")
@Suppress("DEPRECATION")
fun MeisterchartsDemo.configurable(
  propertyName: String,
  property: KMutableProperty0<() -> ValueRange>,
  config: ConfigurableDouble.() -> Unit = {},
) = configurableValueRangeProvider(propertyName, property, config)

@DemoDeclaration
@JvmName("configurableRangeUnified")
@Suppress("DEPRECATION")
fun MeisterchartsDemo.configurable(
  propertyName: String,
  property: KMutableProperty0<IntRange>,
  config: ConfigurableInt.() -> Unit = {},
) = configurableRange(propertyName, property, config)

@DemoDeclaration
@JvmName("configurableBooleanProviderUnified")
@Suppress("DEPRECATION")
fun MeisterchartsDemo.configurable(
  propertyName: String,
  property: KMutableProperty0<BooleanProvider>,
  config: ConfigurableBoolean.() -> Unit = {},
) = configurableBooleanProvider(propertyName, property, config)

@DemoDeclaration
@JvmName("configurableBooleanPropertyUnified")
@Suppress("DEPRECATION")
fun MeisterchartsDemo.configurable(
  propertyName: String,
  property: KMutableProperty0<Boolean>,
  config: ConfigurableBoolean.() -> Unit = {},
) = configurableBoolean(propertyName, property, config)

@DemoDeclaration
@JvmName("configurableBooleanObservableUnified")
@Suppress("DEPRECATION")
fun MeisterchartsDemo.configurable(
  propertyName: String,
  property: ObservableBoolean,
  config: ConfigurableBoolean.() -> Unit = {},
) = configurableBoolean(propertyName, property, config)

@DemoDeclaration
@JvmName("configurableSizePropertyUnified")
@Suppress("DEPRECATION")
fun MeisterchartsDemo.configurable(
  propertyName: String = "Size",
  property: KMutableProperty0<Size>,
  possibleValues: List<Size> = predefinedSizes,
  config: ConfigurableList<Size>.() -> Unit = {},
) = configurableSize(propertyName, property, possibleValues, config)

@DemoDeclaration
@JvmName("configurableSizeObservableUnified")
@Suppress("DEPRECATION")
fun MeisterchartsDemo.configurable(
  propertyName: String = "Size",
  property: ObservableObject<Size>,
  possibleValues: List<Size> = predefinedSizes,
  config: ConfigurableList<Size>.() -> Unit = {},
) = configurableSize(propertyName, property, possibleValues, config)

@DemoDeclaration
@JvmName("configurableColorPickerUnified")
@Suppress("DEPRECATION")
fun MeisterchartsDemo.configurable(
  propertyName: String,
  property: KMutableProperty0<Color>,
  config: ConfigurableColor.() -> Unit = {},
) = configurableColorPicker(propertyName, property, config)

@DemoDeclaration
@JvmName("configurableColorPickerProviderUnified")
@Suppress("DEPRECATION")
fun MeisterchartsDemo.configurable(
  propertyName: String,
  property: KMutableProperty0<ColorProvider>,
  config: ConfigurableColor.() -> Unit = {},
) = configurableColorPickerProvider(propertyName, property, config)

@DemoDeclaration
@JvmName("configurableColorPickerNullableUnified")
@Suppress("DEPRECATION")
fun MeisterchartsDemo.configurable(
  propertyName: String,
  property: KMutableProperty0<Color?>,
  config: ConfigurableColor.() -> Unit = {},
) = configurableColorPickerNullable(propertyName, property, config)

@DemoDeclaration
@JvmName("configurableColorPickerProviderNullableUnified")
@Suppress("DEPRECATION")
fun MeisterchartsDemo.configurable(
  propertyName: String,
  property: KMutableProperty0<ColorProviderNullable>,
  config: ConfigurableColor.() -> Unit = {},
) = configurableColorPickerProviderNullable(propertyName, property, config)

@DemoDeclaration
@JvmName("configurableDecimalsUnified")
@Suppress("DEPRECATION")
fun MeisterchartsDemo.configurable(
  label: String = "Decimal places",
  property: KMutableProperty0<CachedNumberFormat>,
  config: ConfigurableInt.() -> Unit = {},
) = configurableDecimals(label, property, config)

@DemoDeclaration
@JvmName("configurableFontUnified")
@Suppress("DEPRECATION")
fun MeisterchartsDemo.configurable(
  label: String = "Font",
  property: KMutableProperty0<FontDescriptorFragment>,
  config: ConfigurableFont.() -> Unit = {},
) = configurableFont(label, property, config)

@DemoDeclaration
@JvmName("configurableFontProviderUnified")
@Suppress("DEPRECATION")
fun MeisterchartsDemo.configurable(
  label: String = "Font",
  property: KMutableProperty0<() -> FontDescriptorFragment>,
  config: ConfigurableFont.() -> Unit = {},
) = configurableFontProvider(label, property, config)

@DemoDeclaration
@JvmName("configurableLineStyleUnified")
@Suppress("DEPRECATION")
fun MeisterchartsDemo.configurable(
  label: String = "Line Style",
  property: KMutableProperty0<LineStyle>,
  config: ConfigurableList<LineStyle>.() -> Unit = {},
) = configurableLineStyle(label, property, config)

@DemoDeclaration
@JvmName("configurableBoxStyleUnified")
@Suppress("DEPRECATION")
fun MeisterchartsDemo.configurable(
  propertyName: String = "Box Style",
  property: KMutableProperty0<BoxStyle>,
  paddingSeparate: Boolean = false,
) = configurableBoxStyle(propertyName, property, paddingSeparate)

@DemoDeclaration
@JvmName("configurableEnumObservableUnified")
@Suppress("DEPRECATION")
inline fun <reified T : Enum<T>> MeisterchartsDemo.configurable(
  propertyName: String,
  property: ObservableObject<T>,
  possibleValues: List<T> = kotlin.enums.enumEntries<T>().toList(),
  crossinline config: ConfigurableEnum<T>.() -> Unit = {},
) = configurableEnum(propertyName, property, possibleValues, config)

@DemoDeclaration
@JvmName("configurableEnumPropertyUnified")
@Suppress("DEPRECATION")
inline fun <reified T : Enum<T>> MeisterchartsDemo.configurable(
  propertyName: String,
  property: KMutableProperty0<T>,
  possibleValues: List<T> = kotlin.enums.enumEntries<T>().toList(),
  crossinline config: ConfigurableEnum<T>.() -> Unit = {},
) = configurableEnum(propertyName, property, possibleValues, config)

@DemoDeclaration
@JvmName("configurableSeparateInsetsObservableUnified")
@Suppress("DEPRECATION")
fun MeisterchartsDemo.configurableSeparate(
  propertyName: String,
  property: ObservableObject<Insets>,
  config: ConfigurableInsets.() -> Unit = {},
) = configurableInsetsSeparate(propertyName, property, config)

@DemoDeclaration
@JvmName("configurableSeparateInsetsUnified")
@Suppress("DEPRECATION")
fun MeisterchartsDemo.configurableSeparate(
  propertyName: String,
  property: KMutableProperty0<Insets>,
  config: ConfigurableDouble.() -> Unit = {},
) = configurableInsetsSeparate(propertyName, property, config)

@DemoDeclaration
@JvmName("configurableSeparateSizeUnified")
@Suppress("DEPRECATION")
fun MeisterchartsDemo.configurableSeparate(
  propertyName: String,
  property: KMutableProperty0<Size>,
  config: ConfigurableDouble.() -> Unit = {},
) = configurableSizeSeparate(propertyName, property, config)

@DemoDeclaration
@JvmName("configurableSeparateInsetsProviderUnified")
@Suppress("DEPRECATION")
fun MeisterchartsDemo.configurableSeparate(
  propertyName: String,
  property: KMutableProperty0<() -> Insets>,
  config: ConfigurableInsets.() -> Unit = {},
) = configurableInsetsProviderSeparate(propertyName, property, config)

@DemoDeclaration
@JvmName("configurableSeparateCoordinatesUnified")
@Suppress("DEPRECATION")
fun MeisterchartsDemo.configurableSeparate(
  propertyName: String,
  property: KMutableProperty0<Coordinates>,
  config: ConfigurableDouble.() -> Unit = {},
) = configurableCoordinatesSeparate(propertyName, property, config)

