package com.meistercharts.algorithms.painter

import assertk.*
import assertk.assertions.*
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class ColorParsingTest {
  val examples: List<String> = listOf(
    "AliceBlue",
    "#123456",
    "#1b0",
    "#1b07",
    "#ff000080",
    "rgba(255, 0, 0, 0.3)",
    "hsl(120, 100%, 50%)",
    "hsla(120, 100%, 50%, 0.3)",
    "rgb(100%,0%,60%)",
    "rgb(100%,0%,60%, 0.5)",
    "rgb(2.55e2, 0e0, 1.53e2, 1e2%)",
    "hwb(1.5708rad 60% 0%)",
  )

  @Disabled
  @Test
  fun testAll() {
    examples.forEach { unparsed ->
      //Color.web(unparsed).toRgba()
    }
  }

  @Test
  fun testParsing() {
    val color = javafx.scene.paint.Color.web("#007fc3")
    assertThat(color.red).isEqualTo(0.0)
    assertThat(color.green).isEqualTo(0.49803921580314636)
  }
}
