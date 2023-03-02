package com.meistercharts.svg

import com.meistercharts.algorithms.painter.Path
import it.neckar.open.annotations.JavaFriendly
import it.neckar.open.kotlin.lang.toRadians
import com.meistercharts.resources.svg.SvgPath
import it.neckar.open.unit.other.deg
import kotlin.jvm.JvmStatic

/**
 * Parser for svg path
 */
class SVGPathParser(private val svgPathAsString: String) {
  /**
   * The length of the path
   */
  private val length: Int
    get() = svgPathAsString.length

  /**
   * The current position when parsing the string
   */
  private var currentPosition: Int = 0

  /**
   * Set to true if commas are allowed
   */
  private var allowComma: Boolean = false

  private val isDone: Boolean
    get() = toNextNonWhitespace() >= length

  /**
   * Returns the current char
   */
  private val char: Char
    get() = svgPathAsString[currentPosition++]

  /**
   * Returns true if the next character is a number
   */
  private fun nextIsNumber(): Boolean {
    if (toNextNonWhitespace() < length) {
      when (svgPathAsString[currentPosition]) {
        '-', '+', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '.' -> return true
      }
    }
    return false
  }

  /**
   * Returns the current value as float
   */
  private fun fValue(): Double {
    return getDouble()
  }

  /**
   * Returns a radians value
   */
  private fun aValue(): Double {
    return getDouble().toRadians()
  }

  /**
   * Returns the double from the current location
   */
  private fun getDouble(): Double {
    val start = toNextNonWhitespace()
    val end = toNumberEnd()
    allowComma = true
    if (start < end) {
      val flstr = svgPathAsString.substring(start, end)
      try {
        return flstr.toDouble()
      } catch (e: NumberFormatException) {
        throw IllegalArgumentException("invalid double ($flstr) in polygon at pos=$start", e)
      }
    }
    throw IllegalArgumentException("end of polygon looking for double")
  }

  /**
   * Returns a boolean value
   */
  private fun bValue(): Boolean {
    toNextNonWhitespace()
    allowComma = true
    if (currentPosition < length) {
      val flag = svgPathAsString[currentPosition]
      when (flag) {
        '0' -> {
          currentPosition++
          return false
        }
        '1' -> {
          currentPosition++
          return true
        }
      }
      throw IllegalArgumentException("invalid boolean flag ($flag) in polygon at pos=$currentPosition")
    }
    throw IllegalArgumentException("end of polygon looking for boolean")
  }

  /**
   * Skips all white spaces
   */
  private fun toNextNonWhitespace(): Int {
    var canBeComma = allowComma
    while (currentPosition < length) {
      when (svgPathAsString[currentPosition]) {
        ','                   -> {
          if (!canBeComma) {
            return currentPosition
          }
          canBeComma = false
        }
        ' ', '\t', '\r', '\n' -> {
        }
        else                  -> return currentPosition
      }
      currentPosition++
    }
    return currentPosition
  }

  private fun toNumberEnd(): Int {
    var allowSign = true
    var hasExp = false
    var hasDecimal = false
    while (currentPosition < length) {
      when (svgPathAsString[currentPosition]) {
        '-', '+'                                         -> {
          if (!allowSign) {
            return currentPosition
          }
          allowSign = false
        }
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> allowSign = false
        'E', 'e'                                         -> {
          if (hasExp) {
            return currentPosition
          }
          run {
            allowSign = true
            hasExp = allowSign
          }
        }
        '.'                                              -> {
          if (hasExp || hasDecimal) {
            return currentPosition
          }
          hasDecimal = true
          allowSign = false
        }
        else                                             -> return currentPosition
      }
      currentPosition++
    }
    return currentPosition
  }

