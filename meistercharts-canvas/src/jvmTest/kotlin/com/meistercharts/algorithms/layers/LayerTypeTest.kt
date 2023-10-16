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
package com.meistercharts.algorithms.layers

import assertk.*
import assertk.assertions.*

import org.junit.jupiter.api.Test

/**
 */
internal class LayerTypeTest {
  @Test
  internal fun testIt() {
    assertThat(LayerType.Content.below(LayerType.Content)).isFalse()
    assertThat(LayerType.Content.below(LayerType.Background)).isFalse()
    assertThat(LayerType.Content.below(LayerType.Notification)).isTrue()

    assertThat(LayerType.Background.below(LayerType.Content)).isTrue()
    assertThat(LayerType.Background.below(LayerType.Background)).isFalse()
    assertThat(LayerType.Background.below(LayerType.Notification)).isTrue()

    assertThat(LayerType.Notification.below(LayerType.Content)).isFalse()
    assertThat(LayerType.Notification.below(LayerType.Background)).isFalse()
    assertThat(LayerType.Notification.below(LayerType.Notification)).isFalse()
  }
}
