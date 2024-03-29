@file:Suppress("FunctionName", "SpellCheckingInspection")

package it.neckar.open.kotlin.bytearray

/**
 */
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.DoubleBuffer
import java.nio.FloatBuffer
import java.nio.IntBuffer
import java.nio.ShortBuffer

private fun java.nio.Buffer.checkSliceBounds(offset: Int, size: Int) {
  //val end = offset + size - 1
  //if (offset !in 0 until this.capacity()) error("offset=$offset, size=$size not inside ${this.capacity()}")
  //if (end !in 0 until this.capacity()) error("offset=$offset, size=$size not inside ${this.capacity()}")
}

fun ByteBuffer.slice(offset: Int, size: Int): ByteBuffer = run {
  checkSliceBounds(offset, size);
  val out = this.duplicate(); (out as java.nio.Buffer).position(this.position() + offset); (out as java.nio.Buffer).limit(out.position() + size); return out
}

fun ShortBuffer.slice(offset: Int, size: Int): ShortBuffer = run {
  checkSliceBounds(offset, size);
  val out = this.duplicate(); (out as java.nio.Buffer).position(this.position() + offset); (out as java.nio.Buffer).limit(out.position() + size); return out
}

fun IntBuffer.slice(offset: Int, size: Int): IntBuffer = run {
  checkSliceBounds(offset, size);
  val out = this.duplicate(); (out as java.nio.Buffer).position(this.position() + offset); (out as java.nio.Buffer).limit(out.position() + size); return out
}

fun FloatBuffer.slice(offset: Int, size: Int): FloatBuffer = run {
  checkSliceBounds(offset, size);
  val out = this.duplicate(); (out as java.nio.Buffer).position(this.position() + offset); (out as java.nio.Buffer).limit(out.position() + size); return out
}

fun DoubleBuffer.slice(offset: Int, size: Int): DoubleBuffer = run {
  checkSliceBounds(offset, size);
  val out = this.duplicate(); (out as java.nio.Buffer).position(this.position() + offset); (out as java.nio.Buffer).limit(out.position() + size); return out
}

actual class MemBuffer(val buffer: ByteBuffer, val size: Int) {
  val sbuffer = buffer.order(ByteOrder.nativeOrder()).asShortBuffer()
  val ibuffer = buffer.order(ByteOrder.nativeOrder()).asIntBuffer()
  val fbuffer = buffer.order(ByteOrder.nativeOrder()).asFloatBuffer()
  val dbuffer = buffer.order(ByteOrder.nativeOrder()).asDoubleBuffer()
}

actual fun MemBufferAlloc(size: Int): MemBuffer = MemBuffer(ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder()), size)
actual fun MemBufferAllocNoDirect(size: Int): MemBuffer = MemBuffer(ByteBuffer.allocate(size).order(ByteOrder.nativeOrder()), size)
actual fun MemBufferWrap(array: ByteArray): MemBuffer = MemBuffer(ByteBuffer.wrap(array).order(ByteOrder.nativeOrder()), array.size)
actual inline val MemBuffer.size: Int get() = this.size

actual fun MemBuffer._sliceInt8Buffer(offset: Int, size: Int): Int8Buffer = Int8Buffer(this, this.buffer.slice(offset, size))
actual fun MemBuffer._sliceInt16Buffer(offset: Int, size: Int): Int16Buffer = Int16Buffer(this, this.buffer.asShortBuffer().slice(offset, size))
actual fun MemBuffer._sliceInt32Buffer(offset: Int, size: Int): Int32Buffer = Int32Buffer(this, this.buffer.asIntBuffer().slice(offset, size))
actual fun MemBuffer._sliceFloat32Buffer(offset: Int, size: Int): Float32Buffer = Float32Buffer(this, this.buffer.asFloatBuffer().slice(offset, size))
actual fun MemBuffer._sliceFloat64Buffer(offset: Int, size: Int): Float64Buffer = Float64Buffer(this, this.buffer.asDoubleBuffer().slice(offset, size))

actual typealias DataBuffer = MemBuffer

