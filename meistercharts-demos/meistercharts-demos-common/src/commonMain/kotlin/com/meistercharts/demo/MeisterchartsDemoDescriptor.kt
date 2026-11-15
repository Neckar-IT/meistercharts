/*
 * Copyright 2023 Neckar IT GmbH, Mössingen, Germany
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.meistercharts.demo

import kotlin.uuid.Uuid
import it.neckar.open.kotlin.lang.StableRandom
import it.neckar.open.kotlin.lang.random
import it.neckar.uuid.HasUuid
import it.neckar.uuid.plus
import kotlin.enums.enumEntries
import kotlin.reflect.KClass

/**
 * Describe a meistercharts demo.
 *
 * Demo descriptors must be stateless. State may only be held within the created demo ([prepareDemo]).
 * but not in the descriptor itself.
 *
 */
interface MeisterchartsDemoDescriptor<T> : HasUuid {
  /**
   * The UUID to identify the demo descriptor.
   * The UUID can be used to create permanent links to the demo.
   */
  override val uuid: Uuid

  /**
   * The name of the demo
   */
  val name: String

  /**
   * Returns the quality of the demo.
   *
   * If a demo returns null, it has not yet been categorized. This is a temporary state.
   */
  val quality: DemoQuality
    get() = DemoQuality.High

  val variabilityType: VariabilityType
    get() = VariabilityType.Stable

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
   * Starts the demo
   */
  fun createDemo(configuration: PredefinedConfiguration<T>?): MeisterchartsDemo {
    //Use the UUID bits as seed. StableRandom guarantees the same sequence across Kotlin versions
    //and platforms - required for stable E2E baseline screenshots.
    random = StableRandom(uuid.toLongs { mostSignificantBits, leastSignificantBits -> mostSignificantBits xor leastSignificantBits })
    return prepareDemo(configuration)
  }

  /**
   * Returns a new demo instance.
   *
   * The predefined configuration from [predefinedConfigurations] if the returned list is not empty.
   */
  fun prepareDemo(configuration: PredefinedConfiguration<T>?): MeisterchartsDemo
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
  override val uuid: Uuid,
  /**
   * The payload of the configuration
   */
  val payload: T,
  val name: String = payload.toString(),
) : HasUuid {

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is PredefinedConfiguration<*>) return false

    if (uuid != other.uuid) return false
    if (payloadClass != other.payloadClass) return false
    if (name != other.name) return false

    return true
  }

  override fun hashCode(): Int {
    var result = uuid.hashCode()
    result = 31 * result + (payloadClass?.hashCode() ?: 0)
    result = 31 * result + name.hashCode()
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
inline fun <reified T : Enum<T>> createEnumConfigs(demoDescriptorUuid: Uuid): List<PredefinedConfiguration<T>> {
  return enumEntries<T>()
    .map { PredefinedConfiguration(uuid = demoDescriptorUuid.plus(it), payload = it, name = it.name) }
}

inline fun <reified T : Enum<T>> MeisterchartsDemoDescriptor<*>.createEnumConfigs(): List<PredefinedConfiguration<T>> {
  return createEnumConfigs(uuid)
}

/**
 * Creates the demo name - including the [PredefinedConfiguration] name if there is a predefined configuration
 */
fun MeisterchartsDemoDescriptor<*>.createFullDemoName(configuration: PredefinedConfiguration<*>?): String {
  val configSuffix = configuration?.name?.let { description ->
    " - $description"
  }.orEmpty()

  return "${this.name}$configSuffix"
}
