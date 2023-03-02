package com.meistercharts.canvas

import com.meistercharts.algorithms.ChartCalculator
import com.meistercharts.algorithms.painter.BezierCurveTo
import com.meistercharts.algorithms.painter.CanvasPaint
import com.meistercharts.algorithms.painter.Color
import com.meistercharts.algorithms.painter.LineTo
import com.meistercharts.algorithms.painter.MoveTo
import com.meistercharts.algorithms.painter.Path
import com.meistercharts.algorithms.painter.PathAction
import com.meistercharts.algorithms.painter.PathActions
import com.meistercharts.algorithms.painter.QuadraticCurveTo
import com.meistercharts.algorithms.painter.SupportsPathActions
import com.meistercharts.annotations.PhysicalPixel
import com.meistercharts.annotations.Window
import com.meistercharts.annotations.Zoomed
import com.meistercharts.design.corporateDesign
import com.meistercharts.model.Anchoring
import com.meistercharts.model.Coordinates
import com.meistercharts.model.Direction
import com.meistercharts.model.Distance
import com.meistercharts.model.Rectangle
import com.meistercharts.model.RightTriangleType
import com.meistercharts.model.Size
import com.meistercharts.model.Zoom
import it.neckar.open.kotlin.lang.toRadians
import com.meistercharts.style.Shadow
import it.neckar.open.unit.number.MayBeNegative
import it.neckar.open.unit.number.MayBeZero
import it.neckar.open.unit.other.deg
import it.neckar.open.unit.other.pct
import it.neckar.open.unit.other.px
import it.neckar.open.unit.si.rad
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Offers common methods for a charting context
 */
interface CanvasRenderingContext : SupportsPathActions {
  /**
   * Contains the debug configuration for this rendering context
   */
  val debug: DebugConfiguration

  /**
   * Returns the canvas
   */
  val canvas: Canvas

  /**
   * Attention: Creates a new object on every call
   */
  val boundingBox: Rectangle
    get() {
      return Rectangle(0.0, 0.0, width, height)
    }

  /**
   * Returns the canvas size
   */
  @px
  val canvasSize: Size

  /**
   * Returns the width of the canvas
   */
  @px
  val width: Double

  /**
   * Returns the height of the canvas
   */
  @px
  val height: Double

  /**
   * Returns the center of the canvas
   */
  val center: @Window Coordinates
    get() {
      return Coordinates.of(centerX, centerY)
    }

  val centerX: @Window Double
    get() {
      return width / 2.0
    }

  val centerY: @Window Double
    get() {
      return height / 2.0
    }

  /**
   * The global alpha value
   */
  var globalAlpha: @pct Double

  /**
   * The font descriptor
   */
  var font: FontDescriptor

  /**
   * Sets the defaults. This method is called before each paint.
   * It is necessary to set the defaults again, because in HTML the canvas will be reset on resize
   */
  fun applyDefaults() {
    font = corporateDesign.textFont
    lineJoin = LineJoin.Miter
  }

  /**
   * This method only exists so that it can be called from [saved]. Do not call directly
   */
  @Deprecated("use saved{} instead")
  fun save()

  /**
   * This method only exists so that is can be called from [saved]. Do not call directly
   */
  @Deprecated("use saved{} instead")
  fun restore()


  /**
   * Draws an image using the native methods.
   */
  fun paintImage(image: Image, @Window x: Double, @Window y: Double, @Zoomed width: Double, @Zoomed height: Double)

  /**
   * Paints the image exactly on the physical pixel bounds using round().
   * Uses the natural size of the image and ignores the current zoom.
   */
  fun paintImagePixelPerfect(image: Image, @Window x: Double, @Window y: Double)

  fun strokeLine(@Window @px startX: Double, @Window @px startY: Double, @Window @px endX: Double, @Window @px endY: Double)

  /**
   * Schedules the delayed action for later execution. Call [paintDelayed] later to paint the delayed actions.
   * Attention: Each call to this method instantiates a new lambda. Therefore this method should called as infrequently as possible.
   *
   * Delayed actions are *not* restored when [restore] is called
   */
  fun delayed(delayedAction: (gc: CanvasRenderingContext) -> Unit)

