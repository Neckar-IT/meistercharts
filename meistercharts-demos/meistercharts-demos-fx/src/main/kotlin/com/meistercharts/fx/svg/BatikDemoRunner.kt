package com.meistercharts.fx.svg

import javafx.application.Application
import javafx.embed.swing.SwingFXUtils
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.StackPane
import javafx.stage.Stage
import org.apache.batik.transcoder.TranscoderInput
import org.apache.batik.transcoder.image.PNGTranscoder

fun main() {
  Application.launch(BatikDemoRunner::class.java)
}

/**
 *
 */
class BatikDemoRunner : Application() {
  override fun start(primaryStage: Stage) {
    val root = StackPane()


    val imageTranscoder = BufferedImageTranscoder()
    imageTranscoder.addTranscodingHint(PNGTranscoder.KEY_WIDTH, 500f)
    imageTranscoder.addTranscodingHint(PNGTranscoder.KEY_HEIGHT, 500f)

    val imageView = ImageView()


    BatikDemoRunner::class.java.getResourceAsStream("amazon.svg")
      .use { inputStream ->
        val transIn = TranscoderInput(inputStream)

        imageTranscoder.transcode(transIn, null)
        // Use WritableImage if you want to further modify the image (by using a PixelWriter)
        //val out: WritableImage = WritableImage(500, 500)
        val img: Image = SwingFXUtils.toFXImage(imageTranscoder.bufferedImage, null)
        imageView.image = img
      }


    root.children.add(imageView)

    primaryStage.scene = Scene(root, 800.0, 600.0)
    primaryStage.show()
  }
}


