package com.meistercharts.demo

import javafx.application.Application
import javafx.stage.Stage
import java.io.File
import java.io.IOException

/**
 *
 *
 */
fun main(args: Array<String>) {
  val expectedDir = File("/tmp/screenshots/master")
  val currentDir = File("/tmp/screenshots")
  val diffDir = File("/tmp/screenshots/diff").also { it.mkdir() }


  val expectedScreenShots = expectedDir.listFiles { pathname -> pathname.isFile } ?: throw IOException("Could not list content ${expectedDir.absolutePath}")

  expectedScreenShots.forEach { expectedScreenShot ->
    val currentScreenShot = File(currentDir, expectedScreenShot.name)
    if (!currentScreenShot.isFile) {
      println("Can not compare - file not found: $currentScreenShot")
    } else {
      println("Comparing ${currentScreenShot.absolutePath}")
    }

    val diffFile = File(diffDir, "${currentScreenShot.name}-diff.png")
    Runtime.getRuntime().exec(arrayOf("compare", "-compose", "src", currentScreenShot.absolutePath, expectedScreenShot.absolutePath, diffFile.absolutePath))
  }
}

class CompareScreenshots : Application() {
  override fun start(primaryStage: Stage?) {

  }
}
