package it.neckar.open.test.utils

import it.neckar.open.http.Url
import it.neckar.open.kotlin.lang.fromBase64
import it.neckar.open.resources.getResourceAsStreamSafe
import java.awt.image.BufferedImage
import javax.imageio.ImageIO

/**
 * Contains test data that can be used in tests
 */
object TestData {
  /**
   * A very small, but valid jpg image
   */
  val minimalImageJpg: ByteArray =
    "/9j/4AAQSkZJRgABAQEASABIAAD/2wBDAP//////////////////////////////////////////////////////////////////////////////////////wgALCAABAAEBAREA/8QAFBABAAAAAAAAAAAAAAAAAAAAAP/aAAgBAQABPxA=".fromBase64()

  val minimalImageJpgAsUrl: Url.DataScheme = Url.image(minimalImageJpg, "jpeg")

  init {
    //Use this value to copy paste the image into a json if necessary
    require(minimalImageJpgAsUrl.value == "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQEASABIAAD/2wBDAP//////////////////////////////////////////////////////////////////////////////////////wgALCAABAAEBAREA/8QAFBABAAAAAAAAAAAAAAAAAAAAAP/aAAgBAQABPxA=") {
      "Invalid minimalImageJpgAsUrl: ${minimalImageJpgAsUrl.value}"
    }
  }

  val drop300Bytes: ByteArray = this::class.getResourceAsStreamSafe("/images/drop_300.jpg").readBytes()
  val drop300: BufferedImage = ImageIO.read(drop300Bytes.inputStream())

  val dropBytes: ByteArray = this::class.getResourceAsStreamSafe("/images/drop.jpg").readBytes()
  val drop: BufferedImage = ImageIO.read(drop300Bytes.inputStream())
}
