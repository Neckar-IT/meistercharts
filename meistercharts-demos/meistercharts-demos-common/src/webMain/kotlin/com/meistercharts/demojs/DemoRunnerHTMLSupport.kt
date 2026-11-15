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
package com.meistercharts.demojs

import kotlinx.browser.document
import org.w3c.dom.Element
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement

/**
 * Displays uncaught errors in a fixed banner at the top of the demo page.
 *
 * Uses plain DOM calls (no kotlinx.html DOM builders): the JS-flavored builders exist for the
 * js and wasmJs targets but not in the kotlinx-html metadata, which breaks the shared webMain
 * metadata compilation.
 */
object DemoRunnerHTMLSupport {
  init {
    (document.createElement("div") as HTMLDivElement).also {
      it.id = "exceptionBanner"
      it.style.cssText = "position: fixed; top:0; left: 0; background-color: red; color: white; width: 100%;"
      document.body!!.appendChild(it)
    }
  }

  val banner: Element = document.getElementById("exceptionBanner") ?: throw IllegalStateException("Could not find Element with id: exceptionBanner")

  fun displayNullError(message: Any?) {
    appendErrorBlock("NULL Error!", listOf("Message: $message"))
  }

  fun displayError(throwable: Throwable) {
    appendErrorBlock(
      "JS Error!", listOf(
        "Throwable: $throwable",
        "Message: ${throwable.message}",
        "Cause: ${throwable.cause}",
      )
    )
  }

  fun displayOtherError(message: Any?, error: Any) {
    appendErrorBlock(
      "JS OTHER Error!", listOf(
        "Error: $error",
        "Message: $message",
      )
    )
  }

  private fun appendErrorBlock(headline: String, lines: List<String>) {
    val block = document.createElement("div") as HTMLElement
    block.style.cssText = "margin-left: 10px; margin-right: 10px;"

    val headlineElement = document.createElement("h4")
    headlineElement.textContent = headline
    block.appendChild(headlineElement)

    lines.forEach { line ->
      val paragraph = document.createElement("p")
      paragraph.textContent = line
      block.appendChild(paragraph)
    }

    banner.appendChild(block)
    banner.appendChild(document.createElement("hr"))
  }
}
