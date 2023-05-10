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
package com.meistercharts.fx

import com.meistercharts.canvas.Image
import com.meistercharts.canvas.JvmImageLoader
import com.meistercharts.model.Size
import com.meistercharts.events.ImageLoadedEventBroker
import com.google.common.io.ByteStreams
import javafx.application.Platform
import java.io.ByteArrayInputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

/**
 * Loader for JVM images using JavaFX
 */
class ImageLoaderFX : JvmImageLoader {
  override fun loadImage(url: String, callback: (Image) -> Unit) {
    thread {
      val connection = URL(url).openConnection() as HttpURLConnection
      connection.requestMethod = "GET"
      connection.setRequestProperty("User-Agent", "Neckar IT GmbH, MeisterCharts, info@neckar.it, +49 (0)7473 - 959 49 60")
      connection.connectTimeout = 30_000
      connection.readTimeout = 30_000

      connection.inputStream.use {
        val buffer = ByteStreams.toByteArray(it)
        val javaFxImage = javafx.scene.image.Image(ByteArrayInputStream(buffer))
        val image = Image(javaFxImage, Size(javaFxImage.width, javaFxImage.height))

        Platform.runLater {
          callback(image)
          ImageLoadedEventBroker.notifyLoaded()
        }
      }
    }
  }
}
