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
import com.meistercharts.design.SegoeUiDesign
import com.meistercharts.js.MeisterChartsPlatform
import it.neckar.logging.Level
import it.neckar.logging.LogConfigurer
import it.neckar.logging.console.ConsoleLogFunctionsSupport

fun main() {
  //Load logger configuration from local storage
  LogConfigurer.initializeFromLocalStorage(Level.WARN)
  ConsoleLogFunctionsSupport.init("meistercharts")

  MeisterChartsPlatform.init(corporateDesign = SegoeUiDesign)
}

/**
 * If set to true, the styles will be printed to the console
 */
@Deprecated("Use Logggers instead")
const val StyleDebugEnabled: Boolean = false
