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

import it.neckar.open.annotations.UiThread
import com.meistercharts.canvas.ChartSupport
import com.meistercharts.canvas.DirtyReason
import com.meistercharts.canvas.MeisterChart
import com.meistercharts.canvas.MeisterChartsPlatformState
import com.meistercharts.canvas.devicePixelRatioSupport
import com.meistercharts.canvas.nativeComponentsSupport
import it.neckar.open.time.nowMillis
import com.meistercharts.events.ImageLoadedEventBroker
import javafx.scene.image.WritableImage
import javafx.scene.layout.StackPane

/**
 * Implements [MeisterChart] for JavaFX
 */
class MeisterChartFX(
  override val chartSupport: ChartSupport,
  override val description: String,
) : StackPane(), MeisterChart {
  /**
   * Returns the fx canvas from the [layerSupport] cast to [CanvasFX]
   */
  val fxCanvas: CanvasFX
    get() = chartSupport.canvas as CanvasFX

  /**
   * The canvas holder
   */
  val canvasHolder: CanvasHolder = CanvasHolder(fxCanvas.canvas).also {
    children.add(it)
  }

  /**
   * Contains the native components
   */
  val nativeComponents: NativeComponentsFX = NativeComponentsFX(chartSupport.nativeComponentsSupport).also {
    children.add(it)
  }

  init {
    //Ensure the configuration is loaded/configured
    MeisterChartsPlatform.init()

    ImageLoadedEventBroker.onLoaded {
      // repaint when an image becomes available
      chartSupport.markAsDirty(DirtyReason.ResourcesLoaded)
    }.also {
      //Dispose onLoaded
      chartSupport.onDispose(it)
    }

    //Recalculate the canvas rendering size if the device pixel ratio has been updated
    chartSupport.devicePixelRatioSupport.devicePixelRatioProperty.consume {
      fxCanvas.recalculateCanvasRenderingSize()
    }

    MeisterChartsPlatformState.newInstance(this)
    onDispose {
      MeisterChartsPlatformState.instanceDisposed(this)
    }

    chartSupport.scheduleRepaints()
  }

  public override fun setWidth(value: Double) {
    super.setWidth(value)
  }

  public override fun setHeight(value: Double) {
    super.setHeight(value)
  }

  companion object {}
}


/**
 * Renders the MeisterCharts fx instance to an image.
 *
 * This method must only be called for off screen instances that have *not* been added to the scene graph
 *
 */
@UiThread
fun MeisterChartFX.renderOffScreenToImage(width: Int, height: Int): WritableImage {
  require(parent == null) {
    "renderOffScreenToImage is only supported for off screen rendering. This instance has been added to a parent: $parent"
  }

  //set width/height of MeisterCharts
  this.width = width.toDouble()
  this.height = height.toDouble()

  //Call layout - to update the sizes of the holder and canvas itself
  layout()

  //Verify the size
  require(this.width == width.toDouble())
  require(this.height == height.toDouble())
  require(fxCanvas.width == width.toDouble())
  require(fxCanvas.height == height.toDouble())

  require(chartSupport.dirtySupport.dirty) {
    "Expected dirty"
  }

  //paint once
  chartSupport.refresh(nowMillis())

  //Create the image
  return canvasHolder.canvas.screenshot()
}
