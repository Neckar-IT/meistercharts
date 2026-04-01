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
 *
 * Attention: For external interfaces use custom methods instead (e.g. for `StateInstance` use `safeGetOptional()` defined in kotlin-react project)
 **/
inline fun <T : Any?> KProperty0<T>.safeGet(): T? {
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
    //Handle special cases
    when {
      type.simpleName.equals("StateInstance") -> {
        throw PropertyValidationFailedException(
          "Property [${this.name}] has invalid value => expected value: [${type.simpleName}] " +
            "actual value: [$value]. Use method \"getNotNull()\" for properties with instance [${type.simpleName}]"
        )
      }

      type.simpleName.equals("SuspendFunction0") -> {
        if (value is Function1<*, *>) {
          //This is ok, first parameter is $continuation
          return value
        }

        //current
        throw PropertyValidationFailedException("Property [${this.name}] expected a suspend function. Actual value: [$value] with type [${value::class}]")
      }

      else -> throw PropertyValidationFailedException("Property [${this.name}] has invalid value => expected value: [${type.simpleName}] actual value: [$value]")
    }
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



