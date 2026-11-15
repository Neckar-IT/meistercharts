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
package com.meistercharts.fx

import assertk.*
import assertk.assertions.*
import com.meistercharts.color.Color
import org.junit.jupiter.api.Test

class JavaFxColorTest {
  @Test
  fun testConversion() {
    assertThat(Color.web("#123456").toJavaFx().toString()).isEqualTo("0x123456ff")
    assertThat(Color.web("rgba(255, 0, 0, 0.3)").toJavaFx().toString()).isEqualTo("0xff00004d")
    assertThat(Color.web("rgb(255, 0, 0)").toJavaFx().toString()).isEqualTo("0xff0000ff")

    //assertThat(Color.web("rgb(100%,0%,60%)").toJavaFx().toString()).isEqualTo("0xff0099ff")
    //assertThat(Color.web("rgba(100%,0%, 60%, 0.5)").toJavaFx().toString()).isEqualTo("0xff009980")

    //assertThat(Color.web("#1b0").toJavaFx().toString()).isEqualTo("0x11bb00ff")
    //assertThat(Color.web("#1b07").toJavaFx().toString()).isEqualTo("0x11bb0077")
    //assertThat(Color.web("#ff000080").toJavaFx().toString()).isEqualTo("0xff000080")


    //Does not work
    //assertThat(Color.web("hwb(1.5708rad 60% 0%)").toJavaFx().toString()).isEqualTo("0xff009980")
    // assertThat(Color.web("rgba(2.55e2, 0e0, 1.53e2, 1e2%)").toJavaFx().toString()).isEqualTo("0xff0099ff")

    //Strange results!
    //assertThat(Color.web("hsl(120, 100%, 50%)").toJavaFx().toString()).isEqualTo("0x00ff00ff")
    //assertThat(Color.web("hsla(120, 100%, 50%, 0.3)").toJavaFx().toString()).isEqualTo("0x008000ff")
  }
}
