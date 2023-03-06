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
