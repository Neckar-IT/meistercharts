package it.neckar.gradle.icons

import toCamelCase

/**
 * Creates the object that contains the svg path paintables
 */
class GeneratePaintableObject(
  val iconBaseNames: List<String>,
  val objectName: String,
  val packageName: String
) {
  fun create(): String {
    val builder = StringBuilder()

    builder.appendLine(
      """|package $packageName
           |
           |import com.meistercharts.color.Color
           |import com.meistercharts.resources.svg.PathPaintable
           |import com.meistercharts.color.ColorProvider
           |import com.meistercharts.color.ColorProviderNullable
           |import it.neckar.geometry.Direction
           |import it.neckar.geometry.Size
           |import com.meistercharts.resources.svg.SvgPaintableProviders
           |
           |/**
           | * Provides the basic paintables that are provided by MeisterCharts
           | * ** Generated automatically ** do not modify **
           | * call `gradle createIconDeclarations` to regenerate this file
           | */
           |object $objectName {
  """.trimMargin("|")
    )

    iconBaseNames.forEach {
      builder.appendLine("""  fun ${it.toCamelCase()}(size: Size = Size.PX_24, fill: ColorProviderNullable = Color.lightgray, stroke: ColorProviderNullable = null, alignment: Direction = Direction.Center): PathPaintable = SvgPaintableProviders.${it.toCamelCase()}.get(size, fill, stroke, alignment)""")
    }

    //add all
    builder.appendLine()
    builder.appendLine(
      """  fun all(size: Size = Size.PX_24, fill: ColorProvider = Color.lightgray): List<PathPaintable> =
          |    listOf(
        """.trimMargin()
    )

    builder.append(
      iconBaseNames.joinToString(separator = ",\n") {
        """      ${it.toCamelCase()}(size, fill)"""
      }
    )

    builder.appendLine(
      "\n    )\n}"
    )

    return builder.toString()
  }
}
