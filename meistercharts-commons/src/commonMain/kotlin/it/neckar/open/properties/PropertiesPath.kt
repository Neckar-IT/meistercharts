package it.neckar.open.properties

import kotlin.reflect.KProperty1


/**
 * Contains several properties that form a path.
 */
data class PropertiesPath<T>(
  private val properties: List<KProperty1<*, *>>
) {

  fun properties(): List<KProperty1<*, *>> {
    return properties
  }

  /**
   * Returns a string representation of the path.
   */
  fun asString(): String {
    require(properties.isNotEmpty()) { "Cannot create a string representation of an empty path" }
    return properties.joinToString(separator = ".") { it.name }
  }


  fun <ChildType> add(property: KProperty1<out T, ChildType>): PropertiesPath<ChildType> {
    return this.plus(property)
  }

  /**
   * Creates a new [PropertiesPath] by adding the given child property to the current property.
   */
  operator fun <ChildType> plus(property: KProperty1<out T, ChildType>): PropertiesPath<ChildType> {
    return PropertiesPath(properties + property)
  }

  override fun toString(): String {
    return "PropertiesPath(path=${properties.joinToString(separator = ".") { it.name }})"
  }

  companion object {
    /**
     * Creates a new [PropertiesPath] containing the given property.
     */
    operator fun <T> invoke(property: KProperty1<*, T>): PropertiesPath<T> {
      return PropertiesPath<Any>(listOf(property)) as PropertiesPath<T>
    }

    /**
     * Creates an empty [PropertiesPath].
     * Usually this method should not be used directly.
     */
    operator fun invoke(): PropertiesPath<Any> {
      return PropertiesPath(emptyList())
    }
  }
}

/**
 * Creates a new [PropertiesPath] by adding the given child property to the current property.
 */
operator fun <Parent, Child, ValueType> KProperty1<out Parent, Child>.plus(childProperty: KProperty1<out Child, ValueType>): PropertiesPath<ValueType> {
  return PropertiesPath(this).plus(property = childProperty)
}
