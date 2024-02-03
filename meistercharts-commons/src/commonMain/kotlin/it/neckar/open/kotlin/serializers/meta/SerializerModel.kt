package it.neckar.open.kotlin.serializers.meta

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.SerialKind
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.descriptors.elementDescriptors
import kotlin.reflect.KClass

/**
 * Represents a serializer model of a [kotlinx.serialization.Serializable] class.
 */
@Deprecated("Required?????")
sealed interface SerializerModel<T : Any> {
  val serialName: String
  val type: KClass<T>

  companion object {
    inline fun <reified T : Any> create(serializer: KSerializer<T>, type: KClass<T> = T::class): SerializerModel<T> {
      val descriptor = serializer.descriptor
      val model = when (descriptor.kind) {
        PrimitiveKind.BOOLEAN -> PrimitiveSerializerModel(descriptor.serialName, Boolean::class)
        PrimitiveKind.BYTE -> PrimitiveSerializerModel(descriptor.serialName, Byte::class)
        PrimitiveKind.CHAR -> PrimitiveSerializerModel(descriptor.serialName, Char::class)
        PrimitiveKind.DOUBLE -> PrimitiveSerializerModel(descriptor.serialName, Double::class)
        PrimitiveKind.FLOAT -> PrimitiveSerializerModel(descriptor.serialName, Float::class)
        PrimitiveKind.INT -> PrimitiveSerializerModel(descriptor.serialName, Int::class)
        PrimitiveKind.LONG -> PrimitiveSerializerModel(descriptor.serialName, Long::class)
        PrimitiveKind.SHORT -> PrimitiveSerializerModel(descriptor.serialName, Short::class)
        PrimitiveKind.STRING -> PrimitiveSerializerModel(descriptor.serialName, String::class)

        StructureKind.CLASS -> {
          StructuredSerializerModel.create(descriptor, type)
        }


        PolymorphicKind.OPEN -> TODO()
        PolymorphicKind.SEALED -> TODO()

        SerialKind.CONTEXTUAL -> TODO()
        SerialKind.ENUM -> TODO()

        StructureKind.LIST -> TODO()
        StructureKind.MAP -> TODO()
        StructureKind.OBJECT -> TODO()
        else -> {
          TODO("Unknown kind ${descriptor.kind}")
        }
      }

      return model as SerializerModel<T>
    }
  }
}

/**
 * Represents a structured serializer model
 */
data class StructuredSerializerModel<T : Any>(
  override val serialName: String,
  override val type: KClass<T>,
  /**
   * The elements of the structured serializer
   */
  val elements: List<SerializerModel<*>>,
) : SerializerModel<T> {
  companion object {
    fun <T : Any> create(descriptor: SerialDescriptor, type: KClass<T>): StructuredSerializerModel<T> {

      descriptor.elementDescriptors.forEach {
        println("Element: $it")
      }

      val elements: List<SerializerModel<*>> = descriptor.elementDescriptors.map {
        //SerializerModel.create(it.serialName, it.type)
        TODO()
      }

      return StructuredSerializerModel(descriptor.serialName, type, elements)
    }
  }
}

/**
 * Represents a primitive type
 */
data class PrimitiveSerializerModel<T : Any>(
  override val serialName: String,
  override val type: KClass<T>,
) : SerializerModel<T> {

}
