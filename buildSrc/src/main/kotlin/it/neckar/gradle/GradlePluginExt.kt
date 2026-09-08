package it.neckar.gradle

import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinJvmCompilation
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget


/**
 * Returns the JVM compilation of the main source set of the Kotlin Multiplatform project.
 */
fun Project.findJvmMainCompilation(): KotlinJvmCompilation {
  val multiplatformExtension = project.extensions.findByType(KotlinMultiplatformExtension::class.java) ?: error("Kotlin Multiplatform plugin is required")
  val jvmTarget = multiplatformExtension.targets.getByName("jvm") as KotlinJvmTarget
  return jvmTarget.compilations.getByName("main") ?: error("Main compilation not found")
}
