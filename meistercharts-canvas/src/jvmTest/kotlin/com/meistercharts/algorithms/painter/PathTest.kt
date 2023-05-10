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
package com.meistercharts.algorithms.painter

import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.isTrue
import org.junit.jupiter.api.Test

/**
 */
class PathTest {
  @Test
  internal fun testAdd() {
    val path = Path()
    assertThat(path.isEmpty()).isTrue()

    assertThat(path.currentPointOrNull).isNull()
    assertThat(path.firstPointOrNull).isNull()

    path.lineTo(1.0, 2.0)

    assertThat(path.currentPointOrNull).isNotNull()
    assertThat(path.actions).hasSize(1)

    assertThat(path.currentPointOrNull).isEqualTo(path.firstPointOrNull)
    assertThat(path.currentPoint).isEqualTo(path.firstPoint)
  }
}
