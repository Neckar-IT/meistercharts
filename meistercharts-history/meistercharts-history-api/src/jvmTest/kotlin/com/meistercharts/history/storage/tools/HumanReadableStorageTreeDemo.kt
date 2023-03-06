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
package com.meistercharts.history.storage.tools

import com.meistercharts.history.HistoryBucketRange
import com.meistercharts.history.storage.GZippedHistoryStorageSerializer
import com.meistercharts.history.storage.HistoryFileStorage
import com.meistercharts.history.storage.JsonHistoryStorageSerializer
import it.neckar.open.formatting.dateTimeFormatShort
import it.neckar.open.i18n.I18nConfiguration
import java.io.File

/**
 */
fun main() {
  println("Show the storage dir")
  val historyStorage = File("/tmp/storage").let { storageDir ->
    println("\tStorage dir: ${storageDir.absolutePath}")
    HistoryFileStorage(storageDir, GZippedHistoryStorageSerializer(JsonHistoryStorageSerializer()))
  }

  //First layer
  historyStorage.baseDir.listFiles()?.forEach { rangeDirectory ->
    val range = HistoryBucketRange.valueOf(rangeDirectory.name)

    println("range: $range")
    printMillisSubDirsRecursive(rangeDirectory, 1)
  }
}

private val format = dateTimeFormatShort

private fun printMillisSubDirsRecursive(directory: File, depths: Int) {
  directory.listFiles()!!.forEach { file ->
    val millis = file.name.toLong().toDouble()
    val size = if (file.isFile) {
      " -- ${file.length() / 1024} KB"
    } else {
      ""
    }

    println("${" ".repeat(depths)} ${file.name} - ${format.format(millis, I18nConfiguration.GermanyUTC)}$size")


    if (file.isDirectory) {
      printMillisSubDirsRecursive(file, depths + 1)
    }
  }
}
