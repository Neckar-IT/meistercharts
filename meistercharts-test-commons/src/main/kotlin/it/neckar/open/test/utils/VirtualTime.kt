package it.neckar.open.test.utils

import it.neckar.open.unit.si.ms
import org.junit.jupiter.api.extension.ExtendWith

/**
 * Sets the time returned by nowMillis to the given fixed value.
 *
 * If there is a parameter `nowProvider: FixedNowProvider` added to the test methods, the current instance of [it.neckar.open.time.VirtualNowProvider] is assigned.
 *
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FILE, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@ExtendWith(VirtualNowProviderExtension::class)
annotation class VirtualTime(val value: @ms Double = defaultNow) {

  companion object {
    /**
     * This is a selected timestamp that is set for the fixed time provider by default.
     *
     * It represents:
     * * 2021-03-27T21:45:23.002 UTC
     * * 2021-03-27T22:45:23.002+01:00[Europe/Berlin]
     * * 2021-03-28T06:45:23.002+09:00[Asia/Tokyo]
     *
     * Benefits of this date:
     * * there are different dates in different time zones
     * * it is just a few hours before clock change to daylight saving time
     * * it has 2.5 milliseconds
     */
    const val defaultNow: @ms Double = 1616881523002.5 //2021-03-27T21:45:23.002 UTC
  }
}
