package com.meistercharts.algorithms

import assertk.*
import assertk.assertions.*
import it.neckar.open.collections.fastForEach
import it.neckar.open.collections.fastForEachIndexed
import org.junit.jupiter.api.Test
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

class UpdateReasonTest {
  @Test
  fun `each value of UpdateReason should have a unique value`() {
    val valueProperties = UpdateReason.Companion::class.memberProperties.filter {
      it.returnType.classifier == UpdateReason::class
    }

    assertThat(valueProperties.size).isGreaterThan(5)

    val updateReasonValues = mutableSetOf<Int>()

    valueProperties.forEach { property: KProperty1<UpdateReason.Companion, *> ->
      val updateReason = property.getter.call(UpdateReason.Companion) as UpdateReason
      assertThat(updateReasonValues, "inserting ${property.name}").doesNotContain(updateReason.value)
      updateReasonValues.add(updateReason.value)
    }
  }

  @Test
  fun testEntries() {
    UpdateReason.entries.fastForEach {
      assertThat(it).isInstanceOf(UpdateReason::class)
      require(it is UpdateReason)
    }

    val valueProperties = UpdateReason.Companion::class.memberProperties.filter {
      it.returnType.classifier == UpdateReason::class
    }

    try {
      assertThat(UpdateReason.entries).hasSize(valueProperties.size)
      val expected = valueProperties.map { it.get(UpdateReason.Companion) }
      expected.fastForEachIndexed { index, value ->
        requireNotNull(value)
        require(value is UpdateReason)
        assertThat(value).isInstanceOf(UpdateReason::class)
        assertThat(UpdateReason.entries[index]).isEqualTo(value)
      }
    } catch (e: Throwable) {
      println(buildString {
        appendLine(
          "@Boxed"
        )
        appendLine(
          "val entries: List<UpdateReason> = listOf("
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
    val valueProperties = UpdateReason.Companion::class.memberProperties.filter {
      it.returnType.classifier == UpdateReason::class
    }

    try {
      assertThat(UpdateReason.entryLabels).hasSize(valueProperties.size)

      val expected = valueProperties.map { it.get(UpdateReason.Companion) }

      expected.fastForEachIndexed { index, value ->
        requireNotNull(value)
        require(value is UpdateReason)
        assertThat(value).isInstanceOf(UpdateReason::class)
        val expectedName = valueProperties[index].name
        assertThat(UpdateReason.entryLabels[value]).isEqualTo(expectedName)
      }
    } catch (e: Throwable) {
      println(buildString {
        appendLine(
          "@Boxed"
        )
        appendLine(
          "val entryLabels: Map<UpdateReason, String> = mapOf("
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
