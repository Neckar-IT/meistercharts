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
package com.meistercharts.fx.debug

import com.meistercharts.algorithms.ChartCalculator
import com.meistercharts.algorithms.ObservableChartState
import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.algorithms.painter.ChartingStateDebugPainter
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.canvas.ChartSupport
import com.meistercharts.fx.CanvasFX
import com.meistercharts.fx.MeisterChartBuilderFX
import com.meistercharts.fx.time.BaseTimeDomainChartCanvas
import com.meistercharts.model.Coordinates
import com.meistercharts.model.Direction
import it.neckar.open.dispose.Disposable
import javafx.event.EventHandler
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.input.KeyEvent
import javafx.scene.layout.BorderPane
import javafx.stage.Stage
import javafx.stage.StageStyle

/**
 * A pane that shows the state of a charting canvas
 */
class ChartingStateDebugPane(val state: ObservableChartState) : BorderPane() {

  companion object {
    fun show(state: ObservableChartState, at: Coordinates? = null): Disposable {
      val meisterChart = MeisterChartBuilderFX("ChartingStateDebugPane")
        .also {
          it.configure {
            layers.addLayer(ChartingStateDebugLayer(state))
          }
        }
        .build()
      val layerSupport = meisterChart.layerSupport

      Disposable.all(
        state.zoomProperty.consume { _ -> layerSupport.markAsDirty() },
        state.axisOrientationXProperty.consume { _ -> layerSupport.markAsDirty() },
        state.axisOrientationYProperty.consume { _ -> layerSupport.markAsDirty() },
        state.contentAreaSizeProperty.consume { _ -> layerSupport.markAsDirty() },
        state.windowTranslationProperty.consume { _ -> layerSupport.markAsDirty() },
      ).let {
        meisterChart.onDispose(it)
      }

      val stage = Stage(StageStyle.DECORATED)
      stage.scene = Scene(meisterChart, 800.0, 600.0)

      stage.onHidden = EventHandler {
        //Dispose on hide
        meisterChart.dispose()
      }

      stage.show()
      at?.let {
        stage.x = it.x
        stage.y = it.y
      }

      return Disposable {
        stage.close()
      }
    }

    /**
     * Registers the key command
     * Ctrl + Shift + Alt + D that opens the debug view
     */
    fun registerMagicKeyListener(canvas: BaseTimeDomainChartCanvas) {
      val state = canvas.chartState
      registerMagicKeyListener(canvas, state)
    }

    /**
     * Registers the key command
     * Ctrl + Shift + Alt + D that opens the debug view
     */
    fun ChartSupport.registerDebugPaneHotkeys() {
      val canvas = (canvas as CanvasFX).canvas
      registerMagicKeyListener(canvas, rootChartState)
    }

    /**
     * Registers the key command
     * Ctrl + Shift + Alt + D that opens the debug view
     */
    fun registerMagicKeyListener(canvas: Canvas, state: ObservableChartState) {
      canvas.isFocusTraversable = true
      canvas.addEventHandler(KeyEvent.KEY_RELEASED) {
        if (!it.isAltDown || !it.isControlDown || !it.isShiftDown) {
          return@addEventHandler
        }

        if (it.text == "D") {
          show(state)
        }
      }
    }
  }
}

/**
 * A layer that visualizes the chart state
 */
class ChartingStateDebugLayer(val stateToVisualize: ObservableChartState) : AbstractLayer() {
  override val type: LayerType
    get() = LayerType.Notification

  private val chartingStateDebugPainter = ChartingStateDebugPainter()

  override fun paint(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc

    val chartState = paintingContext.chartSupport.currentChartState
    chartState.let {
      gc.fill(Color.black)
      gc.fillText("State $it", 10.0, 20.0, Direction.CenterLeft)

      chartingStateDebugPainter.paintState(ChartCalculator(stateToVisualize), gc, gc.width, gc.height)
    }
  }
}
