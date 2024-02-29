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
package com.meistercharts.design

import assertk.*
import assertk.assertions.*
import org.junit.jupiter.api.Test

/**
 *
 */
class ThemeKeyTest {
  @Suppress("DEPRECATION")
  @Test
  fun testBasics() {
    val key = ThemeKey("da.id") { 42 }
    assertThat(key.resolve()).isEqualTo(42)

    MyTheme.resolve(key).let {
      assertThat(it).isEqualTo(42)
    }

    val value = key.resolve(MyTheme)
    //val valueProvider: IntProvider = key.invoke(MyTheme)
  }

  object MyTheme : DefaultTheme() {
  }
}
