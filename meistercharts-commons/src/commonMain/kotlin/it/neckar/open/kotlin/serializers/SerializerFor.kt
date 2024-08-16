package it.neckar.open.kotlin.serializers

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialInfo
import kotlin.reflect.KClass

/**
 * Annotates a class to be a serializer for a specific type.
 * This is required if [kotlinx.serialization.SerialName] is used.
 */
@OptIn(ExperimentalSerializationApi::class)
@SerialInfo
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class SerializerFor(val type: KClass<*>)
