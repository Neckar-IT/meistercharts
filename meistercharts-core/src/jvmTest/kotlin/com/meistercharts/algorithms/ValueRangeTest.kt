package com.meistercharts.algorithms

import assertk.*
import assertk.assertions.*
import com.meistercharts.annotations.Domain
import com.meistercharts.annotations.DomainRelative
import it.neckar.open.test.utils.isNaN
import org.junit.jupiter.api.Test

/**
 */
internal class ValueRangeTest {
  @Test
  fun testEmpty() {
    assertThat(ValueRange.linear(10.0, 10.0).delta).isEqualTo(0.0)
    assertThat(ValueRange.linear(10.0, 10.0).toDomainRelative(10.0)).isNaN()
    assertThat(ValueRange.linear(10.0, 10.0).toDomainRelative(11.0)).isEqualTo(Double.POSITIVE_INFINITY)
    assertThat(ValueRange.linear(10.0, 10.0).toDomainRelative(9.0)).isEqualTo(Double.NEGATIVE_INFINITY)
  }

  @Test
  fun testCoerce() {
    val valueRange = ValueRange.linear(0.0, 20.0)

    assertThat(11.0.coerceIn(valueRange)).isEqualTo(11.0)
    assertThat(0.0.coerceIn(valueRange)).isEqualTo(0.0)
    assertThat(20.0.coerceIn(valueRange)).isEqualTo(20.0)

    assertThat((-1.0).coerceIn(valueRange)).isEqualTo(0.0)
    assertThat(22.0.coerceIn(valueRange)).isEqualTo(20.0)
  }

  @Test
  fun testDeltaPos() {
    assertThat(ValueRange.linear(0.0, 20.0).deltaPositive).isEqualTo(20.0)
    assertThat(ValueRange.linear(0.0, 10.0).deltaPositive).isEqualTo(10.0)
    assertThat(ValueRange.linear(5.0, 10.0).deltaPositive).isEqualTo(5.0)

    assertThat(ValueRange.linear(-10.0, 10.0).deltaPositive).isEqualTo(10.0)
    assertThat(ValueRange.linear(-10.0, 70.0).deltaPositive).isEqualTo(70.0)

    assertThat(ValueRange.linear(-10.0, -5.0).deltaPositive).isEqualTo(0.0)
  }

  @Test
  fun testDeltaNeg() {
    assertThat(ValueRange.linear(0.0, 10.0).deltaNegative).isEqualTo(0.0)
    assertThat(ValueRange.linear(5.0, 10.0).deltaNegative).isEqualTo(0.0)

    assertThat(ValueRange.linear(-10.0, 10.0).deltaNegative).isEqualTo(10.0)
    assertThat(ValueRange.linear(-10.0, 70.0).deltaNegative).isEqualTo(10.0)

    assertThat(ValueRange.linear(-10.0, -5.0).deltaNegative).isEqualTo(5.0)
  }

  @Test
  internal fun testBaseLine() {
    assertThat(ValueRange.linear(0.0, 20.0).base()).isEqualTo(0.0)
    assertThat(ValueRange.linear(10.0, 20.0).base()).isEqualTo(10.0)

    assertThat(ValueRange.linear(-10.0, 20.0).base()).isEqualTo(0.0)

    assertThat(ValueRange.linear(-10.0, -5.0).base()).isEqualTo(-5.0)
  }

  @Test
  internal fun testDeltaCalc() {
    assertThat(ValueRange.linear(10.0, 20.0).deltaToDomainRelative(10.0)).isEqualTo(1.0)
    assertThat(ValueRange.linear(20.0, 30.0).deltaToDomainRelative(5.0)).isEqualTo(0.5)
    assertThat(ValueRange.linear(-20.0, -10.0).deltaToDomainRelative(5.0)).isEqualTo(0.5)
  }


  @Test
  internal fun testConversionWithAxis() {
    val valueRange = ValueRange.linear(10.0, 20.0)

    assertThat(valueRange.toDomain(0.0)).isEqualTo(10.0)
    assertThat(valueRange.toDomain(1.0)).isEqualTo(20.0)
  }

  @Test
  internal fun testDelta() {
    assertThat(ValueRange.linear(0.0, 10.0).delta).isEqualTo(10.0)
    assertThat(ValueRange.linear(1.0, 10.0).delta).isEqualTo(9.0)
    assertThat(ValueRange.linear(-7.0, 10.0).delta).isEqualTo(17.0)
  }

  @Test
  fun testBasicConversion() {
    val valueRange = ValueRange.linear(0.0, 10.0)

    assertThat(valueRange.start).isEqualTo(0.0)
    assertThat(valueRange.end).isEqualTo(10.0)
    assertThat(valueRange.delta).isEqualTo(10.0)

    assertRoundTrip(valueRange, 0.0, 0.0)
    assertRoundTrip(valueRange, 10.0, 1.0)
    assertRoundTrip(valueRange, 5.0, 0.5)
    assertRoundTrip(valueRange, 4.5, 0.45)
    assertRoundTrip(valueRange, 45.0, 4.5)
    assertRoundTrip(valueRange, -1.0, -0.1)
    assertRoundTrip(valueRange, 11.0, 1.1)
  }

  @Test
  fun testBasicConversionOffset() {
    val valueRange = ValueRange.linear(10.0, 20.0)

    assertThat(valueRange.start).isEqualTo(10.0)
    assertThat(valueRange.end).isEqualTo(20.0)
    assertThat(valueRange.delta).isEqualTo(10.0)

    assertRoundTrip(valueRange, 10.0, 0.0)
    assertRoundTrip(valueRange, 20.0, 1.0)
    assertRoundTrip(valueRange, 15.0, 0.5)
    assertRoundTrip(valueRange, 14.5, 0.45)
    assertRoundTrip(valueRange, 55.0, 4.5)
    assertRoundTrip(valueRange, 9.0, -0.1)
    assertRoundTrip(valueRange, 21.0, 1.1)
  }

  @Test
  fun testBasicConversion2() {
    val valueRange = ValueRange.linear(1.0, 11.0)

    assertThat(valueRange.start).isEqualTo(1.0)
    assertThat(valueRange.end).isEqualTo(11.0)
    assertThat(valueRange.delta).isEqualTo(10.0)

    assertRoundTrip(valueRange, 0.0, 0.0 - 0.1)
    assertRoundTrip(valueRange, 10.0, 1.0 - 0.1)
    assertRoundTrip(valueRange, 5.0, 0.5 - 0.1)
    assertRoundTrip(valueRange, 4.5, 0.45 - 0.1)
    assertRoundTrip(valueRange, 45.0, 4.5 - 0.1)
    assertRoundTrip(valueRange, -1.0, -0.1 - 0.1)
    assertRoundTrip(valueRange, 11.0, 1.1 - 0.1)
  }

  fun assertRoundTrip(valueRange: ValueRange, @Domain domainValue: Double, @DomainRelative expectedDomainRelative: Double) {
    val domainRelative = valueRange.toDomainRelative(domainValue)
    assertThat(domainRelative, "Relative domain value calculated from $domainValue for $valueRange").isEqualTo(expectedDomainRelative)

    val domainConvertedBack = valueRange.toDomain(domainRelative)
    assertThat(domainConvertedBack).isEqualTo(domainValue)
  }
}
