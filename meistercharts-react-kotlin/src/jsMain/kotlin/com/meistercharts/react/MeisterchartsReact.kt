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
package com.meistercharts.react

import com.meistercharts.canvas.DirtyReason
import com.meistercharts.charts.ChartGestalt
import com.meistercharts.js.MeisterchartBuilderJS
import com.meistercharts.js.MeisterchartJS
import it.neckar.commons.kotlin.js.safeGet
import it.neckar.logging.Logger
import it.neckar.logging.LoggerFactory
import it.neckar.logging.debug
import org.w3c.dom.HTMLDivElement
import react.*
import react.dom.*


/**
 * A MeisterCharts React component
 */
fun RBuilder.meistercharts(
  /**
   * Attention! Every time the description is changed, a new [MeisterchartJS] instance is created
   */
  description: String,

  /**
   * Attention! Every time the gestalt is changed, a new [MeisterchartJS] instance is created
   */
  gestalt: ChartGestalt,

  /**
   * If true is provided, mark as dirty is called
   */
  callMarkAsDirty: Boolean,

  width: String = "100%",
  height: String = "600px",

  ): Unit = child(Meistercharts) {
  attrs {
    this.description = description
    this.gestalt = gestalt
    this.callMarkAsDirty = callMarkAsDirty
    this.width = width
    this.height = height
  }
}

/**
 * React component for MeisterCharts
 */
val Meistercharts: FC<MeisterchartProps> = fc("MeisterChartsReact") { props ->
  val description = props::description.safeGet()
  val gestalt = props::gestalt.safeGet()
  val callMarkAsDirty = props::callMarkAsDirty.safeGet()
  val width = props::width.safeGet()
  val height = props::height.safeGet()

  //Holds the reference to the DIV for MeisterCharts
  val meisterChartsContainerRef = useRef<HTMLDivElement>()

  div {
    attrs {
      ref = meisterChartsContainerRef
    }
  }

  /**
   * Creates the MeisterCharts instance
   */
  val meisterChartJS: MeisterchartJS = useMemo(description, gestalt) {
    MeisterchartBuilderJS(
      description = description,
    ).also {
      gestalt.configure(it)
    }.build()
  }

  //Mark as dirty if necessary
  if (callMarkAsDirty) {
    meisterChartJS.markAsDirty(DirtyReason.DataUpdated)
  }

  useEffect(meisterChartsContainerRef, meisterChartJS) {
    val meisterChartsContainer: HTMLDivElement? = meisterChartsContainerRef.current

    logger.debug { "Attaching the MeisterCharts holder to its container DIV : $meisterChartsContainerRef -- ${meisterChartsContainerRef.current}" }
    if (meisterChartsContainer == null) {
      return@useEffect
    }

    meisterChartJS.holder.style.width = width
    meisterChartJS.holder.style.height = height

    meisterChartsContainer.appendChild(meisterChartJS.holder)
  }
}


external interface MeisterchartProps : Props {
  var description: String
  var gestalt: ChartGestalt
  var callMarkAsDirty: Boolean
  var width: String
  var height: String
}


private val logger: Logger = LoggerFactory.getLogger("com.meistercharts.react.MeisterchartsReact")
