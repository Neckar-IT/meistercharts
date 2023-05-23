package it.neckar.open.kotlin.lang

/**
 * Returns kind of a properties' description.
 * ATTENTION: The names will be garbage (in Kotlin/JS)
 */
actual fun props(value: Any): Map<String, Any?> {
  return buildMap {
    var javaClass: Class<out Any>? = value::class.java
    while (javaClass != null) {
      javaClass.declaredFields
        .filter {
          it.isSynthetic.not()
        }
        .forEach {
          it.isAccessible = true
          put(it.name, it.get(value))
        }

      javaClass = javaClass.superclass
    }
  }
}
