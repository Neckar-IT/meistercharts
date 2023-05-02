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
package com.meistercharts.canvas.geometry

import assertk.*
import assertk.assertions.*
import org.junit.jupiter.api.Test

class MatrixTest {
  @Test
  fun testMatrixBasics() {
    val matrix = Matrix()

    assertThat(matrix.toString()).isEqualTo("Matrix(a=1.0, b=0.0, c=0.0, d=1.0, tx=0.0, ty=0.0)")

    assertThat(matrix.a).isEqualTo(1.0)
    assertThat(matrix.b).isEqualTo(0.0)
    assertThat(matrix.c).isEqualTo(0.0)
    assertThat(matrix.d).isEqualTo(1.0)

    assertThat(matrix.tx).isEqualTo(0.0)
    assertThat(matrix.ty).isEqualTo(0.0)

    matrix.translate(7.0, 8.0)

    assertThat(matrix.tx).isEqualTo(7.0)
    assertThat(matrix.ty).isEqualTo(8.0)
  }
}
