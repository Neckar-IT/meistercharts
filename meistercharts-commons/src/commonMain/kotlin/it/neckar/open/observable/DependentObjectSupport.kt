package it.neckar.open.observable

/**
 * Offers support for dependent objects that may be registered.
 * This can be used to avoid premature garbage collection.
 *
 * Dependent objects can be registered at this instance and will be held forever.
 *
 */
interface DependentObjectSupport {
  /**
   * Adds a dependent object that is held by this instance to avoid premature garbage collection.
   *
   * The dependent object is registered using the given key and can later be resolved again using [getDependentObject]
   */
  fun addDependentObject(key: Any, dependentObject: Any)

  /**
   * Adds a dependent object that is held by this instance to avoid premature garbage collection.
   * Uses the dependentObject itself as key
   */
  fun addDependentObject(dependentObject: Any)

  /**
   * Returns the dependent object for the given key - if one has been registered using one of the [addDependentObject] methods.
   */
  fun getDependentObject(key: Any): Any?

  /**
   * Removes the dependent object for the given key.
   * This method returns the removed, dependent object if there has one been registered using the given key.
   */
  fun removeDependentObject(key: Any): Any?
}
