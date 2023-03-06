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
package com.meistercharts.fx.painter

import assertk.*
import assertk.assertions.*
import com.meistercharts.canvas.FontDescriptor
import com.meistercharts.canvas.FontSize
import it.neckar.open.javafx.test.JavaFxTest
import com.meistercharts.fx.font.CanvasFontMetricsCalculatorFX
import javafx.stage.Stage
import org.junit.jupiter.api.Test
import org.testfx.framework.junit5.Start

@JavaFxTest
class CanvasFontMetricsCalculatorFXTest {
  @Start
  fun start(stage: Stage) {
    val calculator = CanvasFontMetricsCalculatorFX()

    assertThat(calculator.calculatePLineDescent(FontDescriptor(size = FontSize(200.00)))).isBetween(30.0, 50.0)
    assertThat(calculator.calculatePLineDescent(FontDescriptor(size = FontSize(201.1874)))).isBetween(30.0, 50.0)

    //calculator.canvas.snapshot().let {
    //  assertThat(it.width).isEqualTo(200.0)
    //  assertThat(it.height).isEqualTo(2400.0)
    //
    //  ImageIO.write(SwingFXUtils.fromFXImage(it, null), "png", File("/tmp/out.png"));
    //}

    assertThat(calculator.calculatePLineDescent(FontDescriptor(size = FontSize(201.189)))).isBetween(30.0, 50.0)
    assertThat(calculator.calculatePLineDescent(FontDescriptor(size = FontSize(203.00)))).isBetween(30.0, 50.0)
    assertThat(calculator.calculatePLineDescent(FontDescriptor(size = FontSize(203.89)))).isBetween(30.0, 50.0)


    //Test with courier

    //val font = FontDescriptor(family = FontFamily("Courier New"), size = FontSize(195.72))

    //calculator.calculateCorrectionValueBottom(font, 36.75 * 4).let {
    //  ImageIO.write(SwingFXUtils.fromFXImage(calculator.canvas.snapshot(), null), "png", File("/tmp/test.png"));
    //  assertThat(it).isEqualTo(42.25)
    //}
  }

  @Test
  fun testIt() {

  }
}
