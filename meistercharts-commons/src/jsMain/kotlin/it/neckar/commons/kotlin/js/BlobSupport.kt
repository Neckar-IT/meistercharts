package it.neckar.commons.kotlin.js

import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.url.URL
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag

/**
 *
 */
object BlobSupport {
  /**
   * Creates a blog for the given content and type
   */
  fun createBlob(content: ByteArray, contentType: String): Blob {
    return Blob(arrayOf(content), BlobPropertyBag(contentType))
  }

  /**
   * Creates a download link and "clicks" that link to download the given blob
   */
  fun downloadBlob(blob: Blob, fileName: String) {
    val objectURL = URL.createObjectURL(blob)

    val hiddenLink = document.createElement("a")
    hiddenLink.setAttribute("style", "display: none;")
    hiddenLink.setAttribute("target", "_blank")
    document.body?.appendChild(hiddenLink) ?: throw IllegalArgumentException("no body found")

    try {
      hiddenLink.setAttribute("href", objectURL)
      hiddenLink.setAttribute("download", fileName)

      hiddenLink.asDynamic().click()
    } finally {
      hiddenLink.remove()
      URL.revokeObjectURL(objectURL)
    }
  }

  fun showBlobInNewWindow(blob: Blob) {
    val objectURL = URL.createObjectURL(blob)

    window.open(objectURL, target = "_blank")
  }
}
