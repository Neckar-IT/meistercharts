package com.meistercharts.algorithms.layers.axis.time

import assertk.*
import assertk.assertions.*
import com.meistercharts.axis.time.TimeAxisTickCalculator
import com.meistercharts.time.klockGreatestSupportedTimestamp
import com.meistercharts.time.klockSmallestSupportedTimestamp
import it.neckar.datetime.minimal.TimeConstants
import it.neckar.datetime.minimal.TimeZone
import it.neckar.open.collections.last
import it.neckar.open.formatting.formatUtc
import it.neckar.open.test.utils.WithTimeZone
import it.neckar.open.unit.si.ms
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

/**
 *
 *
 *
 */
@Disabled
@WithTimeZone("UTC")
class TimeAxisTickCalculatorTest {
  @Test
  fun greatestSupportedTimestamp() {
    val localDateTime = LocalDateTime.of(9999, 12, 31, 23, 59, 59, 999_999_999)
    val millis = localDateTime.toEpochSecond(ZoneOffset.UTC) * 1000.0 + 999;
    assertThat(klockGreatestSupportedTimestamp).isEqualTo(millis)
  }

  @Test
  fun smallestSupportedTimestamp() {
    val localDateTime = LocalDateTime.of(1, 1, 1, 0, 0, 0, 0)
    val millis = localDateTime.toEpochSecond(ZoneOffset.UTC) * 1000.0;
    assertThat(klockSmallestSupportedTimestamp).isEqualTo(millis)
  }

  @Test
  fun testTickDistance1Day() {
    //https://www.epochconverter.com/
    val start = 999301094000.0 //Friday, 31. August 2001 23:38:14
    val end = 1000962739000.0 //Thursday, 20. September 2001 05:12:19

    //we expect a tick every day
    TimeAxisTickCalculator.calculateTickValues(start, end, 1.days.toDouble(DurationUnit.MILLISECONDS), TimeZone.UTC).let {
      assertThat(it[0].formatUtc()).isEqualTo("2001-09-01T00:00:00.000Z")
      assertThat(it[1].formatUtc()).isEqualTo("2001-09-02T00:00:00.000Z")
      assertThat(it[2].formatUtc()).isEqualTo("2001-09-03T00:00:00.000Z")
      assertThat(it[3].formatUtc()).isEqualTo("2001-09-04T00:00:00.000Z")

      assertThat(it.last().formatUtc()).isEqualTo("2001-09-20T00:00:00.000Z")
    }
  }

  @Test
  fun testTickDistance5Days() {
    //> 1 day -> we expect a tick every month on the 1., 5. 10., 15., 20., 25.
    val minTickDistanceGreaterThan1Day = 1.days.toDouble(DurationUnit.MILLISECONDS) + 1.0

    TimeAxisTickCalculator.calculateTickValues(
      999301094000.0, //Friday, 31. August 2001 23:38:14
      1008614294000.0, //Monday, 17. December 2001 18:38:14
      minTickDistanceGreaterThan1Day, TimeZone.UTC
    ).let {
      assertThat(it[0].formatUtc()).isEqualTo("2001-09-01T00:00:00.000Z")
      assertThat(it[1].formatUtc()).isEqualTo("2001-09-05T00:00:00.000Z")
      assertThat(it[2].formatUtc()).isEqualTo("2001-09-10T00:00:00.000Z")
      assertThat(it[3].formatUtc()).isEqualTo("2001-09-15T00:00:00.000Z")
      assertThat(it[4].formatUtc()).isEqualTo("2001-09-20T00:00:00.000Z")
      assertThat(it[5].formatUtc()).isEqualTo("2001-09-25T00:00:00.000Z")
      assertThat(it[6].formatUtc()).isEqualTo("2001-10-01T00:00:00.000Z")
      assertThat(it[7].formatUtc()).isEqualTo("2001-10-05T00:00:00.000Z")

      assertThat(it.last().formatUtc()).isEqualTo("2001-12-15T00:00:00.000Z")
    }

    TimeAxisTickCalculator.calculateTickValues(
      999302937000.0, //Saturday, 1. September 2001 00:08:57
      1008614294000.0, //Monday, 17. December 2001 18:38:14
      minTickDistanceGreaterThan1Day, TimeZone.UTC
    ).let {
      assertThat(it[0].formatUtc()).isEqualTo("2001-09-05T00:00:00.000Z")
      assertThat(it[1].formatUtc()).isEqualTo("2001-09-10T00:00:00.000Z")
      assertThat(it[2].formatUtc()).isEqualTo("2001-09-15T00:00:00.000Z")
      assertThat(it[3].formatUtc()).isEqualTo("2001-09-20T00:00:00.000Z")
      assertThat(it[4].formatUtc()).isEqualTo("2001-09-25T00:00:00.000Z")
      assertThat(it[5].formatUtc()).isEqualTo("2001-10-01T00:00:00.000Z")

      assertThat(it.last().formatUtc()).isEqualTo("2001-12-15T00:00:00.000Z")
    }
  }

  @Test
  fun testTickDistance10Days() {
    //> 5 days -> we expect a tick every month on the 1., 10., 20.
    val minTickDistanceGreaterThan5Days = 5.days.toDouble(DurationUnit.MILLISECONDS) + 1.0

    TimeAxisTickCalculator.calculateTickValues(
      999301094000.0, //Friday, 31. August 2001 23:38:14
      1008614294000.0, //Monday, 17. December 2001 18:38:14
      minTickDistanceGreaterThan5Days, TimeZone.UTC
    ).let {
      assertThat(it[0].formatUtc()).isEqualTo("2001-09-01T00:00:00.000Z")
      assertThat(it[1].formatUtc()).isEqualTo("2001-09-10T00:00:00.000Z")
      assertThat(it[2].formatUtc()).isEqualTo("2001-09-20T00:00:00.000Z")
      assertThat(it[3].formatUtc()).isEqualTo("2001-10-01T00:00:00.000Z")
      assertThat(it[4].formatUtc()).isEqualTo("2001-10-10T00:00:00.000Z")

      assertThat(it.last().formatUtc()).isEqualTo("2001-12-10T00:00:00.000Z")
    }

    TimeAxisTickCalculator.calculateTickValues(
      999302937000.0, //Saturday, 1. September 2001 00:08:57
      1008614294000.0, //Monday, 17. December 2001 18:38:14
      minTickDistanceGreaterThan5Days, TimeZone.UTC
    ).let {
      assertThat(it[0].formatUtc()).isEqualTo("2001-09-10T00:00:00.000Z")
      assertThat(it[1].formatUtc()).isEqualTo("2001-09-20T00:00:00.000Z")
      assertThat(it[2].formatUtc()).isEqualTo("2001-10-01T00:00:00.000Z")
      assertThat(it[3].formatUtc()).isEqualTo("2001-10-10T00:00:00.000Z")

      assertThat(it.last().formatUtc()).isEqualTo("2001-12-10T00:00:00.000Z")
    }
  }

