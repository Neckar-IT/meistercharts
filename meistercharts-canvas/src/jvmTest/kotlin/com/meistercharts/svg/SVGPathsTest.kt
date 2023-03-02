package com.meistercharts.svg

import com.meistercharts.resources.svg.SvgPaths
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

/**
 *
 */
class SVGPathsTest {
  @Disabled
  @Test
  fun testIt() {
    SvgPaths.all.forEach {
      println("--> $it")
      val path = SVGPathParser.from(it).parse()
    }
  }

  @Disabled
  @Test
  internal fun testSingle() {
    SVGPathParser.from(
      SvgPaths.neckarItQr
    ).parse()
      //SVGPathParser(SvgPaths.drag.svgPath).parse()
      .actions.forEach {
        println(it)
      }
  }
}

/*
M 21,9
  H 7
  V 7
  h 14 z
m 0,4
  H 7
  v -2
  h 14 z
m 0,4
  H 7
  V 15
  H 21 Z
M 5,17
  H 3
  V 15
  H 5 Z
M 5,7
  V 9
  H 3
  V 7 Z
m 0,6
  H 3
  v -2
  h 2 z
 */

/*
  M 3 7 L 3 9 L 5 9 L 5 7 L 3 7 z M 7 7 L 7 9 L 10.363281 9 L 8.9570312 7 L 7 7 z M 11.158203 7 L 12.564453 9 L 21 9 L 21 7 L 11.158203 7 z M 3 11 L 3 13 L 5 13 L 5 11 L 3 11 z M 7 11 L 7 13 L 13.177734 13 L 11.771484 11 L 7 11 z M 13.970703 11 L 15.376953 13 L 21 13 L 21 11 L 13.970703 11 z M 3 15 L 3 17 L 5 17 L 5 15 L 3 15 z M 7 15 L 7 17 L 15.990234 17 L 14.583984 15 L 7 15 z M 16.783203 15 L 18.189453 17 L 21 17 L 21 15 L 16.783203 15 z
 */
