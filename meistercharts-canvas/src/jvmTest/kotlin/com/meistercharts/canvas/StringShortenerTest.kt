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

import com.meistercharts.canvas.text.StringShortener
import assertk.*
import assertk.assertions.*

import org.junit.jupiter.api.Test

class StringShortenerTest {
  val text: String = "abcdefghijklmnopqrstuvwxyz"

  @Test
  fun testNoOp() {
    assertThat(StringShortener.NoOp.shorten(text, 999)).isEqualTo(text)
  }

  @Test
  fun testTruncateWithoutSymbol() {
    assertThat(StringShortener.TruncateToLength.shorten(text, 7, "")).isEqualTo("abcdefg")
    assertThat(StringShortener.TruncateToLength.shorten("a", 7, "")).isEqualTo("a")
    assertThat(StringShortener.TruncateToLength.shorten("abcde", 7, "")).isEqualTo("abcde")
    assertThat(StringShortener.TruncateToLength.shorten("abcdef", 7, "")).isEqualTo("abcdef")
    assertThat(StringShortener.TruncateToLength.shorten("abcdefg", 7, "")).isEqualTo("abcdefg")
    assertThat(StringShortener.TruncateToLength.shorten("abcdefgh", 7, "")).isEqualTo("abcdefg")
  }

  @Test
  fun testVeryShort() {
    assertThat(StringShortener.TruncateToLength.shorten(text, 0, "...")).isNull()
    assertThat(StringShortener.TruncateToLength.shorten(text, 1, "...")).isEqualTo("!")
    assertThat(StringShortener.TruncateToLength.shorten(text, 2, "...")).isEqualTo("!")
    assertThat(StringShortener.TruncateToLength.shorten(text, 3, "...")).isEqualTo("...")
  }

  @Test
  fun testTruncateWithSymbol() {
    assertThat(StringShortener.TruncateToLength.shorten(text, 7, "...")).isEqualTo("abcd...")
    assertThat(StringShortener.TruncateToLength.shorten("a", 7, "...")).isEqualTo("a")
    assertThat(StringShortener.TruncateToLength.shorten("ab", 7, "...")).isEqualTo("ab")
    assertThat(StringShortener.TruncateToLength.shorten("abc", 7, "...")).isEqualTo("abc")
    assertThat(StringShortener.TruncateToLength.shorten("abcd", 7, "...")).isEqualTo("abcd")
    assertThat(StringShortener.TruncateToLength.shorten("abcde", 7, "...")).isEqualTo("abcde")
    assertThat(StringShortener.TruncateToLength.shorten("abcdef", 7, "...")).isEqualTo("abcdef")
    assertThat(StringShortener.TruncateToLength.shorten("abcdefg", 7, "...")).isEqualTo("abcdefg")
    assertThat(StringShortener.TruncateToLength.shorten("abcdefgh", 7, "...")).isEqualTo("abcd...")
  }

  @Test
  fun testTruncateWithEllipsis() {
    val truncationSymbol = "…"
    assertThat(StringShortener.TruncateToLength.shorten("abcdefghijklmnopqrstuvwxyz", 7, truncationSymbol)).isEqualTo("abcdef…")
    assertThat(StringShortener.TruncateToLength.shorten("a", 7, truncationSymbol)).isEqualTo("a")
    assertThat(StringShortener.TruncateToLength.shorten("ab", 7, truncationSymbol)).isEqualTo("ab")
    assertThat(StringShortener.TruncateToLength.shorten("abc", 7, truncationSymbol)).isEqualTo("abc")
    assertThat(StringShortener.TruncateToLength.shorten("abcd", 7, truncationSymbol)).isEqualTo("abcd")
    assertThat(StringShortener.TruncateToLength.shorten("abcde", 7, truncationSymbol)).isEqualTo("abcde")
    assertThat(StringShortener.TruncateToLength.shorten("abcdef", 7, truncationSymbol)).isEqualTo("abcdef")
    assertThat(StringShortener.TruncateToLength.shorten("abcdefg", 7, truncationSymbol)).isEqualTo("abcdefg")
    assertThat(StringShortener.TruncateToLength.shorten("abcdefgh", 7, truncationSymbol)).isEqualTo("abcdef…")
  }

  @Test
  fun testTruncateCenterWithSymbol() {
    assertThat(StringShortener.TruncateCenterToLength.shorten(text, 7, "...")).isEqualTo("ab...yz")
    assertThat(StringShortener.TruncateCenterToLength.shorten(text, 8, "...")).isEqualTo("abc...yz")
    assertThat(StringShortener.TruncateCenterToLength.shorten("a", 7, "...")).isEqualTo("a")
    assertThat(StringShortener.TruncateCenterToLength.shorten("ab", 7, "...")).isEqualTo("ab")
    assertThat(StringShortener.TruncateCenterToLength.shorten("abc", 7, "...")).isEqualTo("abc")
    assertThat(StringShortener.TruncateCenterToLength.shorten("abcd", 7, "...")).isEqualTo("abcd")
    assertThat(StringShortener.TruncateCenterToLength.shorten("abcde", 7, "...")).isEqualTo("abcde")
    assertThat(StringShortener.TruncateCenterToLength.shorten("abcdef", 7, "...")).isEqualTo("abcdef")
    assertThat(StringShortener.TruncateCenterToLength.shorten("abcdefg", 7, "...")).isEqualTo("abcdefg")
    assertThat(StringShortener.TruncateCenterToLength.shorten("abcdefgh", 7, "...")).isEqualTo("ab...gh")
  }
}
