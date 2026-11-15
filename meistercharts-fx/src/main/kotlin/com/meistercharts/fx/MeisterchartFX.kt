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
package com.meistercharts.fx

import com.meistercharts.Meistercharts
import com.meistercharts.canvas.ChartSupport
import com.meistercharts.canvas.DirtyReason
import com.meistercharts.canvas.Meisterchart
import com.meistercharts.canvas.devicePixelRatioSupport
import com.meistercharts.canvas.nativeComponentsSupport
import com.meistercharts.events.ImageLoadedEventBroker
import it.neckar.open.annotations.UiThread
import it.neckar.open.dispose.disposeOn
import it.neckar.open.time.nowMillis
import javafx.scene.image.WritableImage
import javafx.scene.layout.StackPane
import it.neckar.open.time.monotonicMillis

/**
 * Implements [Meisterchart] for JavaFX
 */
class MeisterchartFX(
  override val chartSupport: ChartSupport,
  override val description: String,
) : StackPane(), Meisterchart {

  /**
   * Removes the background - makes the chart transparent
   */
  fun removeBackground() {
    this.background = null
    canvasHolder.removeBackground()
  }

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

    @Suppress("DEPRECATION")
    chartSupport.setMeisterchartInstance(this)

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
    }.disposeOn(chartSupport)

    Meistercharts.platformState.newInstance(this)
    onDispose {
      Meistercharts.platformState.instanceDisposed(this)
    }

    //Connect with the render loop
    Meistercharts.renderLoop.onRender(chartSupport).also {
      chartSupport.onDispose(it) //unregister when the chart is disposed
    }
  }

  public override fun setWidth(value: Double) {
    super.setWidth(value)
  }

  public override fun setHeight(value: Double) {
    super.setHeight(value)
  }

  //Do *not* remove - required for extension functions
  companion object
}


/**
 * Renders the MeisterCharts fx instance to an image.
 *
 * This method must only be called for off-screen instances that have *not* been added to the scene graph
 *
 */
@UiThread
fun MeisterchartFX.renderOffScreenToImage(width: Int, height: Int): WritableImage {
  require(parent == null) {
    "renderOffScreenToImage is only supported for off screen rendering. This instance has been added to a parent: $parent"
  }

  this.width = width.toDouble()
  this.height = height.toDouble()

  //Call layout - to update the sizes of the holder and canvas itself
  layout()

  //Verify the size
  require(this.width == width.toDouble()) { "Holder width [${this.width}] does not match requested width [$width] after layout" }
  require(this.height == height.toDouble()) { "Holder height [${this.height}] does not match requested height [$height] after layout" }
  require(fxCanvas.width == width.toDouble()) { "fxCanvas width [${fxCanvas.width}] does not match requested width [$width] after layout" }
  require(fxCanvas.height == height.toDouble()) { "fxCanvas height [${fxCanvas.height}] does not match requested height [$height] after layout" }

  require(chartSupport.dirtySupport.dirty) {
    "Expected dirty"
  }

  chartSupport.render(frameTimestamp = nowMillis(), frameMonotonicMillis = monotonicMillis(), relativeHighRes = 0.0)

  return canvasHolder.canvas.screenshot()
}
