package it.neckar.geometry

import kotlinx.serialization.Serializable
import kotlin.js.JsExport

/**
 * Represents a polygon shape.
 */
@Serializable
@JsExport
class Polygon(private val vertices: List<Coordinates>) : Shape {
  init {
    require(vertices.size >= 3) { "A polygon must have at least 3 vertices" }
  }

  @JsExport.Ignore
  override val location: Coordinates
    get() = vertices.first()

  @JsExport.Ignore
  val verticesCount: Int = vertices.size

  //TODO store field
  @JsExport.Ignore
  override val size: Size
    get() {
      val minX = vertices.minOf { it.x }
      val maxX = vertices.maxOf { it.x }
      val minY = vertices.minOf { it.y }
      val maxY = vertices.maxOf { it.y }
      return Size(maxX - minX, maxY - minY)
    }

  override fun vertices(): List<Coordinates> = vertices

  @JsExport.Ignore
  override fun contains(coordinates: Coordinates): Boolean {
    //simplified variant TODO: correct it
    if (true) {
      val minX = vertices.minOf { it.x }
      val maxX = vertices.maxOf { it.x }
      val minY = vertices.minOf { it.y }
      val maxY = vertices.maxOf { it.y }

      val inX = coordinates.x in minX..maxX
      val inY = coordinates.y in minY..maxY

      return inX &&
        inY
    }

    // Implement point-in-polygon algorithm (e.g., ray-casting algorithm)
    var result = false
    var j = vertices.size - 1
    for (i in vertices.indices) {
      if (vertices[i].y > coordinates.y != vertices[j].y > coordinates.y &&
        coordinates.x < (vertices[j].x - vertices[i].x) * (coordinates.y - vertices[i].y) / (vertices[j].y - vertices[i].y) + vertices[i].x
      ) {
        result = !result
      }
      j = i
    }
    return result
  }

  @JsExport.Ignore
  override fun move(deltaX: Double, deltaY: Double): Polygon {
    val movedVertices = vertices.map { Coordinates.of(it.x + deltaX, it.y + deltaY) }
    return Polygon(movedVertices)
  }

  @JsExport.Ignore
  override fun withX(newX: Double): Polygon {
    val deltaX = newX - location.x
    return move(deltaX, 0.0)
  }

  @JsExport.Ignore
  override fun withY(newY: Double): Polygon {
    val deltaY = newY - location.y
    return move(0.0, deltaY)
  }

  @JsExport.Ignore
  override fun withWidth(newWidth: Double): Polygon {
    // Scaling logic can be added here if needed
    throw UnsupportedOperationException("Resizing a polygon is not supported")
  }

  @JsExport.Ignore
  override fun withHeight(newHeight: Double): Polygon {
    // Scaling logic can be added here if needed
    throw UnsupportedOperationException("Resizing a polygon is not supported")
  }

  @JsExport.Ignore
  override fun withLocation(location: Coordinates): Polygon {
    val deltaX = location.x - this.location.x
    val deltaY = location.y - this.location.y
    return move(deltaX, deltaY)
  }

  @JsExport.Ignore
  override fun expand(left: Double, top: Double, right: Double, bottom: Double): Polygon {
    // Expansion logic can be added here if needed
    throw UnsupportedOperationException("Expanding a polygon is not supported")
  }

  @JsExport.Ignore
  override fun overlaps(other: Shape): Boolean {
    // Implement polygon overlap detection
    throw UnsupportedOperationException("Polygon overlap detection is not implemented")
  }

  @JsExport.Ignore
  override fun doesNotOverlap(other: Shape): Boolean {
    return overlaps(other).not()
  }

  @JsExport.Ignore
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is Polygon) return false

    if (vertices != other.vertices) return false

    return true
  }

  @JsExport.Ignore
  override fun hashCode(): Int {
    return vertices.hashCode()
  }
}
