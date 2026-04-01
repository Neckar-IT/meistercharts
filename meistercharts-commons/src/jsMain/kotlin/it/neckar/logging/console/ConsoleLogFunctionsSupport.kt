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
