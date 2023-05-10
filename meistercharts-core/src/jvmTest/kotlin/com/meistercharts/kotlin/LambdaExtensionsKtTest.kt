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
package com.cedarsoft.kotlin

import assertk.*
import assertk.assertions.*
import it.neckar.open.kotlin.lang.asProvider
import it.neckar.open.kotlin.lang.asProvider1
import org.junit.jupiter.api.Test

class LambdaExtensionsKtTest {
  var p0: () -> String = { "myString" }
  var p1: (Double) -> String = { "myString" }
  var p2: (Double, Int) -> String = { _, _ -> "myString" }

  @Test
  fun testIt() {
    p0 = "asdf".asProvider()
    p1 = "asdf".asProvider1()

    assertThat(p0()).isEqualTo("asdf")
    assertThat(p1(4.0)).isEqualTo("asdf")
  }

  @Test
  fun testAsLambda() {
    p0 = "asdf".asProvider()
    p1 = "asdf".asProvider1()

    assertThat(p0()).isEqualTo("asdf")
    assertThat(p1(4.0)).isEqualTo("asdf")
  }
}