  /**
   * Parses the path
   */
  fun parse(): Path {
    val path = Path()

    this.allowComma = false

    var largeArcFlag = false
    var sweepFlag = false
    var radiusX: Double
    var radiusY: Double
    @deg var xAxisRotation: Double
    var x: Double
    var y: Double
    var lastX = 0.0
    var lastY = 0.0
    var c1x: Double
    var c1y: Double
    var lastC1X = 0.0
    var lastC1Y = 0.0
    var c2x: Double
    var c2y: Double
    var lastC2X = 0.0
    var lastC2Y = 0.0
    var elementCount: Long = 0

    while (!this.isDone) {
      this.allowComma = false
      when (val commandChar: Char = this.char) {
        //Move to absolute
        'M'      -> {
          x = this.fValue()
          y = this.fValue()
          path.moveTo(x, y)
          lastX = x
          lastY = y
          while (this.nextIsNumber()) {
            x = this.fValue()
            y = this.fValue()
            path.lineTo(x, y)
            lastX = x
            lastY = y
          }
          elementCount++
        }
        //Move to relative
        'm'      -> {
          if (elementCount > 0) {
            x = this.fValue() + lastX
            y = this.fValue() + lastY
            path.moveTo(x, y) // move relative
            lastX = x
            lastY = y
          } else {
            x = this.fValue()
            y = this.fValue()
            path.moveTo(x, y)
            lastX = x
            lastY = y
          }
          while (this.nextIsNumber()) {
            x = this.fValue() + lastX
            y = this.fValue() + lastY
            path.lineTo(this.fValue(), this.fValue()) // move relative
            lastX = x
            lastY = y
          }
          elementCount++
        }
        //Line to absolute
        'L'      -> {
          do {
            x = this.fValue()
            y = this.fValue()
            path.lineTo(x, y)
            lastX = x
            lastY = y
          } while (this.nextIsNumber())
          elementCount++
        }
        //Line to relative
        'l'      -> {
          do {
            x = this.fValue() + lastX
            y = this.fValue() + lastY
            path.lineTo(x, y) // move relative
            lastX = x
            lastY = y
          } while (this.nextIsNumber())
          elementCount++
        }
        //Horizontal line to absolute
        'H'      -> {
          do {
            x = this.fValue()
            path.lineTo(x, lastY)
            lastX = x
          } while (this.nextIsNumber())
          elementCount++
        }
        //Horizontal line to relative
        'h'      -> {
          do {
            x = this.fValue() + lastX
            path.lineTo(x, lastY) // move relative
            lastX = x
          } while (this.nextIsNumber())
          elementCount++
        }
        //Vertical line to absolute
        'V'      -> {
          do {
            y = this.fValue()
            path.lineTo(lastX, y)
            lastY = y
          } while (this.nextIsNumber())
          elementCount++
        }
        //Vertical line to relative
        'v'      -> {
          do {
            y = this.fValue() + lastY
            path.lineTo(lastX, y) // move relative
            lastY = y
          } while (this.nextIsNumber())
          elementCount++
        }
        //Quadratic curve to - absolute
        'Q'      -> {
          do {
            c1x = this.fValue()
            c1y = this.fValue()
            x = this.fValue()
            y = this.fValue()
            path.quadraticCurveTo(c1x, c1y, x, y)
            lastC1X = c1x
            lastC1Y = c1y
            lastX = x
            lastY = y
          } while (this.nextIsNumber())
          elementCount++
        }
        //Quadratic curve to - relative
        'q'      -> {
          do {
            c1x = this.fValue() + lastX
            c1y = this.fValue() + lastY
            x = this.fValue() + lastX
            y = this.fValue() + lastY
            path.quadraticCurveTo(c1x, c1y, x, y) // relative move
            lastC1X = c1x
            lastC1Y = c1y
            lastX = x
            lastY = y
          } while (this.nextIsNumber())
          elementCount++
        }
        //Bezier curve to - absolute
        'C'      -> {
          do {
            c1x = this.fValue()
            c1y = this.fValue()
            c2x = this.fValue()
            c2y = this.fValue()
            x = this.fValue()
            y = this.fValue()
            path.bezierCurveTo(c1x, c1y, c2x, c2y, x, y)
            lastC1X = c1x
            lastC1Y = c1y
            lastC2X = c2x
            lastC2Y = c2y
            lastX = x
            lastY = y
          } while (this.nextIsNumber())
          elementCount++
        }
        //Bezier curve to - relative
        'c'      -> {
          do {
            c1x = this.fValue() + lastX
            c1y = this.fValue() + lastY
            c2x = this.fValue() + lastX
            c2y = this.fValue() + lastY
            x = this.fValue() + lastX
            y = this.fValue() + lastY
            path.bezierCurveTo(c1x, c1y, c2x, c2y, x, y) // move relative
            lastC1X = c1x
            lastC1Y = c1y
            lastC2X = c2x
            lastC2Y = c2y
            lastX = x
            lastY = y
          } while (this.nextIsNumber())
          elementCount++
        }

        //Smooth curve to - absolute
        'S'      -> {
          do {
            c2x = this.fValue()
            c2y = this.fValue()
            x = this.fValue()
            y = this.fValue()
            c1x = reflect(lastX, lastC2X)
            c1y = reflect(lastY, lastC2Y)
            path.bezierCurveTo(c1x, c1y, c2x, c2y, x, y)
            lastC1X = c1x
            lastC1Y = c1y
            lastC2X = c2x
            lastC2Y = c2y
            lastX = x
            lastY = y
          } while (this.nextIsNumber())
          elementCount++
        }

        //Smooth curve to - relative
        's'      -> {
          do {
            c2x = this.fValue() + lastX
            c2y = this.fValue() + lastY
            x = this.fValue() + lastX
            y = this.fValue() + lastY
            c1x = reflect(lastX, lastC2X)
            c1y = reflect(lastY, lastC2Y)
            path.bezierCurveTo(c1x, c1y, c2x, c2y, x, y) // move relative
            lastC1X = c1x
            lastC1Y = c1y
            lastC2X = c2x
            lastC2Y = c2y
            lastX = x
            lastY = y
          } while (this.nextIsNumber())
          elementCount++
        }

        //Arc to - absolute
        'A'      -> {
          do {
            radiusX = this.fValue() //radius x
            radiusY = this.fValue() //radius y
            xAxisRotation = this.aValue() //rotation in degrees
            largeArcFlag = this.bValue()
            sweepFlag = this.bValue()
            x = this.fValue()
            y = this.fValue()
            /**
             * https://www.w3.org/TR/SVG/paths.html#PathDataEllipticalArcCommands
             * Draws an arc from the current point to (x,y).
             * radiusX the radius on the x axis
             * radiusY the radius on the y axis
             * largeArc if set to true the larger arc is selected. If set to false the shorter one is selected
             * x the target x coordinate
             * y the target y coordinate
             *
             * Center: The center is calculated automatically
             */
            path.arcTo(radiusX, radiusY, xAxisRotation.toRadians(), largeArcFlag, sweepFlag, x, y);
            lastX = x
            lastY = y
          } while (this.nextIsNumber())
          elementCount++
        }
        //Arc to - relative
        'a'      -> {
          do {
            radiusX = this.fValue()
            radiusY = this.fValue()
            xAxisRotation = this.aValue()
            largeArcFlag = this.bValue()
            sweepFlag = this.bValue()
            x = this.fValue() + lastX
            y = this.fValue() + lastY
            path.arcTo(radiusX, radiusY, xAxisRotation, largeArcFlag, sweepFlag, x, y); //move relative
            lastX = x
            lastY = y
          } while (this.nextIsNumber())
          elementCount++
        }
        'Z', 'z' -> {
          path.closePath()
          lastX = path.currentPoint.x
          lastY = path.currentPoint.y
          elementCount++
        }
        else     -> throw IllegalArgumentException("""invalid command ($commandChar) in SVG polygon at pos=${this.currentPosition}. Path: <$svgPathAsString>""")
      }
      this.allowComma = (false)
    }

    return path
  }

  /**
   * Reflects the value at the anchor point
   */
  private fun reflect(anchorPoint: Double, valueToReflect: Double): Double {
    val delta = valueToReflect - anchorPoint
    return anchorPoint - delta
  }

  companion object {
    fun from(svgPath: SvgPath): SVGPathParser {
      return SVGPathParser(svgPath.value)
    }

    @JavaFriendly
    @JvmStatic
    fun fromString(svgPath: String): SVGPathParser {
      return SVGPathParser(svgPath)
    }
  }
}
