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
package com.meistercharts.demo

import com.meistercharts.demo.descriptors.AEBPresentationDemoDescriptor
import com.meistercharts.demo.descriptors.Anchor2DemoDescriptor
import com.meistercharts.demo.descriptors.AnchorDemoDescriptor
import com.meistercharts.demo.descriptors.AngleDirectionDemoDescriptor
import com.meistercharts.demo.descriptors.AntiAliasingLinesDemoDescriptor
import com.meistercharts.demo.descriptors.AntialiasingRectDemoDescriptor
import com.meistercharts.demo.descriptors.ArrowsDemoDescriptor
import com.meistercharts.demo.descriptors.As30DiagramDemoDescriptor
import com.meistercharts.demo.descriptors.AutoScaleDemoDescriptor
import com.meistercharts.demo.descriptors.BalloonTooltipDemoDescriptor
import com.meistercharts.demo.descriptors.BalloonTooltipLayerDemoDescriptor
import com.meistercharts.demo.descriptors.BarChartSingleDemoDescriptor
import com.meistercharts.demo.descriptors.BarChartGroupedGestaltDemoDescriptor
import com.meistercharts.demo.descriptors.BarChartLayerDemoDescriptor
import com.meistercharts.demo.descriptors.BarChartLegendLayerDemoDescriptor
import com.meistercharts.demo.descriptors.BarChartStackedGestaltDemoDescriptor
import com.meistercharts.demo.descriptors.BarChartStackedLabelDemoDescriptor
import com.meistercharts.demo.descriptors.BarChartStackedSickDemoDescriptor
import com.meistercharts.demo.descriptors.BarLineShowCaseSickDemoDescriptor
import com.meistercharts.demo.descriptors.BarLineShowcaseDemoDescriptor
import com.meistercharts.demo.descriptors.BarValueLabelDemoDescriptor
import com.meistercharts.demo.descriptors.BarValueLabelManualDemoDescriptor
import com.meistercharts.demo.descriptors.Base64EncodingDemoDescriptor
import com.meistercharts.demo.descriptors.BeamChartGestaltDemoDescriptor
import com.meistercharts.demo.descriptors.BeamsLayerDemoDescriptor
import com.meistercharts.demo.descriptors.BeamsLayerWithZonesDemoDescriptor
import com.meistercharts.demo.descriptors.BinaryGestaltDemoDescriptor
import com.meistercharts.demo.descriptors.BinaryPainterDemoDescriptor
import com.meistercharts.demo.descriptors.BooleanEnumStripePainterDemoDescriptor
import com.meistercharts.demo.descriptors.BulletChartGestaltDemoDescriptor
import com.meistercharts.demo.descriptors.ButtonDemoDescriptor
import com.meistercharts.demo.descriptors.CanvasLowLevelDemoDescriptor
import com.meistercharts.demo.descriptors.CanvasTilesDemoDescriptor
import com.meistercharts.demo.descriptors.CanvasTouchZoomAndPanSupportDemoDescriptor
import com.meistercharts.demo.descriptors.CategoryAxisDemoDescriptor
import com.meistercharts.demo.descriptors.CategoryAxisSupportDemoDescriptor
import com.meistercharts.demo.descriptors.CategoryAxisTopTopTitleWithAxisLayerDemoDescriptor
import com.meistercharts.demo.descriptors.CategoryChartLayerDemoDescriptor
import com.meistercharts.demo.descriptors.CategoryChartLayerGroupedDemoDescriptor
import com.meistercharts.demo.descriptors.CategoryChartLayerStackedDemoDescriptor
import com.meistercharts.demo.descriptors.CategoryLayoutCalculationDemoDescriptor
import com.meistercharts.demo.descriptors.CategoryLineChartBalloonTooltipDemoDescriptor
import com.meistercharts.demo.descriptors.CategoryLineChartDemoDescriptor
import com.meistercharts.demo.descriptors.CategoryLineChartGestaltDemoDescriptor
import com.meistercharts.demo.descriptors.CategoryLineChartGestaltWithLinesAndPointsDemoDescriptor
import com.meistercharts.demo.descriptors.CategoryLinesLayerDemoDescriptor
import com.meistercharts.demo.descriptors.ChartSizeClassificationDemo
import com.meistercharts.demo.descriptors.CircularChartDemoDescriptor
import com.meistercharts.demo.descriptors.CircularChartGestaltDemoDescriptor
import com.meistercharts.demo.descriptors.CircularChartGestaltSickDemoDescriptor
import com.meistercharts.demo.descriptors.CircularChartLayerDemoDescriptor
import com.meistercharts.demo.descriptors.CircularChartLegendDemoDescriptor
import com.meistercharts.demo.descriptors.ClassicCompassGestaltDemoDescriptor
import com.meistercharts.demo.descriptors.ClearBackgroundLayerDemoDescriptor
import com.meistercharts.demo.descriptors.ClippingLayerDemoDescriptor
import com.meistercharts.demo.descriptors.ColorTintDemoDescriptor
import com.meistercharts.demo.descriptors.CombinedPaintablesDemoDescriptor
import com.meistercharts.demo.descriptors.ChartStateTransformationDemoDescriptor
import com.meistercharts.demo.descriptors.CompartmentLayoutDemoDescriptor
import com.meistercharts.demo.descriptors.ComponentsOverlayDemoDescriptor
import com.meistercharts.demo.descriptors.ConfigurableFontDemoDescriptor
import com.meistercharts.demo.descriptors.ContentAlwaysVisibleDemoDescriptor
import com.meistercharts.demo.descriptors.ContentAreaDemoDescriptor
import com.meistercharts.demo.descriptors.ContentAreaFixedSizeDemoDescriptor
import com.meistercharts.demo.descriptors.ContentAreaLayerDemoDescriptor
import com.meistercharts.demo.descriptors.ContentViewportDemoDescriptor
import com.meistercharts.demo.descriptors.CorporateDesignDemoDescriptor
import com.meistercharts.demo.descriptors.CrossWireLayerDemoDescriptor
import com.meistercharts.demo.descriptors.CrossWireLayerMovableDemoDescriptor
import com.meistercharts.demo.descriptors.DateTimeFormatDemoDescriptor
import com.meistercharts.demo.descriptors.DelayedWindowSizeStrategyDemoDescriptor
import com.meistercharts.demo.descriptors.DevicePixelRatioDemoDescriptor
import com.meistercharts.demo.descriptors.DirectionalLinesLayerDemoDescriptor
import com.meistercharts.demo.descriptors.DragDropDemoDescriptor
import com.meistercharts.demo.descriptors.DrawingHatchesDemoDescriptor
import com.meistercharts.demo.descriptors.DrawingPrimitivesArcCenterPathDemoDescriptor
import com.meistercharts.demo.descriptors.DrawingPrimitivesArcPathDemoDescriptor
import com.meistercharts.demo.descriptors.DrawingPrimitivesArcsDemoDescriptor
import com.meistercharts.demo.descriptors.DrawingPrimitivesBezierCurveDemoDescriptor
import com.meistercharts.demo.descriptors.DrawingPrimitivesEllipsePathDemoDescriptor
import com.meistercharts.demo.descriptors.DrawingPrimitivesOvalsDemoDescriptor
import com.meistercharts.demo.descriptors.DrawingPrimitivesPathDemoDescriptor
import com.meistercharts.demo.descriptors.DrawingPrimitivesPointTypesDemoDescriptor
import com.meistercharts.demo.descriptors.DrawingPrimitivesQuadraticCurveDemoDescriptor
import com.meistercharts.demo.descriptors.DrawingPrimitivesRectanglesDemoDescriptor
import com.meistercharts.demo.descriptors.EasingDemoDescriptor
import com.meistercharts.demo.descriptors.ElevatorGestaltDemoDescriptor
import com.meistercharts.demo.descriptors.EnumBarOverTimeDemoDescriptor
import com.meistercharts.demo.descriptors.EnvironmentLayerDemoDescriptor
import com.meistercharts.demo.descriptors.EquisizedBoxLayoutDemoDescriptor
import com.meistercharts.demo.descriptors.EventsDebugLayerDemoDescriptor
import com.meistercharts.demo.descriptors.ExtremeValueAxisDemoDescriptor
import com.meistercharts.demo.descriptors.FillBackgroundCheckerLayerDemoDescriptor
import com.meistercharts.demo.descriptors.FillRectDemoDescriptor
import com.meistercharts.demo.descriptors.FillRectExtremeDemoDescriptor
import com.meistercharts.demo.descriptors.FittingWithMarginDemoDescriptor
import com.meistercharts.demo.descriptors.FontAwesomeFontDemoDescriptor
import com.meistercharts.demo.descriptors.FontDemoDescriptor
import com.meistercharts.demo.descriptors.FrameTimestampDemoDescriptor
import com.meistercharts.demo.descriptors.GaugePaintableDemoDescriptor
import com.meistercharts.demo.descriptors.GestaltLifecycleDemoDescriptor
import com.meistercharts.demo.descriptors.GlobalAlphaDemoDescriptor
import com.meistercharts.demo.descriptors.GreedyCategoryAxisLabelPainterDemo
import com.meistercharts.demo.descriptors.GridDemoDescriptor
import com.meistercharts.demo.descriptors.GridWithCategoryAxisDemoDescriptor
import com.meistercharts.demo.descriptors.GridWithValueAxisDemoDescriptor
import com.meistercharts.demo.descriptors.GroupedBarChartClippingDemoDescriptor
import com.meistercharts.demo.descriptors.HelloWorldDemoDescriptor
import com.meistercharts.demo.descriptors.HideAfterTimeoutLayerDemoDescriptor
import com.meistercharts.demo.descriptors.HistogramDemoDescriptor
import com.meistercharts.demo.descriptors.HistoryEnumLayerDemo
import com.meistercharts.demo.descriptors.HistoryReferenceEntryLayerDemo
import com.meistercharts.demo.descriptors.I18nDemoDescriptor
import com.meistercharts.demo.descriptors.ImageLayerDemoDescriptor
import com.meistercharts.demo.descriptors.ImageQualityDemoDescriptor
import com.meistercharts.demo.descriptors.ImageTranslateRotateLayerDemoDescriptor
import com.meistercharts.demo.descriptors.InitialResizeBehaviorDemoDescriptor
import com.meistercharts.demo.descriptors.InteractionConsumedDemoDescriptor
import com.meistercharts.demo.descriptors.InteractiveLayerDemoDescriptor
import com.meistercharts.demo.descriptors.KeyEventsDemoDescriptor
import com.meistercharts.demo.descriptors.LabelPaintableDemoDescriptor
import com.meistercharts.demo.descriptors.LabelPainterDemoDescriptor
import com.meistercharts.demo.descriptors.LayerVisibilityAdapterDemoDescriptor
import com.meistercharts.demo.descriptors.LayerVisibilityToggleOnShortcutDemoDescriptor
import com.meistercharts.demo.descriptors.PaintablesLayouterDemoDescriptor
import com.meistercharts.demo.descriptors.LegendLayerDemoDescriptor
import com.meistercharts.demo.descriptors.StackedPaintablesPaintableDemoDescriptor
import com.meistercharts.demo.descriptors.LimitsLayerDemoDescriptor
import com.meistercharts.demo.descriptors.LineChartLayerDemoDescriptor
import com.meistercharts.demo.descriptors.LinePainterDemoDescriptor
import com.meistercharts.demo.descriptors.LineStyleDemoDescriptor
import com.meistercharts.demo.descriptors.LinearColorGradientDemoDescriptor
import com.meistercharts.demo.descriptors.LisaChartDemoDescriptor
import com.meistercharts.demo.descriptors.LogarithmicValueRangeDemoDescriptor
import com.meistercharts.demo.descriptors.MapGestaltDemoDescriptor
import com.meistercharts.demo.descriptors.MapWithNeckarITLocationDemoDescriptor
import com.meistercharts.demo.descriptors.MapWithPaintablesGestaltDemoDescriptor
import com.meistercharts.demo.descriptors.MirroredPaintableDemoDescriptor
import com.meistercharts.demo.descriptors.MissingResourceHandlerDemoDescriptor
import com.meistercharts.demo.descriptors.ModernCompassGestaltDemoDescriptor
import com.meistercharts.demo.descriptors.MouseAndPointerEventsDemoDescriptor
import com.meistercharts.demo.descriptors.MouseCursorDemoDescriptor
import com.meistercharts.demo.descriptors.MouseEventsDemoDescriptor
import com.meistercharts.demo.descriptors.ValueAxisSupportDemoDescriptor
import com.meistercharts.demo.descriptors.MultiLineText2DemoDescriptor
import com.meistercharts.demo.descriptors.MultiLineText3DemoDescriptor
import com.meistercharts.demo.descriptors.MultiLineTextDemoDescriptor
import com.meistercharts.demo.descriptors.NeckarITFlowDemoDescriptor
import com.meistercharts.demo.descriptors.NeckarITOnlySloganDemoDescriptor
import com.meistercharts.demo.descriptors.NeckarITSloganWithFlowGestaltDemoDescriptor
import com.meistercharts.demo.descriptors.NumberFormatDemoDescriptor
import com.meistercharts.demo.descriptors.PaintPerformanceLayerDemoDescriptor
import com.meistercharts.demo.descriptors.PaintPixelPerfectDemoDescriptor
import com.meistercharts.demo.descriptors.PaintRightAngledTriangleDemoDescriptor
import com.meistercharts.demo.descriptors.PaintTriangleDemoDescriptor
import com.meistercharts.demo.descriptors.PaintableCalculatorDemoDescriptor
import com.meistercharts.demo.descriptors.PaintableDemoDescriptor
import com.meistercharts.demo.descriptors.PaintablesBoundingBoxDemoDescriptor
import com.meistercharts.demo.descriptors.PaintablesPerformanceDemoDescriptor
import com.meistercharts.demo.descriptors.PaletteDemoDescriptor
import com.meistercharts.demo.descriptors.PasspartoutDemoDescriptor
import com.meistercharts.demo.descriptors.PathWithIntersectDemoDescriptor
import com.meistercharts.demo.descriptors.PhysicalCanvasSizeDemo
import com.meistercharts.demo.descriptors.PhysicalPixelsTranslateDemo
import com.meistercharts.demo.descriptors.PhysicalSnapDemoDescriptor
import com.meistercharts.demo.descriptors.PlatformLifecycleDemoDescriptor
import com.meistercharts.demo.descriptors.PuristicCompassGestaltDemoDescriptor
import com.meistercharts.demo.descriptors.PvExpensesPerMonthDemoDescriptor
import com.meistercharts.demo.descriptors.PvProfitDemoDescriptor
import com.meistercharts.demo.descriptors.PvRoofPlanningDemoDescriptor
import com.meistercharts.demo.descriptors.PvSavingsGestaltDemoDescriptor
import com.meistercharts.demo.descriptors.QRPositionDiagramGestaltDemoDescriptor
import com.meistercharts.demo.descriptors.RadialColorGradientDemoDescriptor
import com.meistercharts.demo.descriptors.RainLayerDemoDescriptor
import com.meistercharts.demo.descriptors.RainSensorGestaltDemoDescriptor
import com.meistercharts.demo.descriptors.RectangleEnumStripePainterDemoDescriptor
import com.meistercharts.demo.descriptors.RefreshDemoDescriptor
import com.meistercharts.demo.descriptors.ResizeByHandlesLayerDemoDescriptor
import com.meistercharts.demo.descriptors.RoofBackgroundLayerDemoDescriptor
import com.meistercharts.demo.descriptors.RoundedPolylineDemoDescriptor
import com.meistercharts.demo.descriptors.RoundedRectsDemoDescriptor
import com.meistercharts.demo.descriptors.RubberBandZoomDemoDescriptor
import com.meistercharts.demo.descriptors.SandboxDemoDescriptor
import com.meistercharts.demo.descriptors.ScatterPlotGestaltDemoDescriptor
import com.meistercharts.demo.descriptors.ScatterPlotLayerDemoDescriptor
import com.meistercharts.demo.descriptors.ScatterPlotLogarithmicAxisDemoDescriptor
import com.meistercharts.demo.descriptors.ScrollWithoutModifierTextDemoDescriptor
import com.meistercharts.demo.descriptors.SegmentedLinePainterDemoDescriptor
import com.meistercharts.demo.descriptors.ShadowDemoDescriptor
import com.meistercharts.demo.descriptors.SickValueAxisTitleDemoDescriptor
import com.meistercharts.demo.descriptors.SymbolAndLabelLegendPaintableDemoDescriptor
import com.meistercharts.demo.descriptors.SlippyMapDemoDescriptor
import com.meistercharts.demo.descriptors.SnapDemoDescriptor
import com.meistercharts.demo.descriptors.SnapTranslationDemoDescriptor
import com.meistercharts.demo.descriptors.SplineLineInterpolationPainterDemo
import com.meistercharts.demo.descriptors.StackedBarChartClippingDemoDescriptor
import com.meistercharts.demo.descriptors.StackedBarPaintableDemoDescriptor
import com.meistercharts.demo.descriptors.StackedBarWithLabelDemoDescriptor
import com.meistercharts.demo.descriptors.StringShortenerDemoDescriptor
import com.meistercharts.demo.descriptors.VerticallyStackedPaintablesDemoDescriptor
import com.meistercharts.demo.descriptors.SvgPaintablesDemoDescriptor
import com.meistercharts.demo.descriptors.SvgPathsDemoDescriptor
import com.meistercharts.demo.descriptors.TankDemoDescriptor
import com.meistercharts.demo.descriptors.TargetRefreshRateDemoDescriptor
import com.meistercharts.demo.descriptors.TextBoxDemoDescriptor
import com.meistercharts.demo.descriptors.TextBoxPaintableDemoDescriptor
import com.meistercharts.demo.descriptors.TextBoxSpecialCasesDemoDescriptor
import com.meistercharts.demo.descriptors.TextGapDemoDescriptor
import com.meistercharts.demo.descriptors.TextLayerDemoDescriptor
import com.meistercharts.demo.descriptors.TextLayerI18nDemoDescriptor
import com.meistercharts.demo.descriptors.TextMaxLengthDemoDescriptor
import com.meistercharts.demo.descriptors.TextRenderDemoDescriptor
import com.meistercharts.demo.descriptors.TextTickAlignmentDemoDescriptor
import com.meistercharts.demo.descriptors.TextWithImageDemoDescriptor
import com.meistercharts.demo.descriptors.TextWithinBoxDemoDescriptor
import com.meistercharts.demo.descriptors.TextsLayerDemoDescriptor
import com.meistercharts.demo.descriptors.ThemeDemoDescriptor
import com.meistercharts.demo.descriptors.ThresholdsSupportDemoDescriptor
import com.meistercharts.demo.descriptors.ThresholdsLayerDemoDescriptor
import com.meistercharts.demo.descriptors.TileCalculatorDemoDescriptor
import com.meistercharts.demo.descriptors.TilesDebugLayerDemoDescriptor
import com.meistercharts.demo.descriptors.TilesLayerCachedDemoDescriptor
import com.meistercharts.demo.descriptors.TilesLayerDemoDescriptor
import com.meistercharts.demo.descriptors.TimeAxisDemoDescriptor
import com.meistercharts.demo.descriptors.TimePrecisionDemoDescriptor
import com.meistercharts.demo.descriptors.ToolbarDemoDescriptor
import com.meistercharts.demo.descriptors.ToolbarGestaltDemoDescriptor
import com.meistercharts.demo.descriptors.TooltipDemoDescriptor
import com.meistercharts.demo.descriptors.TouchDoubleTapDemoDescriptor
import com.meistercharts.demo.descriptors.TouchEventsDemoDescriptor
import com.meistercharts.demo.descriptors.TouchSingleTapDemoDescriptor
import com.meistercharts.demo.descriptors.TransformationMatrixDemoDescriptor
import com.meistercharts.demo.descriptors.TranslateOverTimeDemoDescriptor
import com.meistercharts.demo.descriptors.TranslatePerformanceDemoDescriptor
import com.meistercharts.demo.descriptors.TranslationLayerDemoDescriptor
import com.meistercharts.demo.descriptors.TweenDemoDescriptor
import com.meistercharts.demo.descriptors.UnusableAreaPaintableDemoDescriptor
import com.meistercharts.demo.descriptors.UrlPaintableDemoDescriptor
import com.meistercharts.demo.descriptors.MultiValueAxisLayerDemoDescriptor
import com.meistercharts.demo.descriptors.MultiValueAxisWithHudDemoDescriptor
import com.meistercharts.demo.descriptors.DiscreteTimelineChartGestaltDemoDescriptor
import com.meistercharts.demo.descriptors.HistoryReferenceScenariosDemoDescriptor
import com.meistercharts.demo.descriptors.SnapTextDemoDescriptor
import com.meistercharts.demo.descriptors.SymbolAndLabelLegendHeadlinePaintableDemoDescriptor
import com.meistercharts.demo.descriptors.TimeLineChartDemoRandomDescriptor
import com.meistercharts.demo.descriptors.TimelineChartRestDemoDescriptor
import com.meistercharts.demo.descriptors.ValueAxisAllSidesDemoDescriptor
import com.meistercharts.demo.descriptors.ValueAxisAtContentAreaDemoDescriptor
import com.meistercharts.demo.descriptors.ValueAxisContentViewportCalculationsDemoDescriptor
import com.meistercharts.demo.descriptors.ValueAxisContentViewportDemoDescriptor
import com.meistercharts.demo.descriptors.ValueAxisDemoDescriptor
import com.meistercharts.demo.descriptors.ValueAxisHudLayerDemoDescriptor
import com.meistercharts.demo.descriptors.ValueAxisMaxTicksDemoDescriptor
import com.meistercharts.demo.descriptors.ValueAxisTopTopTitleLayerDemoDescriptor
import com.meistercharts.demo.descriptors.ValueAxisTopTopTitleWithAxisLayerDemoDescriptor
import com.meistercharts.demo.descriptors.ValueAxisWithHudAndGridDemoDescriptor
import com.meistercharts.demo.descriptors.ValueAxisWithHudDemoDescriptor
import com.meistercharts.demo.descriptors.ValueAxisWithOffsetDemoDescriptor
import com.meistercharts.demo.descriptors.VersionNumberDemoDescriptor
import com.meistercharts.demo.descriptors.VisualizeTouchesDemoDescriptor
import com.meistercharts.demo.descriptors.WhatsAtDemoDescriptor
import com.meistercharts.demo.descriptors.WithAxisOrientationPaintableDemoDescriptor
import com.meistercharts.demo.descriptors.WithContentViewportDemoDescriptor
import com.meistercharts.demo.descriptors.XyPointPainterDemoDescriptor
import com.meistercharts.demo.descriptors.ZeroLinesLayerDemoDescriptor
import com.meistercharts.demo.descriptors.ZoomAndTranslationDefaults10PctMarginDemoDescriptor
import com.meistercharts.demo.descriptors.ZoomAndTranslationDefaultsMarginDemoDescriptor
import com.meistercharts.demo.descriptors.ZoomFitDemoDescriptor
import com.meistercharts.demo.descriptors.ZoomLayerDemoDescriptor
import com.meistercharts.demo.descriptors.benchmark.BenchmarkDemoDescriptor
import com.meistercharts.demo.descriptors.history.CoronaChartDemoDescriptor
import com.meistercharts.demo.descriptors.history.ExpectedSamplingPeriodDemoDescriptor
import com.meistercharts.demo.descriptors.history.HistoryLayerDemoDescriptor
import com.meistercharts.demo.descriptors.history.HistoryManualDownSamplingDemoDescriptor
import com.meistercharts.demo.descriptors.history.HistoryRecordingDemoDescriptor
import com.meistercharts.demo.descriptors.history.SlowHistoryLayerDemoDescriptor
import com.meistercharts.demo.descriptors.history.TimeLineChartDemoDescriptor
import com.meistercharts.demo.descriptors.history.TimeLineChartGestaltDemoDescriptor
import com.meistercharts.demo.descriptors.history.TimeLineChartGestaltEnumStylesDemoDescriptor
import com.meistercharts.demo.descriptors.history.TimeLineChartGestaltOnlyEnumDemoDescriptor
import com.meistercharts.demo.descriptors.history.TimeLineChartGestaltWithToolbarDemoDescriptor
import com.meistercharts.demo.descriptors.history.TimeLineChartHistoryConfigDemoDescriptor
import com.meistercharts.demo.descriptors.history.TimeLineChartWithOnDemandHistoryConfigurationDemoDescriptor

