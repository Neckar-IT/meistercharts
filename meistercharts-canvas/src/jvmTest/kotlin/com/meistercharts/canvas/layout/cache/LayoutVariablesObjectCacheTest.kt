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
package com.meistercharts.canvas.layout.cache

import org.junit.jupiter.api.Test


class LayoutVariablesObjectCacheTest {
  @Test
  fun testIt() {
    val cache = LayoutVariablesObjectCache { MyLayoutVars() }

    cache.prepare(3)
    cache[0].x = 17.0
    cache[1].x = 18.0
    cache[2].x = 19.0
  }

  private class MyLayoutVars : LayoutVariable {
    var x: Double = Double.NaN

    override fun reset() {
      x = Double.NaN
    }
  }
}