  @Test
  fun testTickDistance15Days() {
    //> 10 days -> we expect a tick every month on the 1. and 15.
    val minTickDistanceGreaterThan10Days = 10.days.toDouble(DurationUnit.MILLISECONDS) + 1.0

    TimeAxisTickCalculator.calculateTickValues(
      999301094000.0, //Friday, 31. August 2001 23:38:14
      1008614294000.0, //Monday, 17. December 2001 18:38:14
      minTickDistanceGreaterThan10Days, TimeZone.UTC
    ).let {
      assertThat(it[0].formatUtc()).isEqualTo("2001-09-01T00:00:00.000Z")
      assertThat(it[1].formatUtc()).isEqualTo("2001-09-15T00:00:00.000Z")
      assertThat(it[2].formatUtc()).isEqualTo("2001-10-01T00:00:00.000Z")
      assertThat(it[3].formatUtc()).isEqualTo("2001-10-15T00:00:00.000Z")
      assertThat(it[4].formatUtc()).isEqualTo("2001-11-01T00:00:00.000Z")

      assertThat(it.last().formatUtc()).isEqualTo("2001-12-15T00:00:00.000Z")
    }

    TimeAxisTickCalculator.calculateTickValues(
      999302937000.0, //Saturday, 1. September 2001 00:08:57
      1008614294000.0, //Monday, 17. December 2001 18:38:14
      minTickDistanceGreaterThan10Days, TimeZone.UTC
    ).let {
      assertThat(it[0].formatUtc()).isEqualTo("2001-09-15T00:00:00.000Z")
      assertThat(it[1].formatUtc()).isEqualTo("2001-10-01T00:00:00.000Z")
      assertThat(it[2].formatUtc()).isEqualTo("2001-10-15T00:00:00.000Z")
      assertThat(it[3].formatUtc()).isEqualTo("2001-11-01T00:00:00.000Z")

      assertThat(it.last().formatUtc()).isEqualTo("2001-12-15T00:00:00.000Z")
    }
  }

  @Test
  fun testTickDistance15DaysSwitchYear() {
    //> 10 days -> we expect a tick every month on the 1. and 15.
    val minTickDistanceGreaterThan10Days = 10.days.toDouble(DurationUnit.MILLISECONDS) + 1.0

    TimeAxisTickCalculator.calculateTickValues(
      1006297737000.0, //Tuesday, 20. November 2001 23:08:57
      1030446313000.0, //Tuesday, 27. August 2002 11:05:13
      minTickDistanceGreaterThan10Days, TimeZone.UTC
    ).let {
      assertThat(it[0].formatUtc()).isEqualTo("2001-12-01T00:00:00.000Z")
      assertThat(it[1].formatUtc()).isEqualTo("2001-12-15T00:00:00.000Z")
      assertThat(it[2].formatUtc()).isEqualTo("2002-01-01T00:00:00.000Z")
      assertThat(it[3].formatUtc()).isEqualTo("2002-01-15T00:00:00.000Z")
      assertThat(it[4].formatUtc()).isEqualTo("2002-02-01T00:00:00.000Z")
      assertThat(it[5].formatUtc()).isEqualTo("2002-02-15T00:00:00.000Z")

      assertThat(it.last().formatUtc()).isEqualTo("2002-08-15T00:00:00.000Z")
    }
  }

  @Test
  fun testCalculateTickValuesGreaterThan3YearsMinDistance() {
    //https://www.epochconverter.com/
    //> 3 Years -> we expect a tick on the 1st of january every 5 years
    @ms val tickDistanceGreaterThan3Years = 365.days.toDouble(DurationUnit.MILLISECONDS) * 3.0 + 1.0
    TimeAxisTickCalculator.calculateTickValues(
      -1012395383000.0, //Thursday, 2. December 1937 11:03:37
      1652829239000.0, //Tuesday, 17. May 2022 23:13:59
      tickDistanceGreaterThan3Years, TimeZone.UTC
    ).let {
      assertThat(it[0].formatUtc()).isEqualTo("1940-01-01T00:00:00.000Z")
      assertThat(it[1].formatUtc()).isEqualTo("1945-01-01T00:00:00.000Z")
      assertThat(it[2].formatUtc()).isEqualTo("1950-01-01T00:00:00.000Z")
      assertThat(it[3].formatUtc()).isEqualTo("1955-01-01T00:00:00.000Z")

      assertThat(it.last().formatUtc()).isEqualTo("2020-01-01T00:00:00.000Z")
    }
  }

  @Test
  fun testCalculateTickValuesGreaterThan168DaysMinDistance() {
    //https://www.epochconverter.com/
    //> 168 days (= 6 * 28) -> we expect a tick every year
    @ms val tickDistanceGreaterThan168Days = 168.days.toDouble(DurationUnit.MILLISECONDS) + 1.0
    TimeAxisTickCalculator.calculateTickValues(
      1023107291000.0, //Monday, 3. June 2002 12:28:11
      1652829239000.0, //Tuesday, 17. May 2022 23:13:59
      tickDistanceGreaterThan168Days, TimeZone.UTC
    ).let {
      assertThat(it[0]).isEqualTo(1041379200000.0) //Wednesday, 1. January 2003 00:00:00
      assertThat(it[1]).isEqualTo(1072915200000.0) //Thursday, 1. January 2004 00:00:00
      assertThat(it[2]).isEqualTo(1104537600000.0) //Saturday, 1. January 2005 00:00:00
      assertThat(it[3]).isEqualTo(1136073600000.0) //Sunday, 1. January 2006 00:00:00
      assertThat(it[4]).isEqualTo(1167609600000.0) //Monday, 1. January 2007 00:00:00

      assertThat(it.last()).isEqualTo(1640995200000.0) //Saturday, 1. January 2022 00:00:00
    }
  }

  @Test
  fun testCalculateTickValuesGreaterThan84DaysMinDistance() {
    //https://www.epochconverter.com/
    //> 84 days (3 * 28) -> we expect a tick every half year
    @ms val tickDistanceGreaterThan84Days = 84.days.toDouble(DurationUnit.MILLISECONDS) + 1.0
    val start = 1023107291000.0
    assertThat(start.formatUtc()).isEqualTo("2002-06-03T12:28:11.000Z")
    val end = 1652829239000.0
    assertThat(end.formatUtc()).isEqualTo("2022-05-17T23:13:59.000Z")

    TimeAxisTickCalculator.calculateTickValues(
      start, //Monday, 3. June 2002 12:28:11
      end, //Tuesday, 17. May 2022 23:13:59
      tickDistanceGreaterThan84Days, TimeZone.UTC
    ).let {
      assertThat(it.size).isEqualTo(41)

      assertThat(it[0].formatUtc()).isEqualTo("2002-01-01T00:00:00.000Z")
      assertThat(it[1].formatUtc()).isEqualTo("2002-07-01T00:00:00.000Z")
      assertThat(it[2].formatUtc()).isEqualTo("2003-01-01T00:00:00.000Z")
      assertThat(it[3].formatUtc()).isEqualTo("2003-07-01T00:00:00.000Z")
      assertThat(it[4].formatUtc()).isEqualTo("2004-01-01T00:00:00.000Z")
      assertThat(it[5].formatUtc()).isEqualTo("2004-07-01T00:00:00.000Z")

      assertThat(it.last().formatUtc()).isEqualTo("2022-01-01T00:00:00.000Z")
    }
  }

