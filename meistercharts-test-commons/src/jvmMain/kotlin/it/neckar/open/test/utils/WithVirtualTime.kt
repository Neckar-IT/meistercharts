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
package it.neckar.open.test.utils

import it.neckar.open.unit.si.ms
import org.junit.jupiter.api.extension.ExtendWith

/**
 * Sets the time returned by nowMillis to the given fixed value.
 *
 * If there is a parameter `nowProvider: [it.neckar.open.time.VirtualNowProvider]` added to the test methods, the current instance of [it.neckar.open.time.VirtualNowProvider] is assigned.
 *
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FILE, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@ExtendWith(VirtualNowProviderExtension::class)
annotation class WithVirtualTime(val value: @ms Double = defaultNow) {

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
