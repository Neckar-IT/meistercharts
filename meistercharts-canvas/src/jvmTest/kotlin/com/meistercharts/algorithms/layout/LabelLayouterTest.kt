package com.meistercharts.algorithms.layout

import assertk.*
import assertk.assertions.*
import org.junit.jupiter.api.Test

/**
 */
class LabelLayouterTest {
  @Test
  fun testBasic() {
    val layouter = LabelLayouter()

    val labelLocation0 = createLL(10.0, 10.0)
    val labelLocation1 = createLL(11.0, 10.0)
    val labelLocation2 = createLL(12.0, 10.0)

    assertThat(labelLocation0.actualCenterY).all {
      isEqualTo(labelLocation0.preferredCenterY)
      isEqualTo(10.0)
    }
    assertThat(labelLocation0.actualMinY).isEqualTo(10.0 - 5)
    assertThat(labelLocation0.actualMaxY).isEqualTo(10.0 + 5)
  }

  @Test
  fun testOptimizeLocallyLower() {
    val layouter = LabelLayouter(3.0)

    val labelLocation0 = createLL(10.0, 10.0)
    val labelLocation1 = createLL(11.0, 10.0)
    val labelLocation2 = createLL(90.0, 10.0)

    layouter.avoidOverlap(labelLocation0, labelLocation1, labelLocation2)

    assertThat(labelLocation0.actualCenterY).all {
      isEqualTo(labelLocation0.preferredCenterY)
      isEqualTo(10.0)
    }
    assertThat(labelLocation2.actualCenterY).all {
      isEqualTo(labelLocation2.preferredCenterY)
      isEqualTo(90.0)
    }

    assertThat(labelLocation1.actualCenterY).isEqualTo(10.0 + 10 + layouter.labelSpacing)

    assertThat(labelLocation1.actualMinY).isEqualTo(labelLocation0.actualMaxY + layouter.labelSpacing)
  }

  @Test
  fun testOptimizeLocallyUpper() {
    val layouter = LabelLayouter(3.0)

    val labelLocation0 = createLL(10.0, 10.0)
    val labelLocation1 = createLL(89.0, 10.0)
    val labelLocation2 = createLL(90.0, 10.0)

    layouter.avoidOverlap(labelLocation0, labelLocation1, labelLocation2)

    assertThat(labelLocation0.actualCenterY).all {
      isEqualTo(labelLocation0.preferredCenterY)
      isEqualTo(10.0)
    }
    assertThat(labelLocation2.actualCenterY).all {
      isEqualTo(labelLocation2.preferredCenterY)
      isEqualTo(90.0)
    }

    assertThat(labelLocation1.actualCenterY).isEqualTo(90 - 10 - +layouter.labelSpacing)

    assertThat(labelLocation1.actualMaxY).isEqualTo(labelLocation2.actualMinY - layouter.labelSpacing)
  }

  @Test
  fun testLayoutReal() {
    val layouter = LabelLayouter(4.0)

    val labelLocation0 = createLL(10.0, 10.0)
    val labelLocation1 = createLL(90.0, 10.0)
    val labelLocation2 = createLL(90.0, 10.0)

    layouter.calculateOptimalPositions(listOf(labelLocation0, labelLocation1, labelLocation2))

    assertThat(labelLocation0.actualCenterY).isEqualTo(10.0)
    assertThat(labelLocation1.actualCenterY).isEqualTo(83.0)
    assertThat(labelLocation2.actualCenterY).isEqualTo(97.0)
  }

  @Test
  fun testSimple() {
    val layouter = LabelLayouter(4.0)

    val labelLocation0 = createLL(10.0, 10.0)
    val labelLocation1 = createLL(30.0, 10.0)

    layouter.calculateOptimalPositions(listOf(labelLocation0))
    assertThat(labelLocation0.actualCenterY).isEqualTo(10.0)

    layouter.calculateOptimalPositions(listOf(labelLocation0, labelLocation1))
    assertThat(labelLocation0.actualCenterY).isEqualTo(10.0)
    assertThat(labelLocation1.actualCenterY).isEqualTo(30.0)
  }

  @Test
  fun testLayoutReal3() {
    val layouter = LabelLayouter(4.0)

    val labelLocation0 = createLL(10.0, 10.0)
    val labelLocation1 = createLL(82.0, 10.0)
    val labelLocation2 = createLL(90.0, 10.0)

    layouter.calculateOptimalPositions(listOf(labelLocation0, labelLocation1, labelLocation2))

    assertThat(labelLocation0.actualCenterY).isEqualTo(10.0)
    assertThat(labelLocation1.actualCenterY).isEqualTo(79.0)
    assertThat(labelLocation2.actualCenterY).isEqualTo(93.0)
  }

  @Test
  fun testLayoutReal4() {
    val layouter = LabelLayouter(4.0)

    val labelLocation0 = createLL(10.0, 10.0)
    val labelLocation1 = createLL(50.0, 10.0)
    val labelLocation2 = createLL(90.0, 10.0)

    labelLocation1.actualCenterY = 89.0
    assertThat(labelLocation1.actualCenterY).isEqualTo(89.0)

    layouter.avoidOverlap(labelLocation0, labelLocation1, labelLocation2)

    assertThat(labelLocation0.actualCenterY).isEqualTo(10.0)
    assertThat(labelLocation1.actualCenterY).isEqualTo(50.0)
    assertThat(labelLocation2.actualCenterY).isEqualTo(90.0)
  }
}
