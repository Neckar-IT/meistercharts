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
package com.meistercharts.canvas

import assertk.*
import assertk.assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail

/**
 */
class DirtySupportTest {
  @Test
  fun testReason() {
    val dirtySupport = DirtySupport()
    assertThat(dirtySupport.dirtyReasonsBits.value).isEqualTo(0)

    dirtySupport.markAsDirty(DirtyReason.Unknown)
    assertThat(dirtySupport.dirty).isTrue()
    assertThat(dirtySupport.isDirtyBecause(DirtyReason.Unknown)).isTrue()
    assertThat(dirtySupport.isDirtyBecause(DirtyReason.UserInteraction)).isFalse()

    dirtySupport.markAsDirty(DirtyReason.UserInteraction)
    assertThat(dirtySupport.dirty).isTrue()
    assertThat(dirtySupport.isDirtyBecause(DirtyReason.Unknown)).isTrue()
    assertThat(dirtySupport.isDirtyBecause(DirtyReason.UserInteraction)).isTrue()

    dirtySupport.clearIsDirty()
    assertThat(dirtySupport.dirty).isFalse()
    assertThat(dirtySupport.isDirtyBecause(DirtyReason.Unknown)).isFalse()
    assertThat(dirtySupport.isDirtyBecause(DirtyReason.UserInteraction)).isFalse()
  }

  @Test
  fun testLogger() {
    val dirtySupport = DirtySupport()

    dirtySupport.ifDirty {
      fail("Must not be called")
    }

    dirtySupport.markAsDirty(DirtyReason.Unknown)

    var called = false
    dirtySupport.ifDirty {
      called = true
    }

    assertThat(called).isTrue()
  }
}
