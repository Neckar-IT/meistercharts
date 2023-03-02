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