  /**
   * Executes all delayed actions.
   * Cleans the delayed actions in the process
   */
  fun paintDelayed()

  /**
   * Cleans the delayed actions
   */
  fun cleanDelayed()

  fun strokeLine(@Window @px start: Coordinates, @Window @px distance: Distance) {
    strokeLine(start.x, start.y, start.x + distance.x, start.y + distance.y)
  }

  fun strokeLine(@Window @px start: Coordinates, @Window @px end: Coordinates) {
    strokeLine(start.x, start.y, end.x, end.y)
  }

  fun fillRectInternal(@Window @px x: Double, @Window @px y: Double, @px @Zoomed width: Double, @px @Zoomed height: Double)

  /**
   * Accepts negative width and height
   */
  fun fillRect(
    @Window @px x: Double,
    @Window @px y: Double,
    @Zoomed @MayBeNegative width: Double,
    @Zoomed @MayBeNegative height: Double,
    xType: LocationType = LocationType.Origin,
    yType: LocationType = LocationType.Origin,
  ) {
    var calculatedWidth = width
    var calculatedX = x

    if (width < 0.0) {
      calculatedWidth = -width
      calculatedX = x - calculatedWidth
    }

    if (xType == LocationType.Center) {
      calculatedX -= width / 2.0
    }

    var calculatedHeight = height
    var calculatedY = y

    if (height < 0.0) {
      calculatedHeight = -height
      calculatedY = y - calculatedHeight
    }

    if (yType == LocationType.Center) {
      calculatedY -= height / 2.0
    }

    fillRectInternal(calculatedX, calculatedY, calculatedWidth, calculatedHeight)
  }

  fun fillRect(@Window @px x: Double, @Window @px y: Double, @px @Zoomed @MayBeNegative size: Size) {
    fillRect(x, y, size.width, size.height)
  }

  fun fillRect(@Window @px location: Coordinates, @px @Zoomed @MayBeNegative size: Size) {
    fillRect(location.x, location.y, size.width, size.height)
  }

  fun fillRect(@Window @px @MayBeNegative bounds: Rectangle) {
    fillRect(bounds.left, bounds.top, bounds.widthAbs, bounds.heightAbs)
  }

  fun strokeRect(@Window @px x: Double, @Window @px y: Double, @MayBeNegative @px @Zoomed width: Double, @MayBeNegative @px @Zoomed height: Double, strokeLocation: StrokeLocation = StrokeLocation.Center) {
    var calculatedWidth = width
    var calculatedX = x

    if (width < 0.0) {
      calculatedWidth = -width
      calculatedX = x - calculatedWidth
    }

    var calculatedHeight = height
    var calculatedY = y

    if (height < 0.0) {
      calculatedHeight = -height
      calculatedY = y - calculatedHeight
    }

    //Fit the rect within
    when (strokeLocation) {
      StrokeLocation.Center -> {
      }

      StrokeLocation.Inside -> {
        val currentLineWidth = lineWidth

        calculatedX += currentLineWidth / 2.0
        calculatedY += currentLineWidth / 2.0

        calculatedWidth -= currentLineWidth
        calculatedHeight -= currentLineWidth
      }

      StrokeLocation.Outside -> {
        val currentLineWidth = lineWidth

        calculatedX -= currentLineWidth / 2.0
        calculatedY -= currentLineWidth / 2.0

        calculatedWidth += currentLineWidth
        calculatedHeight += currentLineWidth
      }
    }

    strokeRectInternal(calculatedX, calculatedY, calculatedWidth, calculatedHeight)
  }

  fun strokeRectInternal(@Window @px x: Double, @Window @px y: Double, @px @Zoomed width: Double, @px @Zoomed height: Double)

  fun strokeRect(@Window @px location: Coordinates, @px @Zoomed size: Size, strokeLocation: StrokeLocation = StrokeLocation.Center) {
    strokeRect(location.x, location.y, size.width, size.height, strokeLocation)
  }

  fun strokeRect(@Window @px bounds: Rectangle, strokeLocation: StrokeLocation = StrokeLocation.Center) {
    strokeRect(bounds.left, bounds.top, bounds.widthAbs, bounds.heightAbs, strokeLocation)
  }

