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
  var virtualNow: @ms @VirtualTime Double = initialNow
    set(value) {
      value.requireFinite()
      require(value >= field) {
        "The virtual now must not be decreased.\n" +
          "Current value: ${field.formatUtcForDebug()}, new value: ${value.formatUtcForDebug()}. Delta: ${value - field} ms\n" +
          "Offset between started and reference time: $offsetBetweenStartedRealTimeAndInitialNow ms."
      }

      field = value
    }

  override fun nowMillis(): Double {
    return virtualNow
  }

  /**
   * When the virtual now provider has been started (real time).
   * This value is used to calculate the offset
   */
  val startedRealTime: @ms @RealClockTime Double = ClockNowProvider.nowMillis().requireFinite()

  /**
   * The difference between started and the reference time
   */
  val offsetBetweenStartedRealTimeAndInitialNow: @ms Double = (startedRealTime - initialNow).requireFinite()

  /**
   * Updates virtual now. Keeps the offset between started and reference time
   */
  fun updateVirtualNow() {
    @RealClockTime @ms val realNow = ClockNowProvider.nowMillis()
    virtualNow = realNow - offsetBetweenStartedRealTimeAndInitialNow
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
    return "VirtualNowProvider(initialNow=${initialNow.formatUtcForDebug()}, virtualNow=${virtualNow.formatUtcForDebug()}, started=${startedRealTime.formatUtcForDebug()}, offsetBetweenStartedAndReferenceTime=$offsetBetweenStartedRealTimeAndInitialNow)"
  }
}
