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

import it.neckar.open.unit.other.px
import org.w3c.dom.DOMRectReadOnly
import org.w3c.dom.Element


/**
 * https://developer.mozilla.org/en-US/docs/Web/API/ResizeObserver
 *
 * Warning: IE11 does not support the ResizeObserver!
 */
@Suppress("UnusedPrivateProperty")
external class ResizeObserver(callback: (Array<ResizeObserverEntry>, ResizeObserver) -> Unit) {
  /**
   * Unobserves all observed Element targets of a particular observer.
   */
  fun disconnect()

  /**
   * Initiates the observing of a specified Element.
   * @param target A reference to an Element or SVGElement to be observed.
   */
  fun observe(target: Element)

  /**
   * Ends the observing of a specified Element.
   * @param target A reference to an Element or SVGElement to be unobserved.
   */
  fun unobserve(target: Element)
}

/**
 * https://developer.mozilla.org/en-US/docs/Web/API/ResizeObserverEntry
 */
external class ResizeObserverEntry {
  /**
   * An object containing the new border box size of the observed element when the callback is run.
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/ResizeObserverEntry/borderBoxSize">borderBoxSize</a>
   */
  val borderBoxSize: Array<BoxSize>

  /**
   * An object containing the new content box size of the observed element when the callback is run.
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/ResizeObserverEntry/contentBoxSize">contentBoxSize</a>
   */
  val contentBoxSize: Array<BoxSize>

  /**
   * A DOMRectReadOnly object containing the new size of the observed element when the callback is run.
   * Note that this is better supported than the above two properties, but it is left over from an earlier
   * implementation of the Resize Observer API, is still included in the spec for web compat reasons, and
   * may be deprecated in future versions.
   */
  val contentRect: DOMRectReadOnly

  /**
   * A reference to the Element or SVGElement being observed.
   */
  val target: Element
}

/**
 * See https://developer.mozilla.org/en-US/docs/Web/API/ResizeObserverEntry/borderBoxSize
 * or https://developer.mozilla.org/en-US/docs/Web/API/ResizeObserverEntry/contentBoxSize
 */
external class BoxSize {
  /**
   * The length of the observed element's border box in the block dimension.
   * For boxes with a horizontal writing-mode, this is the vertical dimension, or height;
   * if the writing-mode is vertical, this is the horizontal dimension, or width.
   */
  val blockSize: @px Number

  /**
   * The length of the observed element's border box in the inline dimension.
   * For boxes with a horizontal writing-mode, this is the horizontal dimension, or width;
   * if the writing-mode is vertical, this is the vertical dimension, or height.
   */
  val inlineSize: @px Number
}
