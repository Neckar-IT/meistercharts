package it.neckar.reflect

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

/**
 * Represents a class name
 */
@JvmInline
@Serializable
value class ClassName(val value: String) {
  init {
    require(value.isNotEmpty()) {
      "ClassName cannot be empty"
    }
  }

  override fun toString(): String {
    return value
  }


}
