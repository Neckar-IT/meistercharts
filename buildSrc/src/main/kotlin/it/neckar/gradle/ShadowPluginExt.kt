package it.neckar.gradle


/**
 * Excludes Kotlin Reflect when minimizing the shadow jar
 */
fun com.github.jengelman.gradle.plugins.shadow.internal.DependencyFilter.excludeKotlinReflect() {
  exclude(dependency(Libs.kotlin_reflect.removeSuffix(":_")))
}

fun com.github.jengelman.gradle.plugins.shadow.internal.DependencyFilter.excludeJNA() {
  exclude(dependency(Libs.jna.removeSuffix(":_")))
}

fun com.github.jengelman.gradle.plugins.shadow.internal.DependencyFilter.excludeClikt() {
  exclude(dependency(Libs.clikt.removeSuffix(":_")))
}
