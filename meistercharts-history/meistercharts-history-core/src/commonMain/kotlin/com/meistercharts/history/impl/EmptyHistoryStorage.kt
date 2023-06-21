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
package com.meistercharts.history.impl

import com.meistercharts.history.HistoryBucket
import com.meistercharts.history.HistoryBucketDescriptor
import com.meistercharts.history.HistoryObserver
import com.meistercharts.history.HistoryStorage
import com.meistercharts.history.ObservableHistoryStorage
import it.neckar.open.unit.number.MayBeNaN
import it.neckar.open.unit.si.ms

/**
 * Mock implementation that returns a new (empty) instance every time
 */
class EmptyHistoryStorage : HistoryStorage, ObservableHistoryStorage {
  override fun get(descriptor: HistoryBucketDescriptor): HistoryBucket? {
    return null
  }

  override fun getStart(): Double {
    return Double.NaN
  }

  override fun getEnd(): @ms @MayBeNaN Double {
    return Double.NaN
  }

  override fun observe(observer: HistoryObserver) {
    //do nothing
  }

  /**
   * Attention: The registered action is never called since the EmptyHistoryStorage is never disposed
   */
  override fun onDispose(action: () -> Unit) {
  }
}
