package it.neckar.gradle.ksp

import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider


/**
 * Depends on all Kotlin compile tasks in the given target project.
 */
fun TaskProvider<*>.dependOnKotlinCompileTasks(target: Project) {
  this.configure {
    target.tasks
      .filter { it.name.startsWith("compileKotlin") }
      .forEach { compileKotlinTask ->
        compileKotlinTask.finalizedBy(this)
        dependsOn(compileKotlinTask)
      }
  }
}

fun TaskProvider<*>.dependOnKotlinCompileTestTasks(target: Project) {
  this.configure {
    target.tasks
      .filter { it.name.startsWith("compileTestKotlin") }
      .forEach { compileKotlinTask ->
        compileKotlinTask.finalizedBy(this)
        dependsOn(compileKotlinTask)
      }
  }
}
