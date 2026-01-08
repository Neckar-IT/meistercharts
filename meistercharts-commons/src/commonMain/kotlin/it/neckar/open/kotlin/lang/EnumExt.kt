package it.neckar.open.kotlin.lang

import kotlin.reflect.KClass

/**
 * Returns the enum entries for this class.
 */
expect val <T : Enum<T>> KClass<T>.enumEntries: Array<T>
