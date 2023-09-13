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
package com.meistercharts.canvas.layout.cache.com.meistercharts.config

import assertk.*
import assertk.assertions.*
import com.meistercharts.config.ChartConfigParsingSupport
import it.neckar.commons.kotlin.js.render
import kotlinx.browser.document
import kotlinx.html.dom.append
import kotlinx.html.js.script
import kotlinx.html.unsafe
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.test.Test

class ParseJsonScripTagTest {
  @Test
  fun testIt() {
    val body = requireNotNull(document.body)

    val toSerialize = MyJsonObject("asdf", 1234)
    val json = Json { prettyPrint = true }

    val id = "meistercharts-json-data"


    body.append {
      script {
        type = "application/json"
        attributes["id"] = id
        unsafe { +json.encodeToString(MyJsonObject.serializer(), toSerialize) }
      }
    }

    assertThat(document.render()).contains("asdf")


    if (false) {
      println("--------------")
      println(document.render())
      println("--------------")
    }

    val parsed = ChartConfigParsingSupport().parseChartsConfig<MyJsonObject>(id)

    assertThat(parsed).isEqualTo(toSerialize)
  }


  @Serializable
  data class MyJsonObject(
    val foo: String,
    val fooBar: Int,
  )
}
