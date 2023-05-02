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
package com.meistercharts.svg

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.xml.sax.InputSource
import java.io.StringReader
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

class SVGPathParserTest {
  @Disabled
  @Test
  fun testParsePath() {
    val path = SVGPathParser("M0 0h24v24H0z").parse()

    path.actions.forEach {
      println("--> $it")
    }
  }

  @Disabled
  @Test
  fun testParsePath2() {
    val path = SVGPathParser(
      """
          M 1 21 h22 L 12 2 1 21 z
          m 12 -3 h -2 v -2 h 2 v 2 z
          m 0 -4 h -2 v -4 h 2 v 4 z
          """.trimIndent()
    ).parse()

    path.actions.forEach {
      println("--> $it")
    }
  }

  @Test
  internal fun testExtractPathFromXml() {
    val svgPath = extractPath(xml)
    val path = SVGPathParser(svgPath).parse()
  }

  @Disabled
  @Test
  internal fun testExtractPathFromXml2() {
    val svgPath = extractPath(xml2)
    val path = SVGPathParser(svgPath).parse()

    path.actions.forEach {
      println("--> $it")
    }
  }

  fun extractPath(svgXml: String): String {
    val db: DocumentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
    val doc: Document = db.parse(InputSource(StringReader(xml)))

    val element = doc.documentElement

    val count = element.childNodes.length

    for (i in 0..element.childNodes.length) {
      val child = element.childNodes.item(i)

      if (child is Element) {
        val fill = child.getAttribute("fill")
        return child.getAttribute("d") ?: throw IllegalStateException("No d attribute found")
      }
    }

    throw IllegalArgumentException("Could not find a path")
  }
}


val xml: String = """
  <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24">
    <path fill="none" d="M0 0h24v24H0z"/>
    <path fill="#666" d="M1 21h22L12 2 1 21zm12-3h-2v-2h2v2zm0-4h-2v-4h2v4z"/>
  </svg>
""".trimIndent().trim()

val xml2: String = """
<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24">
  <path fill="none" d="M0 0h24v24H0V0z" />
  <path fill="#666" d="M15 3l2.3 2.3-2.89 2.87 1.42 1.42L18.7 6.7 21 9V3h-6zM3 9l2.3-2.3 2.87 2.89 1.42-1.42L6.7 5.3 9 3H3v6zm6 12l-2.3-2.3 2.89-2.87-1.42-1.42L5.3 17.3 3 15v6h6zm12-6l-2.3 2.3-2.87-2.89-1.42 1.42 2.89 2.87L15 21h6v-6z" />
</svg>
""".trimIndent().trim()

