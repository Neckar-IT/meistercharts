package it.neckar.gradle.icons

import org.w3c.dom.Document
import org.w3c.dom.Element
import org.xml.sax.InputSource
import java.io.StringReader
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory


private fun String.containsArc() = contains("a") || contains("A")


/**
 * Extracts the paths from an SVG file
 */
class SvgPathExtract {
  /**
   * Returns the path within the given SVG
   */
  fun extract(svgContent: String, fileInfo: String): List<String> {
    val paths = mutableListOf<String>()

    val documentBuilder: DocumentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
    val document: Document = documentBuilder.parse(InputSource(StringReader(svgContent)))

    val documentElement = document.documentElement

    val viewBox = documentElement.getAttribute("viewBox")
    val width = documentElement.getAttribute("width").removeSuffix("px")
    val height = documentElement.getAttribute("width").removeSuffix("px")

    require(viewBox.isNotBlank()) {
      "ViewBox is required"
    }
    require(width.isNotBlank()) { "width is required" }
    require(height.isNotBlank()) { "height is required" }


    println("################# VIEWBOX for $fileInfo ###############")
    println(viewBox)

    val viewBoxValues = viewBox.split(" ").map { it.toDouble() }
    require(viewBoxValues.size == 4) {
      "ViewBox [$viewBox] must have 4 values"
    }

    val viewBoxY = viewBoxValues[1]
    val viewBoxX = viewBoxValues[0]
    val viewBoxWidth = viewBoxValues[2]
    val viewBoxHeight = viewBoxValues[3]

    require(viewBoxX == 0.0) {
      "X in viewBox must be 0"
    }
    require(viewBoxY == 0.0) {
      "Y in viewBox must be 0"
    }

    require(viewBoxWidth == width.toDouble()) {
      "Width in viewBox does not match width attribute"
    }
    require(viewBoxHeight == height.toDouble()) {
      "Height in viewBox does not match height attribute"
    }

    for (i in 0..documentElement.childNodes.length) {
      val child = documentElement.childNodes.item(i)

      if (child is Element) {
        val fill = child.getAttribute("fill")

        if ("none" == fill) {
          //Skip for paths without a fill
          continue
        }

        val path = child.getAttribute("d")
        if (path.containsArc()) {
          throw IllegalArgumentException("Path contains arc - this is not yet supported")
        }
        paths.add(path)
      }
    }

    return paths
  }
}