  @Test
  fun testCalculateTickValuesGreaterThan56DaysMinDistance() {
    //https://www.epochconverter.com/
    //> 56 days (2 * 28) -> we expect a tick every third month
    @ms val tickDistanceGreaterThan56Days = 56.days.toDouble(DurationUnit.MILLISECONDS) + 1.0
    TimeAxisTickCalculator.calculateTickValues(
      1038935515000.0, //Tuesday, 3. December 2002 17:11:55
      1652829239000.0, //Tuesday, 17. May 2022 23:13:59
      tickDistanceGreaterThan56Days, TimeZone.UTC
    ).let {
      assertThat(it[0]).isEqualTo(1041379200000.0) //Wednesday, 1. January 2003 00:00:00
      assertThat(it[1]).isEqualTo(1049155200000.0) //Tuesday, 1. April 2003 00:00:00
      assertThat(it[2]).isEqualTo(1057017600000.0) //Tuesday, 1. July 2003 00:00:00
      assertThat(it[3]).isEqualTo(1064966400000.0) //Wednesday, 1. October 2003 00:00:00

      assertThat(it[4]).isEqualTo(1072915200000.0) //Thursday, 1. January 2004 00:00:00
      assertThat(it[5]).isEqualTo(1080777600000.0) //Thursday, 1. April 2004 00:00:00
      assertThat(it[6]).isEqualTo(1088640000000.0) //Thursday, 1. July 2004 00:00:00
      assertThat(it[7]).isEqualTo(1096588800000.0) //Friday, 1. October 2004 00:00:00

      assertThat(it.last()).isEqualTo(1648771200000.0) //Friday, 1. April 2022 00:00:00
    }
  }

  @Test
  fun testCalculateTickValuesGreaterThan28DaysMinDistance() {
    //https://www.epochconverter.com/
    //we expect a tick every other month
    @ms val tickDistanceGreaterThan28Days = 28.days.toDouble(DurationUnit.MILLISECONDS) + 1.0
    TimeAxisTickCalculator.calculateTickValues(
      1038935515000.0, //Tuesday, 3. December 2002 17:11:55
      1652829239000.0, //Tuesday, 17. May 2022 23:13:59
      tickDistanceGreaterThan28Days, TimeZone.UTC
    ).let {
      assertThat(it[0].formatUtc()).isEqualTo("2003-01-01T00:00:00.000Z")
      assertThat(it[1].formatUtc()).isEqualTo("2003-03-01T00:00:00.000Z")
      assertThat(it[2].formatUtc()).isEqualTo("2003-05-01T00:00:00.000Z")
      assertThat(it[3].formatUtc()).isEqualTo("2003-07-01T00:00:00.000Z")
      assertThat(it[4].formatUtc()).isEqualTo("2003-09-01T00:00:00.000Z")

      assertThat(it.last().formatUtc()).isEqualTo("2022-05-01T00:00:00.000Z")
    }
  }

  @Test
  fun testCalculateTickValuesGreaterThan15DaysMinDistance() {
    //https://www.epochconverter.com/
    //we expect a tick every month
    @ms val tickDistanceGreaterThan15Days = 15.days.toDouble(DurationUnit.MILLISECONDS) + 1.0
    TimeAxisTickCalculator.calculateTickValues(
      1038935515000.0, //Tuesday, 3. December 2002 17:11:55
      1652829239000.0, //Tuesday, 17. May 2022 23:13:59
      tickDistanceGreaterThan15Days, TimeZone.UTC
    ).let {
      assertThat(it[0].formatUtc()).isEqualTo("2003-01-01T00:00:00.000Z")
      assertThat(it[1].formatUtc()).isEqualTo("2003-02-01T00:00:00.000Z")
      assertThat(it[2].formatUtc()).isEqualTo("2003-03-01T00:00:00.000Z")
      assertThat(it[3].formatUtc()).isEqualTo("2003-04-01T00:00:00.000Z")
      assertThat(it[4].formatUtc()).isEqualTo("2003-05-01T00:00:00.000Z")
      assertThat(it[5].formatUtc()).isEqualTo("2003-06-01T00:00:00.000Z")

      assertThat(it.last().formatUtc()).isEqualTo("2022-05-01T00:00:00.000Z")
    }
  }

  @Test
  @Disabled
  fun testCalculateTickValuesGreaterThan24HoursMinDistance() {
    //https://www.epochconverter.com/
    //we expect a tick on the first and 15th of every month
    @ms val tickDistanceGreaterThan24Hours = 24.hours.inWholeMilliseconds + 1.0
    TimeAxisTickCalculator.calculateTickValues(
      1038935515000.0, //Tuesday, 3. December 2002 17:11:55
      1652829239000.0, //Tuesday, 17. May 2022 23:13:59
      tickDistanceGreaterThan24Hours, TimeZone.UTC
    ).let {
      assertThat(it[0]).isEqualTo(1039910400000.0) //Sunday, 15. December 2002 00:00:00

      assertThat(it[1]).isEqualTo(1041379200000.0) //Wednesday, 1. January 2003 00:00:00
      assertThat(it[2]).isEqualTo(1042588800000.0) //Wednesday, 15. January 2003 00:00:00

      assertThat(it[3]).isEqualTo(1044057600000.0) //Saturday, 1. February 2003 00:00:00
      assertThat(it[4]).isEqualTo(1045267200000.0) //Saturday, 15. February 2003 00:00:00

      assertThat(it[5]).isEqualTo(1046476800000.0) //Saturday, 1. March 2003 00:00:00
      assertThat(it[6]).isEqualTo(1047686400000.0) //Saturday, 15. March 2003 00:00:00

      assertThat(it.last()).isEqualTo(1652572800000.0) //Sunday, 15. May 2022 00:00:00
    }
  }

  @Test
  fun testCalculateTickValuesGreaterThan12HoursMinDistance() {
    //https://www.epochconverter.com/
    //we expect a tick every day
    @ms val tickDistanceGreaterThan12Hours = 12.hours.inWholeMilliseconds + 1.0
    TimeAxisTickCalculator.calculateTickValues(
      1040750006000.0, //Tuesday, 24. December 2002 17:13:26
      1652829239000.0, //Tuesday, 17. May 2022 23:13:59
      tickDistanceGreaterThan12Hours, TimeZone.UTC
    ).let {
      assertThat(it[0].formatUtc()).isEqualTo("2002-12-25T00:00:00.000Z")
      assertThat(it[1].formatUtc()).isEqualTo("2002-12-26T00:00:00.000Z")
      assertThat(it[2].formatUtc()).isEqualTo("2002-12-27T00:00:00.000Z")
      assertThat(it[3].formatUtc()).isEqualTo("2002-12-28T00:00:00.000Z")

      assertThat(it.last().formatUtc()).isEqualTo("2022-05-17T00:00:00.000Z")
    }
  }