  /**
   * Accepts negative width and height
   */
  fun fillTriangleRightAngled(
    @Window @px x: Double,
    @Window @px y: Double,
    @Zoomed @MayBeNegative width: Double,
    @Zoomed @MayBeNegative height: Double,
    rightTriangleType: RightTriangleType,
    xLocationType: LocationType = LocationType.Origin,
    yLocationType: LocationType = LocationType.Origin,
  ) {
    triangleRightAngledPath(x, y, width, height, rightTriangleType, xLocationType, yLocationType)
    fill()
  }

  /**
   * Appends the path for a triangle with right angle
   */
  fun triangleRightAngledPath(
    x: @px Double,
    y: @px Double,
    width: @px Double,
    height: @px Double,
    rightTriangleType: RightTriangleType,
    xLocationType: LocationType = LocationType.Origin,
    yLocationType: LocationType = LocationType.Origin,
  ) {
    var calculatedWidth = width
    var calculatedX = x

    if (width < 0.0) {
      calculatedWidth = -width
      calculatedX = x - calculatedWidth
    }

    if (xLocationType == LocationType.Center) {
      calculatedX -= width / 2.0
    }

    var calculatedHeight = height
    var calculatedY = y

    if (height < 0.0) {
      calculatedHeight = -height
      calculatedY = y - calculatedHeight
    }

    if (yLocationType == LocationType.Center) {
      calculatedY -= height / 2.0
    }


    //Top right
    val topRightX = calculatedX + calculatedWidth
    val topRightY = calculatedY

    //bottom right
    val bottomRightX = calculatedX + calculatedWidth
    val bottomRightY = calculatedY + calculatedHeight

    //bottom left
    val bottomLeftX = calculatedX
    val bottomLeftY = calculatedY + calculatedHeight

    //top left
    val topLeftX = calculatedX
    val topLeftY = calculatedY


    beginPath()

    when (rightTriangleType) {
      RightTriangleType.MissingCornerInFirstQuadrant -> {
        moveTo(topLeftX, topLeftY)
        lineTo(bottomRightX, bottomRightY)
        lineTo(bottomLeftX, bottomLeftY)
        closePath()
      }

      RightTriangleType.MissingCornerInSecondQuadrant -> {
        moveTo(topLeftX, topLeftY)
        lineTo(topRightX, topRightY)
        lineTo(bottomLeftX, bottomLeftY)
        closePath()
      }

      RightTriangleType.MissingCornerInThirdQuadrant -> {
        moveTo(topLeftX, topLeftY)
        lineTo(topRightX, topRightY)
        lineTo(bottomRightX, bottomRightY)
        closePath()
      }

      RightTriangleType.MissingCornerInFourthQuadrant -> {
        moveTo(topRightX, topRightY)
        lineTo(bottomRightX, bottomRightY)
        lineTo(bottomLeftX, bottomLeftY)
        closePath()
      }
    }
  }

  fun fillTriangle(x1: @Window @px Double, y1: @Window @px Double, x2: @Window @px Double, y2: @Window @px Double, x3: @Window @px Double, y3: @Window @px Double) {
    beginPath()
    moveTo(x1, y1)
    lineTo(x2, y2)
    lineTo(x3, y3)
    closePath()
    fill()
  }

  /**
   * Strokes a triangle that has a right angle
   */
  fun strokeTriangleRightAngled(
    @Window @px x: Double,
    @Window @px y: Double,
    @MayBeNegative @px @Zoomed width: Double,
    @MayBeNegative @px @Zoomed height: Double,
    rightTriangleType: RightTriangleType,
    xLocationType: LocationType = LocationType.Origin,
    yLocationType: LocationType = LocationType.Origin,
  ) {

    triangleRightAngledPath(
      x = x, y = y, width = width, height = height,
      rightTriangleType = rightTriangleType, xLocationType = xLocationType, yLocationType = yLocationType
    )
    stroke()
  }

  fun strokeTriangle(x1: @Window @px Double, y1: @Window @px Double, x2: @Window @px Double, y2: @Window @px Double, x3: @Window @px Double, y3: @Window @px Double) {
    beginPath()
    moveTo(x1, y1)
    lineTo(x2, y2)
    lineTo(x3, y3)
    closePath()
    stroke()
  }

