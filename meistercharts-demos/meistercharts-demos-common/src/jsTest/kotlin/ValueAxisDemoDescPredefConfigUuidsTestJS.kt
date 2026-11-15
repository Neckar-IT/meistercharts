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
import assertk.*
import assertk.assertions.*
import kotlin.uuid.Uuid
import com.meistercharts.demo.descriptors.ValueAxisDemoDescriptor
import it.neckar.open.collections.fastForEachWithIndex
import kotlin.test.Test

class ValueAxisDemoDescPredefConfigUuidsTestJS {
  @Test
  fun testUuids() {
    val uuids: List<Uuid> = listOf<Uuid>(
      Uuid.parse("ab784a9d-3311-4366-9b5f-5adca9d45136"),
      Uuid.parse("ab784a9d-3311-4366-9b5f-5adca9d451b7"),
      Uuid.parse("ab784a9d-3311-4366-9b5f-5adca9d451a9"),
      Uuid.parse("ab784a9d-3311-4366-9b5f-5adca9d4522a"),
      Uuid.parse("ab784a9d-3311-4366-9b5f-5adca9d450de"),
      Uuid.parse("ab784a9d-3311-4366-9b5f-5adca9d4515f"),
      Uuid.parse("ab784a9d-3311-4366-9b5f-5adca9d45220"),
      Uuid.parse("ab784a9d-3311-4366-9b5f-5adca9d452a1"),
      Uuid.parse("ab784a9d-3311-4366-9b5f-5adca9d62146"),
      Uuid.parse("ab784a9d-3311-4366-9b5f-5adca9d621c7"),
      Uuid.parse("ab784a9d-3311-4366-9b5f-5adca9d621b9"),
      Uuid.parse("ab784a9d-3311-4366-9b5f-5adca9d6223a"),
      Uuid.parse("ab784a9d-3311-4366-9b5f-5adca9d620ee"),
      Uuid.parse("ab784a9d-3311-4366-9b5f-5adca9d6216f"),
      Uuid.parse("ab784a9d-3311-4366-9b5f-5adca9d62230"),
      Uuid.parse("ab784a9d-3311-4366-9b5f-5adca9d622b1"),
    )

    ValueAxisDemoDescriptor().predefinedConfigurations.fastForEachWithIndex { index, config ->
      assertThat(config.uuid).isEqualTo(uuids[index])
    }
  }


}
