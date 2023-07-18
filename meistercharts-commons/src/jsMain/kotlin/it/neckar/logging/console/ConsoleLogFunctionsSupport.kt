package it.neckar.logging.console

import it.neckar.logging.Logger
import it.neckar.logging.LoggerFactory
import kotlinx.browser.window

/**
 * Registers at the window to offer a "CLI" for thr browser console
 */
object ConsoleLogFunctionsSupport {
  /**
   * Registers the console log functions at the window object
   */
  fun init(name: String) {
    logger.debug("Initializing console log functions for $name")
    window.asDynamic()[name] = ConsoleLogFunctions(name)
  }

  private val logger: Logger = LoggerFactory.getLogger("it.neckar.logging.console.ConsoleLogFunctionsSupport")
}
