/*
 * Copyright (C) 2013-2026 Neckar IT GmbH, Mössingen, Germany
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Linking this library statically or dynamically with other modules is
 * making a combined work based on this library. Thus, the terms and
 * conditions of the GNU General Public License cover the whole combination.
 *
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules, regardless of
 * the license terms of these independent modules, and to copy and distribute
 * the resulting combined work under terms of your choice, provided that every
 * copy of the combined work is accompanied by a complete copy of the source
 * code of this library.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
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
