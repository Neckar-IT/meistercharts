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

/**
 * Abstract base class for meistercharts demos
 */
@DemoDeclaration
class MeisterchartsDemo(
  val demoDescriptor: MeisterchartsDemoDescriptor<*>,
  demoConfig: MeisterchartsDemo.() -> Unit,
) {
  /**
   * The dependent objects that hold the properties
   */
  val dependentObjects: DependentObjects = DependentObjects()

  /**
   * The chart configurations
   */
  val configurations: MutableList<MeisterchartBuilder.() -> Unit> = mutableListOf()

  fun meistercharts(configuration: MeisterchartBuilder.() -> Unit) {
    this.configurations.add(configuration)
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
    demoConfig.invoke(this)
  }

  /**
   * Configures the chart builder
   */
  fun MeisterchartBuilder.configure() {
    configurations.forEach {
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
  fun markAsDirty(reason: DirtyReason = DirtyReason.Unknown) {
    layerSupport?.markAsDirty(reason)
  }

  companion object {
    val logger: Logger = LoggerFactory.getLogger("com.meistercharts.demo.MeisterchartsDemo")
  }
}

@DemoDeclaration
fun MeisterchartsDemo.section(
  sectionName: String,
) {
  declare {
    section(sectionName)
  }
}

/**
 * Creates a button within a new section
 */
@DemoDeclaration
fun MeisterchartsDemo.button(
  actionName: String,
  /**
   * Configures the button - is called as soon as the button is created
   */
  buttonConfiguration: DemoButton.() -> Unit = {},
  action: () -> Unit,
) {
  declare {
    val button = button(actionName, action)
    buttonConfiguration(button)
  }
}

