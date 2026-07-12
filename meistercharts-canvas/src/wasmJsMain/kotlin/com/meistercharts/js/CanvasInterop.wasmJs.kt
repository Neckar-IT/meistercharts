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
package com.meistercharts.js

import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement

internal actual fun HTMLCanvasElement.getContext2d(willReadFrequently: Boolean): CanvasRenderingContext2D? {
  return getContext("2d", contextOptions(willReadFrequently)) as? CanvasRenderingContext2D
}

private fun contextOptions(willReadFrequently: Boolean): JsAny = js("({willReadFrequently: willReadFrequently})")

internal actual fun CanvasRenderingContext2D.setLineDashWeb(dashes: DoubleArray) {
  val jsArray = JsArray<JsNumber>()
  for (i in dashes.indices) {
    jsArray[i] = dashes[i].toJsNumber()
  }
  setLineDash(jsArray)
}
