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
package com.meistercharts.canvas.geometry

import it.neckar.open.unit.si.rad
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.sin

data class Matrix(
  var a: Double = 1.0,
  var b: Double = 0.0,
  var c: Double = 0.0,
  var d: Double = 1.0,
  var tx: Double = 0.0,
  var ty: Double = 0.0
) : MutableInterpolable<Matrix>, Interpolable<Matrix> {

  companion object {
    operator fun invoke(a: Float, b: Float = 0f, c: Float = 0f, d: Float = 1f, tx: Float = 0f, ty: Float = 0f): Matrix =
      Matrix(a.toDouble(), b.toDouble(), c.toDouble(), d.toDouble(), tx.toDouble(), ty.toDouble())

    operator fun invoke(a: Int, b: Int = 0, c: Int = 0, d: Int = 1, tx: Int = 0, ty: Int = 0): Matrix =
      Matrix(a.toDouble(), b.toDouble(), c.toDouble(), d.toDouble(), tx.toDouble(), ty.toDouble())

    operator fun invoke(m: Matrix, out: Matrix = Matrix()): Matrix = out.copyFrom(m)
  }

  enum class Type(val id: Int, val hasRotation: Boolean, val hasScale: Boolean, val hasTranslation: Boolean) {
    IDENTITY(1, hasRotation = false, hasScale = false, hasTranslation = false),
    TRANSLATE(2, hasRotation = false, hasScale = false, hasTranslation = true),
    SCALE(3, hasRotation = false, hasScale = true, hasTranslation = false),
    SCALE_TRANSLATE(4, hasRotation = false, hasScale = true, hasTranslation = true),
    COMPLEX(5, hasRotation = true, hasScale = true, hasTranslation = true);
  }

  fun getType(): Type {
    val hasRotation = b != 0.0 || c != 0.0
    val hasScale = a != 1.0 || d != 1.0
    val hasTranslation = tx != 0.0 || ty != 0.0

    return when {
      hasRotation -> Type.COMPLEX
      hasScale && hasTranslation -> Type.SCALE_TRANSLATE
      hasScale -> Type.SCALE
      hasTranslation -> Type.TRANSLATE
      else -> Type.IDENTITY
    }
  }

  fun setTo(a: Double, b: Double, c: Double, d: Double, tx: Double, ty: Double): Matrix = this.apply {
    this.a = a
    this.b = b
    this.c = c
    this.d = d
    this.tx = tx
    this.ty = ty
  }

  fun setTo(a: Float, b: Float, c: Float, d: Float, tx: Float, ty: Float): Matrix = setTo(a.toDouble(), b.toDouble(), c.toDouble(), d.toDouble(), tx.toDouble(), ty.toDouble())
  fun setTo(a: Int, b: Int, c: Int, d: Int, tx: Int, ty: Int): Matrix = setTo(a.toDouble(), b.toDouble(), c.toDouble(), d.toDouble(), tx.toDouble(), ty.toDouble())

  fun copyFrom(that: Matrix): Matrix {
    setTo(that.a, that.b, that.c, that.d, that.tx, that.ty)
    return this
  }

  fun rotate(theta: @rad Double): Matrix = this.apply {
    val cos = cos(theta)
    val sin = sin(theta)

    val a1 = a * cos - b * sin
    b = (a * sin + b * cos)
    a = a1

    val c1 = c * cos - d * sin
    d = (c * sin + d * cos)
    c = c1

    val tx1 = tx * cos - ty * sin
    ty = (tx * sin + ty * cos)
    tx = tx1
  }

  fun skew(skewX: Double, skewY: Double): Matrix {
    val sinX = sin(skewX)
    val cosX = cos(skewX)
    val sinY = sin(skewY)
    val cosY = cos(skewY)

    return this.setTo(
      a * cosY - b * sinX,
      a * sinY + b * cosX,
      c * cosY - d * sinX,
      c * sinY + d * cosX,
      tx * cosY - ty * sinX,
      tx * sinY + ty * cosX
    )
  }

  fun scale(sx: Double, sy: Double = sx): Matrix = setTo(a * sx, b * sx, c * sy, d * sy, tx * sx, ty * sy)
  fun prescale(sx: Double, sy: Double = sx): Matrix = setTo(a * sx, b * sx, c * sy, d * sy, tx, ty)
  fun translate(dx: Double, dy: Double): Matrix = this.apply { this.tx += dx; this.ty += dy }
  fun pretranslate(dx: Double, dy: Double): Matrix = this.apply { tx += a * dx + c * dy; ty += b * dx + d * dy }

  fun prerotate(theta: @rad Double): Matrix = this.apply {
    val m = Matrix()
    m.rotate(theta)
    this.premultiply(m)
  }

  fun preskew(skewX: Double, skewY: Double): Matrix = this.apply {
    val m = Matrix()
    m.skew(skewX, skewY)
    this.premultiply(m)
  }

  fun premultiply(m: Matrix): Matrix = this.premultiply(m.a, m.b, m.c, m.d, m.tx, m.ty)

  fun premultiply(la: Double, lb: Double, lc: Double, ld: Double, ltx: Double, lty: Double): Matrix = setTo(
    la * a + lb * c,
    la * b + lb * d,
    lc * a + ld * c,
    lc * b + ld * d,
    ltx * a + lty * c + tx,
    ltx * b + lty * d + ty
  )

  fun multiply(l: Matrix, r: Matrix): Matrix = setTo(
    l.a * r.a + l.b * r.c,
    l.a * r.b + l.b * r.d,
    l.c * r.a + l.d * r.c,
    l.c * r.b + l.d * r.d,
    l.tx * r.a + l.ty * r.c + r.tx,
    l.tx * r.b + l.ty * r.d + r.ty
  )

  /** Transform point without translation */
  //fun deltaTransformPoint(point: Point) = Point(point.x * a + point.y * c, point.x * b + point.y * d)

  /**
   * Resets the matrix to the identity matrix
   */
  fun reset(): Matrix = setTo(1.0, 0.0, 0.0, 1.0, 0.0, 0.0)

  fun invert(matrixToInvert: Matrix = this): Matrix {
    val src = matrixToInvert
    val dst = this
    val norm = src.a * src.d - src.b * src.c

    if (norm == 0.0) {
      dst.setTo(0.0, 0.0, 0.0, 0.0, -src.tx, -src.ty)
    } else {
      val inorm = 1.0 / norm
      val d = src.a * inorm
      val a = src.d * inorm
      val b = src.b * -inorm
      val c = src.c * -inorm
      dst.setTo(a, b, c, d, -a * src.tx - c * src.ty, -b * src.tx - d * src.ty)
    }

    return this
  }

  fun inverted(out: Matrix = Matrix()): Matrix = out.invert(this)

  fun setTransform(
    x: Double,
    y: Double,
    scaleX: Double,
    scaleY: Double,
    rotation: @rad Double,
    skewX: Double,
    skewY: Double
  ): Matrix {
    if (skewX == 0.0 && skewY == 0.0) {
      if (rotation == 0.0) {
        this.setTo(scaleX, 0.0, 0.0, scaleY, x, y)
      } else {
        val cos = cos(rotation)
        val sin = sin(rotation)
        this.setTo(cos * scaleX, sin * scaleY, -sin * scaleX, cos * scaleY, x, y)
      }
    } else {
      this.reset()
      scale(scaleX, scaleY)
      skew(skewX, skewY)
      rotate(rotation)
      translate(x, y)
    }
    return this
  }

  fun clone(): Matrix = Matrix(a, b, c, d, tx, ty)

  operator fun times(that: Matrix): Matrix = Matrix().multiply(this, that)

  // Transform points
  fun transformX(px: Double, py: Double): Double = this.a * px + this.c * py + this.tx
  fun transformY(px: Double, py: Double): Double = this.d * py + this.b * px + this.ty
  fun transform(px: Double, py: Double, out: Point = Point()): Point = out.setTo(transformX(px, py), transformY(px, py))
  fun transform(p: Point, out: Point = Point()): Point = transform(p.x, p.y, out)
  fun transformX(p: Point): Double = transformX(p.x, p.y)
  fun transformY(p: Point): Double = transformY(p.x, p.y)
  fun transformXf(px: Double, py: Double): Float = transformX(px, py).toFloat()
  fun transformYf(px: Double, py: Double): Float = transformY(px, py).toFloat()
  fun transformXf(px: Float, py: Float): Float = transformX(px.toDouble(), py.toDouble()).toFloat()
  fun transformYf(px: Float, py: Float): Float = transformY(px.toDouble(), py.toDouble()).toFloat()

  data class Transform(
    var x: Double = 0.0, var y: Double = 0.0,
    var scaleX: Double = 1.0, var scaleY: Double = 1.0,
    var skewX: Double = 0.0, var skewY: Double = 0.0,
    var rotation: @rad Double = 0.0
  ) : MutableInterpolable<Transform>,
    Interpolable<Transform> {

    override fun interpolateWith(ratio: Double, other: Transform): Transform = Transform()
      .setToInterpolated(ratio, this, other)

    override fun setToInterpolated(ratio: Double, l: Transform, r: Transform): Transform = this.setTo(
      ratio.interpolate(l.x, r.x),
      ratio.interpolate(l.y, r.y),
      ratio.interpolate(l.scaleX, r.scaleX),
      ratio.interpolate(l.scaleY, r.scaleY),
      ratio.interpolate(l.rotation, r.rotation),
      ratio.interpolate(l.skewX, r.skewX),
      ratio.interpolate(l.skewY, r.skewY)
    )

    fun identity() {
      x = 0.0
      y = 0.0
      scaleX = 1.0
      scaleY = 1.0
      skewX = 0.0
      skewY = 0.0
      rotation = 0.0
    }

    fun setMatrix(matrix: Matrix): Transform {
      this.x = matrix.tx
      this.y = matrix.ty

      this.skewX = atan(-matrix.c / matrix.d)
      this.skewY = atan(matrix.b / matrix.a)

      // Faster isNaN
      if (this.skewX != this.skewX) this.skewX = 0.0
      if (this.skewY != this.skewY) this.skewY = 0.0

      this.scaleY =
        if (this.skewX > -PI_4 && this.skewX < PI_4) matrix.d / cos(this.skewX) else -matrix.c / sin(this.skewX)
      this.scaleX =
        if (this.skewY > -PI_4 && this.skewY < PI_4) matrix.a / cos(this.skewY) else matrix.b / sin(this.skewY)

      if (abs(this.skewX - this.skewY) < 0.0001) {
        this.rotation = this.skewX
        this.skewX = 0.0
        this.skewY = 0.0
      } else {
        this.rotation = 0.0
      }

      return this
    }

    fun toMatrix(out: Matrix = Matrix()): Matrix = out.setTransform(x, y, scaleX, scaleY, rotation, skewX, skewY)
    fun copyFrom(that: Transform): Transform = setTo(that.x, that.y, that.scaleX, that.scaleY, that.rotation, that.skewX, that.skewY)

    fun setTo(x: Double, y: Double, scaleX: Double, scaleY: Double, rotation: @rad Double, skewX: Double, skewY: Double): Transform {
      this.x = x
      this.y = y
      this.scaleX = scaleX
      this.scaleY = scaleY
      this.rotation = rotation
      this.skewX = skewX
      this.skewY = skewY
      return this
    }

    fun clone(): Transform = Transform().copyFrom(this)
  }

  class Computed(val matrix: Matrix, val transform: Transform) {
    companion object;
    constructor(matrix: Matrix) : this(matrix, Transform().setMatrix(matrix))
    constructor(transform: Transform) : this(transform.toMatrix(), transform)
  }

  override fun setToInterpolated(ratio: Double, l: Matrix, r: Matrix): Matrix = this.setTo(
    a = ratio.interpolate(l.a, r.a),
    b = ratio.interpolate(l.b, r.b),
    c = ratio.interpolate(l.c, r.c),
    d = ratio.interpolate(l.d, r.d),
    tx = ratio.interpolate(l.tx, r.tx),
    ty = ratio.interpolate(l.ty, r.ty)
  )

  override fun interpolateWith(ratio: Double, other: Matrix): Matrix =
    Matrix().setToInterpolated(ratio, this, other)

  inline fun <T> saved(callback: Matrix.() -> T): T {
    val a = this.a
    val b = this.b
    val c = this.c
    val d = this.d
    val tx = this.tx
    val ty = this.ty
    try {
      return callback()
    } finally {
      this.a = a
      this.b = b
      this.c = c
      this.d = d
      this.tx = tx
      this.ty = ty
    }
  }

  override fun toString(): String = "Matrix(a=$a, b=$b, c=$c, d=$d, tx=$tx, ty=$ty)"
}


const val PI_4: Double = PI / 4.0
