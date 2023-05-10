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
import com.meistercharts.model.HorizontalAlignment
import org.junit.jupiter.api.Test

/**
 *
 */
class PaintingExtensionKtTest {
  @Test
  fun testLeft() {
    HorizontalAlignment.Left.calculateOffsetXForGap(100.0).let {
      assertThat(it).isEqualTo(100.0)
    }

    HorizontalAlignment.Center.calculateOffsetXForGap(100.0).let {
      assertThat(it).isEqualTo(0.0)
    }

    HorizontalAlignment.Right.calculateOffsetXForGap(100.0).let {
      assertThat(it).isEqualTo(-100.0)
    }
  }
}