  /**
   * Strokes an oval
   * @param x of the upper left corner
   * @param y of the upper left corner
   */
  fun strokeOvalOrigin(@Window @px x: Double, @Window @px y: Double, @px @Zoomed width: Double, @px @Zoomed height: Double)

  fun strokeOvalOrigin(@Window origin: Coordinates, @px @Zoomed size: Size) {
    strokeOvalOrigin(origin.x, origin.y, size.width, size.height)
  }

  /**
   * Appends an oval to the current path
   */
  fun ovalCenter(@Window @px centerX: Double, @Window @px centerY: Double, @px @Zoomed width: Double, @px @Zoomed height: Double)

  /**
   * Strokes an oval
   * @param x of the center
   * @param y of the center
   */
  fun strokeOvalCenter(@Window @px x: Double, @Window @px y: Double, @px @Zoomed width: Double, @px @Zoomed height: Double)

  fun strokeOvalCenter(@Window center: Coordinates, @px @Zoomed size: Size) {
    strokeOvalCenter(center.x, center.y, size.width, size.height)
  }

  fun strokeOvalCenter(@Window center: Coordinates, @px @Zoomed diameter: Double) {
    strokeOvalCenter(center.x, center.y, diameter, diameter)
  }

  /**
   * Fills an oval
   * @param x of the upper left corner
   * @param y of the upper left corner
   */
  fun fillOvalOrigin(@Window @px x: Double, @Window @px y: Double, @px @Zoomed width: Double, @px @Zoomed height: Double)

  fun fillOvalOrigin(@Window origin: Coordinates, @px @Zoomed size: Size) {
    fillOvalOrigin(origin.x, origin.y, size.width, size.height)
  }

  /**
   * Fills an oval
   * @param x of the center
   * @param y of the center
   */
  fun fillOvalCenter(@Window @px x: Double, @Window @px y: Double, @px @Zoomed width: Double, @px @Zoomed height: Double = width)

  fun fillOvalCenter(@Window center: Coordinates, @px @Zoomed size: Size) {
    fillOvalCenter(center.x, center.y, size.width, size.height)
  }

  fun fillOvalCenter(@Window center: Coordinates, @px @Zoomed diameter: Double) {
    fillOvalCenter(center.x, center.y, diameter, diameter)
  }

  /**
   * Strokes an arc.
   * Starts at the right side (3 o'clock) and draws clockwise
   */
  fun strokeArcCenter(@Window x: Double, @Window y: Double, @Zoomed radius: Double, @rad startAngle: Double, @rad arcExtent: Double, arcType: ArcType)

  /**
   * Strokes an arc.
   * Starts at the right side (3 o'clock) and draws clockwise
   */
  fun strokeArcCenter(@Window center: Coordinates, @Zoomed radius: Double, @rad startAngle: Double, @rad arcExtent: Double, arcType: ArcType) {
    strokeArcCenter(center.x, center.y, radius, startAngle, arcExtent, arcType)
  }

  /**
   * Fills an arc.
   * Starts at the right side (3 o'clock) and draws clockwise
   */
  fun fillArcCenter(@Window x: Double, @Window y: Double, @Zoomed radius: Double, @rad startAngle: Double, @rad arcExtent: Double, arcType: ArcType)

  /**
   * Fills an arc.
   * Starts at the right side (3 o'clock) and draws clockwise
   */
  fun fillArcCenter(@Window center: Coordinates, @Zoomed radius: Double, @rad startAngle: Double, @rad arcExtent: Double, arcType: ArcType) {
    fillArcCenter(center.x, center.y, radius, startAngle, arcExtent, arcType)
  }

  /**
   * Stroke the current path
   */
  fun stroke()

  /**
   * Stroke the given path
   */
  fun stroke(pathActions: PathActions) {
    applyPathActions(pathActions)
    stroke()
  }

  /**
   * Activates the shadow
   */
  fun shadow(
    color: Color = Shadow.Default.color,
    blurRadius: @px Double = Shadow.Default.blurRadius,
    offsetX: @px Double = Shadow.Default.offsetX,
    offsetY: @px Double = Shadow.Default.offsetY,
  )

  /**
   * Activates the shadow
   */
  fun shadow(shadow: Shadow = Shadow.Default) {
    shadow(shadow.color, shadow.blurRadius, shadow.offsetX, shadow.offsetY)
  }

