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
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.reflect.full.memberProperties

/**
 *
 */
class ThemeTest {
  lateinit var theme: Theme

  @BeforeEach
  internal fun setUp() {
    theme = NeckarITDesign
  }

  @Test
  fun testExample() {
    //Test fallback
    theme.resolve(ThemeKey("does.not.exist") { NeckarITDesign.textFont }).let {
      assertThat(it).isEqualTo(NeckarITDesign.textFont)
    }
  }

  @Test
  fun testUniqueKeys() {
    val set = mutableSetOf<String>()

    Theme.Companion::class.memberProperties.forEach {
      val themeKey = it.get(Theme) as ThemeKey<*>

      if (set.contains(themeKey.id)) {
        fail("Duplicate key <${themeKey.id}>")
      }

      set.add(themeKey.id)
    }
  }
}
