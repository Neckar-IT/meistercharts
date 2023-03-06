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
package com.meistercharts.geometry

import assertk.*
import assertk.assertions.*
import com.meistercharts.canvas.geometry.Matrix
import com.meistercharts.canvas.geometry.PI_4
import it.neckar.open.kotlin.lang.toDegrees
import it.neckar.open.javafx.test.JavaFxTest
import javafx.scene.transform.Affine
import org.junit.jupiter.api.Test

/**
 *
 */
@JavaFxTest
class MatrixCompareWithFxTest {
  @Test
  fun testOnlyScale() {
    val matrix = Matrix()
    val affine = Affine()

    compareEquals(matrix, affine)

    affine.appendRotation(1.2.toDegrees())
    matrix.prerotate(1.2)
    compareEquals(matrix, affine)
  }

  @Test
  fun testTranslateAndRotate() {
    val matrix = Matrix()
    val affine = Affine()

    compareEquals(matrix, affine)

    affine.appendTranslation(7.0, 8.0)
    matrix.pretranslate(7.0, 8.0)
    compareEquals(matrix, affine)

    affine.appendRotation(PI_4.toDegrees())
    matrix.prerotate(PI_4)
    compareEquals(matrix, affine)

    affine.appendRotation(PI_4.toDegrees())
    matrix.prerotate(PI_4)
    compareEquals(matrix, affine)

    affine.appendRotation(PI_4.toDegrees())
    matrix.prerotate(PI_4)
    compareEquals(matrix, affine)

    affine.appendRotation(PI_4.toDegrees())
    matrix.prerotate(PI_4)
    compareEquals(matrix, affine)
  }

  @Test
  fun testIt() {
    val matrix = Matrix()
    val affine = Affine()

    compareEquals(matrix, affine)

    matrix.pretranslate(19.0, 21.0)
    affine.appendTranslation(19.0, 21.0)
    compareEquals(matrix, affine)

    matrix.pretranslate(7.0, 8.0)
    affine.appendTranslation(7.0, 8.0)
    compareEquals(matrix, affine)


    assertThat(matrix.tx).isEqualTo(26.0)
    assertThat(matrix.ty).isEqualTo(29.0)

    matrix.prescale(1.2, 1.3)
    affine.appendScale(1.2, 1.3)

    assertThat(affine.tx).isEqualTo(26.0)
    assertThat(affine.ty).isEqualTo(29.0)
    assertThat(matrix.tx).isEqualTo(26.0)
    assertThat(matrix.ty).isEqualTo(29.0)

    compareEquals(matrix, affine)

    matrix.pretranslate(1.1, 2.1)
    affine.appendTranslation(1.1, 2.1)
    compareEquals(matrix, affine)

    affine.appendScale(4.0, 5.0)
    matrix.prescale(4.0, 5.0)
    compareEquals(matrix, affine)

    affine.appendRotation(1.0.toDegrees())
    matrix.prerotate(1.0)

    compareEquals(matrix, affine)
  }

  /**
   *  * <pre>
   *
   * JavaFX Affine:
   *[  mxx  mxy  mxz  tx  ]
   *[  myx  myy  myz  ty  ]
   *[  mzx  mzy  mzz  tz  ]
   *
   *
   * Matrix
   *  a b
   *
   * </pre>
   */

  fun compareEquals(matrix: Matrix, affine: Affine) {
    assertThat(matrix.a).isEqualTo(affine.mxx) //scale x
    assertThat(matrix.b).isEqualTo(affine.myx)
    assertThat(matrix.c).isEqualTo(affine.mxy)
    assertThat(matrix.d).isEqualTo(affine.myy) //scale y
    assertThat(matrix.tx).isEqualTo(affine.tx) //translation x
    assertThat(matrix.ty).isEqualTo(affine.ty) //translation y

  }
}
