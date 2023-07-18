package it.neckar.open.time

import it.neckar.open.annotations.TestOnly
import it.neckar.open.kotlin.lang.requireFinite
import it.neckar.open.unit.si.ms

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
  /**
   * The initial time for the now provider
   */
  val initialNow: @ms Double,
) : NowProvider {

  init {
    require(initialNow.isFinite()) { "The initial now must be finite" }
  }

  /**
   * The current time in millis
   */
  var virtualNow: @ms Double = initialNow

  override fun nowMillis(): Double {
    return virtualNow
  }

  /**
   * When the virtual now provider has been started (real time).
   * This value is used to calculate the offset
   */
  val started: @ms Double = ClockNowProvider.nowMillis().requireFinite()

  /**
   * The difference between started and the reference time
   */
  val offsetBetweenStartedAndReferenceTime: @ms Double = (started - referenceTime).requireFinite()

  /**
   * Updates virtual now. Keeps the offset between started and reference time
   */
  fun updateVirtualNow() {
    virtualNow = ClockNowProvider.nowMillis() - offsetBetweenStartedAndReferenceTime
  }

  /**
   * Advances the current time by the given millis
   */
  fun advanceBy(millis: @ms Double) {
    virtualNow += millis
  }

  /**
   * Returns the time this provider has advanced since it has been started
   */
  @TestOnly
  fun advancedTime(): @ms Double {
    return virtualNow - initialNow
  }

  override fun toString(): String {
    return "VirtualNowProvider(initialNow=${initialNow}, virtualNow=${virtualNow}, started=${started}, offsetBetweenStartedAndReferenceTime=$offsetBetweenStartedAndReferenceTime)"
  }

  companion object {
    /**
     * The base reference time that is the default.
     * ATTENTION! This value will be changed without warning!
     */
    const val referenceTime: @ms Double = (1686042664231.0) //2023-06-06T09:11:04.231
  }

}
