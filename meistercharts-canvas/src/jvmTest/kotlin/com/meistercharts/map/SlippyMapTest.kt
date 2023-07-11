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