/**
 * Contains all available charting demo descriptors
 */
object DemoDescriptors {
  /**
   * All charting demo descriptors
   */
  val descriptors: List<ChartingDemoDescriptor<*>> = listOf(
    HelloWorldDemoDescriptor(),
    ContentViewportDemoDescriptor(),
    HistoryReferenceScenariosDemoDescriptor(),
    GestaltLifecycleDemoDescriptor(),
    WithContentViewportDemoDescriptor(),
    ValueAxisContentViewportDemoDescriptor(),
    WhatsAtDemoDescriptor(),
    AutoScaleDemoDescriptor(),
    ThresholdsSupportDemoDescriptor(),
    Base64EncodingDemoDescriptor(),
    FrameTimestampDemoDescriptor(),
    BeamChartGestaltDemoDescriptor(),
    ValueAxisSupportDemoDescriptor(),
    ValueAxisContentViewportCalculationsDemoDescriptor(),
    CategoryAxisSupportDemoDescriptor(),
    SnapTranslationDemoDescriptor(),
    CircularChartGestaltDemoDescriptor(),
    EquisizedBoxLayoutDemoDescriptor(),
    PhysicalSnapDemoDescriptor(),
    PhysicalPixelsTranslateDemo(),
    HistoryEnumLayerDemo(),
    HistoryReferenceEntryLayerDemo(),
    CircularChartGestaltSickDemoDescriptor(),
    LabelPainterDemoDescriptor(),
    StackedBarChartClippingDemoDescriptor(),
    BarChartStackedGestaltDemoDescriptor(),
    GroupedBarChartClippingDemoDescriptor(),
    BarChartStackedLabelDemoDescriptor(),
    BarChartGroupedGestaltDemoDescriptor(),
    BarChartSingleDemoDescriptor(),
    BarValueLabelDemoDescriptor(),
    BarValueLabelManualDemoDescriptor(),
    HistogramDemoDescriptor(),
    NeckarITFlowDemoDescriptor(),
    DirectionalLinesLayerDemoDescriptor(),
    RainSensorGestaltDemoDescriptor(),
    TimeLineChartGestaltEnumStylesDemoDescriptor(),
    TimeLineChartGestaltOnlyEnumDemoDescriptor(),
    ValueAxisWithHudDemoDescriptor(),
    MultiValueAxisWithHudDemoDescriptor(),
    ValueAxisWithHudAndGridDemoDescriptor(),
    ValueAxisHudLayerDemoDescriptor(),
    RainLayerDemoDescriptor(),
    RoofBackgroundLayerDemoDescriptor(),
    PvRoofPlanningDemoDescriptor(),
    UnusableAreaPaintableDemoDescriptor(),
    PvSavingsGestaltDemoDescriptor(),
    VisualizeTouchesDemoDescriptor(),
    TouchSingleTapDemoDescriptor(),
    TouchDoubleTapDemoDescriptor(),
    PvExpensesPerMonthDemoDescriptor(),
    PvProfitDemoDescriptor(),
    LisaChartDemoDescriptor(),
    BulletChartGestaltDemoDescriptor(),
    EasingDemoDescriptor(),
    ClassicCompassGestaltDemoDescriptor(),
    ExtremeValueAxisDemoDescriptor(),
    SymbolAndLabelLegendPaintableDemoDescriptor(),
    SymbolAndLabelLegendHeadlinePaintableDemoDescriptor(),
    VerticallyStackedPaintablesDemoDescriptor(),
    ModernCompassGestaltDemoDescriptor(),
    PuristicCompassGestaltDemoDescriptor(),
    ElevatorGestaltDemoDescriptor(),
    RubberBandZoomDemoDescriptor(),
    TimeLineChartGestaltDemoDescriptor(),
    ExpectedSamplingPeriodDemoDescriptor(),
    TimeLineChartGestaltWithToolbarDemoDescriptor(),
    TimeLineChartHistoryConfigDemoDescriptor(),
    TimeLineChartDemoDescriptor(),
    TimeLineChartDemoRandomDescriptor(),
    TimeLineChartWithOnDemandHistoryConfigurationDemoDescriptor(),
    CoronaChartDemoDescriptor(),
    ScatterPlotLogarithmicAxisDemoDescriptor(),
    CategoryLineChartGestaltDemoDescriptor(),
    CategoryLineChartDemoDescriptor(),
    CategoryLineChartBalloonTooltipDemoDescriptor(),
    CategoryLineChartGestaltWithLinesAndPointsDemoDescriptor(),
    CategoryLinesLayerDemoDescriptor(),
    QRPositionDiagramGestaltDemoDescriptor(),
    TweenDemoDescriptor(),
    AEBPresentationDemoDescriptor(),
    MouseCursorDemoDescriptor(),
    NeckarITOnlySloganDemoDescriptor(),
    GlobalAlphaDemoDescriptor(),
    GreedyCategoryAxisLabelPainterDemo(),
    BarLineShowcaseDemoDescriptor(),
    BarLineShowCaseSickDemoDescriptor(),
    ContentAlwaysVisibleDemoDescriptor(),
    TransformationMatrixDemoDescriptor(),
    ScatterPlotGestaltDemoDescriptor(),
    CorporateDesignDemoDescriptor(),
    ThemeDemoDescriptor(),
    WithAxisOrientationPaintableDemoDescriptor(),
    BinaryGestaltDemoDescriptor(),
    TextBoxPaintableDemoDescriptor(),
    LabelPaintableDemoDescriptor(),
    MapGestaltDemoDescriptor(),
    MapWithNeckarITLocationDemoDescriptor(),
    ColorTintDemoDescriptor(),
    MapWithPaintablesGestaltDemoDescriptor(),
    AngleDirectionDemoDescriptor(),
    LegendLayerDemoDescriptor(),
    StackedPaintablesPaintableDemoDescriptor(),
    As30DiagramDemoDescriptor(),
    ToolbarGestaltDemoDescriptor(),
    TranslationLayerDemoDescriptor(),
    SlippyMapDemoDescriptor(),
    AnchorDemoDescriptor(),
    Anchor2DemoDescriptor(),
    AntiAliasingLinesDemoDescriptor(),
    AntialiasingRectDemoDescriptor(),
    ArrowsDemoDescriptor(),
    SnapDemoDescriptor(),
    SnapTextDemoDescriptor(),
    BarChartLayerDemoDescriptor(),
    BarChartLegendLayerDemoDescriptor(),
    BinaryPainterDemoDescriptor(),
    RectangleEnumStripePainterDemoDescriptor(),
    BooleanEnumStripePainterDemoDescriptor(),
    EnumBarOverTimeDemoDescriptor(),
    ButtonDemoDescriptor(),
    CategoryChartLayerDemoDescriptor(),
    CategoryChartLayerGroupedDemoDescriptor(),
    CategoryChartLayerStackedDemoDescriptor(),
    ChartStateTransformationDemoDescriptor(),
    ZeroLinesLayerDemoDescriptor(),
    CompartmentLayoutDemoDescriptor(),
    LogarithmicValueRangeDemoDescriptor(),
    BarChartStackedSickDemoDescriptor(),
    StackedBarPaintableDemoDescriptor(),
    ResizeByHandlesLayerDemoDescriptor(),
    StackedBarWithLabelDemoDescriptor(),
    PaintablesBoundingBoxDemoDescriptor(),
    PaintableDemoDescriptor(),
    PaintTriangleDemoDescriptor(),
    PaintRightAngledTriangleDemoDescriptor(),
    DiscreteTimelineChartGestaltDemoDescriptor(),
    CanvasLowLevelDemoDescriptor(),
    CircularChartDemoDescriptor(),
    CircularChartLayerDemoDescriptor(),
    CircularChartLegendDemoDescriptor(),
    ValueAxisTopTopTitleLayerDemoDescriptor(),
    CategoryAxisTopTopTitleWithAxisLayerDemoDescriptor(),
    ValueAxisTopTopTitleWithAxisLayerDemoDescriptor(),
    CombinedPaintablesDemoDescriptor(),
    GaugePaintableDemoDescriptor(),
    ComponentsOverlayDemoDescriptor(),
    ConfigurableFontDemoDescriptor(),
    ContentAreaDemoDescriptor(),
    ShadowDemoDescriptor(),
    BalloonTooltipDemoDescriptor(),
    BalloonTooltipLayerDemoDescriptor(),
    ContentAreaLayerDemoDescriptor(),
    ContentAreaFixedSizeDemoDescriptor(),
    LinearColorGradientDemoDescriptor(),
    RadialColorGradientDemoDescriptor(),
    CrossWireLayerDemoDescriptor(),
    CrossWireLayerMovableDemoDescriptor(),
    DateTimeFormatDemoDescriptor(),
    NumberFormatDemoDescriptor(),
    PathWithIntersectDemoDescriptor(),
    DrawingPrimitivesEllipsePathDemoDescriptor(),
    DrawingHatchesDemoDescriptor(),
    DrawingPrimitivesPathDemoDescriptor(),
    DrawingPrimitivesOvalsDemoDescriptor(),
    DrawingPrimitivesArcCenterPathDemoDescriptor(),
    DrawingPrimitivesArcsDemoDescriptor(),
    DrawingPrimitivesArcPathDemoDescriptor(),
    DrawingPrimitivesBezierCurveDemoDescriptor(),
    DrawingPrimitivesPointTypesDemoDescriptor(),
    DrawingPrimitivesQuadraticCurveDemoDescriptor(),
    DrawingPrimitivesRectanglesDemoDescriptor(),
    EnvironmentLayerDemoDescriptor(),
    DevicePixelRatioDemoDescriptor(),
    PhysicalCanvasSizeDemo(),
    ChartSizeClassificationDemo(),
    TouchEventsDemoDescriptor(),
    FontDemoDescriptor(),
    SegmentedLinePainterDemoDescriptor(),
    SplineLineInterpolationPainterDemo(),
    FontAwesomeFontDemoDescriptor(),
    GridDemoDescriptor(),
    GridWithValueAxisDemoDescriptor(),
    GridWithCategoryAxisDemoDescriptor(),
    HideAfterTimeoutLayerDemoDescriptor(),
    SlowHistoryLayerDemoDescriptor(),
    HistoryLayerDemoDescriptor(),
    HistoryRecordingDemoDescriptor(),
    I18nDemoDescriptor(),
    ImageLayerDemoDescriptor(),
    ClearBackgroundLayerDemoDescriptor(),
    FillBackgroundCheckerLayerDemoDescriptor(),
    EventsDebugLayerDemoDescriptor(),
    TranslateOverTimeDemoDescriptor(),
    ImageTranslateRotateLayerDemoDescriptor(),
    InteractionConsumedDemoDescriptor(),
    TimePrecisionDemoDescriptor(),
    InteractiveLayerDemoDescriptor(),
    DragDropDemoDescriptor(),
    CanvasTouchZoomAndPanSupportDemoDescriptor(),
    MouseAndPointerEventsDemoDescriptor(),
    HistoryManualDownSamplingDemoDescriptor(),
    BeamsLayerDemoDescriptor(),
    BeamsLayerWithZonesDemoDescriptor(),
    LayerVisibilityAdapterDemoDescriptor(),
    LayerVisibilityToggleOnShortcutDemoDescriptor(),
    PaintablesLayouterDemoDescriptor(),
    ZoomFitDemoDescriptor(),
    LimitsLayerDemoDescriptor(),
    ThresholdsLayerDemoDescriptor(),
    LineChartLayerDemoDescriptor(),
    LineStyleDemoDescriptor(),
    TextLayerDemoDescriptor(),
    TextLayerI18nDemoDescriptor(),
    TextWithinBoxDemoDescriptor(),
    MouseEventsDemoDescriptor(),
    KeyEventsDemoDescriptor(),
    MultiLineTextDemoDescriptor(),
    MultiLineText2DemoDescriptor(),
    MultiLineText3DemoDescriptor(),
    ZoomAndTranslationDefaults10PctMarginDemoDescriptor(),
    ZoomAndTranslationDefaultsMarginDemoDescriptor(),
    PaletteDemoDescriptor(),
    PasspartoutDemoDescriptor(),
    TargetRefreshRateDemoDescriptor(),
    ClippingLayerDemoDescriptor(),
    PaintableCalculatorDemoDescriptor(),
    CategoryLayoutCalculationDemoDescriptor(),
    PaintPerformanceLayerDemoDescriptor(),
    RoundedRectsDemoDescriptor(),
    RoundedPolylineDemoDescriptor(),
    DelayedWindowSizeStrategyDemoDescriptor(),
    PaintablesPerformanceDemoDescriptor(),
    TextTickAlignmentDemoDescriptor(),
    MirroredPaintableDemoDescriptor(),
    InitialResizeBehaviorDemoDescriptor(),
    FittingWithMarginDemoDescriptor(),
    ScatterPlotLayerDemoDescriptor(),
    ScrollWithoutModifierTextDemoDescriptor(),
    SvgPathsDemoDescriptor(),
    SvgPaintablesDemoDescriptor(),
    TextBoxDemoDescriptor(),
    TextBoxSpecialCasesDemoDescriptor(),
    TextGapDemoDescriptor(),
    TextMaxLengthDemoDescriptor(),
    StringShortenerDemoDescriptor(),
    TextRenderDemoDescriptor(),
    TextWithImageDemoDescriptor(),
    FillRectDemoDescriptor(),
    FillRectExtremeDemoDescriptor(),
    TilesLayerDemoDescriptor(),
    TilesDebugLayerDemoDescriptor(),
    RefreshDemoDescriptor(),
    TilesLayerCachedDemoDescriptor(),
    TileCalculatorDemoDescriptor(),
    TranslatePerformanceDemoDescriptor(),
    BenchmarkDemoDescriptor(),
    CanvasTilesDemoDescriptor(),
    TimeAxisDemoDescriptor(),
    ToolbarDemoDescriptor(),
    TooltipDemoDescriptor(),
    UrlPaintableDemoDescriptor(),
    MissingResourceHandlerDemoDescriptor(),
    PaintPixelPerfectDemoDescriptor(),
    ImageQualityDemoDescriptor(),
    ValueAxisMaxTicksDemoDescriptor(),
    SickValueAxisTitleDemoDescriptor(),
    ValueAxisDemoDescriptor(),
    ValueAxisWithOffsetDemoDescriptor(),
    ValueAxisAllSidesDemoDescriptor(),
    ValueAxisAtContentAreaDemoDescriptor(),
    MultiValueAxisLayerDemoDescriptor(),
    TextsLayerDemoDescriptor(),
    CategoryAxisDemoDescriptor(),
    VersionNumberDemoDescriptor(),
    XyPointPainterDemoDescriptor(),
    PlatformLifecycleDemoDescriptor(),
    LinePainterDemoDescriptor(),
    ZoomLayerDemoDescriptor(),
    NeckarITSloganWithFlowGestaltDemoDescriptor(),
    TankDemoDescriptor(),
    SandboxDemoDescriptor(),
    TimelineChartRestDemoDescriptor(),
  )
}
