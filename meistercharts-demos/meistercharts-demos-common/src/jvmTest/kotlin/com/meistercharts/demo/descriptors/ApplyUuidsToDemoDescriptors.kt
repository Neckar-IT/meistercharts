/*
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
package com.meistercharts.demo.descriptors

import assertk.*
import assertk.assertions.*
import it.neckar.open.collections.fastForEach
import it.neckar.uuid.pseudoRandom
import org.apache.commons.io.filefilter.SuffixFileFilter
import java.io.File
import java.io.FilenameFilter
import kotlin.uuid.Uuid

fun main() {
  val dir = File("/home/johannes/projects/com.cedarsoft.monorepo/internal/open/meistercharts/meistercharts-demos/meistercharts-demos-common/src/commonMain/kotlin/com/meistercharts/demo/descriptors")
  assertThat(dir).isDirectory()

  dir.listFiles(SuffixFileFilter(".kt") as FilenameFilter).fastForEach { demoFile ->

    val fileContent = demoFile.readText()

    if (fileContent.contains(": MeisterchartsDemoDescriptor").not()) {
      println("Skip ${demoFile.name}")
      return@fastForEach
    }

    if (fileContent.contains("override val uuid: Uuid")) {
      //println("+")
      return@fastForEach
    }

    println("${demoFile.name}")
    println("----")


    val newContent = buildString {
      //Package statement
      val firstImportIndex = fileContent.indexOf("import")

      append(fileContent.substring(0, firstImportIndex))
      append("\nimport kotlin.uuid.Uuid\n")
      append("import it.neckar.uuid.uuidFrom\n")


      val endOfClassDeclarationIndex = fileContent.indexOf("{")
      println("index: $endOfClassDeclarationIndex")

      append(fileContent.substring(firstImportIndex, endOfClassDeclarationIndex + 1))
      append("\n  override val uuid: Uuid = uuidFrom(\"${Uuid.pseudoRandom()}\")")
      append(fileContent.substring(endOfClassDeclarationIndex + 1))
    }

    println(newContent)

    demoFile.writeText(newContent)

  }

}