  @Test
  fun testCalculateTickValuesGreaterThan6HoursMinDistance() {
    //https://www.epochconverter.com/
    //we expect a tick every 12 hours
    @ms val tickDistanceGreaterThan6Hours = 6.hours.inWholeMilliseconds + 1.0
    TimeAxisTickCalculator.calculateTickValues(
      1038935515000.0, //Tuesday, 3. December 2002 17:11:55
      1652829239000.0, //Tuesday, 17. May 2022 23:13:59
      tickDistanceGreaterThan6Hours, TimeZone.UTC
    ).let {
      assertThat(it[0]).isEqualTo(1038960000000.0) //Wednesday, 4. December 2002 00:00:00
      assertThat(it[1]).isEqualTo(1039003200000.0) //Wednesday, 4. December 2002 12:00:00

      assertThat(it[2]).isEqualTo(1039046400000.0) //Thursday, 5. December 2002 00:00:00
      assertThat(it[3]).isEqualTo(1039089600000.0) //Thursday, 5. December 2002 12:00:00

      assertThat(it[4]).isEqualTo(1039132800000.0) //Friday, 6. December 2002 00:00:00
      assertThat(it[5]).isEqualTo(1039176000000.0) //Friday, 6. December 2002 12:00:00

      assertThat(it.last()).isEqualTo(1652788800000.0) //Tuesday, 17. May 2022 12:00:00
    }
  }

  @Test
  fun testCalculateTickValuesGreaterThan4HoursMinDistance() {
    //https://www.epochconverter.com/
    //we expect a tick every 6 hours
    @ms val tickDistanceGreaterThan4Hours = 4.hours.inWholeMilliseconds + 1.0
    TimeAxisTickCalculator.calculateTickValues(
      1038935515000.0, //Tuesday, 3. December 2002 17:11:55
      1652829239000.0, //Tuesday, 17. May 2022 23:13:59
      tickDistanceGreaterThan4Hours, TimeZone.UTC
    ).let {
      assertThat(it[0]).isEqualTo(1038938400000.0) //Tuesday, 3. December 2002 18:00:00
      assertThat(it[1]).isEqualTo(1038960000000.0) //Wednesday, 4. December 2002 00:00:00
      assertThat(it[2]).isEqualTo(1038981600000.0) //Wednesday, 4. December 2002 06:00:00
      assertThat(it[3]).isEqualTo(1039003200000.0) //Wednesday, 4. December 2002 12:00:00
      assertThat(it[4]).isEqualTo(1039024800000.0) //Wednesday, 4. December 2002 18:00:00
      assertThat(it[5]).isEqualTo(1039046400000.0) //Thursday, 5. December 2002 00:00:00

      assertThat(it.last()).isEqualTo(1652810400000.0) //Tuesday, 17. May 2022 18:00:00
    }
  }

  @Test
  fun testCalculateTickValuesGreaterThan3HoursMinDistance() {
    //https://www.epochconverter.com/
    //> 3 hours -> we expect a tick every 4 hours
    @ms val tickDistanceGreaterThan3Hours = 3.hours.inWholeMilliseconds + 1.0
    TimeAxisTickCalculator.calculateTickValues(
      1038935515000.0, //Tuesday, 3. December 2002 17:11:55
      1652829239000.0, //Tuesday, 17. May 2022 23:13:59
      tickDistanceGreaterThan3Hours, TimeZone.UTC
    ).let {
      assertThat(it[0]).isEqualTo(1038945600000.0) //Tuesday, 3. December 2002 20:00:00
      assertThat(it[1]).isEqualTo(1038960000000.0) //Wednesday, 4. December 2002 00:00:00
      assertThat(it[2]).isEqualTo(1038974400000.0) //Wednesday, 4. December 2002 04:00:00
      assertThat(it[3]).isEqualTo(1038988800000.0) //Wednesday, 4. December 2002 08:00:00
      assertThat(it[4]).isEqualTo(1039003200000.0) //Wednesday, 4. December 2002 12:00:00
      assertThat(it[5]).isEqualTo(1039017600000.0) //Wednesday, 4. December 2002 16:00:00

      assertThat(it.last()).isEqualTo(1652817600000.0) //Tuesday, 17. May 2022 20:00:00
    }
  }

  @Test
  fun testCalculateTickValuesGreaterThan2HoursMinDistance() {
    //https://www.epochconverter.com/
    //> 2 hours -> we expect a tick every 3 hours
    @ms val tickDistanceGreaterThan2Hours = 2.hours.inWholeMilliseconds + 1.0
    TimeAxisTickCalculator.calculateTickValues(
      1038935515000.0, //Tuesday, 3. December 2002 17:11:55
      1040112299000.0, //Tuesday, 17. December 2002 08:04:59
      tickDistanceGreaterThan2Hours, TimeZone.UTC
    ).let {
      assertThat(it[0].formatUtc()).isEqualTo("2002-12-03T18:00:00.000Z")
      assertThat(it[1].formatUtc()).isEqualTo("2002-12-03T21:00:00.000Z")
      assertThat(it[2].formatUtc()).isEqualTo("2002-12-04T00:00:00.000Z")
      assertThat(it[3].formatUtc()).isEqualTo("2002-12-04T03:00:00.000Z")
      assertThat(it[4].formatUtc()).isEqualTo("2002-12-04T06:00:00.000Z")

      assertThat(it.last().formatUtc()).isEqualTo("2002-12-17T06:00:00.000Z")
    }
  }

  @Test
  fun testCalculateTickValuesGreaterThan1HourMinDistance() {
    //https://www.epochconverter.com/
    //> 1 hour -> we expect a tick every 2 hours
    @ms val tickDistanceGreaterThan1Hour = 1.hours.inWholeMilliseconds + 1.0
    TimeAxisTickCalculator.calculateTickValues(
      1038935515000.0, //Tuesday, 3. December 2002 17:11:55
      1040112299000.0, //Tuesday, 17. December 2002 08:04:59
      tickDistanceGreaterThan1Hour, TimeZone.UTC
    ).let {
      assertThat(it[0].formatUtc()).isEqualTo("2002-12-03T18:00:00.000Z")
      assertThat(it[1].formatUtc()).isEqualTo("2002-12-03T20:00:00.000Z")
      assertThat(it[2].formatUtc()).isEqualTo("2002-12-03T22:00:00.000Z")
      assertThat(it[3].formatUtc()).isEqualTo("2002-12-04T00:00:00.000Z")
      assertThat(it[4].formatUtc()).isEqualTo("2002-12-04T02:00:00.000Z")
      assertThat(it[5].formatUtc()).isEqualTo("2002-12-04T04:00:00.000Z")
      assertThat(it[6].formatUtc()).isEqualTo("2002-12-04T06:00:00.000Z")

      assertThat(it.last().formatUtc()).isEqualTo("2002-12-17T08:00:00.000Z")
    }
  }

  @Test
  fun testCalculateTickValuesGreaterThan30MinutesMinDistance() {
    //https://www.epochconverter.com/
    //> 30 minutes -> we expect a tick every hour
    @ms val tickDistanceGreaterThan30Minutes = 30.minutes.toDouble(DurationUnit.MILLISECONDS) + 1.0
    TimeAxisTickCalculator.calculateTickValues(
      1038935515000.0, //Tuesday, 3. December 2002 17:11:55
      1039334699000.0, //Sunday, 8. December 2002 08:04:59
      tickDistanceGreaterThan30Minutes, TimeZone.UTC
    ).let {
      assertThat(it[0].formatUtc()).isEqualTo("2002-12-03T18:00:00.000Z")
      assertThat(it[1].formatUtc()).isEqualTo("2002-12-03T19:00:00.000Z")
      assertThat(it[2].formatUtc()).isEqualTo("2002-12-03T20:00:00.000Z")
      assertThat(it[3].formatUtc()).isEqualTo("2002-12-03T21:00:00.000Z")
      assertThat(it[4].formatUtc()).isEqualTo("2002-12-03T22:00:00.000Z")

      assertThat(it.last().formatUtc()).isEqualTo("2002-12-08T08:00:00.000Z")
    }
  }

