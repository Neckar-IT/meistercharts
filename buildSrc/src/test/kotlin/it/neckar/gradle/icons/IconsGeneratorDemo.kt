package com.cedarsoft.gradle.icons

import baseNames
import it.neckar.gradle.icons.GeneratePaintableObject
import it.neckar.gradle.icons.GenerateSvgPaths
import listSvgFilesRecursively
import org.junit.jupiter.api.Test
import java.io.File

/**
 *
 */
class IconsGeneratorDemo {
  @Test
  internal fun testBasic() {
    val svgFiles = File("/home/johannes/projects/com.cedarsoft.monorepo/internal/closed/charting/algorithms/src/icons/material").listSvgFilesRecursively().baseNames()
    println(GeneratePaintableObject(svgFiles, "BasicIcons", "da.package").create())
  }
}

fun main() {
  val svgFiles = File("/home/johannes/projects/com.cedarsoft.monorepo/internal/closed/charting/algorithms/src/icons/material").listSvgFilesRecursively()
  println(GenerateSvgPaths(svgFiles, "SvgPaths", "da.package").create())
}
