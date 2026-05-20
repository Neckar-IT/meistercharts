package it.neckar.gradle.icons

import it.neckar.gradle.toCamelCase

/**
 * Creates a paintable provider for each SVG
 */
class GenerateSvgPathsProviders(
  val svgBaseNames: List<String>,
  val objectName: String,
  val packageName: String
) {
  init {
    require(objectName.first().isUpperCase()) { "Object name [$objectName] must start with an uppercase letter" }
  }

  fun create(): String {
    val builder = StringBuilder()

    builder.appendLine(
      """|package $packageName
           |
           |import it.neckar.geometry.Size
           |import com.meistercharts.svg.SVGPathParser
           |
           |/**
           | * Contains the SVG paths that can be used as paintables
           | * Each SVG has a base size of 24x24 and is scaled accordingly
           | * ** Generated automatically ** do not modify **
           | * call `gradle createIconDeclarations` to regenerate this file
           | */
           |object $objectName {
  """.trimMargin("|")
    )

    svgBaseNames.forEach {
      val valName = it.toCamelCase()
      builder.appendLine("""  val $valName: PathPaintableProvider = SVGPathParser.from(SvgPaths.$valName).parse().toProvider(Size.PX_24)""")
    }

    //add all
    builder.appendLine()
    builder.appendLine(
      """  fun all(): List<PathPaintableProvider> = listOf(
        """.trimMargin()
    )

    builder.append(
      svgBaseNames.joinToString(separator = ",\n") {
        """    ${it.toCamelCase()}"""
      }
    )

    builder.appendLine(
      "\n  )\n}"
    )

    return builder.toString()
  }
}