  /**
   * Clears the shadow
   */
  fun clearShadow()

  /**
   * Applies the given path actions
   */
  fun applyPathActions(pathActions: PathActions) {
    beginPath()
    pathActions.actions.forEach {
      pathAction(it)
    }
  }

  /**
   * Applies a single path action
   */
  fun pathAction(action: PathAction) {
    when (action) {
      is MoveTo -> moveTo(action.endPointX, action.endPointY)
      is LineTo -> lineTo(action.endPointX, action.endPointY)
      is QuadraticCurveTo -> quadraticCurveTo(action.controlX, action.controlY, action.endPointX, action.endPointY)
      is BezierCurveTo -> bezierCurveTo(action.control1X, action.control1Y, action.control2X, action.control2Y, action.endPointX, action.endPointY)
      else -> throw UnsupportedOperationException("not supported yet <$action>")
    }
  }

  /**
   * Applies a single path action - but uses factors for the x and y coordinates
   */
  fun pathAction(action: PathAction, factorX: Double, factorY: Double) {
    when (action) {
      is MoveTo -> moveTo(action.endPointX * factorX, action.endPointY * factorY)
      is LineTo -> lineTo(action.endPointX * factorX, action.endPointY * factorY)
      is QuadraticCurveTo -> quadraticCurveTo(action.controlX * factorX, action.controlY * factorY, action.endPointX * factorX, action.endPointY * factorY)
      is BezierCurveTo -> bezierCurveTo(action.control1X * factorX, action.control1Y * factorY, action.control2X * factorX, action.control2Y * factorY, action.endPointX * factorX, action.endPointY * factorY)
      else -> throw UnsupportedOperationException("not supported yet <$action>")
    }
  }

  /**
   * Fill the current path
   */
  fun fill()

  /**
   * Fill the given path
   */
  fun fill(pathActions: PathActions) {
    applyPathActions(pathActions)
    fill()
  }

  /**
   * Translates the canvas
   */
  fun translate(deltaX: Double, deltaY: Double)

  fun translate(distance: Distance) {
    translate(distance.x, distance.y)
  }

  /**
   * Translates by the physical pixel delta (does *not* respect the current [scale])
   */
  fun translatePhysical(deltaX: @PhysicalPixel Double, deltaY: @PhysicalPixel Double)

  /**
   * Translates the canvas to the center
   */
  fun translateToCenter() {
    translate(width / 2.0, height / 2.0)
  }

  fun translateToBottomLeft() {
    translate(0.0, height)
  }

  fun translateToCenterX() {
    translate(width / 2.0, 0.0)
  }

  fun translateToCenterY() {
    translate(0.0, height / 2.0)
  }

  /**
   * Rotates the canvas *clockwise*
   *
   * For performance reasons [rotateRadians] with radians parameter should be preferred
   */
  fun rotateDegrees(angleInDegrees: @deg Double) {
    rotateRadians(angleInDegrees.toRadians())
  }

  /**
   * Rotates the canvas *clockwise*
   */
  fun rotateRadians(angleInRadians: @rad Double)

  /**
   * Adds a scaling transformation.
   * @param x Scaling factor in the horizontal direction. A value of 1 results in no horizontal scaling.
   * @param y Scaling factor in the vertical direction.  A value of 1 results in no vertical scaling.
   */
  fun scale(x: Double, y: Double)

  /**
   * Fills the text relative to the given anchor direction.
   * Applies a gap to one or both coordinates depending on anchor direction value
   *
   * If maxWidth/maxHeight is too
   */
  fun fillText(
    text: String,
    x: @Window Double,
    y: @Window Double,
    anchorDirection: Direction,
    gapHorizontal: @Zoomed Double = 0.0,
    gapVertical: @Zoomed Double = gapHorizontal,
    maxWidth: @Zoomed @MayBeZero Double? = null,
    maxHeight: @Zoomed @MayBeZero Double? = null,
    stringShortener: CanvasStringShortener = CanvasStringShortener.exactButSlowTruncateToLength,
  )