actual val DataBuffer.mem: MemBuffer get() = this
actual fun MemBuffer.getData(): DataBuffer = this
actual fun DataBuffer.getByte(index: Int): Byte = buffer.get(index)
actual fun DataBuffer.setByte(index: Int, value: Byte): Unit = run { buffer.put(index, value) }
actual fun DataBuffer.getShort(index: Int): Short = buffer.getShort(index)
actual fun DataBuffer.setShort(index: Int, value: Short): Unit = run { buffer.putShort(index, value) }
actual fun DataBuffer.getInt(index: Int): Int = buffer.getInt(index)
actual fun DataBuffer.setInt(index: Int, value: Int): Unit = run { buffer.putInt(index, value) }
actual fun DataBuffer.getFloat(index: Int): Float = buffer.getFloat(index)
actual fun DataBuffer.setFloat(index: Int, value: Float): Unit = run { buffer.putFloat(index, value) }
actual fun DataBuffer.getDouble(index: Int): Double = buffer.getDouble(index)
actual fun DataBuffer.setDouble(index: Int, value: Double): Unit = run { buffer.putDouble(index, value) }

actual class Int8Buffer(val mbuffer: MemBuffer, val jbuffer: ByteBuffer)

actual val Int8Buffer.mem: MemBuffer get() = mbuffer
actual val Int8Buffer.offset: Int get() = (jbuffer as java.nio.Buffer).position()
actual val Int8Buffer.size: Int get() = (jbuffer as java.nio.Buffer).limit() - offset
actual operator fun Int8Buffer.get(index: Int): Byte = jbuffer.get(offset + index)
actual operator fun Int8Buffer.set(index: Int, value: Byte): Unit = run { jbuffer.put(offset + index, value) }

actual class Int16Buffer(val mbuffer: MemBuffer, val jbuffer: ShortBuffer)

actual val Int16Buffer.mem: MemBuffer get() = mbuffer
actual val Int16Buffer.offset: Int get() = (jbuffer as java.nio.Buffer).position()
actual val Int16Buffer.size: Int get() = (jbuffer as java.nio.Buffer).limit() - offset
actual operator fun Int16Buffer.get(index: Int): Short = jbuffer.get(offset + index)
actual operator fun Int16Buffer.set(index: Int, value: Short): Unit = run { jbuffer.put(offset + index, value) }

actual class Int32Buffer(val mbuffer: MemBuffer, val jbuffer: IntBuffer)

actual val Int32Buffer.mem: MemBuffer get() = mbuffer
actual val Int32Buffer.offset: Int get() = (jbuffer as java.nio.Buffer).position()
actual val Int32Buffer.size: Int get() = (jbuffer as java.nio.Buffer).limit() - offset
actual operator fun Int32Buffer.get(index: Int): Int = jbuffer.get(offset + index)
actual operator fun Int32Buffer.set(index: Int, value: Int): Unit = run { jbuffer.put(offset + index, value) }

actual class Float32Buffer(val mbuffer: MemBuffer, val jbuffer: FloatBuffer)

actual val Float32Buffer.mem: MemBuffer get() = mbuffer
actual val Float32Buffer.offset: Int get() = (jbuffer as java.nio.Buffer).position()
actual val Float32Buffer.size: Int get() = (jbuffer as java.nio.Buffer).limit() - offset
actual operator fun Float32Buffer.get(index: Int): Float = jbuffer.get(offset + index)
actual operator fun Float32Buffer.set(index: Int, value: Float): Unit = run { jbuffer.put(offset + index, value) }

actual class Float64Buffer(val mbuffer: MemBuffer, val jbuffer: DoubleBuffer)

actual val Float64Buffer.mem: MemBuffer get() = mbuffer
actual val Float64Buffer.offset: Int get() = (jbuffer as java.nio.Buffer).position()
actual val Float64Buffer.size: Int get() = (jbuffer as java.nio.Buffer).limit() - offset
actual operator fun Float64Buffer.get(index: Int): Double = jbuffer.get(offset + index)
actual operator fun Float64Buffer.set(index: Int, value: Double): Unit = run { jbuffer.put(offset + index, value) }

