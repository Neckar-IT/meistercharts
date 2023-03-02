package com.meistercharts.resources.svg

import com.meistercharts.model.Size
import com.meistercharts.svg.SVGPathParser

/**
 * Contains the SVG paths that can be used as paintables
 * Each SVG has a base size of 24x24 and is scaled accordingly
 * ** Generated automatically ** do not modify **
 * call `gradle createIconDeclarations` to regenerate this file
 */
object SvgPaintableProviders {
  val mapMarker: PathPaintableProvider = SVGPathParser.from(SvgPaths.mapMarker).parse().toProvider(Size.PX_24)
  val autoScale: PathPaintableProvider = SVGPathParser.from(SvgPaths.autoScale).parse().toProvider(Size.PX_24)
  val delete: PathPaintableProvider = SVGPathParser.from(SvgPaths.delete).parse().toProvider(Size.PX_24)
  val drag: PathPaintableProvider = SVGPathParser.from(SvgPaths.drag).parse().toProvider(Size.PX_24)
  val end: PathPaintableProvider = SVGPathParser.from(SvgPaths.end).parse().toProvider(Size.PX_24)
  val error: PathPaintableProvider = SVGPathParser.from(SvgPaths.error).parse().toProvider(Size.PX_24)
  val first: PathPaintableProvider = SVGPathParser.from(SvgPaths.first).parse().toProvider(Size.PX_24)
  val home: PathPaintableProvider = SVGPathParser.from(SvgPaths.home).parse().toProvider(Size.PX_24)
  val hourglass: PathPaintableProvider = SVGPathParser.from(SvgPaths.hourglass).parse().toProvider(Size.PX_24)
  val last: PathPaintableProvider = SVGPathParser.from(SvgPaths.last).parse().toProvider(Size.PX_24)
  val legend: PathPaintableProvider = SVGPathParser.from(SvgPaths.legend).parse().toProvider(Size.PX_24)
  val neckarItQr: PathPaintableProvider = SVGPathParser.from(SvgPaths.neckarItQr).parse().toProvider(Size.PX_24)
  val noAutoScale: PathPaintableProvider = SVGPathParser.from(SvgPaths.noAutoScale).parse().toProvider(Size.PX_24)
  val noLegend: PathPaintableProvider = SVGPathParser.from(SvgPaths.noLegend).parse().toProvider(Size.PX_24)
  val ok: PathPaintableProvider = SVGPathParser.from(SvgPaths.ok).parse().toProvider(Size.PX_24)
  val pause: PathPaintableProvider = SVGPathParser.from(SvgPaths.pause).parse().toProvider(Size.PX_24)
  val play: PathPaintableProvider = SVGPathParser.from(SvgPaths.play).parse().toProvider(Size.PX_24)
  val questionmark: PathPaintableProvider = SVGPathParser.from(SvgPaths.questionmark).parse().toProvider(Size.PX_24)
  val resetZoom: PathPaintableProvider = SVGPathParser.from(SvgPaths.resetZoom).parse().toProvider(Size.PX_24)
  val rotate: PathPaintableProvider = SVGPathParser.from(SvgPaths.rotate).parse().toProvider(Size.PX_24)
  val start: PathPaintableProvider = SVGPathParser.from(SvgPaths.start).parse().toProvider(Size.PX_24)
  val timestampsAbsolute: PathPaintableProvider = SVGPathParser.from(SvgPaths.timestampsAbsolute).parse().toProvider(Size.PX_24)
  val timestampsRelative: PathPaintableProvider = SVGPathParser.from(SvgPaths.timestampsRelative).parse().toProvider(Size.PX_24)
  val trash: PathPaintableProvider = SVGPathParser.from(SvgPaths.trash).parse().toProvider(Size.PX_24)
  val visibility: PathPaintableProvider = SVGPathParser.from(SvgPaths.visibility).parse().toProvider(Size.PX_24)
  val warning: PathPaintableProvider = SVGPathParser.from(SvgPaths.warning).parse().toProvider(Size.PX_24)
  val yAxis: PathPaintableProvider = SVGPathParser.from(SvgPaths.yAxis).parse().toProvider(Size.PX_24)
  val zoomIn: PathPaintableProvider = SVGPathParser.from(SvgPaths.zoomIn).parse().toProvider(Size.PX_24)
  val zoomOut: PathPaintableProvider = SVGPathParser.from(SvgPaths.zoomOut).parse().toProvider(Size.PX_24)

  fun all(): List<PathPaintableProvider> = listOf(
    mapMarker,
    autoScale,
    delete,
    drag,
    end,
    error,
    first,
    home,
    hourglass,
    last,
    legend,
    neckarItQr,
    noAutoScale,
    noLegend,
    ok,
    pause,
    play,
    questionmark,
    resetZoom,
    rotate,
    start,
    timestampsAbsolute,
    timestampsRelative,
    trash,
    visibility,
    warning,
    yAxis,
    zoomIn,
    zoomOut
  )
}
