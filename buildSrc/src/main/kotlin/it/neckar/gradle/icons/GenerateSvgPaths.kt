package it.neckar.gradle.icons

import com.google.common.io.Files
import it.neckar.gradle.toCamelCase
import java.io.File
import java.nio.charset.StandardCharsets

/**
 * Extracts the svg paths for each SVG  and creates constants
 */
class GenerateSvgPaths(
  val svgFiles: List<File>,
  val objectName: String,
  val packageName: String
) {
  fun create(): String {
    val builder = StringBuilder()

    builder.appendLine(
      """|package $packageName
           |
           |/**
           | * Contains the SVG paths for the image IDs that are provided by MeisterCharts
           | * ** Generated automatically ** do not modify **
           | * call `gradle createIconDeclarations` to regenerate this file
           | */
           |object $objectName {
  """.trimMargin("|")
    )

    val valueNames = mutableListOf<String>()

    svgFiles.forEach { file ->
      val baseName = file.nameWithoutExtension

      val svgContent = Files.toByteArray(file).toString(StandardCharsets.UTF_8)
      val pathAsString = SvgPathExtract().extract(svgContent, file.name)

      if (pathAsString.size > 1) {
        println("Warning! Too many paths. Not supported!: $baseName")
      }

      val firstSvgPath = pathAsString.first()

      val valName = baseName.toCamelCase()
      builder.appendLine("""  val $valName: SvgPath = SvgPath("$firstSvgPath")""")

      valueNames.add(valName)
    }

    builder.append("\n")
    builder.append("  val all: List<SvgPath> = listOf(")
    builder.append(valueNames.joinToString(", "))
    builder.append(")\n")

    builder.appendLine(
      """|}
  """.trimMargin("|")
    )

    return builder.toString()
  }
}
