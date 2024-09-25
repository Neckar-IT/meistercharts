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
import com.meistercharts.algorithms.painter.PathActions
import it.neckar.open.collections.fastForEach
import it.neckar.open.collections.fastForEachIndexed
import org.junit.jupiter.api.Test

/**
 *
 */
class IconsTest {
  @Test
  fun testParsePaths() {
    val all = Icons.all()
    assertThat(all).hasSize(35)
  }

  @Test
  fun testAllIconsInRange() {
    val names: List<String> = collectNames()

    Icons.all().fastForEachIndexed { index, pathPaintable ->
      val name = names[index]
      verifyPath(pathPaintable.pathActions, name)
    }
  }

  private fun collectNames(): List<String> {
    val names = mutableListOf<String>()
    val allMethods = Icons::class.java.declaredMethods
    return allMethods.filter {
      it.returnType.simpleName == "PathPaintable"
    }.map { it.name.removeSuffix("\$default") }
      .sorted()
  }

  /**
   * Verifies that each coordinate in the path paints within the 24x24 area
   */
  private fun verifyPath(pathActions: PathActions, name: String) {
    pathActions.actions.fastForEachIndexed { index, pathAction ->
      assertThat(pathAction.endPointX, "Icon [$name], action[$index]: $pathAction").isBetween(0.0, 24.0)
      assertThat(pathAction.endPointY, "Icon [$name], action[$index]: $pathAction").isBetween(0.0, 24.0)
    }
  }

  @Test
  fun testArrow() {
    val path = Icons.arrow()
    path.pathActions.actions.fastForEach {
      println(it)
    }
  }
}
