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
package com.meistercharts.js

import com.meistercharts.canvas.ChartSupport
import com.meistercharts.canvas.DirtyReason
import com.meistercharts.canvas.LayerSupport
import com.meistercharts.canvas.MeisterChart
import com.meistercharts.canvas.MeisterChartsPlatformState
import com.meistercharts.canvas.devicePixelRatioSupport
import com.meistercharts.events.FontLoadedEventBroker
import com.meistercharts.events.ImageLoadedEventBroker
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLDivElement

/**
 * JavaScript implementation for [MeisterChart]
 */
class MeisterChartJS(
  override val chartSupport: ChartSupport,
  override val description: String,
) : MeisterChart {

  /**
   * The holder of the chart
   */
  val holder: HTMLDivElement = (document.createElement("DIV") as HTMLDivElement).also {
    it.appendChild(htmlCanvas.canvasElement)
    it.classList.add(MeisterChartClasses.holder, MeisterChartClasses.chartId(chartSupport.chartId))
    chartSupport.onDispose {
      it.removeChild(htmlCanvas.canvasElement)
    }

    //Set line-height to 0 to prevent a line-height offset for the following scenario:
    /*
      <div style="width: 300px; height: 350px;">
        <p>There should be no black background visible but there is an offset of 4.8 pixels due to the inner DIV.</p>
        <div style="background-color: black;">
          <canvas style="background-color: magenta; width: 100%; height: 100%;"></canvas>
        </div>
      </div>
     */
    it.style.setProperty("line-height", "0")

    //Set the style to 100%
    it.style.setProperty("width", "100%")
    it.style.setProperty("height", "100%")
    it.style.setProperty("position", "relative")

    //Reset other options that might have been set from CSS
    it.style.setProperty("border", "0")
    it.style.setProperty("padding", "0")
    it.style.setProperty("margin", "0")
  }

  /**
   * The canvas from [LayerSupport] cast to [CanvasJS].
   *
   * Do *NOT* add the html canvas directly. Instead add the [holder]
   */
  val htmlCanvas: CanvasJS
    get() = chartSupport.canvas as CanvasJS

  /**
   * Contains the native components
   */
  val nativeComponents: NativeComponentsJS = NativeComponentsJS(chartSupport).also { nativeComponents ->
    holder.appendChild(nativeComponents.div)
    chartSupport.onDispose {
      holder.removeChild(nativeComponents.div)
    }
  }

  init {
    //Ensure the configuration is loaded/configured
    MeisterChartsPlatform.init()

    ImageLoadedEventBroker.onLoaded {
      //repaint when an image becomes available
      chartSupport.markAsDirty(DirtyReason.ResourcesLoaded)
    }.also(chartSupport::onDispose)

    FontLoadedEventBroker.onLoaded {
      //repaint when a font becomes available
      chartSupport.markAsDirty(DirtyReason.ResourcesLoaded)
    }.also(chartSupport::onDispose)

    //Dispose the canvas when the chart support is disposed
    chartSupport.onDispose(htmlCanvas)

    //Recalculate the canvas rendering size if the device pixel ratio has been updated
    chartSupport.devicePixelRatioSupport.devicePixelRatioProperty.consume {
      htmlCanvas.recalculateCanvasRenderingSize()
    }

    // Do not(!) set the width or height attribute of canvasEle to the corresponding width or height of parentElement.
    // The size of parentElement may depend on the size of canvasElement. Hence, the size of parentElement might be 0 at this point.
    htmlCanvas.canvasElement.style.setProperty("width", "100%")
    htmlCanvas.canvasElement.style.setProperty("height", "100%")

    htmlCanvas.canvasElement.style.setProperty("box-sizing", "border-box")


    MeisterChartsPlatformState.newInstance(this)
    onDispose {
      MeisterChartsPlatformState.instanceDisposed(this)
    }

    //Request the first animation
    window.requestAnimationFrame { chartSupport.scheduleRepaints(it) }
  }
}
