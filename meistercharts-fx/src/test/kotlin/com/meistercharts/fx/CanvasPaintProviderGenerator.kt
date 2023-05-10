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
package com.meistercharts.fx

import javafx.scene.paint.Color
import org.junit.jupiter.api.Test
import java.lang.reflect.Modifier
import kotlin.math.roundToInt

/**
 * Generates the color statements that can be copied to [com.meistercharts.algorithms.painter.CanvasPaint]
 */
class CanvasPaintProviderGenerator {
  @Test
  fun testIt() {
    val type = Color::class.java

    type.declaredFields.forEach {
      if (!Modifier.isStatic(it.modifiers)) {
        return@forEach
      }
      if (Modifier.isPrivate(it.modifiers)) {
        return@forEach
      }

      val colorInstance = it.get(null) as Color

      val opacity = colorInstance.opacity
      val opacityString = if (opacity != 1.0) {
        ", $opacity"
      } else {
        ""
      }

      val red = (colorInstance.red * 255).roundToInt()
      val green = (colorInstance.green * 255).roundToInt()
      val blue = (colorInstance.blue * 255).roundToInt()

      println(
        """
        @JvmField
        val ${it.name.toLowerCase()}: RgbaColor = RgbaColor($red, $green, $blue$opacityString)
        """.trimIndent()
      )
    }
  }
}