  @Test
  fun testCalculateTickValuesGreaterThan15MinutesMinDistance() {
    //https://www.epochconverter.com/
    //> 15 minutes -> we expect a tick every half hour
    @ms val tickDistanceGreaterThan15Minutes = 15.minutes.toDouble(DurationUnit.MILLISECONDS) + 1.0
    TimeAxisTickCalculator.calculateTickValues(
      1038935515000.0, //Tuesday, 3. December 2002 17:11:55
      1039334699000.0, //Sunday, 8. December 2002 08:04:59
      tickDistanceGreaterThan15Minutes, TimeZone.UTC
    ).let {
      assertThat(it[0].formatUtc()).isEqualTo("2002-12-03T17:30:00.000Z")
      assertThat(it[1].formatUtc()).isEqualTo("2002-12-03T18:00:00.000Z")
      assertThat(it[2].formatUtc()).isEqualTo("2002-12-03T18:30:00.000Z")
      assertThat(it[3].formatUtc()).isEqualTo("2002-12-03T19:00:00.000Z")
      assertThat(it[4].formatUtc()).isEqualTo("2002-12-03T19:30:00.000Z")
      assertThat(it[5].formatUtc()).isEqualTo("2002-12-03T20:00:00.000Z")

      assertThat(it.last().formatUtc()).isEqualTo("2002-12-08T08:00:00.000Z")
    }
  }

  @Test
  fun testCalculateTickValuesGreaterThan10MinutesMinDistance() {
    //https://www.epochconverter.com/
    //> 10 minutes -> we expect a tick every 15 minutes
    @ms val tickDistanceGreaterThan10Minutes = 10.minutes.toDouble(DurationUnit.MILLISECONDS) + 1.0
    val startTimestamp = 1038935515000.0
    assertThat(startTimestamp.formatUtc()).isEqualTo("2002-12-03T17:11:55.000Z")

    TimeAxisTickCalculator.calculateTickValues(
      startTimestamp, //Tuesday, 3. December 2002 17:11:55
      1039334699000.0, //Sunday, 8. December 2002 08:04:59
      tickDistanceGreaterThan10Minutes, TimeZone.UTC
    ).let {
      assertThat(it[0].formatUtc()).isEqualTo("2002-12-03T17:00:00.000Z")
      assertThat(it[1].formatUtc()).isEqualTo("2002-12-03T17:15:00.000Z")
      assertThat(it[2].formatUtc()).isEqualTo("2002-12-03T17:30:00.000Z")
      assertThat(it[3].formatUtc()).isEqualTo("2002-12-03T17:45:00.000Z")
      assertThat(it[4].formatUtc()).isEqualTo("2002-12-03T18:00:00.000Z")
      assertThat(it[5].formatUtc()).isEqualTo("2002-12-03T18:15:00.000Z")
      assertThat(it[6].formatUtc()).isEqualTo("2002-12-03T18:30:00.000Z")
    }
  }

  @Test
  fun testCalculateTickValuesGreaterThan5MinutesMinDistance() {
    //https://www.epochconverter.com/
    //> 5 minutes -> we expect a tick every 10 minutes
    @ms val tickDistanceGreaterThan5Minutes = 5.minutes.toDouble(DurationUnit.MILLISECONDS) + 1.0
    TimeAxisTickCalculator.calculateTickValues(
      1038935515000.0, //Tuesday, 3. December 2002 17:11:55
      1038989099000.0, //Wednesday, 4. December 2002 08:04:59
      tickDistanceGreaterThan5Minutes, TimeZone.UTC
    ).let {
      assertThat(it[0].formatUtc()).isEqualTo("2002-12-03T17:20:00.000Z")
      assertThat(it[1].formatUtc()).isEqualTo("2002-12-03T17:30:00.000Z")
      assertThat(it[2].formatUtc()).isEqualTo("2002-12-03T17:40:00.000Z")
      assertThat(it[3].formatUtc()).isEqualTo("2002-12-03T17:50:00.000Z")
      assertThat(it[4].formatUtc()).isEqualTo("2002-12-03T18:00:00.000Z")

      assertThat(it.last().formatUtc()).isEqualTo("2002-12-04T08:00:00.000Z")
    }
  }

  @Test
  fun testCalculateTickValuesGreaterThan2MinutesMinDistance() {
    //https://www.epochconverter.com/
    //> 2 minutes -> we expect a tick every 5 minutes
    @ms val tickDistanceGreaterThan2Minutes = 2.minutes.toDouble(DurationUnit.MILLISECONDS) + 1.0
    TimeAxisTickCalculator.calculateTickValues(
      1038935515000.0, //Tuesday, 3. December 2002 17:11:55
      1038989099000.0, //Wednesday, 4. December 2002 08:04:59
      tickDistanceGreaterThan2Minutes, TimeZone.UTC
    ).let {
      assertThat(it[0].formatUtc()).isEqualTo("2002-12-03T17:15:00.000Z")
      assertThat(it[1].formatUtc()).isEqualTo("2002-12-03T17:20:00.000Z")
      assertThat(it[2].formatUtc()).isEqualTo("2002-12-03T17:25:00.000Z")
      assertThat(it[3].formatUtc()).isEqualTo("2002-12-03T17:30:00.000Z")

      assertThat(it.last().formatUtc()).isEqualTo("2002-12-04T08:00:00.000Z")
    }
  }

  @Test
  fun testCalculateTickValuesGreaterThan1MinuteMinDistance() {
    //https://www.epochconverter.com/
    //> 1 minutes -> we expect a tick every 2 minutes
    @ms val tickDistanceGreaterThan1Minute = 1.minutes.toDouble(DurationUnit.MILLISECONDS) + 1.0
    TimeAxisTickCalculator.calculateTickValues(
      1038935515000.0, //Tuesday, 3. December 2002 17:11:55
      1038989099000.0, //Wednesday, 4. December 2002 08:04:59
      tickDistanceGreaterThan1Minute, TimeZone.UTC
    ).let {
      assertThat(it[0].formatUtc()).isEqualTo("2002-12-03T17:12:00.000Z")
      assertThat(it[1].formatUtc()).isEqualTo("2002-12-03T17:14:00.000Z")
      assertThat(it[2].formatUtc()).isEqualTo("2002-12-03T17:16:00.000Z")
      assertThat(it[3].formatUtc()).isEqualTo("2002-12-03T17:18:00.000Z")

      assertThat(it.last().formatUtc()).isEqualTo("2002-12-04T08:04:00.000Z")
    }
  }

