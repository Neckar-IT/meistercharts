package com.meistercharts.demo

import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.PredefinedConfiguration
import com.meistercharts.demo.createFullDemoDescription
import it.neckar.open.kotlin.lang.containsAll
import it.neckar.open.javafx.consumeImmediately
import it.neckar.open.javafx.map
import it.neckar.open.javafx.properties.*
import it.neckar.logging.Logger
import it.neckar.logging.LoggerDelegate
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javafx.beans.value.ObservableValue
import javafx.scene.control.TreeItem
import java.util.function.Predicate

/**
 *
 * Contains the model for the charting demos runner
 *
 */
class ChartingDemosModel {
  private val logger: Logger by LoggerDelegate()


  /**
   * Contains the current demo description and optional predefined configuration. If updated a new demo will be instantiated.
   */
  val currentDescriptorWithConfigProperty: ObjectProperty<DemoDescriptorWithPredefinedConfiguration?> = SimpleObjectProperty<DemoDescriptorWithPredefinedConfiguration?>().also { property ->
    property.consumeImmediately {
      logger.debug("currentDescriptorWithConfig value: $it")
    }
  }

  var currentDescriptorWithConfig: DemoDescriptorWithPredefinedConfiguration? by currentDescriptorWithConfigProperty

  /**
   * Contains the current value of the filter text
   */
  val filterTextProperty: StringProperty = SimpleStringProperty("")
  val filterText: String by filterTextProperty

  /**
   * Returns the current filter predicate
   */
  val filterPredicateProperty: ObservableValue<Predicate<TreeItem<TreeObject>>> = filterTextProperty.map { filterString ->
    return@map if (filterString.isNullOrBlank()) {
      Predicate<TreeItem<TreeObject>> {
        true
      }
    } else {
      Predicate<TreeItem<TreeObject>> { treeItem ->
        val chartingDemoDescriptor = treeItem.value.descriptor ?: return@Predicate false

        //Check if this tree item itself is contained
        val containsSelf = contains(chartingDemoDescriptor, treeItem.value.configuration, filterString)
        if (containsSelf) {
          return@Predicate true
        }

        //check if any of the children is visible
        return@Predicate treeItem.children.any { child ->
          val childTreeObject = child.value
          childTreeObject != null && contains(requireNotNull(childTreeObject.descriptor), childTreeObject.configuration, filterString)
        }
      }
    }
  }

  /**
   * Returns true if the descriptor and (optional) configuration
   */
  private fun contains(
    chartingDemoDescriptor: ChartingDemoDescriptor<*>,
    predefinedConfiguration: PredefinedConfiguration<*>?,
    filterString: String
  ): Boolean {
    /**
     * All parts
     */
    val parts = filterString.split(' ', ',').toSet()

    val descriptor = chartingDemoDescriptor ?: return false

    //The full demo description
    val fullDemoDescription = descriptor.createFullDemoDescription(predefinedConfiguration)

    return fullDemoDescription.containsAll(parts, ignoreCase = true)
  }

  val filterPredicate: Predicate<TreeItem<TreeObject>> by filterPredicateProperty
}

/**
 * A demo descriptor and a configuration.
 */
data class DemoDescriptorWithPredefinedConfiguration(
  val demoDescriptor: ChartingDemoDescriptor<*>,
  val predefinedConfiguration: PredefinedConfiguration<*>?
)

data class TreeObject(
  val type: TreeNodeType,
  val category: DemoCategory? = null,
  val descriptor: ChartingDemoDescriptor<*>? = null,
  /**
   * The predefined configuration
   */
  val configuration: PredefinedConfiguration<*>? = null,
) {
  companion object {
    fun create(category: DemoCategory): TreeObject {
      return TreeObject(TreeNodeType.Category, category)
    }

    fun createDemo(descriptor: ChartingDemoDescriptor<*>, configuration: PredefinedConfiguration<*>?): TreeObject {
      return TreeObject(type = TreeNodeType.Demo, descriptor = descriptor, configuration = configuration)
    }

    fun createConfig(descriptor: ChartingDemoDescriptor<*>, configuration: PredefinedConfiguration<*>?): TreeObject {
      return TreeObject(type = TreeNodeType.PredefinedConfig, descriptor = descriptor, configuration = configuration)
    }
  }
}

enum class TreeNodeType {
  /**
   * Represents a main node - for a category
   */
  Category,

  /**
   * Represents a main demo node
   */
  Demo,

  /**
   * Represents a predefined config for a demo
   */
  PredefinedConfig
}
