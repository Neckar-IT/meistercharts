package it.neckar.gradle.npmbundle

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import org.gradle.api.DefaultTask
import org.gradle.api.file.CopySpec
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import javax.inject.Inject

/**
 * Copies the bundled content and writes [moduleName] and [version] into the copied `package.json`:
 * the checked-in file has no name, because the production and the development bundle each publish under their own.
 */
@DisableCachingByDefault
abstract class CopyBundleContentTask : DefaultTask() {

  @get:Internal
  abstract val filesToBundle: Property<CopySpec>

  @get:Input
  abstract val moduleName: Property<String>

  @get:Input
  abstract val version: Property<String>

  @get:OutputDirectory
  abstract val destinationDir: DirectoryProperty

  @get:Inject
  abstract val fileSystemOperations: FileSystemOperations

  init {
    // Gradle cannot read the source files out of a CopySpec, and a task with outputs alone stays up to date when they change.
    doNotTrackState("The files behind filesToBundle are not declared as inputs")
  }

  @TaskAction
  fun copyBundleContent() {
    val files = filesToBundle.get()
    val destination = destinationDir.get().asFile

    // sync: a file the spec stopped listing must not survive in the package from an earlier run.
    fileSystemOperations.sync {
      with(files)
      into(destination)
    }

    val packageJson = destination.resolve("package.json")
    require(packageJson.isFile) {
      "Expected ${packageJson.absolutePath} to exist. Add the project's package.json to filesToBundle of the extension that configures $name."
    }
    packageJson.writeText(packageJsonWithNameAndVersion(packageJson.readText(), moduleName.get(), version.get()))
  }
}

@OptIn(ExperimentalSerializationApi::class)
private val packageJsonFormat: Json = Json {
  prettyPrint = true
  prettyPrintIndent = "  "
}

/** Returns [packageJson] with [name] and [version] as its first fields; every other field keeps its value and order. */
internal fun packageJsonWithNameAndVersion(packageJson: String, name: String, version: String): String {
  val content: JsonObject = Json.parseToJsonElement(packageJson).jsonObject
  require(content.containsKey("name").not()) {
    "The checked-in package.json holds the name ${content["name"]}. Remove it: moduleName of the extension names the package."
  }
  val updated = JsonObject(mapOf("name" to JsonPrimitive(name), "version" to JsonPrimitive(version)) + (content - "version"))
  return packageJsonFormat.encodeToString(JsonObject.serializer(), updated) + "\n"
}
