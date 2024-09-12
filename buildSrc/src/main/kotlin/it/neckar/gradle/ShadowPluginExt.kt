package it.neckar.gradle


/**
 * Excludes Kotlin Reflect when minimizing the shadow jar
 */
fun com.github.jengelman.gradle.plugins.shadow.internal.DependencyFilter.excludeKotlinReflect() {
  exclude(dependency(Libs.kotlin_reflect.removeSuffix(":_")))
}
