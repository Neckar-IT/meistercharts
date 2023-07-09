package com.meistercharts.map

import assertk.*
import assertk.assertions.*
import com.meistercharts.maps.MapCoordinates
import com.meistercharts.maps.latitude2DomainRelative
import com.meistercharts.maps.longitude2DomainRelative
import com.meistercharts.maps.toDomainRelativeX
import com.meistercharts.maps.toDomainRelativeY
import org.junit.jupiter.api.Test

class SlippyMapTest {
  @Test
  fun testConversions() {
    assertThat(MapCoordinates.neckarIt.latitude.toDomainRelativeY()).isEqualTo(0.3458895530415205)
    assertThat(MapCoordinates.neckarIt.longitude.toDomainRelativeX()).isEqualTo(0.5251412897611111)

    assertThat(MapCoordinates.neckarIt.latitude2DomainRelative()).isEqualTo(0.3458895530415205)
    assertThat(MapCoordinates.neckarIt.longitude2DomainRelative()).isEqualTo(0.5251412897611111)
  }
}
