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
package com.meistercharts.fx

import com.meistercharts.algorithms.ZoomAndTranslationSupport
import com.meistercharts.algorithms.axis.AxisSelection
import it.neckar.open.javafx.FXDragSupport
import it.neckar.open.unit.other.px
import javafx.scene.Node
import javafx.scene.input.MouseEvent

/**
 * Supports vertical panning using mouse drag
 *
 */
@Deprecated("Replace with ZoomAndPanLayer")
object ZoomAndTranslationSupportFX {
  /**
   * Install vertical zooming and panning
   */
  @JvmStatic
  fun installVertical(eventSource: Node, zoomAndTranslationSupport: ZoomAndTranslationSupport) {
    installPanning(eventSource, zoomAndTranslationSupport, AxisSelection.Y)
    installZooming(eventSource, zoomAndTranslationSupport, AxisSelection.Y)
    installDoubleClickResetHandler(eventSource, zoomAndTranslationSupport)
  }

  /**
   * Install zooming and panning for both axis
   */
  @JvmStatic
  fun install(eventSource: Node, zoomAndTranslationSupport: ZoomAndTranslationSupport) {
    installPanning(eventSource, zoomAndTranslationSupport, AxisSelection.Both)
    installZooming(eventSource, zoomAndTranslationSupport, AxisSelection.Both)
    installDoubleClickResetHandler(eventSource, zoomAndTranslationSupport)
  }

  @JvmStatic
  private fun installDoubleClickResetHandler(eventSource: Node, zoomAndTranslationSupport: ZoomAndTranslationSupport) {
    eventSource.addEventHandler(MouseEvent.MOUSE_CLICKED) {
      if (it.clickCount != 2) {
        //only reset when the click count is 2
        return@addEventHandler
      }

      zoomAndTranslationSupport.resetToDefaults()
    }
  }

  @JvmStatic
  fun installZooming(eventSource: Node, zoomAndTranslationSupport: ZoomAndTranslationSupport, axisSelection: AxisSelection) {
    eventSource.setOnScroll { event -> zoomAndTranslationSupport.modifyZoom(event.deltaY > 0, axisSelection, event.x, event.y) }
  }

  @JvmStatic
  private fun installPanning(eventSource: Node, zoomAndTranslationSupport: ZoomAndTranslationSupport, axisSelection: AxisSelection) {
    val dragSupport = FXDragSupport(eventSource)
    dragSupport.install(object : FXDragSupport.Handler<Node> {
      override fun dragStartDetected(draggedNode: Node, @px mouseX: Double, @px mouseY: Double) {}

      override fun dragged(draggedNode: Node, @px deltaX: Double, @px deltaY: Double) {
        zoomAndTranslationSupport.translateWindow(axisSelection, deltaX, deltaY)
      }

      override fun dragFinished(draggedNode: Node, @px x: Double, @px y: Double) {}
    })
  }
}
