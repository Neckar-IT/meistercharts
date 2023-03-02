package com.meistercharts.demo

import kotlin.reflect.KClass

/**
 * Describe a charting demo.
 *
 * Demo descriptors must be stateless. State may only be held within the created demo ([createDemo]).
 * but not in the descriptor itself.
 *
 */
interface ChartingDemoDescriptor<T> {
  /**
   * The name of the demo
   */
  val name: String

  /**
   * The description of the demo - as HTML
   */
  //language=HTML
  val description: String
    get() {
      return name
    }

  /**
   * The category of the demo
   */
  val category: DemoCategory

  val importance: Importance
    get() = Importance.Normal

  /**
   * Returns the predefined configurations for the demo.
   * May return an empty list.
   *
   * For each predefined configuration a new entry is added to the demo navigation tree
   *
   * Attention: The predefined configurations are held in the navigation tree. Therefore these instances must not hold any references to the demo itself.
   * Often times it is a gut idea to use a provider or other sort of lambda as predefined configuration to avoid any possible memory leaks
   */
  val predefinedConfigurations: List<PredefinedConfiguration<T>>
    get() = emptyList()

  /**
   * Returns the default config (the first predefined configuration) or null if there are no configurations.
   */
  val defaultPredefinedConfig: PredefinedConfiguration<T>?
    get() {
      return predefinedConfigurations.firstOrNull()
    }

  /**
   * Returns a new demo instance.
   *
   * The predefined configuration from [predefinedConfigurations] if the returned list is not empty.
   */
  fun createDemo(configuration: PredefinedConfiguration<T>?): ChartingDemo
}

/**
 * Represents a predefined configuration for a demo.
 * A demo can have zero, one or multiple configurations.
 *
 * Each configuration is shown in the navigation
 *
 *
 * ATTENTION: Compares using the description and the *type* of the payload!
 * Does *not* call the equals method of the payload
 */
data class PredefinedConfiguration<out T>(
  /**
   * The payload of the configuration
   */
  val payload: T,
  val description: String = payload.toString()
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is PredefinedConfiguration<*>) return false

    if (payloadClass != other.payloadClass) return false
    if (description != other.description) return false

    return true
  }

  override fun hashCode(): Int {
    var result = payloadClass?.hashCode() ?: 0
    result = 31 * result + description.hashCode()
    return result
  }

  /**
   * Returns the class of the property
   */
  private val payloadClass: KClass<*>?
    get() {
      return payload?.let {
        val kClass = it::class
        kClass
      }
    }
}

/**
 * Creates configurations for enum values
 */
inline fun <reified T : Enum<T>> createEnumConfigs(): List<PredefinedConfiguration<T>> {
  return enumValues<T>()
    .map { PredefinedConfiguration(it, it.name) }
}

/**
 * Creates the demo description - including the description if there is a predefined configuration
 */
fun ChartingDemoDescriptor<*>.createFullDemoDescription(configuration: PredefinedConfiguration<*>?): String {
  val configSuffix = configuration?.description?.let { description ->
    " - $description"
  }.orEmpty()

  val label = "${this.name}$configSuffix"

  return when (this.importance) {
    Importance.Normal -> label
    Importance.InDevelopment -> "[$label]"
    Importance.Deprecated -> "[DEPR] $label"
  }
}

enum class Importance {
  Normal,

  /**
   * Demo that is currently in development - possibly not fully working at the moment
   */
  InDevelopment,

  /**
   * Deprecated demo - that is no longer active
   */
  Deprecated,
}
