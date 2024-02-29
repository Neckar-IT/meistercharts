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
package com.meistercharts.resources

import com.meistercharts.canvas.paintable.Paintable
import com.meistercharts.color.Color
import com.meistercharts.color.ColorProvider
import com.meistercharts.color.ColorProviderNullable
import com.meistercharts.resources.svg.SvgPaintableProviders
import it.neckar.geometry.Direction
import it.neckar.geometry.Size

/**
 * Provides the basic paintables that are provided by MeisterCharts
 * ** Generated automatically ** do not modify **
 * call `gradle createIconDeclarations` to regenerate this file
 */
object Icons {
  fun mapMarker(size: Size = Size.PX_24, fill: ColorProviderNullable = Color.lightgray, stroke: ColorProviderNullable = null, alignment: Direction = Direction.Center): Paintable = SvgPaintableProviders.mapMarker.get(size, fill, stroke, alignment)
  fun autoScale(size: Size = Size.PX_24, fill: ColorProviderNullable = Color.lightgray, stroke: ColorProviderNullable = null, alignment: Direction = Direction.Center): Paintable = SvgPaintableProviders.autoScale.get(size, fill, stroke, alignment)
  fun delete(size: Size = Size.PX_24, fill: ColorProviderNullable = Color.lightgray, stroke: ColorProviderNullable = null, alignment: Direction = Direction.Center): Paintable = SvgPaintableProviders.delete.get(size, fill, stroke, alignment)
  fun drag(size: Size = Size.PX_24, fill: ColorProviderNullable = Color.lightgray, stroke: ColorProviderNullable = null, alignment: Direction = Direction.Center): Paintable = SvgPaintableProviders.drag.get(size, fill, stroke, alignment)
  fun end(size: Size = Size.PX_24, fill: ColorProviderNullable = Color.lightgray, stroke: ColorProviderNullable = null, alignment: Direction = Direction.Center): Paintable = SvgPaintableProviders.end.get(size, fill, stroke, alignment)
  fun error(size: Size = Size.PX_24, fill: ColorProviderNullable = Color.lightgray, stroke: ColorProviderNullable = null, alignment: Direction = Direction.Center): Paintable = SvgPaintableProviders.error.get(size, fill, stroke, alignment)
  fun first(size: Size = Size.PX_24, fill: ColorProviderNullable = Color.lightgray, stroke: ColorProviderNullable = null, alignment: Direction = Direction.Center): Paintable = SvgPaintableProviders.first.get(size, fill, stroke, alignment)
  fun home(size: Size = Size.PX_24, fill: ColorProviderNullable = Color.lightgray, stroke: ColorProviderNullable = null, alignment: Direction = Direction.Center): Paintable = SvgPaintableProviders.home.get(size, fill, stroke, alignment)
  fun hourglass(size: Size = Size.PX_24, fill: ColorProviderNullable = Color.lightgray, stroke: ColorProviderNullable = null, alignment: Direction = Direction.Center): Paintable = SvgPaintableProviders.hourglass.get(size, fill, stroke, alignment)
  fun last(size: Size = Size.PX_24, fill: ColorProviderNullable = Color.lightgray, stroke: ColorProviderNullable = null, alignment: Direction = Direction.Center): Paintable = SvgPaintableProviders.last.get(size, fill, stroke, alignment)
  fun legend(size: Size = Size.PX_24, fill: ColorProviderNullable = Color.lightgray, stroke: ColorProviderNullable = null, alignment: Direction = Direction.Center): Paintable = SvgPaintableProviders.legend.get(size, fill, stroke, alignment)
  fun neckarItQr(size: Size = Size.PX_24, fill: ColorProviderNullable = Color.lightgray, stroke: ColorProviderNullable = null, alignment: Direction = Direction.Center): Paintable = SvgPaintableProviders.neckarItQr.get(size, fill, stroke, alignment)
  fun noAutoScale(size: Size = Size.PX_24, fill: ColorProviderNullable = Color.lightgray, stroke: ColorProviderNullable = null, alignment: Direction = Direction.Center): Paintable = SvgPaintableProviders.noAutoScale.get(size, fill, stroke, alignment)
  fun noLegend(size: Size = Size.PX_24, fill: ColorProviderNullable = Color.lightgray, stroke: ColorProviderNullable = null, alignment: Direction = Direction.Center): Paintable = SvgPaintableProviders.noLegend.get(size, fill, stroke, alignment)
  fun ok(size: Size = Size.PX_24, fill: ColorProviderNullable = Color.lightgray, stroke: ColorProviderNullable = null, alignment: Direction = Direction.Center): Paintable = SvgPaintableProviders.ok.get(size, fill, stroke, alignment)
  fun pause(size: Size = Size.PX_24, fill: ColorProviderNullable = Color.lightgray, stroke: ColorProviderNullable = null, alignment: Direction = Direction.Center): Paintable = SvgPaintableProviders.pause.get(size, fill, stroke, alignment)
  fun play(size: Size = Size.PX_24, fill: ColorProviderNullable = Color.lightgray, stroke: ColorProviderNullable = null, alignment: Direction = Direction.Center): Paintable = SvgPaintableProviders.play.get(size, fill, stroke, alignment)
  fun questionmark(size: Size = Size.PX_24, fill: ColorProviderNullable = Color.lightgray, stroke: ColorProviderNullable = null, alignment: Direction = Direction.Center): Paintable = SvgPaintableProviders.questionmark.get(size, fill, stroke, alignment)
  fun resetZoom(size: Size = Size.PX_24, fill: ColorProviderNullable = Color.lightgray, stroke: ColorProviderNullable = null, alignment: Direction = Direction.Center): Paintable = SvgPaintableProviders.resetZoom.get(size, fill, stroke, alignment)
  fun rotate(size: Size = Size.PX_24, fill: ColorProviderNullable = Color.lightgray, stroke: ColorProviderNullable = null, alignment: Direction = Direction.Center): Paintable = SvgPaintableProviders.rotate.get(size, fill, stroke, alignment)
  fun start(size: Size = Size.PX_24, fill: ColorProviderNullable = Color.lightgray, stroke: ColorProviderNullable = null, alignment: Direction = Direction.Center): Paintable = SvgPaintableProviders.start.get(size, fill, stroke, alignment)
  fun timestampsAbsolute(size: Size = Size.PX_24, fill: ColorProviderNullable = Color.lightgray, stroke: ColorProviderNullable = null, alignment: Direction = Direction.Center): Paintable = SvgPaintableProviders.timestampsAbsolute.get(size, fill, stroke, alignment)
  fun timestampsRelative(size: Size = Size.PX_24, fill: ColorProviderNullable = Color.lightgray, stroke: ColorProviderNullable = null, alignment: Direction = Direction.Center): Paintable = SvgPaintableProviders.timestampsRelative.get(size, fill, stroke, alignment)
  fun trash(size: Size = Size.PX_24, fill: ColorProviderNullable = Color.lightgray, stroke: ColorProviderNullable = null, alignment: Direction = Direction.Center): Paintable = SvgPaintableProviders.trash.get(size, fill, stroke, alignment)
  fun visibility(size: Size = Size.PX_24, fill: ColorProviderNullable = Color.lightgray, stroke: ColorProviderNullable = null, alignment: Direction = Direction.Center): Paintable = SvgPaintableProviders.visibility.get(size, fill, stroke, alignment)
  fun warning(size: Size = Size.PX_24, fill: ColorProviderNullable = Color.lightgray, stroke: ColorProviderNullable = null, alignment: Direction = Direction.Center): Paintable = SvgPaintableProviders.warning.get(size, fill, stroke, alignment)
  fun yAxis(size: Size = Size.PX_24, fill: ColorProviderNullable = Color.lightgray, stroke: ColorProviderNullable = null, alignment: Direction = Direction.Center): Paintable = SvgPaintableProviders.yAxis.get(size, fill, stroke, alignment)
  fun zoomIn(size: Size = Size.PX_24, fill: ColorProviderNullable = Color.lightgray, stroke: ColorProviderNullable = null, alignment: Direction = Direction.Center): Paintable = SvgPaintableProviders.zoomIn.get(size, fill, stroke, alignment)
  fun zoomOut(size: Size = Size.PX_24, fill: ColorProviderNullable = Color.lightgray, stroke: ColorProviderNullable = null, alignment: Direction = Direction.Center): Paintable = SvgPaintableProviders.zoomOut.get(size, fill, stroke, alignment)

  fun all(size: Size = Size.PX_24, fill: ColorProvider = Color.lightgray): List<Paintable> =
    listOf(
      mapMarker(size, fill),
      autoScale(size, fill),
      delete(size, fill),
      drag(size, fill),
      end(size, fill),
      error(size, fill),
      first(size, fill),
      home(size, fill),
      hourglass(size, fill),
      last(size, fill),
      legend(size, fill),
      neckarItQr(size, fill),
      noAutoScale(size, fill),
      noLegend(size, fill),
      ok(size, fill),
      pause(size, fill),
      play(size, fill),
      questionmark(size, fill),
      resetZoom(size, fill),
      rotate(size, fill),
      start(size, fill),
      timestampsAbsolute(size, fill),
      timestampsRelative(size, fill),
      trash(size, fill),
      visibility(size, fill),
      warning(size, fill),
      yAxis(size, fill),
      zoomIn(size, fill),
      zoomOut(size, fill)
    )
}
