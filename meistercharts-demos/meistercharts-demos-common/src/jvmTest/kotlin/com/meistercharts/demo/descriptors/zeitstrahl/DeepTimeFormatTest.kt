/*
 * Copyright 2023 Neckar IT GmbH, Mössingen, Germany
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.meistercharts.demo.descriptors.zeitstrahl

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test

class DeepTimeFormatTest {
  @Test
  fun `labels deep time in billions, millions and grouped years`() {
    assertThat(DeepTimeFormat.yearsAgo(13.787e9, 1e9)).isEqualTo("vor 13,8 Mrd. Jahren")
    assertThat(DeepTimeFormat.yearsAgo(66e6, 1e6)).isEqualTo("vor 66 Mio. Jahren")
    assertThat(DeepTimeFormat.yearsAgo(12_000.0, 1_000.0)).isEqualTo("vor 12.000 Jahren")
  }

  @Test
  fun `writes every digit a fine step needs`() {
    assertThat(DeepTimeFormat.yearsAgo(5_700_010.0, 10.0)).isEqualTo("vor 5.700.010 Jahren")
    assertThat(DeepTimeFormat.yearsAgo(13_787_000_000.0, 1e7)).isEqualTo("vor 13.787 Mio. Jahren")
  }

  @Test
  fun `labels historical years without a year 0`() {
    assertThat(DeepTimeFormat.historicalYear(-43)).isEqualTo("44 v. Chr.")
    assertThat(DeepTimeFormat.historicalYear(0)).isEqualTo("1 v. Chr.")
    assertThat(DeepTimeFormat.historicalYear(-9_999)).isEqualTo("10.000 v. Chr.")
    assertThat(DeepTimeFormat.historicalYear(1582)).isEqualTo("1582")
    assertThat(DeepTimeFormat.historicalYear(20_000)).isEqualTo("20.000")
  }

  @Test
  fun `labels a day`() {
    assertThat(DeepTimeFormat.day(CivilDate(1789, 7, 14))).isEqualTo("14. Juli 1789")
  }
}
