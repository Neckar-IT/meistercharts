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

internal actual class WebResizeObserver actual constructor(onResize: () -> Unit) {
  private val observer: ResizeObserverWasm = ResizeObserverWasm { entries, _ ->
    if (entries.length > 0) {
      onResize()
    }
  }

  actual fun observe(target: Element) {
    observer.observe(target)
  }

  actual fun unobserve(target: Element) {
    observer.unobserve(target)
  }
}

/**
 * https://developer.mozilla.org/en-US/docs/Web/API/ResizeObserver
 */
@JsName("ResizeObserver")
private external class ResizeObserverWasm(callback: (JsArray<ResizeObserverEntryWasm>, ResizeObserverWasm) -> Unit) : JsAny {
  fun disconnect()

  fun observe(target: Element)

  fun unobserve(target: Element)
}

/**
 * https://developer.mozilla.org/en-US/docs/Web/API/ResizeObserverEntry
 */
@JsName("ResizeObserverEntry")
private external class ResizeObserverEntryWasm : JsAny {
  val target: Element
}
