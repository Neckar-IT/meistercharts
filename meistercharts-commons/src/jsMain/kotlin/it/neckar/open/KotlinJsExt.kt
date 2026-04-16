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
package it.neckar.open

import kotlin.reflect.KClass

/**
 *
 */

/**
 * JavaScript Object type
 */
external class Object

/**
 * JavaScript delete operator. The returned [Boolean] indicates whether the
 * property was removed; in practice this is almost always used purely for
 * side-effect, so the return value may be ignored.
 */
@IgnorableReturnValue
external fun delete(p: dynamic): Boolean

internal data class LegacyTest(val test: Boolean = true)

/**
 * A helper property to test whether current compiler is running in legacy mode.
 */
@Deprecated("Should no longer be required, since the legacy compiler has been removed")
val isLegacyBackend: Boolean by lazy {
  LegacyTest().asDynamic()["test"] == true
}

/**
 * A helper function for JavaScript delete operator
 */
fun delete(thing: dynamic, key: String) {
  delete(thing[key])
}

/**
 * Helper function for creating JavaScript objects.
 */
inline fun obj(init: dynamic.() -> Unit): dynamic {
  return (Object()).apply(init)
}

/**
 * Helper function for creating JavaScript objects with given type.
 */
inline fun <T> obj(init: T.() -> Unit): T {
  return (js("{}") as T).apply(init)
}

/**
 * Helper function for creating JavaScript objects from dynamic constructors.
 */
@Suppress("UNUSED_VARIABLE")
fun <T> Any?.createInstance(vararg args: dynamic): T {
  val jsClassConstructor = this
  val argsArray = (listOf<dynamic>(null) + args).toTypedArray()
  return js("new (Function.prototype.bind.apply(jsClassConstructor, argsArray))").unsafeCast<T>()
}

/**
 * Helper function to enumerate properties of a data class.
 */
@Suppress("UnsafeCastFromDynamic")
fun getAllPropertyNames(obj: Any): List<String> {
  val prototype = js("Object").getPrototypeOf(obj)
  val prototypeProps: Array<String> = js("Object").getOwnPropertyNames(prototype)
  val pList = prototypeProps.filter { it != "constructor" }.filterNot { prototype.propertyIsEnumerable(it) }.toList()
  return if (isLegacyBackend) {
    val ownProps: Array<String> = js("Object").getOwnPropertyNames(obj)
    ownProps.toList() + pList
  } else {
    pList
  }
}

/**
 * Helper extension function to convert a data class to a plain JS object.
 */
fun toPlainObj(data: Any): dynamic {
  val properties = getAllPropertyNames(data)
  val ret = js("{}")
  properties.forEach {
    ret[it] = data.asDynamic()[it]
  }
  return ret
}

/**
 * Helper function to convert a plain JS object to a data class.
 */
fun <T : Any> toKotlinObj(data: dynamic, kClass: KClass<T>): T {
  val newT = kClass.js.createInstance<T>()
  for (key in js("Object").keys(data)) {
    newT.asDynamic()[key] = data[key]
  }
  return newT
}

/**
 * Returns true if the given value is undefined.
 */
fun Any?.isUndefined(): Boolean = this === undefined

/**
 * Returns true if the given value is undefined.
 */
fun isUndefined(value: Any?): Boolean = value === undefined

/**
 * Returns true if the given value is undefined.
 */
fun isUndefined(value: dynamic): Boolean = value === undefined
