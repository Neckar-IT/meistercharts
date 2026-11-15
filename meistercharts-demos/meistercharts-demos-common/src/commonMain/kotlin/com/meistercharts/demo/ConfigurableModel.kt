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
import kotlin.math.PI
import kotlin.reflect.KMutableProperty0
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.DurationUnit

/*
 * Backing model classes for the `configurable*` demo DSL (defined in ConfigurableDsl.kt):
 * each Configurable* holds a control's current value plus its change listeners;
 * AbstractConfigurable is the common base.
 */

/**
 * Abstract base class for configurable
 */
abstract class AbstractConfigurable<T>(val propertyName: String) {
  val onChange: MutableList<(T) -> Unit> = mutableListOf()

  /**
   * Call this method for every change
   */
  fun changed(newValue: T) {
    if (onChange.isEmpty()) {
      MeisterchartsDemo.logger.debug("No onChange event registered for $propertyName! This is probably a bug! Maybe you wanted to use a property instead: object::prop")
    }
    //TODO reactivate strict require() once all demos register an onChange callback
    //  (was: require(onChange.isNotEmpty()) { "..." }, soft-disabled via if(true)/else)

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
        MeisterchartsDemo.logger.warn("WARNING. Min <$value> too low for current value: <${this.value}>")
      }

      field = value
    }

  var max: Double = 1.0
    set(value) {
      if (value < this.value) {
        MeisterchartsDemo.logger.warn("WARNING. Max <$value> too low for current value: <${this.value}>")
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

/**
 * Configuration for a string
 */
class ConfigurableString(propertyName: String, initialValue: String) : AbstractConfigurableWithValue<String>(propertyName, initialValue) {
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

class ConfigurableInt(propertyName: String, initialValue: Int = 0) : AbstractConfigurableWithValue<Int>(propertyName, initialValue) {
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

  val fontFamilyProperty: ObservableObject<FontFamily?> = ObservableObject(null)
  val fontGenericFamilyProperty: ObservableObject<GenericFontFamily> = ObservableObject(GenericFontFamily.SansSerif)
  val fontSizeProperty: ObservableDouble = ObservableDouble(16.0)
  val fontWeightProperty: ObservableObject<FontWeight> = ObservableObject(FontWeight.Normal)
  val fontStyleProperty: ObservableObject<FontStyle> = ObservableObject(FontStyle.Normal)
  val fontVariantProperty: ObservableObject<FontVariant> = ObservableObject(FontVariant.Normal)

  var value: FontDescriptor
    get() = toFontDescriptor()
    set(value) {
      fontFamilyProperty.value = value.families?.first()
      fontGenericFamilyProperty.value = value.genericFamily
      fontSizeProperty.value = value.size.size
      fontWeightProperty.value = value.weight
      fontStyleProperty.value = value.style
      fontVariantProperty.value = value.variant
    }

  fun toFontDescriptor(): FontDescriptor {
    val fontFamily = fontFamilyProperty.get()
    val families = if (fontFamily == null) null else listOf(fontFamily)
    return FontDescriptor(families, FontSize(fontSizeProperty.get()), fontWeightProperty.get(), fontStyleProperty.get(), fontVariantProperty.get(), fontGenericFamilyProperty.get()).also {
      MeisterchartsDemo.logger.info("Converted configurable font to font descriptor: $it")
    }
  }

  fun applyValues(source: FontDescriptorFragment) {
    source.families?.let {
      fontFamilyProperty.value = it.first()
    }
    source.genericFamily?.let {
      fontGenericFamilyProperty.value = it
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
    val action: (newValue: Any?) -> Unit = {
      changed(toFontDescriptor())
    }

    fontFamilyProperty.consume(action = action)
    fontGenericFamilyProperty.consume(action = action)
    fontSizeProperty.consume(action = action)
    fontWeightProperty.consume(action = action)
    fontStyleProperty.consume(action = action)
    fontVariantProperty.consumeImmediately(action) //Immediately
  }
}


@Target(AnnotationTarget.FUNCTION, AnnotationTarget.TYPE, AnnotationTarget.CLASS)
@DslMarker
annotation class DemoDeclaration
