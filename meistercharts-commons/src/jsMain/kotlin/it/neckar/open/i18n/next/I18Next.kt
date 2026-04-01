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
package it.neckar.open.i18n.next

import kotlin.js.Promise

@JsModule("i18next")
@JsNonModule
external val i18next: I18Next

external class I18Next : EventEmitter {
  /**
   * The default export of the i18next module is an i18next instance ready to be initialized by calling init. You can create additional instances using the [createInstance] function.
   * [options] see [https://www.i18next.com/overview/configuration-options] for details
   */
  fun init(options: dynamic = definedExternally, callback: (err: Any?, t: Any?) -> Unit = definedExternally): Promise<Any?>

  /**
   * The use function is there to load additional plugins to i18next.
   */
  fun use(module: dynamic): I18Next

  /**
   * Please have a look at the translation functions like:
   * * interpolation ([https://www.i18next.com/translation-function/interpolation])
   * * formatting ([https://www.i18next.com/translation-function/formatting])
   * * plurals ([https://www.i18next.com/translation-function/plurals])
   * for more details on using it.
   *
   * [keys] can be a single key or an array of keys or a key with [options] object
   * The first one that resolves will be returned.
   */
  fun t(vararg keys: Any?, options: dynamic = definedExternally): String

  /**
   * Uses the same resolve functionality as the [t] function and returns true if a key exists.
   */
  fun exists(vararg arguments: Any?, options: dynamic = definedExternally): Boolean


  fun loadResources(callback: (err: Any?) -> Unit = definedExternally)
  fun reloadResources(lngs: String, ns: String, callback: (err: Any?) -> Unit): Promise<Any?>
  fun changeLanguage(lngs: String, callback: (err: Any?, t: Any?) -> Unit): Promise<Any?>
  fun getFixedT(lngs: String, ns: String): (key: String, opts: dynamic, rest: Array<Any?>?) -> Unit

  fun setDefaultNamespace(ns: String)
  fun loadNamespaces(ns: Array<String>, callback: (err: Any?) -> Unit): Promise<Any?>
  fun loadLanguages(lngs: Array<String>, callback: (err: Any?) -> Unit): Promise<Any?>
  fun dir(lng: String): String
  fun createInstance(options: dynamic = definedExternally, callback: (err: Any?, t: Any?) -> Unit): I18Next
  fun cloneInstance(options: dynamic = definedExternally, callback: (err: Any?, t: Any?) -> Unit = definedExternally): I18Next
}

open external class EventEmitter {
  fun on(events: String, listener: (arguments: Array<Any?>) -> Unit)
  fun off(event: String, listener: (arguments: Array<Any?>) -> Unit)
  fun emit(event: String, vararg arguments: Any?)
}
