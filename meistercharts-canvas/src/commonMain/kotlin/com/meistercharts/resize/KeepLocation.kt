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
package com.meistercharts.resize

import com.meistercharts.annotations.DomainRelative
import com.meistercharts.annotations.Window
import com.meistercharts.annotations.WindowRelative
import com.meistercharts.annotations.Zoomed
import it.neckar.geometry.Coordinates
import com.meistercharts.zoom.UpdateReason
import com.meistercharts.zoom.ZoomAndTranslationSupport

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
      zoomAndTranslationSupport.resetToDefaults(reason = UpdateReason.WindowResize)
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

    zoomAndTranslationSupport.moveWindow(deltaX, deltaY, reason = UpdateReason.WindowResize)
  }
}
