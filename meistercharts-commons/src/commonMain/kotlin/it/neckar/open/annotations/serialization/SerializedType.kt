package it.neckar.open.annotations.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialInfo
import kotlin.reflect.KClass

/**
 * Annotates a class to be a serializer for a specific type.
 * Should be used together with `kotlinx.serialization.SerialName` to allow KSP plugins to detect the "real" type that is being serialized.
 */
@OptIn(ExperimentalSerializationApi::class)
@SerialInfo // Marks this annotation as a serialization-related annotation
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class SerializedType(val type: KClass<*>)
