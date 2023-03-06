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
package com.meistercharts.fx.time

import it.neckar.open.annotations.UiThread
import com.meistercharts.algorithms.TimeRange
import com.meistercharts.algorithms.ValueRange
import com.meistercharts.algorithms.ZoomAndTranslationModifier
import com.meistercharts.algorithms.axis.Axis
import com.meistercharts.annotations.ContentAreaRelative
import com.meistercharts.annotations.Domain
import com.meistercharts.annotations.TimeRelative
import com.meistercharts.annotations.Window
import com.meistercharts.annotations.Zoomed
import com.meistercharts.fx.DomainChartCanvas
import it.neckar.open.javafx.properties.*
import it.neckar.open.unit.other.pct
import it.neckar.open.unit.other.px
import it.neckar.open.unit.quantity.Time
import it.neckar.open.unit.si.ms
import javafx.beans.property.DoubleProperty
import javafx.beans.property.ObjectProperty
import javafx.beans.property.ReadOnlyDoubleProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleObjectProperty

/**
 * Base class for time based charts.
 *
 * Contains one time and one domain axis.
 *
 */
@Deprecated("Use classes from algorithms instead")
abstract class BaseTimeDomainChartCanvas protected constructor(
  @Time timeRange: TimeRange,
  @Domain domainValueRange: ValueRange,
  zoomAndPanModifier: ZoomAndTranslationModifier
) : DomainChartCanvas(zoomAndPanModifier, domainValueRange) {
  /**
   * The time range
   */
  val timeRangeProperty: ObjectProperty<TimeRange> = SimpleObjectProperty(timeRange)
  var timeRange: TimeRange by timeRangeProperty

  @ms
  @Time
  private val lowerVisibleBoundTimeWritableProperty: DoubleProperty = SimpleDoubleProperty()

  @ms
  @Time
  val lowerVisibleBoundTimeProperty: ReadOnlyDoubleProperty
    get() = lowerVisibleBoundTimeWritableProperty

  @ms
  @Time
  val lowerVisibleBoundTime: Double by lowerVisibleBoundTimeProperty


  @ms
  @Time
  private val upperVisibleBoundTimeWritableProperty: DoubleProperty = SimpleDoubleProperty()

  @ms
  @Time
  val upperVisibleBoundTimeProperty: ReadOnlyDoubleProperty
    get() = upperVisibleBoundTimeWritableProperty

  @ms
  @Time
  val upperVisibleBoundTime: Double by upperVisibleBoundTimeProperty


  init {
    //Recalculate visible bounds on changes to the domain value range
    this.timeRangeProperty.addListener { _, _, _ -> recalculateVisibleBounds() }
  }

  fun time2window(@Time @ms time: Double): @px @Window Double {
    return timeRelative2window(time2timeRelative(time))
  }

  fun timeDuration2zoomed(@Time @ms duration: Double): @px @Window Double {
    return timeRelative2zoomed(timeDuration2timeRelativeDelta(duration))
  }

  fun window2time(@px @Window value: Double): @Time @ms Double {
    @TimeRelative @pct val relativeTime = window2timeRelative(value)
    return timeRelative2time(relativeTime)
  }

  fun documentRelative2time(@ContentAreaRelative value: Double): @Time @ms Double {
    @TimeRelative @pct val relativeTime = documentRelative2timeRelative(value)
    return timeRelative2time(relativeTime)
  }

  fun documentRelative2timeRelative(@ContentAreaRelative value: Double): @ContentAreaRelative Double {
    return zoomAndTranslationSupport.chartCalculator.contentAreaRelative2domainRelativeX(value)
  }

  fun time2timeRelative(@Time @ms time: Double): @TimeRelative Double {
    return timeRange.time2relative(time)
  }

  /**
   * Returns a delta for the duration
   */
  fun timeDuration2timeRelativeDelta(@Time @ms duration: Double): @TimeRelative @pct Double {
    return timeRange.time2relativeDelta(duration)
  }

  fun timeRelative2time(@TimeRelative relativeTime: Double): @Time Double {
    return timeRange.relative2time(relativeTime)
  }

  @UiThread
  fun timeRelative2window(@TimeRelative @pct value: Double): @Window @px Double {
    return when (domainAxis) {
      Axis.Y -> zoomAndTranslationSupport.chartCalculator.domainRelative2windowX(value)
      Axis.X -> zoomAndTranslationSupport.chartCalculator.domainRelative2windowY(value)
    }
  }

  @UiThread
  fun timeRelative2zoomed(@TimeRelative @pct value: Double): @Zoomed @px Double {
    return when (domainAxis) {
      Axis.Y -> zoomAndTranslationSupport.chartCalculator.domainRelative2zoomedX(value)
      Axis.X -> zoomAndTranslationSupport.chartCalculator.domainRelative2zoomedY(value)
    }
  }

  fun window2timeRelative(@Window @px value: Double): @TimeRelative @pct Double {
    return when (domainAxis) {
      Axis.Y -> zoomAndTranslationSupport.chartCalculator.window2domainRelativeX(value)
      Axis.X -> zoomAndTranslationSupport.chartCalculator.window2domainRelativeY(value)
    }
  }

  override fun recalculateVisibleBounds() {
    super.recalculateVisibleBounds()
    lowerVisibleBoundTimeWritableProperty.value = window2time(0.0)
    upperVisibleBoundTimeWritableProperty.value = window2time(width)
  }
}
