package it.neckar.gradle.npmbundle

import org.gradle.api.DefaultTask
import org.gradle.api.file.CopySpec
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.property
import org.gradle.work.DisableCachingByDefault

/**
 * Copies the bundled content
 */
@DisableCachingByDefault
open class CopyBundleContentTask : DefaultTask() {

  @Internal
  val filesToBundle: Property<CopySpec> = project.objects.property<CopySpec>()

  @OutputDirectory
  val destinationDir: DirectoryProperty = project.objects.directoryProperty()

  @TaskAction
  fun copyBundleContent() {
    val files = filesToBundle.get()
    val destination = destinationDir.get().asFile

    project.copy {
      with(files)
      into(destination)
    }
  }
}
