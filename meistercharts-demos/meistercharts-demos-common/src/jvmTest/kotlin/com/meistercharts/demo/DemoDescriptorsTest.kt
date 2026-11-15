/*
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
package com.meistercharts.demo

import assertk.*
import assertk.assertions.*
import org.junit.jupiter.api.Test

class DemoDescriptorsTest {
  @Test
  fun testUniqueUuids() {
    val uuids = DemoDescriptors.descriptors.map { it.uuid }
    assertThat(uuids).hasSize(uuids.toSet().size)
  }

  @Test
  fun testUniqueUuidsPredefinedConfigs() {
    val predefinedConfigurationsUuids = DemoDescriptors.descriptors.flatMap { it.predefinedConfigurations }.map { it.uuid }
    val demoDescriptorUuids = DemoDescriptors.descriptors.map { it.uuid }

    val allUuids = predefinedConfigurationsUuids + demoDescriptorUuids

    val duplicates = allUuids.groupBy {
      it
    }.filter {
      it.value.size > 1
    }.map {
      it.key
    }

    assertThat(duplicates).isEmpty()
    assertThat(allUuids).hasSize(allUuids.toSet().size)
  }

  @Test
  fun testByUuidFragment() {
    val runnableDemo = DemoDescriptors.runnableDemos.first()

    assertThat(DemoDescriptors.byUuidFragment(runnableDemo.uuid.toString())).isEqualTo(runnableDemo)
    assertThat(DemoDescriptors.byUuidFragment(runnableDemo.uuid.toString().take(8))).isEqualTo(runnableDemo)
  }

  @Test
  fun testByUuidFragmentNoMatch() {
    //A fragment that no UUID can start with - UUIDs are lower case hex with dashes
    assertThat(DemoDescriptors.byUuidFragment("zzzz")).isNull()
  }

  @Test
  fun testByUuidFragmentTooShort() {
    assertFailure {
      DemoDescriptors.byUuidFragment("abc")
    }.isInstanceOf(IllegalArgumentException::class)
  }
}
