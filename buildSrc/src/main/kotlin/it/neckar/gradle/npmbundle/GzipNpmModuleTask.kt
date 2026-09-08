package it.neckar.gradle.npmbundle

import it.neckar.gradle.child
import com.google.common.io.Files
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream
import org.gradle.api.DefaultTask
import org.gradle.api.InvalidUserDataException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.property
import java.io.FileOutputStream

/**
 * Gzips the npm module
 */
abstract class GzipNpmModuleTask : DefaultTask() {
  @get:Input
  abstract val archiveFileNameProperty: Property<String>

  @get:Input
  abstract val dirNameInArchiveProperty: Property<String>

  @get:InputDirectory
  abstract val sourceDirProperty: DirectoryProperty

  @get:OutputDirectory
  abstract val targetDirectoryForArchiveProperty: DirectoryProperty

  @Suppress("unused")
  @TaskAction
  fun zipContent() {
    val sourceDir = sourceDirProperty.get().asFile
    if (!sourceDir.exists() || !sourceDir.isDirectory) {
      throw InvalidUserDataException("Directory does not exist <${sourceDir.absolutePath}>")
    }

    val dirNameInArchive = dirNameInArchiveProperty.get()
    val tarGzFile = targetDirectoryForArchiveProperty.get().asFile.child("${archiveFileNameProperty.get()}.tar.gz")

    //Zip the content
    FileOutputStream(tarGzFile).use { fileOutputStream ->
      GzipCompressorOutputStream(fileOutputStream)
        .use { gzipOut ->
          TarArchiveOutputStream(gzipOut)
            .use { tarOut ->

              val sourceFiles = sourceDir.listFiles() ?: error("Could not list files in ${sourceDir.absolutePath}")
              sourceFiles.forEach { sourceFile ->
                val archiveEntry = tarOut.createArchiveEntry(sourceFile, "$dirNameInArchive/${sourceFile.name}")
                tarOut.putArchiveEntry(archiveEntry)
                Files.copy(sourceFile, tarOut)
                tarOut.closeArchiveEntry()
              }
            }
        }
    }
  }
}
