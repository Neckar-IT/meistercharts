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
package com.meistercharts.algorithms.painter.stripe.refentry

import com.meistercharts.algorithms.painter.stripe.AbstractStripePainterPaintingVariables
import com.meistercharts.algorithms.painter.stripe.StripePainterPaintingVariables
import com.meistercharts.history.HistoryConfiguration
import com.meistercharts.history.ReferenceEntriesDataMap
import com.meistercharts.history.ReferenceEntryData
import com.meistercharts.history.ReferenceEntryDataSeriesIndex
import com.meistercharts.history.ReferenceEntryId
import com.meistercharts.history.ReferenceEntryDifferentIdsCount

/**
 * Painting variables for enums
 */
interface ReferenceEntryStripePainterPaintingVariables : StripePainterPaintingVariables<ReferenceEntryDataSeriesIndex, ReferenceEntryId, ReferenceEntryDifferentIdsCount, ReferenceEntryData?> {
  //TODO add
}

/**
 * Painting variables for referenceEntry stripes
 */
class DefaultReferenceEntryStripePainterPaintingVariables : AbstractStripePainterPaintingVariables<ReferenceEntryDataSeriesIndex, ReferenceEntryId, ReferenceEntryDifferentIdsCount, ReferenceEntryData?>(
  dataSeriesIndexDefault = ReferenceEntryDataSeriesIndex.zero,
  value1Default = ReferenceEntryId.NoValue,
  value2Default = ReferenceEntryDifferentIdsCount.NoValue,
  value3Default = null,
), ReferenceEntryStripePainterPaintingVariables {
}
