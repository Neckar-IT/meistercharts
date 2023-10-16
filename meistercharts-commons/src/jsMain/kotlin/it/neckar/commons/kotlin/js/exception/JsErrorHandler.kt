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
