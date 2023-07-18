/**
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
package com.meistercharts.canvas

import assertk.*
import assertk.assertions.*
import com.meistercharts.canvas.text.truncateToLength
import org.junit.jupiter.api.Test

class StringShortenerKtTest {
  @Test
  fun testVeryShort() {
    assertThat(truncateToLength("abcde", 1)).isEqualTo("…")
    assertThat(truncateToLength("abcde", 2)).isEqualTo("a…")
    assertThat(truncateToLength("abcde", 3)).isEqualTo("ab…")
    assertThat(truncateToLength("abcde", 4)).isEqualTo("abc…")
  }

  @Test
  fun testIt() {
    assertThat(truncateToLength("a", 12)).isEqualTo("a")
    assertThat(truncateToLength("abcd", 12)).isEqualTo("abcd")

    "abcde".let { text ->
      assertThat(truncateToLength(text, 12)).isEqualTo("abcde")
      assertThat(truncateToLength(text, 5)).isEqualTo("abcde")
      assertThat(truncateToLength(text, 4)).isEqualTo("abc…")
      assertThat(truncateToLength(text, 3)).isEqualTo("ab…")
      assertThat(truncateToLength(text, 2)).isEqualTo("a…")
      assertThat(truncateToLength(text, 1)).isEqualTo("…")
    }

    "ab cd".let { text ->
      assertThat(truncateToLength(text, 12)).isEqualTo("ab cd")
      assertThat(truncateToLength(text, 5)).isEqualTo("ab cd")
      assertThat(truncateToLength(text, 4)).isEqualTo("ab …")
      assertThat(truncateToLength(text, 3)).isEqualTo("ab…")
      assertThat(truncateToLength(text, 2)).isEqualTo("a…")
      assertThat(truncateToLength(text, 1)).isEqualTo("…")
    }
  }
}
