package com.meistercharts.map

import com.meistercharts.algorithms.tile.TileIndex
import it.neckar.open.unit.other.deg
import com.google.common.io.ByteStreams
import javafx.application.Application
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.StackPane
import javafx.stage.Stage
import org.tbee.javafx.scene.layout.MigPane
import java.io.ByteArrayInputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.PI
import kotlin.math.asinh
import kotlin.math.floor
import kotlin.math.tan

fun main() {
  Application.launch(OpenStreetMapDemo::class.java)
}

class OpenStreetMapDemo : Application() {

  private val image: ObjectProperty<Image> = SimpleObjectProperty(null)

  override fun start(primaryStage: Stage) {
    val root = MigPane("insets 0, fill", "[][]unrelated[][][][fill, grow]", "[][fill, grow]")

    val latitudeTextField = TextField()
    latitudeTextField.text = "48.404732"

    val longitudeTextField = TextField()
    longitudeTextField.text = "9.052400"

    val goButton = Button("Go!")
    goButton.setOnAction {
      goTo(latitudeTextField.text, longitudeTextField.text)
    }

    val imageView = ImageView()
    imageView.imageProperty().bind(image)
    imageView.isPreserveRatio = true
    val imageViewStackPane = StackPane()
    imageViewStackPane.children.add(imageView)
    imageViewStackPane.minWidth = 1.0
    imageViewStackPane.minHeight = 1.0
    imageView.fitWidthProperty().bind(imageViewStackPane.widthProperty())
    imageView.fitHeightProperty().bind(imageViewStackPane.heightProperty())

    root.add(Label("Latitude"))
    root.add(latitudeTextField)
    root.add(Label("Longitude"))
    root.add(longitudeTextField)
    root.add(goButton, "wrap")
    root.add(imageViewStackPane, "span, grow")


    primaryStage.scene = Scene(root)

    primaryStage.width = 1024.0
    primaryStage.height = 768.0
    primaryStage.show()

  }

  private fun goTo(latitudeAsString: String, longitudeAsString: String) {
    @deg val latitude = latitudeAsString.toDoubleOrNull()
    @deg val longitude = longitudeAsString.toDoubleOrNull()
    if (latitude == null || longitude == null) {
      return
    }
    val zoom = 12
    // compute url
    val tileCoordinates = getXYTile(latitude, longitude, zoom)
    // see https://wiki.openstreetmap.org/wiki/Slippy_map_tilenames
    val url = URL("https://a.tile.openstreetmap.org/$zoom/${tileCoordinates.x}/${tileCoordinates.y}.png") // a 256x256 pixel tile

    val connection = url.openConnection() as HttpURLConnection
    connection.requestMethod = "GET"
    connection.setRequestProperty("User-Agent", "Neckar IT GmbH - MeisterCharts")
    connection.connectTimeout = 3000
    connection.readTimeout = 3000

    connection.inputStream.use {
      val buffer = ByteStreams.toByteArray(it)
      image.set(Image(ByteArrayInputStream(buffer)))
    }
  }

  // see https://wiki.openstreetmap.org/wiki/Slippy_map_tilenames#Kotlin
  private fun getXYTile(@deg latitude: Double, @deg longitude: Double, zoom: Int): TileIndex {
    val latRad = Math.toRadians(latitude)
    var xTile = floor((longitude + 180) / 360 * (1 shl zoom)).toInt()
    var yTile = floor((1.0 - asinh(tan(latRad)) / PI) / 2 * (1 shl zoom)).toInt()

    if (xTile < 0) {
      xTile = 0
    }
    if (xTile >= (1 shl zoom)) {
      xTile = (1 shl zoom) - 1
    }
    if (yTile < 0) {
      yTile = 0
    }
    if (yTile >= (1 shl zoom)) {
      yTile = (1 shl zoom) - 1
    }

    return TileIndex(xTile, yTile)
  }
}
