package it.neckar.open.kotlin.lang

import kotlin.reflect.KClass

@Deprecated("Enum entries are not available in JavaScript target", level = DeprecationLevel.ERROR)
actual val <T : Enum<T>> KClass<T>.enumEntries: Array<T>
  get() = throw UnsupportedOperationException("Enum entries are not available in JavaScript target")
