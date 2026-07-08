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
package com.meistercharts.algorithms.painter

import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.isTrue
import it.neckar.geometry.Coordinates
import it.neckar.open.test.utils.isNaN
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

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

  @Test
  fun testBufferContents() {
    val path = Path()
    assertThat(path.actionCount).isEqualTo(0)

    path.moveTo(1.0, 2.0)
    path.lineTo(3.0, 4.0)
    path.quadraticCurveTo(5.0, 6.0, 7.0, 8.0)
    path.bezierCurveTo(9.0, 10.0, 11.0, 12.0, 13.0, 14.0)

    assertThat(path.actionCount).isEqualTo(4)
    assertThat(path.actionTypeAt(0)).isEqualTo(PathActionType.MoveTo)
    assertThat(path.actionTypeAt(1)).isEqualTo(PathActionType.LineTo)
    assertThat(path.actionTypeAt(2)).isEqualTo(PathActionType.QuadraticCurveTo)
    assertThat(path.actionTypeAt(3)).isEqualTo(PathActionType.BezierCurveTo)

    //MoveTo: one pair
    assertThat(path.coordinateXAt(0)).isEqualTo(1.0)
    assertThat(path.coordinateYAt(0)).isEqualTo(2.0)
    //LineTo: one pair
    assertThat(path.coordinateXAt(1)).isEqualTo(3.0)
    assertThat(path.coordinateYAt(1)).isEqualTo(4.0)
    //QuadraticCurveTo: control, end
    assertThat(path.coordinateXAt(2)).isEqualTo(5.0)
    assertThat(path.coordinateYAt(2)).isEqualTo(6.0)
    assertThat(path.coordinateXAt(3)).isEqualTo(7.0)
    assertThat(path.coordinateYAt(3)).isEqualTo(8.0)
    //BezierCurveTo: control1, control2, end
    assertThat(path.coordinateXAt(4)).isEqualTo(9.0)
    assertThat(path.coordinateYAt(4)).isEqualTo(10.0)
    assertThat(path.coordinateXAt(5)).isEqualTo(11.0)
    assertThat(path.coordinateYAt(5)).isEqualTo(12.0)
    assertThat(path.coordinateXAt(6)).isEqualTo(13.0)
    assertThat(path.coordinateYAt(6)).isEqualTo(14.0)
  }

  @Test
  fun testMaterializedActions() {
    val path = Path()
    path.moveTo(1.0, 2.0)
    path.quadraticCurveTo(5.0, 6.0, 7.0, 8.0)
    path.bezierCurveTo(9.0, 10.0, 11.0, 12.0, 13.0, 14.0)

    val actions = path.actions
    assertThat(actions).hasSize(3)

    assertThat(actions[0]).isInstanceOf(MoveTo::class)
    assertThat(actions[0].endPointX).isEqualTo(1.0)
    assertThat(actions[0].endPointY).isEqualTo(2.0)

    val quadraticCurveTo = actions[1] as QuadraticCurveTo
    assertThat(quadraticCurveTo.controlX).isEqualTo(5.0)
    assertThat(quadraticCurveTo.controlY).isEqualTo(6.0)
    assertThat(quadraticCurveTo.endPointX).isEqualTo(7.0)
    assertThat(quadraticCurveTo.endPointY).isEqualTo(8.0)

    val bezierCurveTo = actions[2] as BezierCurveTo
    assertThat(bezierCurveTo.control1X).isEqualTo(9.0)
    assertThat(bezierCurveTo.control1Y).isEqualTo(10.0)
    assertThat(bezierCurveTo.control2X).isEqualTo(11.0)
    assertThat(bezierCurveTo.control2Y).isEqualTo(12.0)
    assertThat(bezierCurveTo.endPointX).isEqualTo(13.0)
    assertThat(bezierCurveTo.endPointY).isEqualTo(14.0)
  }

  @Test
  fun testCurrentAndFirstPoint() {
    val path = Path()

    //The first/current point is the *end* point of the action - not the control point
    path.quadraticCurveTo(5.0, 6.0, 7.0, 8.0)
    assertThat(path.firstPoint).isEqualTo(Coordinates(7.0, 8.0))
    assertThat(path.currentPoint).isEqualTo(Coordinates(7.0, 8.0))

    path.bezierCurveTo(9.0, 10.0, 11.0, 12.0, 13.0, 14.0)
    assertThat(path.firstPoint).isEqualTo(Coordinates(7.0, 8.0))
    assertThat(path.currentPoint).isEqualTo(Coordinates(13.0, 14.0))
    assertThat(path.currentPointXOrNaN()).isEqualTo(13.0)
    assertThat(path.currentPointYOrNaN()).isEqualTo(14.0)
    assertThat(path.firstPointXOrNaN()).isEqualTo(7.0)
    assertThat(path.firstPointYOrNaN()).isEqualTo(8.0)
  }

  @Test
  fun testPointAccessOnEmptyPath() {
    val path = Path()

    assertThat(path.currentPointXOrNaN()).isNaN()
    assertThat(path.currentPointYOrNaN()).isNaN()
    assertThat(path.firstPointXOrNaN()).isNaN()
    assertThat(path.firstPointYOrNaN()).isNaN()

    assertThrows<NoSuchElementException> { path.currentPoint }
    assertThrows<NoSuchElementException> { path.firstPoint }
    assertThrows<NoSuchElementException> { path.firstPointOfLastPart }
  }

  @Test
  fun testFirstPointOfLastPart() {
    val path = Path()
    path.moveTo(1.0, 2.0)
    path.lineTo(3.0, 4.0)
    assertThat(path.firstPointOfLastPart).isEqualTo(Coordinates(1.0, 2.0))

    //Second sub path
    path.moveTo(10.0, 20.0)
    path.lineTo(30.0, 40.0)
    path.bezierCurveTo(9.0, 10.0, 11.0, 12.0, 13.0, 14.0)
    assertThat(path.firstPointOfLastPart).isEqualTo(Coordinates(10.0, 20.0))
  }

  @Test
  fun testClosePath() {
    val path = Path()
    path.moveTo(1.0, 2.0)
    path.lineTo(3.0, 4.0)
    path.closePath()

    assertThat(path.actionCount).isEqualTo(3)
    assertThat(path.actionTypeAt(2)).isEqualTo(PathActionType.LineTo)
    assertThat(path.currentPoint).isEqualTo(Coordinates(1.0, 2.0))
  }

  @Test
  fun testBeginPathResets() {
    val path = Path()
    path.moveTo(1.0, 2.0)
    path.lineTo(3.0, 4.0)
    assertThat(path.isNewPath).isFalse()

    path.beginPath()

    assertThat(path.isNewPath).isTrue()
    assertThat(path.isEmpty()).isTrue()
    assertThat(path.actionCount).isEqualTo(0)
    assertThat(path.currentPointXOrNaN()).isNaN()

    //The path is usable again after the reset
    path.moveTo(5.0, 6.0)
    assertThat(path.actionCount).isEqualTo(1)
    assertThat(path.currentPoint).isEqualTo(Coordinates(5.0, 6.0))
  }

  @Test
  fun testRemoveLastActions() {
    val path = Path()
    path.moveTo(1.0, 2.0)
    path.lineTo(3.0, 4.0)
    path.bezierCurveTo(9.0, 10.0, 11.0, 12.0, 13.0, 14.0)
    path.lineTo(5.0, 6.0)

    path.removeLastActions(2)

    assertThat(path.actionCount).isEqualTo(2)
    assertThat(path.currentPoint).isEqualTo(Coordinates(3.0, 4.0))

    //Appending after the removal works
    path.lineTo(7.0, 8.0)
    assertThat(path.actionCount).isEqualTo(3)
    assertThat(path.currentPoint).isEqualTo(Coordinates(7.0, 8.0))
  }

  @Test
  fun testCopy() {
    val path = Path()
    path.moveTo(1.0, 2.0)
    path.quadraticCurveTo(5.0, 6.0, 7.0, 8.0)

    val copied = path.copy()
    assertThat(copied.actionCount).isEqualTo(2)
    assertThat(copied.actionTypeAt(1)).isEqualTo(PathActionType.QuadraticCurveTo)
    assertThat(copied.currentPoint).isEqualTo(Coordinates(7.0, 8.0))

    //The copy is independent
    copied.lineTo(30.0, 40.0)
    assertThat(copied.actionCount).isEqualTo(3)
    assertThat(path.actionCount).isEqualTo(2)
  }

  @Test
  fun testFrom() {
    val path = Path.from(listOf(Coordinates(1.0, 2.0), Coordinates(3.0, 4.0), Coordinates(5.0, 6.0)))

    assertThat(path.actionCount).isEqualTo(3)
    assertThat(path.actionTypeAt(0)).isEqualTo(PathActionType.MoveTo)
    assertThat(path.actionTypeAt(1)).isEqualTo(PathActionType.LineTo)
    assertThat(path.actionTypeAt(2)).isEqualTo(PathActionType.LineTo)
  }
}
