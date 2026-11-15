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
package com.meistercharts.demojs.descriptors

import com.meistercharts.demo.MeisterchartsDemoDescriptor
import com.meistercharts.demojs.descriptors.EventsJSDemoDescriptor
import com.meistercharts.demojs.descriptors.FontCacheJSDemoDescriptor
import com.meistercharts.demojs.descriptors.FontMetricsJSDebugDemoDescriptor
import com.meistercharts.demojs.descriptors.FontMetricsJSDemoDescriptor
import com.meistercharts.demojs.descriptors.ZeroSizeDemoDescriptor

/**
 * Demo descriptors that are specific for the web targets (JS + Wasm)
 */
object DemoDescriptorsJS {
  val descriptors: List<MeisterchartsDemoDescriptor<*>> = listOf(
    FontCacheJSDemoDescriptor(),
    ExplainFontsJSDemoDescriptor(),
    FontMetricsJSDemoDescriptor(),
    FontMetricsJSDebugDemoDescriptor(),
    EventsJSDemoDescriptor(),
    ZeroSizeDemoDescriptor(),
    CssPropertiesDemoDescriptor(),
    DisposeDemoDescriptor(),
  ) + platformSpecificDemoDescriptors
}

/**
 * Demo descriptors that only work on one web target (e.g. kotlin.js.Date, performance.memory,
 * ChartConfigParsingSupport - all JS-only). Empty on Wasm.
 */
internal expect val platformSpecificDemoDescriptors: List<MeisterchartsDemoDescriptor<*>>
