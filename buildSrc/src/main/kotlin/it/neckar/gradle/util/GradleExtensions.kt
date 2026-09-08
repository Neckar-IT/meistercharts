package it.neckar.gradle.util

import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.NamedDomainObjectSet
import org.gradle.api.UnknownDomainObjectException

/**
 * Returns the named object or null if it does not exist.
 */
internal fun <T : Any> NamedDomainObjectSet<T>.findNamed(name: String): NamedDomainObjectProvider<T>? {
  return try {
    this.named(name)
  } catch (_: UnknownDomainObjectException) {
    null
  }
}
