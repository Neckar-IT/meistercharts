package com.meistercharts.processvalues

import assertk.*
import assertk.assertions.*
import com.meistercharts.algorithms.painter.Color
import it.neckar.open.kotlin.lang.and
import it.neckar.open.kotlin.lang.wrapped
import org.junit.jupiter.api.Test

class StyleLambdaTest {
  @Test
  fun testBasics() {

    val style1 = Style1()
    val style2 = Style2()

    //Check default configuration
    assertThat(style1.backgroundColor).isEqualTo(Color.orange)
    assertThat(style2.backgroundColor).isEqualTo(Color.blue) //default value
    assertThat(style2.foregroundColor1).isEqualTo(Color.black) //default value
    assertThat(style2.foregroundColor2).isEqualTo(Color.black) //default value
    assertThat(style2.foregroundColor3).isEqualTo(Color.black) //default value


    val style1Config: Style1.() -> Unit = {
      backgroundColor = Color.beige

      style2Configuration = wrapped(style2Configuration) {
        foregroundColor1 = Color.cyan
      }

      style2Configuration = style2Configuration.and {
        foregroundColor2 = Color.red
      }

      ::style2Configuration.wrapped {
        foregroundColor3 = Color.orange
      }
    }

    style1.style1Config()
    style1.style2Configuration(style2)


    assertThat(style1.backgroundColor).isEqualTo(Color.beige)
    assertThat(style2.backgroundColor).isEqualTo(Color.brown) //from default lambda
    assertThat(style2.foregroundColor1).isEqualTo(Color.cyan) //From new lamba
    assertThat(style2.foregroundColor2).isEqualTo(Color.red) //From new lamba
    assertThat(style2.foregroundColor3).isEqualTo(Color.orange) //From new lamba
  }

  open class Style1 {
    var backgroundColor: Color = Color.orange

    var style2Configuration: Style2.() -> Unit = {
      backgroundColor = Color.brown
    }
  }

  open class Style2 {
    var backgroundColor: Color = Color.blue
    var foregroundColor1: Color = Color.black
    var foregroundColor2: Color = Color.black
    var foregroundColor3: Color = Color.black
  }
}
