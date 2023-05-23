import kotlin.reflect.KClass

/**
 *
 */

/**
 * JavaScript Object type
 */
external class Object

/**
 * JavaScript delete operator
 */
external fun delete(p: dynamic): Boolean

internal data class LegacyTest(val test: Boolean = true)

/**
 * A helper property to test whether current compiler is running in legacy mode.
 */
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
