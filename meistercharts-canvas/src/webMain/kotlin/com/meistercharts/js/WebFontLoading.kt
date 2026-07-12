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

/**
 * Registers [onLoadingDone] on `document.fonts` ("loadingdone" event of the CSS Font Loading API).
 * The callback receives a *provider* for the formatted descriptions of the font faces loaded by
 * this event - as provider, so the list (one interop string copy per face) is only built when the
 * consumer actually logs it.
 *
 * The FontFaceSet external declaration is target-specific (Array vs JsArray in the event payload),
 * therefore the shared web code goes through this expect function.
 *
 * @return false if `document.fonts` is not supported by the browser
 */
internal expect fun listenForFontLoadingDone(onLoadingDone: (loadedFonts: () -> List<String>) -> Unit): Boolean
