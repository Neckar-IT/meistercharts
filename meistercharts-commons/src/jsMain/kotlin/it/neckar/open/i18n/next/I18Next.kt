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
