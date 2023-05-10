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
import it.neckar.open.collections.fastForEach
import it.neckar.open.collections.fastForEachIndexed
import org.junit.jupiter.api.Test
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

class DirtyReasonTest {
  @Test
  fun `each value of DirtyReason should have a unique value`() {
    val valueProperties = DirtyReason.Companion::class.memberProperties.filter {
      it.returnType.classifier == DirtyReason::class
    }

    assertThat(valueProperties.size).isGreaterThan(5)

    val dirtyReasonValues = mutableSetOf<Int>()

    valueProperties.forEach { property: KProperty1<DirtyReason.Companion, *> ->
      val dirtyReason = property.getter.call(DirtyReason.Companion) as DirtyReason
      assertThat(dirtyReasonValues, "inserting ${property.name}").doesNotContain(dirtyReason.value)
      dirtyReasonValues.add(dirtyReason.value)
    }
  }

  @Test
  fun testEntries() {
    DirtyReason.entries.fastForEach {
      assertThat(it).isInstanceOf(DirtyReason::class)
      require(it is DirtyReason)
    }

    val valueProperties = DirtyReason.Companion::class.memberProperties.filter {
      it.returnType.classifier == DirtyReason::class
    }

    try {
      assertThat(DirtyReason.entries).hasSize(valueProperties.size)
      val expected = valueProperties.map { it.get(DirtyReason.Companion) }
      expected.fastForEachIndexed { index, value ->
        requireNotNull(value)
        require(value is DirtyReason)
        assertThat(value).isInstanceOf(DirtyReason::class)
        assertThat(DirtyReason.entries[index]).isEqualTo(value)
      }
    } catch (e: Throwable) {
      println(buildString {
        appendLine(
          "@Boxed"
        )
        appendLine(
          "val entries: List<DirtyReason> = listOf("
        )
        appendLine(valueProperties.joinToString(", ") { it.name })
        appendLine(
          ")"
        )
      })

      throw e
    }
  }

  @Test
  fun testLabels() {
    val valueProperties = DirtyReason.Companion::class.memberProperties.filter {
      it.returnType.classifier == DirtyReason::class
    }

    try {
      assertThat(DirtyReason.entryLabels).hasSize(valueProperties.size)

      val expected = valueProperties.map { it.get(DirtyReason.Companion) }

      expected.fastForEachIndexed { index, value ->
        requireNotNull(value)
        require(value is DirtyReason)
        assertThat(value).isInstanceOf(DirtyReason::class)
        val expectedName = valueProperties[index].name
        assertThat(DirtyReason.entryLabels[value]).isEqualTo(expectedName)
      }
    } catch (e: Throwable) {
      println(buildString {
        appendLine(
          "@Boxed"
        )
        appendLine(
          "val entryLabels: Map<DirtyReason, String> = mapOf("
        )
        appendLine(valueProperties.joinToString(",\n") { it.name + " to \"${it.name}\"" })
        appendLine(
          ")"
        )
      })

      throw e
    }
  }
}


