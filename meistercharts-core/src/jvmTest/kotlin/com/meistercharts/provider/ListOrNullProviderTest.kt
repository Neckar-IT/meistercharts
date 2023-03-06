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
package com.meistercharts.provider

import assertk.*
import assertk.assertions.*
import it.neckar.open.provider.MultiProvider
import it.neckar.open.provider.MultiProviderIndexContextAnnotation
import org.junit.jupiter.api.Test

/**
 *
 */
class ListOrNullProviderTest {
  @Test
  fun testNull() {
    val provider: MultiProvider<MyProviderContextInfo, String?> = MultiProvider.alwaysNull()
    assertThat(provider.valueAt(17)).isNull()
  }

  @Test
  fun testEmptyList() {
    val provider = MultiProvider.forListOrNull<MyProviderContextInfo, String>(listOf<String>())

    assertThat(provider.valueAt(1717)).isNull()
    assertThat(provider.valueAt(0)).isNull()
  }

  @Test
  fun testListOrNull() {
    val provider = MultiProvider.forListOrNull<MyProviderContextInfo, String>(listOf("a", "b", "c"))

    assertThat(provider.valueAt(0)).isEqualTo("a")
    assertThat(provider.valueAt(2)).isEqualTo("c")
    assertThat(provider.valueAt(3)).isEqualTo(null)
  }
}

@MustBeDocumented
@Retention(AnnotationRetention.SOURCE)
@MultiProviderIndexContextAnnotation
annotation class MyProviderContextInfo