inline operator fun ByteBuffer.set(index: Int, value: Byte) = this.put(index, value)
inline operator fun ShortBuffer.set(index: Int, value: Short) = this.put(index, value)
inline operator fun IntBuffer.set(index: Int, value: Int) = this.put(index, value)
inline operator fun FloatBuffer.set(index: Int, value: Float) = this.put(index, value)
inline operator fun DoubleBuffer.set(index: Int, value: Double) = this.put(index, value)
inline operator fun MemBuffer.set(index: Int, value: Byte) = this.buffer.put(index, value)
inline operator fun MemBuffer.get(index: Int): Byte = this.buffer.get(index)

private inline fun <T> arraycopy(size: Int, src: Any?, srcPos: Int, dst: Any?, dstPos: Int, setDst: (Int, T) -> Unit, getSrc: (Int) -> T) {
  val overlapping = src === dst && dstPos > srcPos
  if (overlapping) {
    var n = size
    while (--n >= 0) setDst(dstPos + n, getSrc(srcPos + n))
  } else {
    for (n in 0 until size) setDst(dstPos + n, getSrc(srcPos + n))
  }
}

actual fun arraycopy(src: MemBuffer, srcPos: Int, dst: MemBuffer, dstPos: Int, size: Int): Unit {
  arraycopy(size, src, srcPos, dst, dstPos, { it, value -> dst[it] = value }, { src[it] })
}

actual fun arraycopy(src: ByteArray, srcPos: Int, dst: MemBuffer, dstPos: Int, size: Int): Unit {
  arraycopy(size, src, srcPos, dst, dstPos, { it, value -> dst[it] = value }, { src[it] })
}

actual fun arraycopy(src: MemBuffer, srcPos: Int, dst: ByteArray, dstPos: Int, size: Int): Unit {
  arraycopy(size, src, srcPos, dst, dstPos, { it, value -> dst[it] = value }, { src[it] })
}

actual fun arraycopy(src: ShortArray, srcPos: Int, dst: MemBuffer, dstPos: Int, size: Int): Unit {
  arraycopy(size, src, srcPos, dst, dstPos, { it, value -> dst.sbuffer[it] = value }, { src[it] })
}

actual fun arraycopy(src: MemBuffer, srcPos: Int, dst: ShortArray, dstPos: Int, size: Int): Unit {
  arraycopy(size, src, srcPos, dst, dstPos, { it, value -> dst[it] = value }, { src.sbuffer[it] })
}

actual fun arraycopy(src: IntArray, srcPos: Int, dst: MemBuffer, dstPos: Int, size: Int): Unit {
  arraycopy(size, src, srcPos, dst, dstPos, { it, value -> dst.ibuffer[it] = value }, { src[it] })
}

actual fun arraycopy(src: MemBuffer, srcPos: Int, dst: IntArray, dstPos: Int, size: Int): Unit {
  arraycopy(size, src, srcPos, dst, dstPos, { it, value -> dst[it] = value }, { src.ibuffer[it] })
}

actual fun arraycopy(src: FloatArray, srcPos: Int, dst: MemBuffer, dstPos: Int, size: Int): Unit {
  arraycopy(size, src, srcPos, dst, dstPos, { it, value -> dst.fbuffer[it] = value }, { src[it] })
}

actual fun arraycopy(src: MemBuffer, srcPos: Int, dst: FloatArray, dstPos: Int, size: Int): Unit {
  arraycopy(size, src, srcPos, dst, dstPos, { it, value -> dst[it] = value }, { src.fbuffer[it] })
}

actual fun arraycopy(src: DoubleArray, srcPos: Int, dst: MemBuffer, dstPos: Int, size: Int): Unit {
  arraycopy(size, src, srcPos, dst, dstPos, { it, value -> dst.dbuffer[it] = value }, { src[it] })
}

actual fun arraycopy(src: MemBuffer, srcPos: Int, dst: DoubleArray, dstPos: Int, size: Int): Unit {
  arraycopy(size, src, srcPos, dst, dstPos, { it, value -> dst[it] = value }, { src.dbuffer[it] })
}
