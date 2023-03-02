package com.meistercharts.algorithms.axis

import assertk.*
import assertk.assertions.*
import it.neckar.open.kotlin.lang.findMagnitude
import it.neckar.open.kotlin.lang.findMagnitudeCeil
import it.neckar.open.kotlin.lang.findMagnitudeValue
import it.neckar.open.kotlin.lang.findMagnitudeValueCeil
import org.junit.jupiter.api.Test
import kotlin.math.pow

/**
 *
 */
class CalculateFindMagnitudeTest {
  @Test
  fun testBug() {
    assertThat(IntermediateValuesMode.Also2.findSmaller(10_000.0) { true }).isEqualTo(2_000.0)
  }

  @Test
  fun testIt() {
    val value = 12.0

    val lowerTick = 10.0.pow(value.findMagnitude())
    val upperTick = 10.0.pow(value.findMagnitudeCeil())

    assertThat(lowerTick).isEqualTo(10.0)
    assertThat(upperTick).isEqualTo(100.0)
  }

  @Test
  fun testValue() {
    assertThat(12.0.findMagnitudeValue()).isEqualTo(10.0)
    assertThat(12.0.findMagnitudeValueCeil()).isEqualTo(100.0)
  }

  @Test
  fun testIntermediate() {
    val value = 12.0

    val lowerTick = 10.0.pow(value.findMagnitude())
    val upperTick = 10.0.pow(value.findMagnitudeCeil())

    assertThat(lowerTick).isEqualTo(10.0)
    assertThat(upperTick).isEqualTo(100.0)

    val intermediateValuesMode = IntermediateValuesMode.Also5and2

    val upperTick2 = intermediateValuesMode.findSmaller(upperTick) {
      it >= value
    }
    assertThat(upperTick2).isEqualTo(20.0)
  }

  @Test
  fun testQuery2findSmaller() {
    var count = 0

    IntermediateValuesMode.Also2.findSmaller(10.0) {
      when (count) {
        0 -> assertThat(it).isEqualTo(2.0)
        1 -> assertThat(it).isEqualTo(4.0)
        2 -> assertThat(it).isEqualTo(6.0)
        3 -> assertThat(it).isEqualTo(8.0)
        else -> fail("Why??? $it")
      }

      count++
      false
    }

    assertThat(count).isEqualTo(4)
  }

  @Test
  fun testQuery2findLarger() {
    var count = 0

    IntermediateValuesMode.Also2.findLarger(10.0) {
      when (count) {
        0 -> assertThat(it).isEqualTo(80.0)
        1 -> assertThat(it).isEqualTo(60.0)
        2 -> assertThat(it).isEqualTo(40.0)
        3 -> assertThat(it).isEqualTo(20.0)
        else -> fail("Why??? $it for count $count")
      }

      count++
      false
    }.also { assertThat(it).isEqualTo(10.0) }

    assertThat(count).isEqualTo(4)
  }

  @Test
  fun testQuery5findSmaller() {
    var count = 0

    IntermediateValuesMode.Also5.findSmaller(10.0) {
      when (count) {
        0 -> assertThat(it).isEqualTo(5.0)
        else -> fail("Why??? $it")
      }

      count++
      false
    }.also { assertThat(it).isEqualTo(10.0) }

    assertThat(count).isEqualTo(1)
  }

  @Test
  fun testQuery5findLarger() {
    var count = 0

    IntermediateValuesMode.Also5.findLarger(10.0) {
      when (count) {
        0 -> assertThat(it).isEqualTo(50.0)
        else -> fail("Why??? $it")
      }

      count++
      false
    }.also { assertThat(it).isEqualTo(10.0) }

    assertThat(count).isEqualTo(1)
  }

  @Test
  fun testQuery2and5findSmaller() {
    var count = 0

    IntermediateValuesMode.Also5and2.findSmaller(10.0) {
      when (count) {
        0 -> assertThat(it).isEqualTo(2.0)
        1 -> assertThat(it).isEqualTo(4.0)
        2 -> assertThat(it).isEqualTo(5.0)
        3 -> assertThat(it).isEqualTo(6.0)
        4 -> assertThat(it).isEqualTo(8.0)
        else -> fail("Why??? $it")
      }

      count++
      false
    }.also { assertThat(it).isEqualTo(10.0) }

    assertThat(count).isEqualTo(5)
  }

  @Test
  fun testQuery2and5findLarger() {
    var count = 0

    IntermediateValuesMode.Also5and2.findLarger(10.0) {
      when (count) {
        0 -> assertThat(it).isEqualTo(80.0)
        1 -> assertThat(it).isEqualTo(60.0)
        2 -> assertThat(it).isEqualTo(50.0)
        3 -> assertThat(it).isEqualTo(40.0)
        4 -> assertThat(it).isEqualTo(20.0)
        else -> fail("Why??? $it")
      }

      count++
      false
    }.also { assertThat(it).isEqualTo(10.0) }

    assertThat(count).isEqualTo(5)
  }
}
