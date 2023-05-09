@file:Suppress("FoldInitializerAndIfToElvis", "SENSELESS_COMPARISON")

package it.neckar.commons.kotlin.js

import kotlin.reflect.KClass
import kotlin.reflect.KProperty0

/**
 * Checks if the passed property is valid (not null & correct instance)
 * and returns it.
 * @return the value of the property
 **/
inline fun <reified T : Any> KProperty0<T>.safeGet(): T {
  return safeGet(T::class)
}

/**
 * you can't really check nullable types to not be null
 * but for symmetrical reasons this method exists
 * @return the value of the nullable property
 **/
inline fun <reified T : Any?> KProperty0<T>.safeGet(): T? {
  return this.get()
}

/**
 * Checks if the passed property is valid (not null & correct instance)
 * and returns it.
 * @return the value of the property
 **/
fun <T : Any> KProperty0<T>.safeGet(type: KClass<T>): T {
  val value = this.get()

  if (value == null) {
    throw PropertyValidationFailedException("Property [${this.name}] is not set")
  }

  if ((type.isInstance(value)).not()) {
    if (type.simpleName.equals("StateInstance")) {
      throw PropertyValidationFailedException("Property [${this.name}] has invalid value => expected value: [${type.simpleName}] " +
        "actual value: [$value]. Use method \"getNotNull()\" for properties with instance [${type.simpleName}]")
    }
    throw PropertyValidationFailedException("Property [${this.name}] has invalid value => expected value: [${type.simpleName}] actual value: [$value]")
  }

  return value
}

/**
 * Throws an exception if the given property is null
 * @return passed property
 * */
fun <T : Any> KProperty0<T>.getNotNull(): T {
  val value = this.get()
  if (value == null) {
    throw PropertyValidationFailedException("Property [${this.name}] is not set")
  }
  return value
}

class PropertyValidationFailedException(message: String, cause: Throwable? = null) : Exception(message, cause)



