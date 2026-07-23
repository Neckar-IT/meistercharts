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
package com.meistercharts.style

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test

class MaterialColorTest {
  @Test
  fun `brown shades match the Material Design palette`() {
    //Official Material Design brown palette - the 100/200/300 entries were shifted one shade.
    assertThat(MaterialColor.BROWN_50.web).isEqualTo("#EFEBE9")
    assertThat(MaterialColor.BROWN_100.web).isEqualTo("#D7CCC8")
    assertThat(MaterialColor.BROWN_200.web).isEqualTo("#BCAAA4")
    assertThat(MaterialColor.BROWN_300.web).isEqualTo("#A1887F")
    assertThat(MaterialColor.BROWN_400.web).isEqualTo("#8D6E63")
  }
}
