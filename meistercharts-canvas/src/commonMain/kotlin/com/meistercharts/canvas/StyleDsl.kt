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
package com.meistercharts.canvas

/**
 * DSL marker for the visual **Style** of a painter or paintable.
 *
 * Config-object naming convention in meistercharts:
 * - Painter / Paintable  -> nested `Style`         + [StyleDsl]         — purely visual (colors, widths, fonts, gaps)
 * - Layer / Gestalt      -> nested `Configuration` + [ConfigurationDsl] — full surface (data, behaviour, appearance)
 *
 * A `Style` must hold visual properties only. Data and behaviour belong in the owning
 * Layer/Gestalt [ConfigurationDsl] object, never in a painter [Style].
 *
 * Separate markers (instead of a single one) keep the DSL receiver scopes isolated: a painter
 * style builder nested inside a layer configuration builder cannot accidentally resolve the
 * layer's members, and vice versa.
 */
@DslMarker
annotation class StyleDsl
