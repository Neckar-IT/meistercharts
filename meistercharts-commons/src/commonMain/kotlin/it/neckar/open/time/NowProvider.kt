package it.neckar.open.time

import it.neckar.open.unit.si.ms

/**
 * Provider that returns now()
 */
interface NowProvider {
  /**
   * Provides the current time in millis
   */
  fun nowMillis(): Double
}

/**
 * Provides now using the clock.
 * This is the default implementation for [NowProvider] that should be used in most cases
 */
expect object ClockNowProvider : NowProvider {
  override fun nowMillis(): Double
}

/**
 * Implementation that returns a virtual value - should only be used for testing purposes.
 *
 *
 * ATTENTION: It is required to reset the original [NowProvider] after finishing the unit test by calling [resetNowProvider].
 *
 * This is done automatically if the annotation @VirtualTime is used for a test
 *
 * Example code to be used in the unit tests
 * ```
 * @FixedTime
 * class MyTestClass{
 *  @Test
 *  fun testMethod() {
 *    [...]
 *  }
 *
 *  @Test
 *  fun testMethodWithParameter(nowProvider: VirtualNowProvider) {
 *    [...]
 *  }
 * }
 * ```
 */
class VirtualNowProvider(
  var virtualNow: @ms Double
) : NowProvider {

  override fun nowMillis(): Double {
    return virtualNow
  }

  /**
   * Adds the given millis to the current time
   */
  fun add(millis: @ms Double) {
    virtualNow += millis
  }
}
