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
