package it.neckar.geometry

import kotlinx.serialization.Serializable

/**
 * Represents a polygon shape.
 */
@Serializable
data class Polygon(private val vertices: List<Coordinates>) : Shape {
  init {
    require(vertices.size >= 3) { "A polygon must have at least 3 vertices, but was ${vertices.size}: $vertices" }
  }

  override val location: Coordinates
    get() = vertices.first()

  val verticesCount: Int
    get() = vertices.size

  val boundingBox: Rectangle
    get() {
      val minX = vertices.minOf { it.x }
      val maxX = vertices.maxOf { it.x }
      val minY = vertices.minOf { it.y }
      val maxY = vertices.maxOf { it.y }
      return Rectangle(Coordinates.of(minX, minY), Size(maxX - minX, maxY - minY))
    }

  override val size: Size
    get() = boundingBox.size

  /**
   * Whether the polygon is convex or not.
   * A polygon is convex if all its interior angles are less than 180 degrees.
   * This is determined by checking the sign of the cross product of each triplet of consecutive vertices.
   * If the sign changes, the polygon is concave.
   * If the sign remains the same for all triplets, the polygon is convex.
   * * Note: This method assumes that the vertices are ordered in a consistent manner (clockwise or counter-clockwise).
   * * @return true if the polygon is convex, false if it is concave
   * * Reference: https://en.wikipedia.org/wiki/Convex_polygon#Convexity_checking
   */
  val isConvex: Boolean
    get() {
      if (vertices.size == 3) return true // Triangles are always convex
      var sign = 0
      for (vertexIndex in vertices.indices) {
        val p1 = vertices[vertexIndex]
        val p2 = vertices[(vertexIndex + 1) % vertices.size]
        val p3 = vertices[(vertexIndex + 2) % vertices.size]

        val crossProduct = (p2.x - p1.x) * (p3.y - p1.y) - (p2.y - p1.y) * (p3.x - p1.x)
        if (crossProduct != 0.0) {
          if (sign == 0) {
            sign = if (crossProduct > 0) 1 else -1
          } else if ((crossProduct > 0 && sign < 0) || (crossProduct < 0 && sign > 0)) {
            return false // Sign change detected, polygon is not convex
          }
        }
      }
      return true // No sign change detected, polygon is convex
    }

  val isConcave: Boolean
    get() = !isConvex

  /**
   * Calculates the perimeter of the polygon by summing the lengths of its edges.
   */
  val perimeter: Double
    get() {
      var perimeter = 0.0
      for (i in vertices.indices) {
        val nextIndex = (i + 1) % vertices.size
        val deltaX = vertices[nextIndex].x - vertices[i].x
        val deltaY = vertices[nextIndex].y - vertices[i].y
        perimeter += kotlin.math.sqrt(deltaX * deltaX + deltaY * deltaY)
      }
      return perimeter
    }

  /**
   * Calculates the area of the polygon using the shoelace formula.
   * https://en.wikipedia.org/wiki/Shoelace_formula
   */
  val area: Double
    get() {
      var area = 0.0
      for (vertexIndex in 0 until vertices.size) {
        val nextVertexIndex = (vertexIndex + 1) % vertices.size
        val vertex = vertices[vertexIndex]
        val nextVertex = vertices[nextVertexIndex]
        area += vertex.x * nextVertex.y - nextVertex.x * vertex.y
      }
      return kotlin.math.abs(area) / 2.0
    }

  override fun vertices(): List<Coordinates> = vertices

  override fun contains(coordinates: Coordinates): Boolean {
    //simplified variant TODO: correct it
    return boundingBox.contains(coordinates)
  }

  /**
   * Checks if the polygon contains the given coordinates using the ray-casting algorithm.
   * This method works correctly with irregular polygons, including concave ones.
   * But it is less precise around the edges and corners of the polygon due to floating-point arithmetic.
   */
  fun containsRayCast(coordinates: Coordinates): Boolean {
    if (boundingBox.contains(coordinates).not()) return false
    // Implement point-in-polygon algorithm (e.g., ray-casting algorithm)
    var result = false
    var previousVertexIndex = vertices.size - 1
    for (vertexIndex in vertices.indices) {
      val vertex = vertices[vertexIndex]
      val previousVertex = vertices[previousVertexIndex]
      // Check if the point is on the edge
      if ((vertex.y > coordinates.y) != (previousVertex.y > coordinates.y) &&
        (coordinates.x < (previousVertex.x - vertex.x) * (coordinates.y - vertex.y) / (previousVertex.y - vertex.y) + vertex.x)
      ) {
        result = result.not()
      }
      previousVertexIndex = vertexIndex
    }
    return result
  }

  override fun move(deltaX: Double, deltaY: Double): Polygon {
    val movedVertices = vertices.map { Coordinates.of(it.x + deltaX, it.y + deltaY) }
    return Polygon(movedVertices)
  }

  override fun withX(newX: Double): Polygon {
    val deltaX = newX - location.x
    return move(deltaX, 0.0)
  }

  override fun withY(newY: Double): Polygon {
    val deltaY = newY - location.y
    return move(0.0, deltaY)
  }

  override fun withWidth(newWidth: Double): Polygon {
    // Scaling logic can be added here if needed
    throw UnsupportedOperationException("Resizing a polygon is not supported")
  }

  override fun withHeight(newHeight: Double): Polygon {
    // Scaling logic can be added here if needed
    throw UnsupportedOperationException("Resizing a polygon is not supported")
  }

  override fun withLocation(location: Coordinates): Polygon {
    val deltaX = location.x - this.location.x
    val deltaY = location.y - this.location.y
    return move(deltaX, deltaY)
  }

  override fun expand(left: Double, top: Double, right: Double, bottom: Double): Polygon {
    // Expansion logic can be added here if needed
    throw UnsupportedOperationException("Expanding a polygon is not supported")
  }

  override fun overlaps(other: Shape): Boolean {
    // Implement polygon overlap detection
    throw UnsupportedOperationException("Polygon overlap detection is not implemented")
  }

  override fun doesNotOverlap(other: Shape): Boolean {
    return overlaps(other).not()
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is Polygon) return false

    if (vertices != other.vertices) return false

    return true
  }

  override fun hashCode(): Int {
    return vertices.hashCode()
  }
}