  /**
   * Fills the text relative to the given anchor and direction.
   * Keeps the text within the provided box
   */
  fun fillTextWithin(
    text: String,
    x: @Window Double,
    y: @Window Double,
    anchorDirection: Direction,
    gapHorizontal: @Zoomed Double = 0.0,
    gapVertical: @Zoomed Double = gapHorizontal,
    boxX: @Window Double,
    boxY: @Window Double,
    boxWidth: @Zoomed Double,
    boxHeight: @Zoomed Double,
    stringShortener: CanvasStringShortener = CanvasStringShortener.exactButSlowTruncateToLength,
  )

  fun fillText(text: String, anchoring: @Window Anchoring, maxWidth: @Zoomed Double? = null, maxHeight: @Zoomed Double? = null, stringShortener: CanvasStringShortener = CanvasStringShortener.exactButSlowTruncateToLength) {
    fillText(text, anchoring.anchor, anchoring.anchorDirection, anchoring.gapHorizontal, anchoring.gapVertical, maxWidth, maxHeight, stringShortener)
  }

  fun fillText(
    text: String,
    location: @Window Coordinates,
    direction: Direction,
    gapHorizontal: @Zoomed Double = 0.0,
    gapVertical: @Zoomed Double = gapHorizontal,
    maxWidth: @Zoomed @MayBeZero Double? = null,
    maxHeight: @Zoomed @MayBeZero Double? = null,
    stringShortener: CanvasStringShortener = CanvasStringShortener.exactButSlowTruncateToLength,
  ) {
    fillText(text, location.x, location.y, direction, gapHorizontal, gapVertical, maxWidth, maxHeight, stringShortener)
  }

  /**
   * Strokes the text relative to the given anchor direction
   */
  fun strokeText(
    text: String,
    x: Double,
    y: Double,
    anchorDirection: Direction,
    gapHorizontal: @Zoomed Double = 0.0,
    gapVertical: @Zoomed Double = gapHorizontal,
    maxWidth: @Zoomed @MayBeZero Double? = null,
    maxHeight: @Zoomed @MayBeZero Double? = null,
    stringShortener: CanvasStringShortener = CanvasStringShortener.exactButSlowTruncateToLength,
  )

  fun strokeText(
    text: String,
    anchoring: @Window Anchoring,
    maxWidth: @Zoomed Double? = null,
    maxHeight: @Zoomed Double? = null,
    stringShortener: CanvasStringShortener = CanvasStringShortener.exactButSlowTruncateToLength,
  ) {
    strokeText(
      text = text,
      location = anchoring.anchor,
      direction = anchoring.anchorDirection,
      gapHorizontal = anchoring.gapHorizontal,
      gapVertical = anchoring.gapVertical,
      maxWidth = maxWidth,
      maxHeight = maxHeight,
      stringShortener = stringShortener
    )
  }

  fun strokeText(
    text: String,
    location: @Window Coordinates,
    direction: Direction,
    gapHorizontal: @Zoomed Double = 0.0,
    gapVertical: @Zoomed Double = gapHorizontal,
    maxWidth: @Zoomed @MayBeZero Double? = null,
    maxHeight: @Zoomed @MayBeZero Double? = null,
    stringShortener: CanvasStringShortener = CanvasStringShortener.exactButSlowTruncateToLength,
  ) {
    strokeText(
      text = text,
      x = location.x,
      y = location.y,
      anchorDirection = direction,
      gapHorizontal = gapHorizontal,
      gapVertical = gapVertical,
      maxWidth = maxWidth,
      maxHeight = maxHeight,
      stringShortener = stringShortener
    )
  }

  /**
   * Calculates the width of [text] using the current font.
   * @see calculateTextSize
   */
  fun calculateTextWidth(text: String): @px Double

  /**
   * Calculates the size of [text] using the current font.
   * @see calculateTextWidth
   */
  fun calculateTextSize(text: String): @px Size

  /**
   * Adds the rect to the current path
   */
  fun rect(@px x: Double, @px y: Double, width: @MayBeNegative Double, height: @MayBeNegative Double)

  /**
   * Clears the given rectangle.
   *
   * ATTENTION: Do *not* use this method to clear the complete canvas: use [ChartSupport.markAsDirty] instead.
   */
  fun clearRect(x: @px Double, y: @px Double, width: @px Double, height: @px Double)

