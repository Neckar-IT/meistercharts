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
package com.meistercharts.js.external

import org.w3c.dom.events.EventTarget
import kotlin.js.Promise

/**
 * The [FontFaceSet](https://developer.mozilla.org/en-US/docs/Web/API/FontFaceSet) interface of the
 * [CSS Font Loading API](https://developer.mozilla.org/en-US/docs/Web/API/CSS_Font_Loading_API) manages
 * the loading of font-faces and querying of their download status. It is available as document.fonts.
 *
 * Experimental! Check the browser compatibility carefully before using this in production.
 */
external class FontFaceSet : EventTarget {
  /**
   * Indicates the font-face's loading status. It will be one of 'loading' or 'loaded'.
   */
  val status: String

  /**
   * A [Promise] which resolves once font loading and layout operations have completed.
   *
   * @see [FontFaceSet.ready](https://developer.mozilla.org/en-US/docs/Web/API/FontFaceSet/ready)
   */
  val ready: Promise<FontFaceSet>
}

/**
 * Adds a [listener] that will be called whenever an event of type loadingdone is fired, indicating that this [FontFaceSet] has finished loading.
 */
fun FontFaceSet.listenForLoadingDone(listener: (FontFaceSetLoadEvent) -> Unit) {
  addEventListener("loadingdone", { event ->
    listener(event.unsafeCast<FontFaceSetLoadEvent>())
  })
}

/**
 * Adds a [listener] that will be called whenever an event of type loading is fired, indicating that this [FontFaceSet] has started loading.
 */
fun FontFaceSet.listenForLoading(listener: (FontFaceSetLoadEvent) -> Unit) {
  addEventListener("loadingdone", { event ->
    listener(event.unsafeCast<FontFaceSetLoadEvent>())
  })
}

/**
 * Adds a [listener] that will be called whenever an event of type loadingerror is fired, indicating that an error occurred whilst loading this [FontFaceSet].
 */
fun FontFaceSet.listenForLoadingError(listener: (FontFaceSetLoadEvent) -> Unit) {
  addEventListener("loadingdone", { event ->
    listener(event.unsafeCast<FontFaceSetLoadEvent>())
  })
}


/**
 * The [FontFaceSetLoadEvent](https://developer.mozilla.org/en-US/docs/Web/API/FontFaceSetLoadEvent) interface
 * of the [CSS Font Loading API](https://developer.mozilla.org/en-US/docs/Web/API/CSS_Font_Loading_API) is fired
 * whenever a [FontFaceSet] loads.
 */
external class FontFaceSetLoadEvent {
  /**
   * Returns an array of [FontFace] instances each of which represents a single usable font.
   *
   * @see [FontFaceSetLoadEvent.fontfaces](https://developer.mozilla.org/en-US/docs/Web/API/FontFaceSetLoadEvent/fontfaces)
   */
  val fontfaces: Array<FontFace>
}

/**
 * The [FontFace](https://developer.mozilla.org/en-US/docs/Web/API/FontFace) interface represents a single usable font face.
 * It allows control of the source of the font face, being a URL to an external resource, or a buffer; it also allows control
 * of when the font face is loaded and its current status.
 */
external class FontFace {
  /**
   * A [CSSOMString](https://developer.mozilla.org/en-US/docs/Web/API/CSSOMString) that retrieves or sets the family of the font.
   * It is equivalent to the [font-family](https://developer.mozilla.org/en-US/docs/Web/CSS/@font-face/font-family) descriptor.
   */
  val family: String

  /**
   * A [CSSOMString](https://developer.mozilla.org/en-US/docs/Web/API/CSSOMString) that retrieves or sets the style of the font.
   * It is equivalent to the [font-style](https://developer.mozilla.org/en-US/docs/Web/CSS/@font-face/font-style) descriptor.
   */
  val style: String

  /**
   * A [CSSOMString](https://developer.mozilla.org/en-US/docs/Web/API/CSSOMString) that retrieves or sets the variant of the font.
   * It is equivalent to the [font-variant](https://developer.mozilla.org/en-US/docs/Web/CSS/@font-face/font-variant) descriptor.
   */
  val variant: String

  /**
   * A [CSSOMString](https://developer.mozilla.org/en-US/docs/Web/API/CSSOMString) that contains the weight of the font.
   * It is equivalent to the [font-weight](https://developer.mozilla.org/en-US/docs/Web/CSS/@font-face/font-weight) descriptor.
   */
  val weight: String

  /**
   * Returns an enumerated value indicating the status of the font, one of  "unloaded", "loading", "loaded", or "error".
   */
  val status: String
}

/**
 * Describes this [FontFace] as the font CSS shorthand property.
 */
fun FontFace.describe(): String {
  //https://developer.mozilla.org/en-US/docs/Web/CSS/font
  return "<$style $variant $weight $family>"
}
