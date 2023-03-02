package com.meistercharts.fx

import it.neckar.open.annotations.UiThread
import com.meistercharts.algorithms.ChartCalculator
import com.meistercharts.algorithms.KeepCenterOnWindowResize
import com.meistercharts.algorithms.MutableChartState
import com.meistercharts.algorithms.ValueRange
import com.meistercharts.algorithms.WindowResizeBehavior
import com.meistercharts.algorithms.ZoomAndTranslationModifier
import com.meistercharts.algorithms.ZoomAndTranslationSupport
import com.meistercharts.algorithms.axis.Axis
import com.meistercharts.algorithms.axis.AxisOrientationY
import com.meistercharts.algorithms.impl.DefaultChartState
import com.meistercharts.algorithms.impl.ZoomAndTranslationDefaults
import com.meistercharts.annotations.Domain
import com.meistercharts.annotations.DomainRelative
import com.meistercharts.annotations.Window
import com.meistercharts.fx.binding.bind
import com.meistercharts.model.Size
import it.neckar.open.kotlin.lang.or0ifNaN
import it.neckar.open.javafx.AbstractFxCanvas
import it.neckar.open.javafx.properties.getValue
import it.neckar.open.javafx.properties.setValue
import it.neckar.open.observable.ObservableObject
import it.neckar.open.unit.other.pct
import it.neckar.open.unit.other.px
import javafx.beans.binding.Bindings
import javafx.beans.property.DoubleProperty
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.canvas.GraphicsContext
import java.util.concurrent.Callable

/**
 * Abstract base class that contains chart related methods for charts with a value axis (usually Y).
 * Nothing is known about the x axis
 *
 */
