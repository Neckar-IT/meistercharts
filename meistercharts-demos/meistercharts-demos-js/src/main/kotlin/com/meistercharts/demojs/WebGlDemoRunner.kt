package com.meistercharts.demojs

import com.meistercharts.js.canvasRenderingContextWebGl
import kotlinx.browser.document
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLElement

/**
 * Starts a tabbed view that allows to select and configure various demo applications
 * @param container the element that will hold the demo
 */
@Suppress("unused") // public API
@JsName("startWebGlDemos")
@JsExport
fun startWebGlDemos(container: HTMLElement) {
  println("startWebGlDemos")


  val canvasElement: HTMLCanvasElement = document.createElement("CANVAS") as HTMLCanvasElement
  val webGl = canvasElement.canvasRenderingContextWebGl

  println("webGl: $webGl")

  webGl.clearColor(1.0f, 0.5f, 1.0f, 1.0f)
  //val mask = WebGLRenderingContextBase.COLOR_BUFFER_BIT
  webGl.clear(16384) //the mask above does not work

  //val gc = canvasElement.canvasRenderingContext2D
  //gc.fillStyle ="#111111"
  //gc.fillRect(0.0, 0.0, canvasElement.width.toDouble(), canvasElement.height.toDouble())
  //gc.fillStyle ="#ffffff"
  //gc.fillText("Hello World", 10.0, 10.0)

  //Append the canvas element
  container.appendChild(canvasElement)
}
