package it.neckar.open.test.utils

interface ConfigurationCallback<T, A : Annotation> {
  /**
   * Returns the old value that has been set originally.
   * This method is called first - the returned value is stored and reset later.
   */
  fun getOriginalValue(): T

  /**
   * Extracts the value from the annotation. The returned value is then later set (see [applyValue])
   */
  fun extract(annotation: A): T?

  /**
   * Is called with the value that shall be applied.
   * Is called twice. Once before the test is run with the new value. Once after the test has run with the old value
   */
  fun applyValue(value: T)
}
