package it.neckar.open.test.utils

/**
 * Strategy for [ConfiguringSupport].
 *
 * Provides and sets values
 */
interface ConfiguringStrategy<T, A : Annotation> {
  /**
   * Returns the original (currently set) value.
   * This method is called first - the returned value is stored and reset later.
   */
  fun getOriginalValue(): T

  /**
   * Extracts the value from the annotation. The returned value is then later set (see [applyValue])
   */
  fun extract(annotation: A): T?

  /**
   * Is called with the value that shall be applied.
   * Is called twice:
   * * Before the test is run with the new value.
   * * After the test has run with the old value
   */
  fun applyValue(value: T)
}
