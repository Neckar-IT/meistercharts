package it.neckar.open.observable

/**
 * Holds dependent objects to avoid premature garbage collection
 */
class DependentObjects : DependentObjectSupport {
  /**
   * Contains objects that should be stored within the observable object.
   * This can be used to avoid premature garbage collection or add specific stuff
   * to the observable object itself.
   */
  private val dependentObjects: MutableMap<Any, Any> = hashMapOf()

  /**
   * Adds a dependent object that is kept
   */
  override fun addDependentObject(key: Any, dependentObject: Any) {
    set(key, dependentObject)
  }

  /**
   * Adds a dependent object that is kept - using the object itself as the key
   */
  override fun addDependentObject(dependentObject: Any) {
    addDependentObject(dependentObject, dependentObject)
  }

  operator fun set(key: Any, dependentObject: Any) {
    dependentObjects[key] = dependentObject
  }

  /**
   * Returns the dependent object for the given key - if there is one
   */
  override fun getDependentObject(key: Any): Any? {
    return get(key)
  }

  /**
   * Returns the dependent object for the given key if there is one
   */
  operator fun get(key: Any): Any? {
    return dependentObjects[key]
  }

  /**
   * Removes the dependent object for the given key
   */
  override fun removeDependentObject(key: Any): Any? {
    return dependentObjects.remove(key)
  }
}