  @Test
  fun testCalculateTickValuesGreaterThan30SecondsMinDistance() {
    //https://www.epochconverter.com/
    //> 30 seconds -> we expect a tick every minute
    @ms val tickDistanceGreaterThan30Seconds = 30.seconds.toDouble(DurationUnit.MILLISECONDS) + 1.0
    TimeAxisTickCalculator.calculateTickValues(
      1038935515000.0, //Tuesday, 3. December 2002 17:11:55
      1038989099000.0, //Wednesday, 4. December 2002 08:04:59
      tickDistanceGreaterThan30Seconds, TimeZone.UTC
    ).let {
      assertThat(it[0].formatUtc()).isEqualTo("2002-12-03T17:12:00.000Z")
      assertThat(it[1].formatUtc()).isEqualTo("2002-12-03T17:13:00.000Z")
      assertThat(it[2].formatUtc()).isEqualTo("2002-12-03T17:14:00.000Z")
      assertThat(it[3].formatUtc()).isEqualTo("2002-12-03T17:15:00.000Z")

      assertThat(it.last().formatUtc()).isEqualTo("2002-12-04T08:04:00.000Z")
    }
  }

  @Test
  fun testCalculateTickValuesGreaterThan20SecondsMinDistance() {
    //https://www.epochconverter.com/
    //> 20 seconds -> we expect a tick every 30 seconds
    @ms val tickDistanceGreaterThan20Seconds = 20.seconds.toDouble(DurationUnit.MILLISECONDS) + 1.0
    TimeAxisTickCalculator.calculateTickValues(
      1038935515000.0, //Tuesday, 3. December 2002 17:11:55
      1038989099000.0, //Wednesday, 4. December 2002 08:04:59
      tickDistanceGreaterThan20Seconds, TimeZone.UTC
    ).let {
      assertThat(it[0].formatUtc()).isEqualTo("2002-12-03T17:12:00.000Z")
      assertThat(it[1].formatUtc()).isEqualTo("2002-12-03T17:12:30.000Z")
      assertThat(it[2].formatUtc()).isEqualTo("2002-12-03T17:13:00.000Z")
      assertThat(it[3].formatUtc()).isEqualTo("2002-12-03T17:13:30.000Z")

      assertThat(it.last().formatUtc()).isEqualTo("2002-12-04T08:04:30.000Z")
    }
  }

  @Test
  fun testCalculateTickValuesGreaterThan10SecondsMinDistance() {
    //https://www.epochconverter.com/
    //> 10 seconds -> we expect a tick every 15 seconds
    @ms val tickDistanceGreaterThan10Seconds = 10.seconds.toDouble(DurationUnit.MILLISECONDS) + 1.0
    TimeAxisTickCalculator.calculateTickValues(
      1038935515000.0, //Tuesday, 3. December 2002 17:11:55
      1038989099000.0, //Wednesday, 4. December 2002 08:04:59
      tickDistanceGreaterThan10Seconds, TimeZone.UTC
    ).let {
      assertThat(it[0].formatUtc()).isEqualTo("2002-12-03T17:12:00.000Z")
      assertThat(it[1].formatUtc()).isEqualTo("2002-12-03T17:12:15.000Z")
      assertThat(it[2].formatUtc()).isEqualTo("2002-12-03T17:12:30.000Z")
      assertThat(it[3].formatUtc()).isEqualTo("2002-12-03T17:12:45.000Z")

      assertThat(it.last().formatUtc()).isEqualTo("2002-12-04T08:04:45.000Z")
    }
  }

  @Test
  fun testCalculateTickValuesGreaterThan5SecondsMinDistance() {
    //https://www.epochconverter.com/
    //> 5 seconds -> we expect a tick every 10 seconds
    @ms val tickDistanceGreaterThan5Seconds = 5.seconds.toDouble(DurationUnit.MILLISECONDS) + 1.0
    val startTimestamp = 1038935515000.0
    assertThat(startTimestamp.formatUtc()).isEqualTo("2002-12-03T17:11:55.000Z")

    val endTimestamp = startTimestamp + TimeConstants.millisPerSecond * 10 * 15
    assertThat(endTimestamp.formatUtc()).isEqualTo("2002-12-03T17:14:25.000Z")

    TimeAxisTickCalculator.calculateTickValues(
      startTimestamp, //Tuesday, 3. December 2002 17:11:55
      endTimestamp, //Wednesday, 4. December 2002 08:04:59
      tickDistanceGreaterThan5Seconds, TimeZone.UTC
    ).let {
      assertThat(it[0].formatUtc()).isEqualTo("2002-12-03T17:11:50.000Z")
      assertThat(it[1].formatUtc()).isEqualTo("2002-12-03T17:12:00.000Z")
      assertThat(it[2].formatUtc()).isEqualTo("2002-12-03T17:12:10.000Z")
      assertThat(it[3].formatUtc()).isEqualTo("2002-12-03T17:12:20.000Z")
      assertThat(it[4].formatUtc()).isEqualTo("2002-12-03T17:12:30.000Z")
    }
  }

  @Test
  fun testCalculateTickValuesGreaterThan3SecondsMinDistance() {
    //https://www.epochconverter.com/
    //> 3 seconds -> we expect a tick every 5 seconds
    @ms val tickDistanceGreaterThan3Seconds = 3.seconds.toDouble(DurationUnit.MILLISECONDS) + 1.0
    TimeAxisTickCalculator.calculateTickValues(
      1038935515000.0, //Tuesday, 3. December 2002 17:11:55
      1038953849000.0, //Tuesday, 3. December 2002 22:17:29
      tickDistanceGreaterThan3Seconds, TimeZone.UTC
    ).let {
      assertThat(it[0].formatUtc()).isEqualTo("2002-12-03T17:11:55.000Z")
      assertThat(it[1].formatUtc()).isEqualTo("2002-12-03T17:12:00.000Z")
      assertThat(it[2].formatUtc()).isEqualTo("2002-12-03T17:12:05.000Z")
      assertThat(it[3].formatUtc()).isEqualTo("2002-12-03T17:12:10.000Z")

      assertThat(it.last().formatUtc()).isEqualTo("2002-12-03T22:17:25.000Z")
    }
  }

  @Test
  fun testCalculateTickValuesGreaterThan2SecondsMinDistance() {
    //https://www.epochconverter.com/
    //> 2 seconds -> we expect a tick every 5 seconds
    @ms val tickDistanceGreaterThan2Seconds = 2.seconds.toDouble(DurationUnit.MILLISECONDS) + 1.0
    TimeAxisTickCalculator.calculateTickValues(
      1038935515000.0, //Tuesday, 3. December 2002 17:11:55
      1038953849000.0, //Tuesday, 3. December 2002 22:17:29
      tickDistanceGreaterThan2Seconds, TimeZone.UTC
    ).let {
      assertThat(it[0].formatUtc()).isEqualTo("2002-12-03T17:11:55.000Z")
      assertThat(it[1].formatUtc()).isEqualTo("2002-12-03T17:12:00.000Z")
      assertThat(it[2].formatUtc()).isEqualTo("2002-12-03T17:12:05.000Z")
      assertThat(it[3].formatUtc()).isEqualTo("2002-12-03T17:12:10.000Z")
      assertThat(it[4].formatUtc()).isEqualTo("2002-12-03T17:12:15.000Z")

      assertThat(it.last().formatUtc()).isEqualTo("2002-12-03T22:17:25.000Z")
    }
  }

