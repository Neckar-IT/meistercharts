package com.meistercharts.algorithms

import com.meistercharts.annotations.ContentArea
import com.meistercharts.annotations.DomainRelative
import it.neckar.open.unit.number.MayBeZero
import com.meistercharts.annotations.Window
import com.meistercharts.annotations.WindowRelative
import com.meistercharts.annotations.Zoomed
import com.meistercharts.model.Coordinates
import com.meistercharts.model.Insets
import com.meistercharts.model.Size

/**
 * The behavior when the window is resized
 *
 */
fun interface WindowResizeBehavior {
  /**
   * Is called when a resize (of the window or the content area) has happened.
   * Depending on the implementation of [com.meistercharts.canvas.ContentAreaSizingStrategy] either
   * the content area or the window area or both might have changed.
   */
  fun handleResize(zoomAndTranslationSupport: ZoomAndTranslationSupport, windowResizeEvent: WindowResizeEvent)
}

/**
 * Is called when a resize has happened.
 * Contains both the old and new size of the content area and the window area.
 */
data class WindowResizeEvent constructor(
  @Window val oldWindowSize: Size,
  @Window val newWindowSize: Size,

  @ContentArea @MayBeZero val oldContentAreaSize: Size,
  @ContentArea @MayBeZero val newContentAreaSize: Size,

  @Zoomed @MayBeZero val contentViewportMargin: Insets,
)

/**
 * When resizing the window the value in origin stays there
 */
object KeepOriginOnWindowResize : WindowResizeBehavior {
  override fun handleResize(zoomAndTranslationSupport: ZoomAndTranslationSupport, windowResizeEvent: WindowResizeEvent) {
    //Do nothing
  }
}

/**
 * When resizing the window the value in the center of the window stays there
 */
object KeepCenterOnWindowResize : KeepLocation(0.5, 0.5)

/**
 * Resets zoom *and* translation to default values on resize
 */
object ResetToDefaultsOnWindowResize : WindowResizeBehavior {
  override fun handleResize(zoomAndTranslationSupport: ZoomAndTranslationSupport, windowResizeEvent: WindowResizeEvent) {
    zoomAndTranslationSupport.resetToDefaults()
  }
}

/**
 * When resizing the window the value in the center of the window stays there
 */
open class KeepLocation(
  /**
   * The position on the x-axis that is kept
   */
  val windowRelativeX: () -> @WindowRelative Double,
  /**
   * The position on the y-axis that is kept
   */
  val windowRelativeY: () -> @WindowRelative Double,
) : WindowResizeBehavior {

  constructor(
    windowRelativeX: @WindowRelative Double,
    windowRelativeY: @WindowRelative Double,
  ) : this({ windowRelativeX }, { windowRelativeY })

  override fun handleResize(zoomAndTranslationSupport: ZoomAndTranslationSupport, windowResizeEvent: WindowResizeEvent) {
    keepLocationOnResize(zoomAndTranslationSupport, windowResizeEvent)
  }

  private fun keepLocationOnResize(zoomAndTranslationSupport: ZoomAndTranslationSupport, windowResizeEvent: WindowResizeEvent) {
    if (windowResizeEvent.newWindowSize.atLeastOneZero()) {
      //Calculation does not make any sense for that case
      return
    }

    if (windowResizeEvent.oldWindowSize.atLeastOneZero()) {
      //If the old size has been "0.0" we do not have a meaningful value.
      //So we reset the zoom
      zoomAndTranslationSupport.resetToDefaults()
      return
    }

    val chartCalculator = zoomAndTranslationSupport.chartCalculator

    //find the domain value for the old window size at the center
    @WindowRelative val windowRelativeX = windowRelativeX()
    @WindowRelative val windowRelativeY = windowRelativeY()

    @DomainRelative val oldDomainRelativeValuesAtCenter: Coordinates = chartCalculator
      .withContentAreaSize(windowResizeEvent.oldContentAreaSize)
      .window2domainRelative(windowResizeEvent.oldWindowSize.width * windowRelativeX, windowResizeEvent.oldWindowSize.height * windowRelativeY)

    //Calculate the new location of the domain value that has been in the center
    @Window val newLocationOfOldCenterValue = chartCalculator.domainRelative2window(oldDomainRelativeValuesAtCenter)

    @Zoomed val deltaX = windowResizeEvent.newWindowSize.width * windowRelativeX - newLocationOfOldCenterValue.x
    @Zoomed val deltaY = windowResizeEvent.newWindowSize.height * windowRelativeY - newLocationOfOldCenterValue.y

    zoomAndTranslationSupport.moveWindow(deltaX, deltaY)
  }
}
