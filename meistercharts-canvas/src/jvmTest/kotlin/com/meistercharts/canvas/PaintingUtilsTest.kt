package com.meistercharts.canvas

import assertk.*
import assertk.assertions.*
import org.junit.jupiter.api.Test

/**
 *
 */
class PaintingUtilsTest {
  @Test
  fun testSnapPosition() {
    assertThat(PaintingUtils.snapPositionFactor(2.4, true, 1.0)).isEqualTo(2.0)
    assertThat(PaintingUtils.snapPositionFactor(2.49, true, 1.0)).isEqualTo(2.0)
    assertThat(PaintingUtils.snapPositionFactor(2.5, true, 1.0)).isEqualTo(2.0)
    assertThat(PaintingUtils.snapPositionFactor(2.51, true, 1.0)).isEqualTo(3.0)
    assertThat(PaintingUtils.snapPositionFactor(2.6, true, 1.0)).isEqualTo(3.0)

    assertThat(PaintingUtils.snapPositionFactor(2.4, true, 2.0)).isEqualTo(2.0)
    assertThat(PaintingUtils.snapPositionFactor(2.49, true, 2.0)).isEqualTo(2.0)
    assertThat(PaintingUtils.snapPositionFactor(2.5, true, 2.0)).isEqualTo(2.0)
    assertThat(PaintingUtils.snapPositionFactor(2.55, true, 2.0)).isEqualTo(2.0)
    assertThat(PaintingUtils.snapPositionFactor(2.6, true, 2.0)).isEqualTo(2.0)
    assertThat(PaintingUtils.snapPositionFactor(3.1, true, 2.0)).isEqualTo(4.0)

    assertThat(PaintingUtils.snapPositionFactor(2.4, true, 0.5)).isEqualTo(2.5)
    assertThat(PaintingUtils.snapPositionFactor(2.49, true, 0.5)).isEqualTo(2.5)
    assertThat(PaintingUtils.snapPositionFactor(2.5, true, 0.5)).isEqualTo(2.5)
    assertThat(PaintingUtils.snapPositionFactor(2.51, true, 0.5)).isEqualTo(2.5)
    assertThat(PaintingUtils.snapPositionFactor(2.6, true, 0.5)).isEqualTo(2.5)

    assertThat(PaintingUtils.snapPositionFactor(2.9, true, 0.5)).isEqualTo(3.0)
  }

  @Test
  fun testSnapFactor() {
    //ATTENTION: Always rounds up!
    assertThat(PaintingUtils.snapSizeFactor(2.5, true, 1.0)).isEqualTo(3.0)
    assertThat(PaintingUtils.snapSizeFactor(2.5, true, 2.0)).isEqualTo(4.0)

    assertThat(PaintingUtils.snapSizeFactor(2.5, true, 0.5)).isEqualTo(2.5)
    assertThat(PaintingUtils.snapSizeFactor(2.7, true, 0.5)).isEqualTo(3.0)
    assertThat(PaintingUtils.snapSizeFactor(2.8, true, 0.5)).isEqualTo(3.0)

    assertThat(PaintingUtils.snapSizeFactor(10.53, true, 1.0)).isEqualTo(11.0)
    assertThat(PaintingUtils.snapSizeFactor(10.53, true, 0.5)).isEqualTo(11.0)
    assertThat(PaintingUtils.snapSizeFactor(11.03, true, 0.5)).isEqualTo(11.5)

    assertThat(PaintingUtils.snapSizeFactor(10.5, true, 1.27)).isCloseTo(1.27 * 9, 0.0001)
    assertThat(PaintingUtils.snapSizeFactor(11.44, true, 1.27)).isCloseTo(1.27 * 10, 0.0001)
  }

  @Test
  fun testSnapping() {
    //Szenario 1
    assertThat(PaintingUtils.snapPosition(0.1, true)).isEqualTo(0.0)
    assertThat(PaintingUtils.snapSize(0.5, true)).isEqualTo(1.0)
  }

  @Test
  fun testSnapMode() {
    assertThat(PaintingUtils.snapSize(0.5, true, SnapMode.EVEN_ODD)).isEqualTo(1.0)
    assertThat(PaintingUtils.snapSize(0.5, true, SnapMode.ODD)).isEqualTo(1.0)
    assertThat(PaintingUtils.snapSize(0.5, true, SnapMode.EVEN)).isEqualTo(2.0)

    assertThat(PaintingUtils.snapSize(0.1, true, SnapMode.EVEN_ODD)).isEqualTo(1.0)
    assertThat(PaintingUtils.snapSize(0.1, true, SnapMode.ODD)).isEqualTo(1.0)
    assertThat(PaintingUtils.snapSize(0.1, true, SnapMode.EVEN)).isEqualTo(2.0)

    assertThat(PaintingUtils.snapSize(1.0, true, SnapMode.EVEN_ODD)).isEqualTo(1.0)
    assertThat(PaintingUtils.snapSize(1.0, true, SnapMode.ODD)).isEqualTo(1.0)
    assertThat(PaintingUtils.snapSize(1.0, true, SnapMode.EVEN)).isEqualTo(2.0)

    assertThat(PaintingUtils.snapSize(1.5, true, SnapMode.EVEN_ODD)).isEqualTo(2.0)
    assertThat(PaintingUtils.snapSize(1.5, true, SnapMode.ODD)).isEqualTo(3.0)
    assertThat(PaintingUtils.snapSize(1.5, true, SnapMode.EVEN)).isEqualTo(2.0)

    assertThat(PaintingUtils.snapSize(2.0, true, SnapMode.EVEN_ODD)).isEqualTo(2.0)
    assertThat(PaintingUtils.snapSize(2.0, true, SnapMode.ODD)).isEqualTo(3.0)
    assertThat(PaintingUtils.snapSize(2.0, true, SnapMode.EVEN)).isEqualTo(2.0)
  }

  /**
   * Szenario: Painting a rect from the center(!)
   *
   * ATTENTION: Does only work with *even* sizes
   */
  @Test
  fun testSnapSzenarioPaintCenterEvent() {
    val snappedCenter = PaintingUtils.snapPosition(
      value = 10.84,
      snapToPixel = true
    )
    val snappedSize = PaintingUtils.snapSize(
      value = 1.2,
      snapToPixel = true
    )
    val snappedSizeHalf = snappedSize / 2.0
    val left = snappedCenter - snappedSizeHalf
    val right = snappedCenter + snappedSizeHalf
    assertThat(snappedCenter).isEqualTo(expected = 11.0)
    assertThat(snappedSize).isEqualTo(expected = 2.0)
    assertThat(left).isEqualTo(
      expected = 10.0
    )
    assertThat(right).isEqualTo(12.0)
  }
}