@Deprecated("Use classes from algorithms instead")
abstract class DomainChartCanvas(
  zoomAndPanModifier: ZoomAndTranslationModifier,
  @Domain domainValueRange: ValueRange
) : AbstractFxCanvas() {
  /**
   * The chart view state. Is automatically connected to the width/height of the canvas
   */
  val chartState: MutableChartState = DefaultChartState()

  val zoomAndTranslationSupport: ZoomAndTranslationSupport

  val windowResizeBehaviorProperty: ObjectProperty<WindowResizeBehavior> = SimpleObjectProperty(KeepCenterOnWindowResize)
  var windowResizeBehavior: WindowResizeBehavior by windowResizeBehaviorProperty

  /**
   * The range for the value axis
   */
  @Domain
  val domainValueRangeProperty: ObjectProperty<ValueRange> = SimpleObjectProperty(ValueRange.linear(0.0, 100.0))

  @Domain
  var domainValueRange: ValueRange by domainValueRangeProperty

  /**
   * Contains the orientation of the chart - which is the domain axis
   */
  @Domain
  val domainAxisProperty: ObjectProperty<Axis> = SimpleObjectProperty(Axis.Y)
  @Domain
  var domainAxis: Axis by domainAxisProperty

  val valueAxisOrientation: AxisOrientationY
    get() = chartState.axisOrientationYProperty.value

  val chartCalculator: ChartCalculator
    get() = zoomAndTranslationSupport.chartCalculator

  /**
   * Contains the visible lower bound (domain value).
   * This property never contains NaN. If NaN is correct, "0" is set instead
   */
  @Domain
  val lowerVisibleBoundValueProperty: DoubleProperty = SimpleDoubleProperty()

  @Domain
  var lowerVisibleBoundValue: Double by lowerVisibleBoundValueProperty

  /**
   * Contains the visible upper bound (domain value)
   * This property never contains NaN. If NaN is correct, "0" is set instead
   */
  @Domain
  val upperVisibleBoundValueProperty: DoubleProperty = SimpleDoubleProperty()

  @Domain
  var upperVisibleBoundValue: Double by upperVisibleBoundValueProperty


  init {
    zoomAndTranslationSupport = ZoomAndTranslationSupport(chartState, zoomAndPanModifier, ZoomAndTranslationDefaults.noTranslation)

    this.domainValueRangeProperty.value = domainValueRange

    //Bind to the size of the chart view state to  the canvas
    chartState.contentAreaSizeProperty.bind(
      Bindings.createObjectBinding(
        Callable { Size.of(widthProperty().get(), heightProperty().get()) },
        widthProperty(), heightProperty()
      )
    )

    this.chartState.onChange {
      this.markAsDirty()
    }

    run {
      //Recalculate bounds used for the y axis if necessary
      heightProperty().addListener { _, oldValue, _ ->
        if (oldValue.toDouble() == 0.0) {
          //Call initially - on first show
          recalculateVisibleBounds()

          //Reset to defaults initially
          zoomAndTranslationSupport.resetToDefaults()
        }
      }

      chartState.axisOrientationYProperty.consume { _ -> recalculateVisibleBounds() }
      chartState.axisOrientationXProperty.consume { _ -> recalculateVisibleBounds() }
      chartState.windowTranslationProperty.consume { _ -> recalculateVisibleBounds() }
      chartState.zoomProperty.consume { _ -> recalculateVisibleBounds() }

      //Recalculate on changes to the domain value range
      this.domainValueRangeProperty.addListener { _, _, _ -> recalculateVisibleBounds() }
    }

    //Callback on resize
    //TODO how to implement????
    //chartState.contentAreaSizeProperty.consumeChanges { oldValue, newValue -> zoomAndPanSupport.handleResize(oldValue, newValue, resizeBehavior) }
  }

  @UiThread
  override fun paint(gc: GraphicsContext) {
    //The background
    clearBackground(gc)
    paintBackground(gc)

    paintDiagram(gc)
  }


  @UiThread
  protected open fun paintBackground(gc: GraphicsContext) {
  }

  /**
   * Paint the diagram.
   * When this method is called, the background has been cleared and painted
   */
  @UiThread
  protected abstract fun paintDiagram(gc: GraphicsContext)


  /**
   * Converts a domain value to a pixel value within the window.
   * Respects zooming, panning.
   *
   *
   * This method uses the y value range to convert the given model value to pixels
   */
  @px
  @UiThread
  @Window
  fun domain2window(@Domain value: Double): Double {
    return domainRelative2window(domain2domainRelative(value))
  }

  @DomainRelative
  @px
  fun domain2domainRelative(@Domain modelValue: Double): Double {
    return domainValueRange.toDomainRelative(modelValue)
  }

  @Domain
  @px
  fun domainRelative2domain(@DomainRelative domainRelative: Double): Double {
    return domainValueRange.toDomain(domainRelative)
  }

  @px
  @Domain
  @UiThread
  fun window2domain(@Window value: Double): Double {
    return domainRelative2domain(window2domainRelative(value))
  }

  @UiThread
  @Window
  @px
  fun domainRelative2window(@DomainRelative @pct value: Double): Double {
    return when (domainAxis) {
      Axis.Y -> zoomAndTranslationSupport.chartCalculator.domainRelative2windowY(value)
      Axis.X -> zoomAndTranslationSupport.chartCalculator.domainRelative2windowX(value)
    }
  }

  /**
   * Converts the window value to domain relative
   */
  @px
  @DomainRelative
  @pct
  fun window2domainRelative(@Window @px value: Double): Double {
    return when (domainAxis) {
      Axis.Y -> zoomAndTranslationSupport.chartCalculator.window2domainRelativeY(value)
      Axis.X -> zoomAndTranslationSupport.chartCalculator.window2domainRelativeX(value)
    }
  }

  /**
   * Recalculates the visible bounds
   */
  protected open fun recalculateVisibleBounds() {
    upperVisibleBoundValueProperty.value = window2domain(0.0).or0ifNaN()
    lowerVisibleBoundValueProperty.value = window2domain(height).or0ifNaN()
  }

  fun valueAxisOrientationProperty(): ObservableObject<AxisOrientationY> {
    return chartState.axisOrientationYProperty
  }
}
