package it.neckar.gradle

import com.google.devtools.ksp.gradle.KspExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.findByType

/**
 * Contains methods for KSP project related stuff
 */
fun Project.fixKspTaskDependencies() {
  val kspExtension = findKspExtension() ?: return //No KSP extension found, return immediately
  kspExtension.requireNotNull()

  tasks.whenTaskAdded {
    if (name.startsWith("ksp")) {
      addAllMustRunAfterForKspTasks()
    }
  }
}

fun Project.addAllMustRunAfterForKspTasks() {
  val kspCommonMainKotlinMetadata = tasks.findByName("kspCommonMainKotlinMetadata") ?: return

  //Add all ksp tasks to the mustRunAfter list
  tasks.filter { it.name.startsWith("ksp") && it != kspCommonMainKotlinMetadata }
    .forEach {
      it.mustRunAfter(kspCommonMainKotlinMetadata)
    }
}

fun Project.findKspExtension(): KspExtension? = this.extensions.findByType<KspExtension>()

