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

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class ColorParsingTest {
  val examples: List<String> = listOf(
    "AliceBlue",
    "#123456",
    "#1b0",
    "#1b07",
    "#ff000080",
    "rgba(255, 0, 0, 0.3)",
    "hsl(120, 100%, 50%)",
    "hsla(120, 100%, 50%, 0.3)",
    "rgb(100%,0%,60%)",
    "rgb(100%,0%,60%, 0.5)",
    "rgb(2.55e2, 0e0, 1.53e2, 1e2%)",
    "hwb(1.5708rad 60% 0%)",
  )

  @Disabled
  @Test
  fun testAll() {
    examples.forEach { unparsed ->
      //Color.web(unparsed).toRgba()
    }
  }
}