  /**
   * Applies the font descriptor fragment
   */
  fun font(fontFragment: FontDescriptorFragment) {
    if (fontFragment.isEmpty()) {
      //do no change anything
      return
    }

    font = font.combineWith(fontFragment)
  }

  /**
   * Sets the color used for strokes
   */
  fun strokeStyle(color: CanvasPaint)

  /**
   * Sets the stroke style
   */
  fun stroke(color: CanvasPaint) {
    strokeStyle(color)
  }

  /**
   * Sets the color used to fill the drawing
   */
  fun fillStyle(color: CanvasPaint)

  fun fill(color: CanvasPaint) {
    fillStyle(color)
  }

  /**
   * Sets both fill and stroke
   */
  fun fillAndStroke(fill: CanvasPaint, stroke: CanvasPaint) {
    fillStyle(fill)
    strokeStyle(stroke)
  }

  /**
   * Sets the line-dash pattern.
   * @param dashes a list of numbers that specifies distances to alternately draw a line and a gap.
   * If the number of elements in the array is odd, the elements of the array get copied and concatenated.
   * For example, `[5, 15, 25]` will become `[5, 15, 25, 5, 15, 25]`.
   * An empty array clears the dashes, so that a solid line will be drawn.
   */
  fun setLineDash(@px vararg dashes: Double)

  /**
   * Clears the line dash
   */
  fun clearLineDash() {
    setLineDash()
  }

  /**
   * Returns the font metrics for the current font.
   */
  fun getFontMetrics(): FontMetrics

  /**
   * Returns the font alignment correction value.
   * This value is required for Top/Center because the canvas does not align the fonts properly in all cases.
   */
  fun getFontAlignmentCorrection(anchorDirection: Direction): @px Double {
    return getFontMetrics().alignmentCorrectionInformation[anchorDirection.verticalAlignment]
  }

  /**
   * Resets the current transformation
   */
  fun resetTransform()

  /**
   * Sets the clip
   */
  fun clip(@px x: Double, @px y: Double, @px width: Double, @px height: Double)

  /**
   * Clears the complete canvas (physical size)
   */
  fun clear() {
    clearRect(-translationX, -translationY, canvas.physicalWidth / scaleX, canvas.physicalHeight / scaleY)
  }

  /**
   * Snaps the x/y-translation to the nearest integer value (for physical pixels!).
   *
   * The additional value can be used to snap to `*.5` by providing `0.5`.
   *
   * The additional values should be <1.0. They are only applied when [snapX] or [snapY] are set to true.
   */
  fun snapPhysicalTranslation(additionalValueX: @PhysicalPixel Double = 0.0, additionalValueY: @PhysicalPixel Double = 0.0, snapX: Boolean = true, snapY: Boolean = true)

  /**
   * Creates a pattern
   */
  fun pattern(patternCanvas: Canvas)

  /**
   * Calculates a x value that should be added to the current translation to snap to the physical pixel on the x-axis
   */
  fun calculatePhysicalSnapCorrectionX(translationX: Double, snapX: Boolean = true): @px Double

  /**
   * Calculates a y value that should be added to the current translation to snap to the physical pixel on the y-axis
   */
  fun calculatePhysicalSnapCorrectionY(translationY: Double, snapY: Boolean = true): @px Double

  @px
  var lineWidth: Double

  var lineJoin: LineJoin

  /**
   * The current translation
   */
  var translation: @Zoomed Distance

  /**
   * The current translation in physical pixels (without scale)
   */
  val translationPhysical: @PhysicalPixel Distance
  val translationPhysicalX: @PhysicalPixel Double
  val translationPhysicalY: @PhysicalPixel Double

  /**
   * The translation on the x axis
   */
  val translationX: @Zoomed Double

  /**
   * The translation on the y axis
   */
  val translationY: @Zoomed Double

  /**
   * Returns the native translation as provided by the native graphics context.
   * Only for debugging purposes. Should not be used in production code - for performance reasons
   */
  @Deprecated("Only for debugging purposes")
  val nativeTranslation: Distance?

  /**
   * The current scale. The default might be different from 1.0/1.0 due to the device pixel ratio (see [com.meistercharts.algorithms.Environment.devicePixelRatio])
   *
   * The initial scale is configured in the method [applyDefaults]
   */
  var scale: Zoom

