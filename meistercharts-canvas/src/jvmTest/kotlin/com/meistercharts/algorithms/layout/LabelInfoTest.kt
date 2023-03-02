package com.meistercharts.algorithms.layout

import assertk.*
import assertk.assertions.*
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.annotations.Window
import com.meistercharts.annotations.Zoomed
import com.meistercharts.label.DomainRelativeLabel
import com.meistercharts.label.LabelData
import com.meistercharts.label.LayoutedLabel
import org.junit.jupiter.api.Test

/**
 */
internal class LabelInfoTest {
  @Test
  fun testOverlaps() {
    assertThat(createLL(10.0, 10.0).overlapsActualY(createLL(20.0, 10.0))).isTrue()
    assertThat(createLL(10.0, 10.0).overlapsActualY(createLL(10.0, 10.0))).isTrue()
    assertThat(createLL(10.0, 10.0).overlapsActualY(createLL(0.0, 10.0))).isTrue()
    assertThat(createLL(10.0, 10.0).overlapsActualY(createLL(-0.1, 10.0))).isFalse()
    assertThat(createLL(10.0, 10.0).overlapsActualY(createLL(20.1, 10.0))).isFalse()
  }
}


fun createLL(@Window preferredCenterY: Double, @Zoomed height: Double): LayoutedLabel {
  val layoutedLabel = LayoutedLabel(DomainRelativeLabel(0.0, LabelData("asdf", Color.aquamarine)), 10.0)

  layoutedLabel.preferredCenterY = preferredCenterY.toDouble()
  layoutedLabel.height = height.toDouble()

  return layoutedLabel
}
