package it.neckar.commons.kotlin.js

import it.neckar.open.unit.si.s
import kotlin.js.Console


/**
 * Adds debug to the console
 */
inline fun Console.debug(s: String?): Unit = asDynamic().debug(s)
inline fun Console.debug(s: String?, o: Any?): Unit = asDynamic().debug(s, o)
