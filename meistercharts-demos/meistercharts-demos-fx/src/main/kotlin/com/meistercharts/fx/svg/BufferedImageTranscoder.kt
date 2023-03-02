package com.meistercharts.fx.svg

import org.apache.batik.transcoder.TranscoderException
import org.apache.batik.transcoder.TranscoderOutput
import org.apache.batik.transcoder.image.ImageTranscoder
import java.awt.image.BufferedImage

/**
 */
class BufferedImageTranscoder : ImageTranscoder() {
  var bufferedImage: BufferedImage? = null
    private set

  override fun createImage(width: Int, height: Int): BufferedImage {
    return BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
  }

  @Throws(TranscoderException::class)
  override fun writeImage(img: BufferedImage, to: TranscoderOutput?) {
    bufferedImage = img
  }

}
