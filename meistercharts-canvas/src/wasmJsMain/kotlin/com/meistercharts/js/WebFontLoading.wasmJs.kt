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
package com.meistercharts.js

internal actual fun listenForFontLoadingDone(onLoadingDone: (loadedFonts: () -> List<String>) -> Unit): Boolean {
  if (jsHasDocumentFonts().not()) {
    return false
  }
  jsListenForFontLoadingDone { event ->
    onLoadingDone {
      val fonts = jsEventFontFaceDescriptions(event)
      List(fonts.length) { index -> fonts[index]?.toString() ?: "?" }
    }
  }
  return true
}

private fun jsHasDocumentFonts(): Boolean = js("typeof document !== 'undefined' && 'fonts' in document")

private fun jsListenForFontLoadingDone(callback: (JsAny) -> Unit): Unit = js("document.fonts.addEventListener('loadingdone', callback)")

/**
 * Returns the formatted descriptions of the font faces loaded by THIS event (event.fontfaces) -
 * not all registered fonts of the document.
 */
private fun jsEventFontFaceDescriptions(event: JsAny): JsArray<JsString> =
  js("event.fontfaces.map(function(f) { return f.family + ' ' + f.style + ', ' + f.variant + ' ' + f.weight; })")
