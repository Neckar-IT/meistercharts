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
package com.meistercharts.demojs

import kotlinx.browser.window
import org.w3c.dom.ErrorEvent

/**
 * Registers window-level handlers that display uncaught errors in the demo banner.
 * The JsErrorHandler infrastructure (typed window.onerror payloads) is JS-only, therefore
 * Wasm listens to the "error" and "unhandledrejection" events directly.
 */
internal actual fun registerDemoErrorHandlers() {
  window.addEventListener("error", { event ->
    val message = (event as? ErrorEvent)?.message ?: event.type
    DemoRunnerHTMLSupport.displayOtherError(message, "window error event")
  })
  window.addEventListener("unhandledrejection", { event ->
    DemoRunnerHTMLSupport.displayOtherError(jsRejectionReason(event), "unhandled promise rejection")
  })
}

/**
 * Extracts the rejection reason of a PromiseRejectionEvent as string.
 * The typed PromiseRejectionEvent binding is not part of kotlinx-browser, therefore plain interop.
 */
private fun jsRejectionReason(event: JsAny): String = js("String(event.reason)")
