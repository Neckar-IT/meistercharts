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
@file:Suppress("RETURN_VALUE_NOT_USED_COERCION") // Test scope: the constructor is called for its preconditions, the instance is intentionally discarded

package com.meistercharts.custom.rainsensor

import assertk.assertThat
import assertk.assertions.hasMessage
import assertk.assertions.isEqualTo
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.canvas.paintable.Paintable
import com.meistercharts.platform.jvm.MeisterchartsJvm
import com.meistercharts.resources.JvmLocalResourcePaintableFactory
import it.neckar.geometry.Rectangle
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class RainLayerConfigurationTest {
  /**
   * [RainSensorResources] instantiates its paintables eagerly - without a factory the class can not be initialized at all
   */
  @BeforeEach
  fun installLocalResourcePaintableFactory() {
    MeisterchartsJvm.localResourcePaintableFactory = JvmLocalResourcePaintableFactory { _, _, _ ->
      object : Paintable {
        override fun boundingBox(paintingContext: LayerPaintingContext): Rectangle = Rectangle.zero
        override fun paint(paintingContext: LayerPaintingContext, x: Double, y: Double) {
        }
      }
    }
  }

  @Test
  fun testDefaultsAreValid() {
    val configuration = RainLayer.Configuration()

    assertThat(configuration.parallaxLayersCount).isEqualTo(3)
    assertThat(configuration.dropDuration).isEqualTo(2_000.0)
    assertThat(configuration.spaceHorizontalPerDrop).isEqualTo(140.0)
    assertThat(configuration.spaceVerticalPerDrop).isEqualTo(170.0)
  }

  @Test
  fun testParallaxLayersCountMustBePositive() {
    assertThat(assertThrows<IllegalArgumentException> {
      RainLayer.Configuration(parallaxLayersCount = 0)
    }).hasMessage("parallaxLayersCount must be greater than 0 but was <0>")
  }

  @Test
  fun testDropDurationMustBePositive() {
    assertThat(assertThrows<IllegalArgumentException> {
      RainLayer.Configuration(dropDuration = 0.0)
    }).hasMessage("dropDuration must be greater than 0 ms but was <0.0>")
  }

  @Test
  fun testSpaceHorizontalPerDropMustBePositive() {
    val configuration = RainLayer.Configuration()

    assertThat(assertThrows<IllegalArgumentException> {
      configuration.spaceHorizontalPerDrop = 0.0
    }).hasMessage("spaceHorizontalPerDrop must be greater than 0 but was <0.0>")

    //the rejected assignment must not have modified the configuration
    assertThat(configuration.spaceHorizontalPerDrop).isEqualTo(140.0)
  }

  @Test
  fun testSpaceVerticalPerDropMustBePositive() {
    val configuration = RainLayer.Configuration()

    assertThat(assertThrows<IllegalArgumentException> {
      configuration.spaceVerticalPerDrop = -1.0
    }).hasMessage("spaceVerticalPerDrop must be greater than 0 but was <-1.0>")

    assertThat(configuration.spaceVerticalPerDrop).isEqualTo(170.0)
  }

  @Test
  fun testNotANumberIsRejectedWithTheValue() {
    assertThat(assertThrows<IllegalArgumentException> {
      RainLayer.Configuration().spaceHorizontalPerDrop = Double.NaN
    }).hasMessage("spaceHorizontalPerDrop must be greater than 0 but was <NaN>")
  }
}
