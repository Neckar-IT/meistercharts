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
package com.meistercharts.canvas.layout.buffer

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.meistercharts.color.Color
import com.meistercharts.style.BoxStyle
import it.neckar.open.test.utils.isNaN
import org.junit.jupiter.api.Test

class ValueLabelsBufferTest {
  @Test
  fun testSetAndGet() {
    val buffer = ValueLabelsBuffer(BoxStyle.gray, Color.black())

    buffer.prepare(2)
    buffer.set(0, locationY = 17.0, label = "label 0", boxStyle = BoxStyle.modernBlue, textColor = Color.red())

    assertThat(buffer.size).isEqualTo(2)
    assertThat(buffer.locationYAt(0)).isEqualTo(17.0)
    assertThat(buffer.labelAt(0)).isEqualTo("label 0")
    assertThat(buffer.boxStyleAt(0)).isEqualTo(BoxStyle.modernBlue)
    assertThat(buffer.textColorAt(0)).isEqualTo(Color.red())
  }

  @Test
  fun testSkippedEntriesKeepDefaults() {
    val buffer = ValueLabelsBuffer(BoxStyle.gray, Color.black())

    buffer.prepare(2)
    buffer.set(0, locationY = 17.0, label = "label 0", boxStyle = BoxStyle.modernBlue, textColor = Color.red())

    //index 1 has not been set - defaults from prepare
    assertThat(buffer.locationYAt(1)).isNaN()
    assertThat(buffer.labelAt(1)).isEqualTo(StringMultiBuffer.Uninitialized)
    assertThat(buffer.boxStyleAt(1)).isEqualTo(BoxStyle.gray)
    assertThat(buffer.textColorAt(1)).isEqualTo(Color.black())
  }

  @Test
  fun testPrepareResets() {
    val buffer = ValueLabelsBuffer(BoxStyle.gray, Color.black())
    buffer.prepare(1)
    buffer.set(0, locationY = 17.0, label = "label 0", boxStyle = BoxStyle.modernBlue, textColor = Color.red())

    buffer.prepare(1)

    assertThat(buffer.locationYAt(0)).isNaN()
    assertThat(buffer.boxStyleAt(0)).isEqualTo(BoxStyle.gray)
    assertThat(buffer.textColorAt(0)).isEqualTo(Color.black())
  }
}
