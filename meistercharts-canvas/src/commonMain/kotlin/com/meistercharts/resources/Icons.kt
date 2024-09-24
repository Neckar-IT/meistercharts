package com.meistercharts.resources

import com.meistercharts.color.Color
import com.meistercharts.resources.svg.PathPaintable
import com.meistercharts.color.ColorProvider
import com.meistercharts.color.ColorProviderNullable
import it.neckar.geometry.Direction
import it.neckar.geometry.Size
import com.meistercharts.resources.svg.SvgPaintableProviders

/**
 * Provides the basic paintables that are provided by MeisterCharts
 * ** Generated automatically ** do not modify **
 * call `gradle createIconDeclarations` to regenerate this file
 */
object Icons {
  fun mapMarker(size: Size = Size.PX_24, fill: ColorProviderNullable = Color.lightgray, stroke: ColorProviderNullable = null, alignment: Direction = Direction.Center): PathPaintable = SvgPaintableProviders.mapMarker.get(size, fill, stroke, alignment)
  fun arrow(size: Size = Size.PX_24, fill: ColorProviderNullable = Color.lightgray, stroke: ColorProviderNullable = null, alignment: Direction = Direction.Center): PathPaintable = SvgPaintableProviders.arrow.get(size, fill, stroke, alignment)
  fun autoScale(size: Size = Size.PX_24, fill: ColorProviderNullable = Color.lightgray, stroke: ColorProviderNullable = null, alignment: Direction = Direction.Center): PathPaintable = SvgPaintableProviders.autoScale.get(size, fill, stroke, alignment)
  fun delete(size: Size = Size.PX_24, fill: ColorProviderNullable = Color.lightgray, stroke: ColorProviderNullable = null, alignment: Direction = Direction.Center): PathPaintable = SvgPaintableProviders.delete.get(size, fill, stroke, alignment)
  fun drag(size: Size = Size.PX_24, fill: ColorProviderNullable = Color.lightgray, stroke: ColorProviderNullable = null, alignment: Direction = Direction.Center): PathPaintable = SvgPaintableProviders.drag.get(size, fill, stroke, alignment)
  fun end(size: Size = Size.PX_24, fill: ColorProviderNullable = Color.lightgray, stroke: ColorProviderNullable = null, alignment: Direction = Direction.Center): PathPaintable = SvgPaintableProviders.end.get(size, fill, stroke, alignment)
  fun error(size: Size = Size.PX_24, fill: ColorProviderNullable = Color.lightgray, stroke: ColorProviderNullable = null, alignment: Direction = Direction.Center): PathPaintable = SvgPaintableProviders.error.get(size, fill, stroke, alignment)
  fun first(size: Size = Size.PX_24, fill: ColorProviderNullable = Color.lightgray, stroke: ColorProviderNullable = null, alignment: Direction = Direction.Center): PathPaintable = SvgPaintableProviders.first.get(size, fill, stroke, alignment)
  fun home(size: Size = Size.PX_24, fill: ColorProviderNullable = Color.lightgray, stroke: ColorProviderNullable = null, alignment: Direction = Direction.Center): PathPaintable = SvgPaintableProviders.home.get(size, fill, stroke, alignment)
  fun hourglass(size: Size = Size.PX_24, fill: ColorProviderNullable = Color.lightgray, stroke: ColorProviderNullable = null, alignment: Direction = Direction.Center): PathPaintable = SvgPaintableProviders.hourglass.get(size, fill, stroke, alignment)
  fun last(size: Size = Size.PX_24, fill: ColorProviderNullable = Color.lightgray, stroke: ColorProviderNullable = null, alignment: Direction = Direction.Center): PathPaintable = SvgPaintableProviders.last.get(size, fill, stroke, alignment)
  fun legend(size: Size = Size.PX_24, fill: ColorProviderNullable = Color.lightgray, stroke: ColorProviderNullable = null, alignment: Direction = Direction.Center): PathPaintable = SvgPaintableProviders.legend.get(size, fill, stroke, alignment)
  fun neckarItQr(size: Size = Size.PX_24, fill: ColorProviderNullable = Color.lightgray, stroke: ColorProviderNullable = null, alignment: Direction = Direction.Center): PathPaintable = SvgPaintableProviders.neckarItQr.get(size, fill, stroke, alignment)
  fun noAutoScale(size: Size = Size.PX_24, fill: ColorProviderNullable = Color.lightgray, stroke: ColorProviderNullable = null, alignment: Direction = Direction.Center): PathPaintable = SvgPaintableProviders.noAutoScale.get(size, fill, stroke, alignment)
  fun noLegend(size: Size = Size.PX_24, fill: ColorProviderNullable = Color.lightgray, stroke: ColorProviderNullable = null, alignment: Direction = Direction.Center): PathPaintable = SvgPaintableProviders.noLegend.get(size, fill, stroke, alignment)
  fun ok(size: Size = Size.PX_24, fill: ColorProviderNullable = Color.lightgray, stroke: ColorProviderNullable = null, alignment: Direction = Direction.Center): PathPaintable = SvgPaintableProviders.ok.get(size, fill, stroke, alignment)
  fun pause(size: Size = Size.PX_24, fill: ColorProviderNullable = Color.lightgray, stroke: ColorProviderNullable = null, alignment: Direction = Direction.Center): PathPaintable = SvgPaintableProviders.pause.get(size, fill, stroke, alignment)
  fun play(size: Size = Size.PX_24, fill: ColorProviderNullable = Color.lightgray, stroke: ColorProviderNullable = null, alignment: Direction = Direction.Center): PathPaintable = SvgPaintableProviders.play.get(size, fill, stroke, alignment)
  fun questionmark(size: Size = Size.PX_24, fill: ColorProviderNullable = Color.lightgray, stroke: ColorProviderNullable = null, alignment: Direction = Direction.Center): PathPaintable = SvgPaintableProviders.questionmark.get(size, fill, stroke, alignment)
  fun redo(size: Size = Size.PX_24, fill: ColorProviderNullable = Color.lightgray, stroke: ColorProviderNullable = null, alignment: Direction = Direction.Center): PathPaintable = SvgPaintableProviders.redo.get(size, fill, stroke, alignment)
  fun resetZoom(size: Size = Size.PX_24, fill: ColorProviderNullable = Color.lightgray, stroke: ColorProviderNullable = null, alignment: Direction = Direction.Center): PathPaintable = SvgPaintableProviders.resetZoom.get(size, fill, stroke, alignment)
  fun rotate(size: Size = Size.PX_24, fill: ColorProviderNullable = Color.lightgray, stroke: ColorProviderNullable = null, alignment: Direction = Direction.Center): PathPaintable = SvgPaintableProviders.rotate.get(size, fill, stroke, alignment)
  fun rotateLeft(size: Size = Size.PX_24, fill: ColorProviderNullable = Color.lightgray, stroke: ColorProviderNullable = null, alignment: Direction = Direction.Center): PathPaintable = SvgPaintableProviders.rotateLeft.get(size, fill, stroke, alignment)
  fun rotateRight(size: Size = Size.PX_24, fill: ColorProviderNullable = Color.lightgray, stroke: ColorProviderNullable = null, alignment: Direction = Direction.Center): PathPaintable = SvgPaintableProviders.rotateRight.get(size, fill, stroke, alignment)
  fun selectAll(size: Size = Size.PX_24, fill: ColorProviderNullable = Color.lightgray, stroke: ColorProviderNullable = null, alignment: Direction = Direction.Center): PathPaintable = SvgPaintableProviders.selectAll.get(size, fill, stroke, alignment)
  fun start(size: Size = Size.PX_24, fill: ColorProviderNullable = Color.lightgray, stroke: ColorProviderNullable = null, alignment: Direction = Direction.Center): PathPaintable = SvgPaintableProviders.start.get(size, fill, stroke, alignment)
  fun timestampsAbsolute(size: Size = Size.PX_24, fill: ColorProviderNullable = Color.lightgray, stroke: ColorProviderNullable = null, alignment: Direction = Direction.Center): PathPaintable = SvgPaintableProviders.timestampsAbsolute.get(size, fill, stroke, alignment)
  fun timestampsRelative(size: Size = Size.PX_24, fill: ColorProviderNullable = Color.lightgray, stroke: ColorProviderNullable = null, alignment: Direction = Direction.Center): PathPaintable = SvgPaintableProviders.timestampsRelative.get(size, fill, stroke, alignment)
  fun trash(size: Size = Size.PX_24, fill: ColorProviderNullable = Color.lightgray, stroke: ColorProviderNullable = null, alignment: Direction = Direction.Center): PathPaintable = SvgPaintableProviders.trash.get(size, fill, stroke, alignment)
  fun undo(size: Size = Size.PX_24, fill: ColorProviderNullable = Color.lightgray, stroke: ColorProviderNullable = null, alignment: Direction = Direction.Center): PathPaintable = SvgPaintableProviders.undo.get(size, fill, stroke, alignment)
  fun visibility(size: Size = Size.PX_24, fill: ColorProviderNullable = Color.lightgray, stroke: ColorProviderNullable = null, alignment: Direction = Direction.Center): PathPaintable = SvgPaintableProviders.visibility.get(size, fill, stroke, alignment)
  fun warning(size: Size = Size.PX_24, fill: ColorProviderNullable = Color.lightgray, stroke: ColorProviderNullable = null, alignment: Direction = Direction.Center): PathPaintable = SvgPaintableProviders.warning.get(size, fill, stroke, alignment)
  fun yAxis(size: Size = Size.PX_24, fill: ColorProviderNullable = Color.lightgray, stroke: ColorProviderNullable = null, alignment: Direction = Direction.Center): PathPaintable = SvgPaintableProviders.yAxis.get(size, fill, stroke, alignment)
  fun zoomIn(size: Size = Size.PX_24, fill: ColorProviderNullable = Color.lightgray, stroke: ColorProviderNullable = null, alignment: Direction = Direction.Center): PathPaintable = SvgPaintableProviders.zoomIn.get(size, fill, stroke, alignment)
  fun zoomOut(size: Size = Size.PX_24, fill: ColorProviderNullable = Color.lightgray, stroke: ColorProviderNullable = null, alignment: Direction = Direction.Center): PathPaintable = SvgPaintableProviders.zoomOut.get(size, fill, stroke, alignment)

  fun all(size: Size = Size.PX_24, fill: ColorProvider = Color.lightgray): List<PathPaintable> =
    listOf(
      mapMarker(size, fill),
      arrow(size, fill),
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
      redo(size, fill),
      resetZoom(size, fill),
      rotate(size, fill),
      rotateLeft(size, fill),
      rotateRight(size, fill),
      selectAll(size, fill),
      start(size, fill),
      timestampsAbsolute(size, fill),
      timestampsRelative(size, fill),
      trash(size, fill),
      undo(size, fill),
      visibility(size, fill),
      warning(size, fill),
      yAxis(size, fill),
      zoomIn(size, fill),
      zoomOut(size, fill)
    )
}