val xmlQrCode: String = """
  <?xml version="1.0" standalone="no"?>
  <svg xmlns="http://www.w3.org/2000/svg" version="1.1" width="253" height="253">
  <path style="fill:rgb(0, 0, 0)" d="M 11,11 l 11,0 0,11 -11,0 z M 22,11 l 11,0 0,11 -11,0 z M 33,11 l 11,0 0,11 -11,0 z M 44,11 l 11,0 0,11 -11,0 z M 55,11 l 11,0 0,11 -11,0 z M 66,11 l 11,0 0,11 -11,0 z M 77,11 l 11,0 0,11 -11,0 z M 110,11 l 11,0 0,11 -11,0 z M 121,11 l 11,0 0,11 -11,0 z M 165,11 l 11,0 0,11 -11,0 z M 176,11 l 11,0 0,11 -11,0 z M 187,11 l 11,0 0,11 -11,0 z M 198,11 l 11,0 0,11 -11,0 z M 209,11 l 11,0 0,11 -11,0 z M 220,11 l 11,0 0,11 -11,0 z M 231,11 l 11,0 0,11 -11,0 z M 11,22 l 11,0 0,11 -11,0 z M 77,22 l 11,0 0,11 -11,0 z M 99,22 l 11,0 0,11 -11,0 z M 132,22 l 11,0 0,11 -11,0 z M 143,22 l 11,0 0,11 -11,0 z M 165,22 l 11,0 0,11 -11,0 z M 231,22 l 11,0 0,11 -11,0 z M 11,33 l 11,0 0,11 -11,0 z M 33,33 l 11,0 0,11 -11,0 z M 44,33 l 11,0 0,11 -11,0 z M 55,33 l 11,0 0,11 -11,0 z M 77,33 l 11,0 0,11 -11,0 z M 165,33 l 11,0 0,11 -11,0 z M 187,33 l 11,0 0,11 -11,0 z M 198,33 l 11,0 0,11 -11,0 z M 209,33 l 11,0 0,11 -11,0 z M 231,33 l 11,0 0,11 -11,0 z M 11,44 l 11,0 0,11 -11,0 z M 33,44 l 11,0 0,11 -11,0 z M 44,44 l 11,0 0,11 -11,0 z M 55,44 l 11,0 0,11 -11,0 z M 77,44 l 11,0 0,11 -11,0 z M 99,44 l 11,0 0,11 -11,0 z M 121,44 l 11,0 0,11 -11,0 z M 132,44 l 11,0 0,11 -11,0 z M 165,44 l 11,0 0,11 -11,0 z M 187,44 l 11,0 0,11 -11,0 z M 198,44 l 11,0 0,11 -11,0 z M 209,44 l 11,0 0,11 -11,0 z M 231,44 l 11,0 0,11 -11,0 z M 11,55 l 11,0 0,11 -11,0 z M 33,55 l 11,0 0,11 -11,0 z M 44,55 l 11,0 0,11 -11,0 z M 55,55 l 11,0 0,11 -11,0 z M 77,55 l 11,0 0,11 -11,0 z M 165,55 l 11,0 0,11 -11,0 z M 187,55 l 11,0 0,11 -11,0 z M 198,55 l 11,0 0,11 -11,0 z M 209,55 l 11,0 0,11 -11,0 z M 231,55 l 11,0 0,11 -11,0 z M 11,66 l 11,0 0,11 -11,0 z M 77,66 l 11,0 0,11 -11,0 z M 99,66 l 11,0 0,11 -11,0 z M 110,66 l 11,0 0,11 -11,0 z M 143,66 l 11,0 0,11 -11,0 z M 165,66 l 11,0 0,11 -11,0 z M 231,66 l 11,0 0,11 -11,0 z M 11,77 l 11,0 0,11 -11,0 z M 22,77 l 11,0 0,11 -11,0 z M 33,77 l 11,0 0,11 -11,0 z M 44,77 l 11,0 0,11 -11,0 z M 55,77 l 11,0 0,11 -11,0 z M 66,77 l 11,0 0,11 -11,0 z M 77,77 l 11,0 0,11 -11,0 z M 99,77 l 11,0 0,11 -11,0 z M 121,77 l 11,0 0,11 -11,0 z M 143,77 l 11,0 0,11 -11,0 z M 165,77 l 11,0 0,11 -11,0 z M 176,77 l 11,0 0,11 -11,0 z M 187,77 l 11,0 0,11 -11,0 z M 198,77 l 11,0 0,11 -11,0 z M 209,77 l 11,0 0,11 -11,0 z M 220,77 l 11,0 0,11 -11,0 z M 231,77 l 11,0 0,11 -11,0 z M 110,88 l 11,0 0,11 -11,0 z M 11,99 l 11,0 0,11 -11,0 z M 22,99 l 11,0 0,11 -11,0 z M 33,99 l 11,0 0,11 -11,0 z M 44,99 l 11,0 0,11 -11,0 z M 55,99 l 11,0 0,11 -11,0 z M 77,99 l 11,0 0,11 -11,0 z M 88,99 l 11,0 0,11 -11,0 z M 99,99 l 11,0 0,11 -11,0 z M 110,99 l 11,0 0,11 -11,0 z M 143,99 l 11,0 0,11 -11,0 z M 154,99 l 11,0 0,11 -11,0 z M 176,99 l 11,0 0,11 -11,0 z M 198,99 l 11,0 0,11 -11,0 z M 220,99 l 11,0 0,11 -11,0 z M 22,110 l 11,0 0,11 -11,0 z M 33,110 l 11,0 0,11 -11,0 z M 44,110 l 11,0 0,11 -11,0 z M 55,110 l 11,0 0,11 -11,0 z M 66,110 l 11,0 0,11 -11,0 z M 88,110 l 11,0 0,11 -11,0 z M 110,110 l 11,0 0,11 -11,0 z M 132,110 l 11,0 0,11 -11,0 z M 154,110 l 11,0 0,11 -11,0 z M 165,110 l 11,0 0,11 -11,0 z M 176,110 l 11,0 0,11 -11,0 z M 187,110 l 11,0 0,11 -11,0 z M 198,110 l 11,0 0,11 -11,0 z M 209,110 l 11,0 0,11 -11,0 z M 220,110 l 11,0 0,11 -11,0 z M 231,110 l 11,0 0,11 -11,0 z M 11,121 l 11,0 0,11 -11,0 z M 22,121 l 11,0 0,11 -11,0 z M 33,121 l 11,0 0,11 -11,0 z M 44,121 l 11,0 0,11 -11,0 z M 55,121 l 11,0 0,11 -11,0 z M 66,121 l 11,0 0,11 -11,0 z M 77,121 l 11,0 0,11 -11,0 z M 88,121 l 11,0 0,11 -11,0 z M 99,121 l 11,0 0,11 -11,0 z M 132,121 l 11,0 0,11 -11,0 z M 143,121 l 11,0 0,11 -11,0 z M 154,121 l 11,0 0,11 -11,0 z M 165,121 l 11,0 0,11 -11,0 z M 176,121 l 11,0 0,11 -11,0 z M 209,121 l 11,0 0,11 -11,0 z M 220,121 l 11,0 0,11 -11,0 z M 66,132 l 11,0 0,11 -11,0 z M 99,132 l 11,0 0,11 -11,0 z M 121,132 l 11,0 0,11 -11,0 z M 132,132 l 11,0 0,11 -11,0 z M 143,132 l 11,0 0,11 -11,0 z M 154,132 l 11,0 0,11 -11,0 z M 187,132 l 11,0 0,11 -11,0 z M 198,132 l 11,0 0,11 -11,0 z M 209,132 l 11,0 0,11 -11,0 z M 22,143 l 11,0 0,11 -11,0 z M 33,143 l 11,0 0,11 -11,0 z M 55,143 l 11,0 0,11 -11,0 z M 77,143 l 11,0 0,11 -11,0 z M 88,143 l 11,0 0,11 -11,0 z M 110,143 l 11,0 0,11 -11,0 z M 143,143 l 11,0 0,11 -11,0 z M 165,143 l 11,0 0,11 -11,0 z M 187,143 l 11,0 0,11 -11,0 z M 198,143 l 11,0 0,11 -11,0 z M 231,143 l 11,0 0,11 -11,0 z M 99,154 l 11,0 0,11 -11,0 z M 121,154 l 11,0 0,11 -11,0 z M 154,154 l 11,0 0,11 -11,0 z M 165,154 l 11,0 0,11 -11,0 z M 176,154 l 11,0 0,11 -11,0 z M 187,154 l 11,0 0,11 -11,0 z M 198,154 l 11,0 0,11 -11,0 z M 209,154 l 11,0 0,11 -11,0 z M 231,154 l 11,0 0,11 -11,0 z M 11,165 l 11,0 0,11 -11,0 z M 22,165 l 11,0 0,11 -11,0 z M 33,165 l 11,0 0,11 -11,0 z M 44,165 l 11,0 0,11 -11,0 z M 55,165 l 11,0 0,11 -11,0 z M 66,165 l 11,0 0,11 -11,0 z M 77,165 l 11,0 0,11 -11,0 z M 99,165 l 11,0 0,11 -11,0 z M 110,165 l 11,0 0,11 -11,0 z M 121,165 l 11,0 0,11 -11,0 z M 132,165 l 11,0 0,11 -11,0 z M 143,165 l 11,0 0,11 -11,0 z M 165,165 l 11,0 0,11 -11,0 z M 176,165 l 11,0 0,11 -11,0 z M 209,165 l 11,0 0,11 -11,0 z M 220,165 l 11,0 0,11 -11,0 z M 11,176 l 11,0 0,11 -11,0 z M 77,176 l 11,0 0,11 -11,0 z M 132,176 l 11,0 0,11 -11,0 z M 154,176 l 11,0 0,11 -11,0 z M 176,176 l 11,0 0,11 -11,0 z M 187,176 l 11,0 0,11 -11,0 z M 198,176 l 11,0 0,11 -11,0 z M 209,176 l 11,0 0,11 -11,0 z M 220,176 l 11,0 0,11 -11,0 z M 11,187 l 11,0 0,11 -11,0 z M 33,187 l 11,0 0,11 -11,0 z M 44,187 l 11,0 0,11 -11,0 z M 55,187 l 11,0 0,11 -11,0 z M 77,187 l 11,0 0,11 -11,0 z M 99,187 l 11,0 0,11 -11,0 z M 110,187 l 11,0 0,11 -11,0 z M 165,187 l 11,0 0,11 -11,0 z M 176,187 l 11,0 0,11 -11,0 z M 187,187 l 11,0 0,11 -11,0 z M 198,187 l 11,0 0,11 -11,0 z M 11,198 l 11,0 0,11 -11,0 z M 33,198 l 11,0 0,11 -11,0 z M 44,198 l 11,0 0,11 -11,0 z M 55,198 l 11,0 0,11 -11,0 z M 77,198 l 11,0 0,11 -11,0 z M 99,198 l 11,0 0,11 -11,0 z M 110,198 l 11,0 0,11 -11,0 z M 154,198 l 11,0 0,11 -11,0 z M 187,198 l 11,0 0,11 -11,0 z M 209,198 l 11,0 0,11 -11,0 z M 220,198 l 11,0 0,11 -11,0 z M 11,209 l 11,0 0,11 -11,0 z M 33,209 l 11,0 0,11 -11,0 z M 44,209 l 11,0 0,11 -11,0 z M 55,209 l 11,0 0,11 -11,0 z M 77,209 l 11,0 0,11 -11,0 z M 99,209 l 11,0 0,11 -11,0 z M 132,209 l 11,0 0,11 -11,0 z M 143,209 l 11,0 0,11 -11,0 z M 154,209 l 11,0 0,11 -11,0 z M 176,209 l 11,0 0,11 -11,0 z M 209,209 l 11,0 0,11 -11,0 z M 11,220 l 11,0 0,11 -11,0 z M 77,220 l 11,0 0,11 -11,0 z M 99,220 l 11,0 0,11 -11,0 z M 110,220 l 11,0 0,11 -11,0 z M 121,220 l 11,0 0,11 -11,0 z M 132,220 l 11,0 0,11 -11,0 z M 154,220 l 11,0 0,11 -11,0 z M 187,220 l 11,0 0,11 -11,0 z M 198,220 l 11,0 0,11 -11,0 z M 209,220 l 11,0 0,11 -11,0 z M 11,231 l 11,0 0,11 -11,0 z M 22,231 l 11,0 0,11 -11,0 z M 33,231 l 11,0 0,11 -11,0 z M 44,231 l 11,0 0,11 -11,0 z M 55,231 l 11,0 0,11 -11,0 z M 66,231 l 11,0 0,11 -11,0 z M 77,231 l 11,0 0,11 -11,0 z M 99,231 l 11,0 0,11 -11,0 z M 110,231 l 11,0 0,11 -11,0 z M 143,231 l 11,0 0,11 -11,0 z M 165,231 l 11,0 0,11 -11,0 z M 176,231 l 11,0 0,11 -11,0 z M 198,231 l 11,0 0,11 -11,0 z M 220,231 l 11,0 0,11 -11,0 z " />  </svg>
""".trimIndent()
