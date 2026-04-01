/*
 * Copyright (C) 2013-2026 Neckar IT GmbH, Mössingen, Germany
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Linking this library statically or dynamically with other modules is
 * making a combined work based on this library. Thus, the terms and
 * conditions of the GNU General Public License cover the whole combination.
 *
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules, regardless of
 * the license terms of these independent modules, and to copy and distribute
 * the resulting combined work under terms of your choice, provided that every
 * copy of the combined work is accompanied by a complete copy of the source
 * code of this library.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package it.neckar.commons.kotlin.js.exception

import it.neckar.open.collections.fastForEach
import kotlinx.browser.window


/**
 * The active error handler.
 * Must be activated by calling [JsErrorHandler.registerWindowErrorHandler]
 */
var errorHandler: JsErrorHandler = ConsoleJsErrorHandler

/**
 * Handles JS errors / exceptions
 */
interface JsErrorHandler {
  /**
   * Is called for "real" throwable (e.g. from a coroutines context)
   */
  fun error(throwable: Throwable)

  /**
   * Is called for other errors - that are not of type throwable
   */
  fun otherError(message: dynamic, error: Any)

  /**
   * Is called for null errors - that are not of type throwable
   */
  fun nullError(message: dynamic)


  companion object {
    /**
     * Must be called to register the window error handler
     */
    fun registerWindowErrorHandler(handler: JsErrorHandler = ConsoleJsErrorHandler) {
      errorHandler = handler

      window.onerror = { message, source, lineno, colno, error ->
        println("Window error handler called: $message")

        when (error) {
          null -> {
           /* console.error("null error received")
            console.error(message)
            console.error("@$source:$lineno $colno")*/
            errorHandler.nullError(message)
          }

          is Throwable -> {
            errorHandler.error(error)
          }

          else -> errorHandler.otherError(message, error)
        }
      }
    }
  }
}

/**
 * Delegates to multiple other delegates
 */
class JsErrorHandlerMultiplexer(
  val delegates: List<JsErrorHandler>,
) : JsErrorHandler {

  init {
    require(delegates.isNotEmpty()) { "At least one delegate required" }
  }

  override fun nullError(message: dynamic) {
    delegates.fastForEach {
      it.nullError(message)
    }
  }

  override fun error(throwable: Throwable) {
    delegates.fastForEach {
      it.error(throwable)
    }
  }

  override fun otherError(message: dynamic, error: Any) {
    delegates.fastForEach {
      it.otherError(message, error)
    }
  }
}

/**
 * Console JS error handler
 */
object ConsoleJsErrorHandler : JsErrorHandler {
  override fun nullError(message: dynamic) {
    console.error("------------ EXCEPTION HANDLER - null error ----------")
    console.error("Error-Message: <${message}>")

    console.error("------------ /EXCEPTION HANDLER ----------")
  }

  override fun otherError(message: dynamic, error: Any) {
    console.error("------------ EXCEPTION HANDLER - other error ----------")
    console.error("Error: <$error>", error)
    console.error("Message: <${message}>")

    if (error is Throwable) {
      error.printStackTrace()
    } else {
      console.error("Error is not a throwable")
      console.error("but of type <${error::class.simpleName}>")
    }

    console.error("------------ /EXCEPTION HANDLER ----------")
  }

  override fun error(throwable: Throwable) {
    console.error("------------ EXCEPTION HANDLER ----------")
    console.error("Class: <${throwable::class.simpleName}>")
    console.error("Message: <${throwable.message}>")

    throwable.cause?.let { cause ->
      console.error("Cause.class: <${cause::class.simpleName}>")
      console.error("Cause.message: <${cause.message}>")
    }

    console.error("Stacktrace:")
    throwable.printStackTrace()
    console.error("------------ /EXCEPTION HANDLER ----------")
  }
}