  @Test
  fun testCalculateTickValuesGreaterThan1SecondMinDistance() {
    //https://www.epochconverter.com/
    //> 1 seconds -> we expect a tick every 2 seconds
    @ms val tickDistanceGreaterThan1Second = 1.seconds.toDouble(DurationUnit.MILLISECONDS) + 1.0
    TimeAxisTickCalculator.calculateTickValues(
      1038935515000.0, //Tuesday, 3. December 2002 17:11:55
      1038953849000.0, //Tuesday, 3. December 2002 22:17:29
      tickDistanceGreaterThan1Second, TimeZone.UTC
    ).let {
      assertThat(it[0].formatUtc()).isEqualTo("2002-12-03T17:11:56.000Z")
      assertThat(it[1].formatUtc()).isEqualTo("2002-12-03T17:11:58.000Z")
      assertThat(it[2].formatUtc()).isEqualTo("2002-12-03T17:12:00.000Z")
      assertThat(it[3].formatUtc()).isEqualTo("2002-12-03T17:12:02.000Z")

      assertThat(it.last().formatUtc()).isEqualTo("2002-12-03T22:17:28.000Z")
    }
  }

  @Test
  fun testCalculateTickValuesGreaterThan500MillisMinDistance() {
    //https://www.epochconverter.com/
    //> 500 milliseconds -> we expect a tick every second
    @ms val tickDistanceGreaterThan500Milliseconds = 501.0
    TimeAxisTickCalculator.calculateTickValues(
      1038935515000.0, //Tuesday, 3. December 2002 17:11:55
      1038953849000.0, //Tuesday, 3. December 2002 22:17:29
      tickDistanceGreaterThan500Milliseconds, TimeZone.UTC
    ).let {
      assertThat(it[0].formatUtc()).isEqualTo("2002-12-03T17:11:55.000Z")
      assertThat(it[1].formatUtc()).isEqualTo("2002-12-03T17:11:56.000Z")
      assertThat(it[2].formatUtc()).isEqualTo("2002-12-03T17:11:57.000Z")
      assertThat(it[3].formatUtc()).isEqualTo("2002-12-03T17:11:58.000Z")

      assertThat(it.last().formatUtc()).isEqualTo("2002-12-03T22:17:29.000Z")
    }
  }

  @Test
  fun testCalculateTickValuesGreaterThan250MillisMinDistance() {
    //https://www.epochconverter.com/
    //> 250 milliseconds -> we expect a tick every 500 ms
    @ms val tickDistanceGreaterThan250Milliseconds = 251.0
    val start = 1038935515000.0
    assertThat(start.formatUtc()).isEqualTo("2002-12-03T17:11:55.000Z")
    val end = start + 500.0 * 20
    assertThat(end.formatUtc()).isEqualTo("2002-12-03T17:12:05.000Z")

    val delta = end - start
    assertThat(delta / 500.0).isEqualTo(20.0)

    TimeAxisTickCalculator.calculateTickValues(
      start, //Tuesday, 3. December 2002 17:11:55
      end, //Tuesday, 3. December 2002 22:17:29
      tickDistanceGreaterThan250Milliseconds, TimeZone.UTC
    ).let {
      assertThat(it[0].formatUtc()).isEqualTo("2002-12-03T17:11:55.000Z")
      assertThat(it[1].formatUtc()).isEqualTo("2002-12-03T17:11:55.500Z")
      assertThat(it[2].formatUtc()).isEqualTo("2002-12-03T17:11:56.000Z")
      assertThat(it[3].formatUtc()).isEqualTo("2002-12-03T17:11:56.500Z")

      assertThat(it.last().formatUtc()).isEqualTo("2002-12-03T17:12:05.000Z")
    }
  }

  @Test
  fun testCalculateTickValuesGreaterThan200MillisMinDistance() {
    //https://www.epochconverter.com/
    //> 200 milliseconds -> we expect a tick every 250 ms
    @ms val tickDistanceGreaterThan200Milliseconds = 201.0
    TimeAxisTickCalculator.calculateTickValues(
      1038935515000.0, //Tuesday, 3. December 2002 17:11:55
      1038953849333.0, //Tuesday, 3. December 2002 22:17:29
      tickDistanceGreaterThan200Milliseconds, TimeZone.UTC
    ).let {
      assertThat(it[0].formatUtc()).isEqualTo("2002-12-03T17:11:55.000Z")
      assertThat(it[1].formatUtc()).isEqualTo("2002-12-03T17:11:55.250Z")
      assertThat(it[2].formatUtc()).isEqualTo("2002-12-03T17:11:55.500Z")
      assertThat(it[3].formatUtc()).isEqualTo("2002-12-03T17:11:55.750Z")
      assertThat(it[4].formatUtc()).isEqualTo("2002-12-03T17:11:56.000Z")

      assertThat(it.last().formatUtc()).isEqualTo("2002-12-03T22:17:29.250Z")
    }
  }

  @Test
  fun testCalculateTickValuesGreaterThan100MillisMinDistance() {
    //https://www.epochconverter.com/
    //> 100 milliseconds -> we expect a tick every 200 ms
    @ms val tickDistanceGreaterThan100Milliseconds = 101.0
    TimeAxisTickCalculator.calculateTickValues(
      1038935515000.0, //Tuesday, 3. December 2002 17:11:55
      1038953849333.0, //Tuesday, 3. December 2002 22:17:29
      tickDistanceGreaterThan100Milliseconds, TimeZone.UTC
    ).let {
      assertThat(it[0].formatUtc()).isEqualTo("2002-12-03T17:11:55.000Z")
      assertThat(it[1].formatUtc()).isEqualTo("2002-12-03T17:11:55.200Z")
      assertThat(it[2].formatUtc()).isEqualTo("2002-12-03T17:11:55.400Z")
      assertThat(it[3].formatUtc()).isEqualTo("2002-12-03T17:11:55.600Z")

      assertThat(it.last().formatUtc()).isEqualTo("2002-12-03T22:17:29.200Z")
    }
  }

  @Test
  fun testCalculateTickValuesGreaterThan50MillisMinDistance() {
    //https://www.epochconverter.com/
    //> 50 milliseconds -> we expect a tick every 100 ms
    @ms val tickDistanceGreaterThan50Milliseconds = 51.0
    TimeAxisTickCalculator.calculateTickValues(
      1038935515000.0, //Tuesday, 3. December 2002 17:11:55
      1038953849333.0, //Tuesday, 3. December 2002 22:17:29
      tickDistanceGreaterThan50Milliseconds, TimeZone.UTC
    ).let {
      assertThat(it[0].formatUtc()).isEqualTo("2002-12-03T17:11:55.000Z")
      assertThat(it[1].formatUtc()).isEqualTo("2002-12-03T17:11:55.100Z")
      assertThat(it[2].formatUtc()).isEqualTo("2002-12-03T17:11:55.200Z")
      assertThat(it[3].formatUtc()).isEqualTo("2002-12-03T17:11:55.300Z")

      assertThat(it.last().formatUtc()).isEqualTo("2002-12-03T22:17:29.300Z")
    }
  }

