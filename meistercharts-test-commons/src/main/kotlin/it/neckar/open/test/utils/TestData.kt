package it.neckar.open.test.utils

import it.neckar.open.kotlin.lang.fromBase64

/**
 * Contains test data that can be used in tests
 */
object TestData {
  /**
   * A very small, but valid jpg image
   */
  val minimalImageJpg: ByteArray =
    "/9j/4AAQSkZJRgABAQEASABIAAD/2wBDAP//////////////////////////////////////////////////////////////////////////////////////wgALCAABAAEBAREA/8QAFBABAAAAAAAAAAAAAAAAAAAAAP/aAAgBAQABPxA=".fromBase64()
}
