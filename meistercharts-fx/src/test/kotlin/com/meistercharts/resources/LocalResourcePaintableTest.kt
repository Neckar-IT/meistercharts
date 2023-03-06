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
package com.meistercharts.resources

import assertk.*
import assertk.assertions.*
import it.neckar.open.javafx.test.JavaFxTest
import com.meistercharts.fx.MeisterChartsPlatform
import kotlinx.coroutines.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@JavaFxTest
class LocalResourcePaintableTest {
  @BeforeEach
  fun setUp() {
    runBlocking(Dispatchers.Main) {
      MeisterChartsPlatform.init()
    }
  }

  @Test
  fun testLocalRes() {
    try {
      LocalResourcePaintable("invalid://asdf")
      fail("Where is the exception?")
    } catch (e: Exception) {
    }

    assertThat(LocalResourcePaintable("aTestResource.txt"))
      .isNotNull()
      .given {
        assertThat(it.delegate).isNotNull()
        assertThat(it.relativePath).isEqualTo("aTestResource.txt")
      }
  }
}
