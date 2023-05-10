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
package com.meistercharts.fx.debug

import assertk.*
import it.neckar.open.javafx.test.JavaFxTest
import org.junit.jupiter.api.Test
import java.lang.ref.WeakReference

@JavaFxTest
class MemoryLeakVerifierTest {
  @Test
  fun testCleanup() {
    MemoryLeakVerifier(getRef()).assertGarbageCollected()
  }

  @Test
  fun testNotKleanup() {
    val myDataClass = MyDataClass("asdf")
    try {
      MemoryLeakVerifier(WeakReference(myDataClass)).assertGarbageCollected()
      fail("Where is the exception?")
    } catch (e: Exception) {
    }
  }


  private fun getRef() = WeakReference(MyDataClass("asdf"))

  data class MyDataClass(val name: String)
}
