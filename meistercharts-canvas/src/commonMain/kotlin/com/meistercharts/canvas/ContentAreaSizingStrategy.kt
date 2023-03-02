package com.meistercharts.canvas

import com.meistercharts.algorithms.ChartState
import com.meistercharts.algorithms.MutableChartState
import com.meistercharts.algorithms.WindowResizeEvent
import com.meistercharts.annotations.ContentArea
import it.neckar.open.unit.number.MayBeZero
import com.meistercharts.annotations.Zoomed
import com.meistercharts.model.Insets
import com.meistercharts.model.Size
import it.neckar.open.collections.arrayOfNotNull
import it.neckar.open.dispose.Disposable
import it.neckar.open.dispose.DisposeSupport
import it.neckar.open.observable.ObservableObject
import it.neckar.open.observable.ReadOnlyObservableObject

/**
 * Defines how the content area size is configured
 */
interface ContentAreaSizingStrategy : Disposable {
  /**
   * Is called from the canvas support to bind the content area size and other
   * resize related events.
   *
   * Implementations must notify the resize behavior ([ChartSupport#resizeBehavior]) on every change
   * to window size or content area size.
   */
  fun bindResize(chartSupport: ChartSupport)
}

/**
 * Abstract base class that combines commonly used code
 */
abstract class AbstractContentAreaSizingStrategy(
  /**
   * Calculates the content area size.
   * Is (at least) called:
   * - once initially
   * - on a window change event
   * - whenever a one of the provided dependencies is updated
   */
  val contentAreaSizeCalculator: (ChartState) -> @ContentArea @MayBeZero Size,

  /**
   * Additional dependencies that are relevant when calculating the content area size.
   * Whenever one of these dependencies is updated, the content area is recalculated
   */
  vararg val dependencies: ReadOnlyObservableObject<*>,

  ) : ContentAreaSizingStrategy {

  override fun bindResize(chartSupport: ChartSupport) {
    val chartState = chartSupport.rootChartState

    //Update the content area size initially
    run {
      val oldContentAreaSize = chartState.contentAreaSize
      //set the content area size initially
      chartState.contentAreaSize = contentAreaSizeCalculator(chartState)

      //Call initially
      chartSupport.windowResizeBehavior.handleResize(
        chartSupport.zoomAndTranslationSupport, WindowResizeEvent(
          oldWindowSize = chartState.windowSize,
          newWindowSize = chartState.windowSize,
          oldContentAreaSize = oldContentAreaSize,
          newContentAreaSize = chartState.contentAreaSize,
          contentViewportMargin = chartState.contentViewportMargin,
        )
      )
    }

    //Update the content area size on a window size change
    chartState.windowSizeProperty.consumeChanges { oldWindowSize, newWindowSize ->
      recalculate(chartState, oldWindowSize, newWindowSize, chartSupport)
    }

    //Update the content area size on a content viewport margin change
    chartState.contentViewportMarginProperty.consumeChanges { _, _ ->
      recalculate(chartState, chartState.windowSize, chartState.windowSize, chartSupport)
    }

    //Recalculate whenever a dependency has been updated
    dependencies.forEach { dependency ->
      dependency.consume {
        val windowSize = chartSupport.rootChartState.windowSize
        recalculate(chartState, windowSize, windowSize, chartSupport)
      }.also { disposable ->
        disposeSupport.onDispose(disposable)
      }
    }
  }

  private val disposeSupport = DisposeSupport()

  override fun dispose() {
    disposeSupport.dispose()
  }

  /**
   * The recalculate method is called, whenever something (might) have changed
   */
  protected fun recalculate(
    /**
     * The chart state that is updated
     */
    chartState: MutableChartState,

    oldWindowSize: @Zoomed @MayBeZero Size,
    newWindowSize: @Zoomed @MayBeZero Size,

    chartSupport: ChartSupport,
  ) {
    @ContentArea val oldContentAreaSize = chartState.contentAreaSize
    @ContentArea @MayBeZero val newContentAreaSize = contentAreaSizeCalculator(chartState)

    if (newContentAreaSize == chartState.contentAreaSize && oldWindowSize == newWindowSize) {
      //Nothing has changed, skip
      return
    }

    chartState.contentAreaSize = newContentAreaSize

    //call the window resize behavior
    chartSupport.windowResizeBehavior.handleResize(
      chartSupport.zoomAndTranslationSupport, WindowResizeEvent(
        oldWindowSize = oldWindowSize, newWindowSize = newWindowSize, oldContentAreaSize = oldContentAreaSize, newContentAreaSize = newContentAreaSize, contentViewportMargin = chartState.contentViewportMargin
      )
    )
  }
}

