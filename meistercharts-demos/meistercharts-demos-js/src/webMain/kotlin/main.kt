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
import com.meistercharts.demojs.startMeisterchartsDemo
import com.meistercharts.version.MeisterChartsVersion
import it.neckar.logging.Level
import it.neckar.logging.LogConfigurer
import it.neckar.logging.LoggerFactory
import it.neckar.logging.LoggerLocalStorageKeys
import kotlinx.browser.document
import org.w3c.dom.HTMLElement

fun main() {
  LogConfigurer.initializeFromLocalStorage(Level.INFO)
  logger.info("Initialized Log levels from local storage (\"${LoggerLocalStorageKeys.RootLevel}\"). Active root Level: ${LogConfigurer.rootLevel}")
  logger.info("Starting Demos - ${MeisterChartsVersion.versionAsStringVerbose}")

  val navigationContainer = document.getElementById("navigation-container") ?: throw IllegalStateException("Element <navigation-container> not found in the document")
  val meisterchartsContainer = document.getElementById("meistercharts-container") ?: throw IllegalStateException("Element <meistercharts-container> not found in the document")
  val descriptionContainer = document.getElementById("description-container") ?: throw IllegalStateException("Element <description-container> not found in the document")
  val configurationPaneContainer = document.getElementById("configuration-pane-container") ?: throw IllegalStateException("Element <configuration-pane-container> not found in the document")

  //Do not wait for web-fonts to be loaded but start the demo directly.
  //This way we can test that our handling of font-loading works.
  startMeisterchartsDemo(
    navigationContainer as HTMLElement,
    meisterchartsContainer as HTMLElement,
    descriptionContainer as HTMLElement,
    configurationPaneContainer as HTMLElement,
  )

}

private val logger = LoggerFactory.getLogger("demosjs")

