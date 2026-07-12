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

import org.w3c.dom.Element

/**
 * Facade for the browser ResizeObserver (https://developer.mozilla.org/en-US/docs/Web/API/ResizeObserver).
 *
 * The external declaration is target-specific: the callback receives an Array on JS but must use
 * JsArray on Wasm, therefore the shared web code goes through this expect class.
 * [onResize] is invoked whenever the observed elements were resized (only for non-empty entry lists).
 */
internal expect class WebResizeObserver(onResize: () -> Unit) {
  /**
   * Initiates the observing of a specified Element.
   */
  fun observe(target: Element)

  /**
   * Ends the observing of a specified Element.
   */
  fun unobserve(target: Element)
}