  @Test
  fun testCalculateTickValuesGreaterThan25MillisMinDistance() {
    //https://www.epochconverter.com/
    //> 25 milliseconds -> we expect a tick every 50 ms
    @ms val tickDistanceGreaterThan25Milliseconds = 26.0
    TimeAxisTickCalculator.calculateTickValues(
      1038935515000.0, //Tuesday, 3. December 2002 17:11:55
      1038938249333.0, //Tuesday, 3. December 2002 17:57:29
      tickDistanceGreaterThan25Milliseconds, TimeZone.UTC
    ).let {
      assertThat(it[0].formatUtc()).isEqualTo("2002-12-03T17:11:55.000Z")
      assertThat(it[1].formatUtc()).isEqualTo("2002-12-03T17:11:55.050Z")
      assertThat(it[2].formatUtc()).isEqualTo("2002-12-03T17:11:55.100Z")
      assertThat(it[3].formatUtc()).isEqualTo("2002-12-03T17:11:55.150Z")

      assertThat(it.last().formatUtc()).isEqualTo("2002-12-03T17:57:29.300Z")
    }
  }

  @Test
  fun testCalculateTickValuesGreaterThan20MillisMinDistance() {
    //https://www.epochconverter.com/
    //> 20 milliseconds -> we expect a tick every 25 ms
    @ms val tickDistanceGreaterThan20Milliseconds = 21.0
    TimeAxisTickCalculator.calculateTickValues(
      1038935515000.0, //Tuesday, 3. December 2002 17:11:55
      1038938249333.0, //Tuesday, 3. December 2002 17:57:29
      tickDistanceGreaterThan20Milliseconds, TimeZone.UTC
    ).let {
      assertThat(it[0].formatUtc()).isEqualTo("2002-12-03T17:11:55.000Z")
      assertThat(it[1].formatUtc()).isEqualTo("2002-12-03T17:11:55.025Z")
      assertThat(it[2].formatUtc()).isEqualTo("2002-12-03T17:11:55.050Z")
      assertThat(it[3].formatUtc()).isEqualTo("2002-12-03T17:11:55.075Z")

      assertThat(it.last().formatUtc()).isEqualTo("2002-12-03T17:57:29.325Z")
    }
  }

  @Test
  fun testCalculateTickValuesGreaterThan10MillisMinDistance() {
    //https://www.epochconverter.com/
    //> 10 milliseconds -> we expect a tick every 20 ms
    @ms val tickDistanceGreaterThan10Milliseconds = 15.0
    TimeAxisTickCalculator.calculateTickValues(
      1038935515000.0, //Tuesday, 3. December 2002 17:11:55
      1038938249333.0, //Tuesday, 3. December 2002 17:57:29
      tickDistanceGreaterThan10Milliseconds, TimeZone.UTC
    ).let {
      assertThat(it[0].formatUtc()).isEqualTo("2002-12-03T17:11:55.000Z")
      assertThat(it[1].formatUtc()).isEqualTo("2002-12-03T17:11:55.020Z")
      assertThat(it[2].formatUtc()).isEqualTo("2002-12-03T17:11:55.040Z")
      assertThat(it[3].formatUtc()).isEqualTo("2002-12-03T17:11:55.060Z")

      assertThat(it.last().formatUtc()).isEqualTo("2002-12-03T17:57:29.320Z")
    }
  }

  @Test
  fun testCalculateTickValuesGreaterThan5MillisMinDistance() {
    //https://www.epochconverter.com/
    //> 5 milliseconds -> we expect a tick every 10 ms
    @ms val tickDistanceGreaterThan5Milliseconds = 6.0
    TimeAxisTickCalculator.calculateTickValues(
      1038935515000.0, //Tuesday, 3. December 2002 17:11:55
      1038935606333.0, //Tuesday, 3. December 2002 17:13:26
      tickDistanceGreaterThan5Milliseconds, TimeZone.UTC
    ).let {
      assertThat(it[0].formatUtc()).isEqualTo("2002-12-03T17:11:55.000Z")
      assertThat(it[1].formatUtc()).isEqualTo("2002-12-03T17:11:55.010Z")
      assertThat(it[2].formatUtc()).isEqualTo("2002-12-03T17:11:55.020Z")
      assertThat(it[3].formatUtc()).isEqualTo("2002-12-03T17:11:55.030Z")

      assertThat(it.last().formatUtc()).isEqualTo("2002-12-03T17:13:26.330Z")
    }
  }

  @Test
  fun testCalculateTickValuesGreaterThan2MillisMinDistance() {
    //https://www.epochconverter.com/
    //> 2 milliseconds -> we expect a tick every 5 ms
    @ms val tickDistanceGreaterThan2Milliseconds = 3.0
    TimeAxisTickCalculator.calculateTickValues(
      1038935515000.0, //Tuesday, 3. December 2002 17:11:55
      1038935606333.0, //Tuesday, 3. December 2002 17:13:26
      tickDistanceGreaterThan2Milliseconds, TimeZone.UTC
    ).let {
      assertThat(it[0].formatUtc()).isEqualTo("2002-12-03T17:11:55.000Z")
      assertThat(it[1].formatUtc()).isEqualTo("2002-12-03T17:11:55.005Z")
      assertThat(it[2].formatUtc()).isEqualTo("2002-12-03T17:11:55.010Z")
      assertThat(it[3].formatUtc()).isEqualTo("2002-12-03T17:11:55.015Z")

      assertThat(it.last().formatUtc()).isEqualTo("2002-12-03T17:13:26.330Z")
    }
  }

  @Test
  fun testCalculateTickValuesGreaterThan1MillisMinDistance() {
    //https://www.epochconverter.com/
    //> 1 milliseconds -> we expect a tick every 2 ms
    @ms val tickDistanceGreaterThan1Millisecond = 1.1
    TimeAxisTickCalculator.calculateTickValues(
      1038935515000.0, //Tuesday, 3. December 2002 17:11:55
      1038935606333.0, //Tuesday, 3. December 2002 17:13:26
      tickDistanceGreaterThan1Millisecond, TimeZone.UTC
    ).let {
      assertThat(it[0].formatUtc()).isEqualTo("2002-12-03T17:11:55.000Z")
      assertThat(it[1].formatUtc()).isEqualTo("2002-12-03T17:11:55.002Z")
      assertThat(it[2].formatUtc()).isEqualTo("2002-12-03T17:11:55.004Z")
      assertThat(it[3].formatUtc()).isEqualTo("2002-12-03T17:11:55.006Z")

      assertThat(it.last().formatUtc()).isEqualTo("2002-12-03T17:13:26.332Z")
    }
  }

  @Test
  fun testCalculateTickValuesVerySmallMinDistance() {
    //https://www.epochconverter.com/
    //we expect a tick every 1 ms (minimum value)
    @ms val minTickDistance = 0.3
    TimeAxisTickCalculator.calculateTickValues(
      1038935515000.0, //Tuesday, 3. December 2002 17:11:55.000
      1038935515333.117, //Tuesday, 3. December 2002 17:11:55.333
      minTickDistance, TimeZone.UTC
    ).let {
      assertThat(it[0]).isEqualTo(1038935515000.0)
      assertThat(it[1]).isEqualTo(1038935515001.0)
      assertThat(it[2]).isEqualTo(1038935515002.0)
      assertThat(it[3]).isEqualTo(1038935515003.0)

      assertThat(it.last()).isEqualTo(1038935515333.0)
    }
  }

}
