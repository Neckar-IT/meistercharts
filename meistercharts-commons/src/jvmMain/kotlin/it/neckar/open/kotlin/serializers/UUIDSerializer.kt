package it.neckar.open.kotlin.serializers

import java.nio.ByteBuffer
import java.util.UUID

private val UUID.bytes: ByteArray
  get() {
    val byteBuffer = ByteBuffer.wrap(ByteArray(16)) // Create a new ByteBuffer with 16 bytes (length of a UUID)
    byteBuffer.putLong(this.mostSignificantBits) // Inserts the upper 64 bits
    byteBuffer.putLong(this.leastSignificantBits) // Inserts the lower 64 bits
    return byteBuffer.array()
  }
