/*
 * Copyright (C) 2013-2026 Neckar IT GmbH, Mössingen, Germany
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Linking this library statically or dynamically with other modules is
 * making a combined work based on this library. Thus, the terms and
 * conditions of the GNU General Public License cover the whole combination.
 *
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules, regardless of
 * the license terms of these independent modules, and to copy and distribute
 * the resulting combined work under terms of your choice, provided that every
 * copy of the combined work is accompanied by a complete copy of the source
 * code of this library.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
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
   * Creates a blob for the given content and type
   */
  fun createBlob(content: ByteArray, contentType: String): Blob {
    return Blob(arrayOf(content), BlobPropertyBag(contentType))
  }

  /**
   * Creates a download link and "clicks" that link to download the given blob
   */
  fun downloadBlob(blob: Blob, fileName: String) {
    val objectURL = toObjectUrl(blob)

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
    val objectURL = toObjectUrl(blob)

    window.open(objectURL, target = "_blank")
  }

  fun toObjectUrl(blob: Blob): String = URL.createObjectURL(blob)
}

inline fun Blob.toObjectUrl(): String {
  return BlobSupport.toObjectUrl(this)
}
