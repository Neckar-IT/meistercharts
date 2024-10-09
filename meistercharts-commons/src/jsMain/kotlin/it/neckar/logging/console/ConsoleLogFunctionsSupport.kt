package it.neckar.logging.console

import it.neckar.logging.Logger
import it.neckar.logging.LoggerFactory
import kotlinx.browser.window

/**
 * Registers at the window to offer a "CLI" for the browser console
 */
object ConsoleLogFunctionsSupport {
  private val logger: Logger = LoggerFactory.getLogger("it.neckar.logging.console.ConsoleLogFunctionsSupport")

  /**
   * Registers the console log functions at the window object
   */
  fun init(name: String) {
    logger.debug("Initializing console log functions for $name")

    val currentObject = window.asDynamic()[name]
    if (currentObject != null) {
      logger.warn("Object [$name] already exists in the global scope. Overwriting it!")
    }
    window.asDynamic()[name] = ConsoleLogFunctions(name)
  }
}