  /**
   * The currently configured scale
   */
  var scaleX: Double
  var scaleY: Double
}

/**
 * The join style for lines
 */
enum class LineJoin {

  /**
   * Joins path segments by extending their outside edges until they meet.
   **/
  Miter,

  /**
   * Joins path segments by connecting the outer corners
   * of their wide outlines with a straight segment.
   */
  Bevel,

  /**
   * Joins path segments by rounding off the corner
   * at a radius of half the line width.
   */
  Round
}

/**
 * Type of an arc
 */
enum class ArcType {
  /**
   * The closure type for an open arc with no path segments connecting
   * the two ends of the arc segment.
   */
  Open,

  /**
   * The closure type for an arc closed by drawing a straight line segment
   * from the start of the arc segment to the end of the arc segment.
   */
  @Deprecated("Currently only open is supported in HTML")
  Chord,

  /**
   * The closure type for an arc closed by drawing straight line segments
   * from the start of the arc segment to the center of the full ellipse
   * and from that point to the end of the arc segment.
   */
  Round
}

/**
 * Where the stroke is located (on a rect)
 */
enum class StrokeLocation {
  /**
   * The line is located on the center of the shape.
   * Half the line is painted outside, half inside of the shape
   */
  Center,

  /**
   * The line is painted *inside* a given shape.
   */
  Inside,

  /**
   * The line is painted *outside* a given shape.
   */
  Outside
}

/**
 * Describes how a coordinate is interpreted
 */
enum class LocationType {
  /**
   * The coordinate represents the origin (e.g. of a rectangle)
   */
  Origin,

  /**
   * The coordinate represents the center (e.g. of a rectangle)
   */
  Center
}

/**
 * Calls the given action with a saved context
 */
inline fun CanvasRenderingContext.saved(action: (gc: CanvasRenderingContext) -> Unit) {
  contract {
    callsInPlace(action, InvocationKind.EXACTLY_ONCE)
  }

  //Performance considerations
  //Performance test proved, that it is not (much) better to save just the transformation matrix and
  //reset the values later.
  //Just keep using the native save functions from the context

  @Suppress("DEPRECATION")
  save()
  try {
    action(this)
  } finally {
    @Suppress("DEPRECATION")
    restore()
  }
}

/**
 * Strokes this path on the given context
 */
fun Path.stroke(gc: CanvasRenderingContext, x: @Zoomed Double = 0.0, y: @Zoomed Double = 0.0) {
  gc.saved {
    gc.translate(x, y)
    gc.stroke(this)
  }
}

/**
 * Fills this path on the given context
 */
fun Path.fill(gc: CanvasRenderingContext, x: @Zoomed Double = 0.0, y: @Zoomed Double = 0.0) {
  gc.saved {
    gc.translate(x, y)
    gc.fill(this)
  }
}

/**
 * Converts an (absolute) value into a value that can be used with the *current* translation of this rendering context.
 *
 * This method can be very useful, when absolute values (e.g. @ContentAreaRelative) must be used with the currently translated rendering context.
 *
 * Attention: Currently, does *NOT* respect scaling!
 */
fun CanvasRenderingContext.forTranslationX(absoluteValueX: @Zoomed Double): @Zoomed Double {
  return absoluteValueX - translationX
}

/**
 * Converts an (absolute) value into a value that can be used with the *current* translation of this rendering context.
 *
 * This method can be very useful, when absolute values (e.g. @ContentAreaRelative) must be used with the currently translated rendering context.
 *
 * Attention: Currently, does *NOT* respect scaling!
 */
fun CanvasRenderingContext.forTranslationY(absoluteValueY: @Zoomed Double): @Zoomed Double {
  return absoluteValueY - translationY
}

/**
 * Clips the rendering context to the viewport provided by the chart calculator.
 *
 * ATTENTION: This methods must not be used for layers that paint labels. These labels can be painted outside of the content viewport.
 */
fun CanvasRenderingContext.clipToContentViewport(chartCalculator: ChartCalculator) {
  clip(chartCalculator.contentViewportMinX(), chartCalculator.contentViewportMinY(), chartCalculator.contentViewportWidth, chartCalculator.contentViewportHeight)
}
