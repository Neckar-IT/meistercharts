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
package demo

import com.meistercharts.algorithms.painter.Color
import it.neckar.open.kotlin.lang.and

/**
 *
 */

// tag::styleExample[]
class MyChart(
  styleConfiguration: Style.() -> Unit = {} //<1>
) {
  val style: Style = Style().also(styleConfiguration) //<2>

  /**
   * The style for this chart
   */
  open class Style {
    var backgroundColor: Color = Color.white //<3>
    var foregroundColor: Color = Color.orange
  }
}
// end::styleExample[]

fun callWithStyleConfiguration() {
  // tag::callWithStyleConfiguration[]

  MyChart() {
    backgroundColor = Color.blue //<1>
  }
  // end::callWithStyleConfiguration[]
}

// tag::subStyleExample[]
class MyChartWithSubStyles(
  styleConfiguration: Style.() -> Unit = {} //<1>
) {
  val style: Style = Style().also(styleConfiguration) //<2>

  //somewhere the other components are instantiated

  init {
    MyChart(style.myChartConfiguration) //<4>
  }

  /**
   * The style for this chart
   */
  open class Style {
    var bg: Color = Color.white //<5>

    //<3>
    var myChartConfiguration: MyChart.Style.() -> Unit = {
      backgroundColor = bg //<5> Attention. Shadowing!
    }
  }
}
// end::subStyleExample[]

fun callWithSubStyleConfiguration() {
  // tag::callWithSubStyleConfiguration[]

  MyChartWithSubStyles() {
    bg = Color.blue //<1>

    // <2>
    myChartConfiguration = myChartConfiguration.and {
      this.foregroundColor = Color.burlywood //<3>
    }
  }
  // end::callWithSubStyleConfiguration[]
}


// tag::subStyleExampleRef[]
class MyChartWithReferencedSubStyles(
  styleConfiguration: Style.() -> Unit = {} //<1>
) {

  val myChart = MyChart() //<2>

  val style: Style = Style(myChart.style).also(styleConfiguration) //<3>

  /**
   * The style for this chart
   */
  open class Style(
    val myChart: MyChart.Style //<4>
  ) {
    var bg: Color = Color.white //<5>
  }
}
// end::subStyleExampleRef[]


fun callWithSubStyleRef() {
  // tag::callWithSubStyleRef[]

  MyChartWithReferencedSubStyles() {
    bg = Color.blue //<1>

    myChart.backgroundColor = Color.burlywood // <2>
    myChart.foregroundColor = Color.beige // <3>
  }
  // end::callWithSubStyleRef[]
}
