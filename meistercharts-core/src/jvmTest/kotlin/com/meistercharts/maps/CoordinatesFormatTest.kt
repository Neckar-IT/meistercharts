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
package com.meistercharts.maps

import assertk.assertThat
import assertk.assertions.doesNotContain
import assertk.assertions.endsWith
import assertk.assertions.startsWith
import org.junit.jupiter.api.Test

/**
 * Verifies that negative (southern / western) coordinates are formatted with a hemisphere suffix
 * instead of a numeric minus sign on every component.
 */
class CoordinatesFormatTest {
  @Test
  fun `negative latitude formats as south without sign`() {
    val formatted = Latitude(-10.5).format()
    assertThat(formatted).doesNotContain("-")
    assertThat(formatted).startsWith("10°")
    assertThat(formatted).endsWith("S")
  }

  @Test
  fun `positive latitude formats as north`() {
    val formatted = Latitude(48.5).format()
    assertThat(formatted).doesNotContain("-")
    assertThat(formatted).endsWith("N")
  }

  @Test
  fun `negative longitude formats as west without sign`() {
    val formatted = Longitude(-10.5).format()
    assertThat(formatted).doesNotContain("-")
    assertThat(formatted).startsWith("10°")
    assertThat(formatted).endsWith("W")
  }
}
