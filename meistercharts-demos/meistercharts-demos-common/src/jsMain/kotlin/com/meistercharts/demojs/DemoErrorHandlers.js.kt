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

import it.neckar.commons.kotlin.js.exception.ConsoleJsErrorHandler
import it.neckar.commons.kotlin.js.exception.JsErrorHandler
import it.neckar.commons.kotlin.js.exception.JsErrorHandlerMultiplexer

internal actual fun registerDemoErrorHandlers() {
  JsErrorHandler.registerWindowErrorHandler(
    JsErrorHandlerMultiplexer(
      listOf(
        ConsoleJsErrorHandler,
        object : JsErrorHandler {
          override fun nullError(message: dynamic) {
            DemoRunnerHTMLSupport.displayNullError(message)
          }

          override fun error(throwable: Throwable) {
            DemoRunnerHTMLSupport.displayError(throwable)
          }

          override fun otherError(message: dynamic, error: Any) {
            DemoRunnerHTMLSupport.displayOtherError(message, error)
          }
        }
      )
    )
  )
}