/**
 * The content area size is set to a fixed size
 */
class FixedContentAreaSize(val fixedSizeProvider: () -> @ContentArea Size) : AbstractContentAreaSizingStrategy(
  { _ -> fixedSizeProvider() }
) {
  constructor(fixedSize: @ContentArea Size) : this({ fixedSize })
}

/**
 * The content area width is set to a fixed value.
 *
 * The content area height is bound to the window height.
 *
 * This strategy is useful for charts where the X axis is an (endless) time axis
 */
class FixedContentAreaWidth(
  val fixedWidth: @ContentArea Double,
  val minHeight: @ContentArea Double = 0.0
) : AbstractContentAreaSizingStrategy({ chartState -> Size(fixedWidth, chartState.windowHeight.coerceAtLeast(minHeight)) })

/**
 * The content area height is set to a fixed value.
 *
 * The content area width is bound to the window width.
 *
 * This strategy is useful for charts where the Y axis is an (endless) time axis
 */
class FixedContentAreaHeight(
  val fixedHeightProvider: () -> @ContentArea Double,
  val minWidth: @ContentArea Double = 0.0
) : AbstractContentAreaSizingStrategy(
  { chartState -> Size(chartState.windowWidth.coerceAtLeast(minWidth), fixedHeightProvider()) }
) {

  constructor(
    fixedHeight: @ContentArea Double,
    minWidth: @ContentArea Double = 0.0
  ) : this({ fixedHeight }, minWidth)
}

/**
 * The content area size is bound to the window size
 * --> When the zoom level is set to 1.0/1.0, the complete content area size is visible in the windows
 */
@Deprecated("Probably no longer required - has been replaced by BindContentArea2ContentViewport")
class BindContentAreaSize2WindowSize constructor(
  /**
   * The margin property that triggers a recalculation of the content-area size when it changes
   */
  val marginProperty: ObservableObject<@Zoomed Insets>?,
) : AbstractContentAreaSizingStrategy(
  { chartState ->
    @Zoomed val margin = marginProperty?.value ?: Insets.empty

    chartState.windowSize.minus(margin.offsetWidth, margin.offsetHeight).coerceAtLeast(Size.zero)
  }, *marginProperty.arrayOfNotNull()
) {}

/**
 * Binds the content area to the content viewport.
 *
 * --> When the zoom level is set to 1.0/1.0, the complete content area is visible within the content viewport
 */
class BindContentAreaSize2ContentViewport : AbstractContentAreaSizingStrategy(
  { chartState ->
    val margin = chartState.contentViewportMargin
    chartState.windowSize.minus(margin.offsetWidth, margin.offsetHeight).coerceAtLeast(Size.zero)
  },
)

/**
 * Binds the content area to an observable property
 */
class BindContentAreaSize2Property constructor(
  /**
   * The margin property that triggers a recalculation of the content-area size when it changes
   */
  val contentAreaSizeProperty: ObservableObject<@ContentArea Size>,
) : AbstractContentAreaSizingStrategy(
  { _ ->
    contentAreaSizeProperty.get()
  }, contentAreaSizeProperty
)
