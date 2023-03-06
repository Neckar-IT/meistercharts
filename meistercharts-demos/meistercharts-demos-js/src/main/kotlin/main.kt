/**
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
import com.meistercharts.version.MeisterChartsVersion
import it.neckar.logging.Level
import it.neckar.logging.LogConfigurer
import it.neckar.logging.LoggerFactory

fun main() {
  LogConfigurer.initializeFromLocalStorage(Level.INFO)
  logger.info("Initialized Log levels from local storage. Active root Level: ${LogConfigurer.rootLevel}")
  logger.info("Starting DemosJS - ${MeisterChartsVersion.versionAsStringVerbose}")
}

private val logger = LoggerFactory.getLogger("demosjs")

