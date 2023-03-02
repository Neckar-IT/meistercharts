package com.meistercharts.demojs

import com.meistercharts.demo.ChartingDemoDescriptor
import com.meistercharts.demo.DemoCategory
import com.meistercharts.demo.createFullDemoDescription
import com.meistercharts.js.CanvasFontMetricsCalculatorJS.Companion.logger
import it.neckar.open.collections.fastForEachIndexed
import it.neckar.open.observable.ObservableObject
import it.neckar.logging.debug
import it.neckar.logging.info
import kotlinx.browser.document
import kotlinx.browser.localStorage
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.get
import org.w3c.dom.set

/**
 * Creates a tabbed view that allows to select and configure various demo applications
 */
class ChartingDemosWithNavigationJS : AbstractChartingDemosJS() {
  /**
   * The top panel above the navigation - contains the search box etc.
   */
  private val navigationTop = document.getElementById("navigationTop") ?: throw IllegalStateException("element with id <navigationTop> must not be null")

  /**
   * Contains the navigation tree - is scrollable
   */
  private val navigationTree = document.getElementById("navigationTree") ?: throw IllegalStateException("element with id <navigationTree> must not be null")

  init {
    createNavigation()
  }

  private fun createNavigation() {
    //There seems to be a bug with the Google Chrome browser that leads to memory leaks when using SELECT-elements.
    //In order to prevent those memory leaks the configuration pane (which contains SELECT-elements) can be hidden.
    val showConfigurationPane = ObservableObject(true)
    val showConfigurationPaneCheckBox = document.checkBox(showConfigurationPane, "Show configuration")
    showConfigurationPaneCheckBox.style.display = "block"
    showConfigurationPaneCheckBox.style.paddingLeft = "5px"
    showConfigurationPaneCheckBox.style.paddingRight = "5px"
    showConfigurationPaneCheckBox.style.paddingTop = "5px"
    showConfigurationPaneCheckBox.style.paddingBottom = "10px"
    navigationTop.appendChild(showConfigurationPaneCheckBox)

    val searchField = document.createElement("INPUT") as HTMLInputElement
    searchField.setAttribute("type", "search")
    searchField.setAttribute("name", "nav-search")
    searchField.setAttribute("placeholder", "search")
    searchField.addEventListener("input", {
      hideNavigationButtons(navigationTree, searchField.value)
    })
    searchField.classList.add("searchField")
    navigationTop.appendChild(searchField)


    val demoDescriptorClassNameToRestore = localStorage["lastDemoDescriptor"]
    val demoDescriptorConfigIndexToRestore = localStorage["lastDemoDescriptorConfigIndex"]?.toIntOrNull()

    var lastActiveNavigationButton: HTMLElement? = null
    val demoCreator: (ChartingDemoDescriptor<*>) -> Unit = { descriptor ->
      val predefinedConfigurations = if (descriptor.predefinedConfigurations.isEmpty()) {
        //add default configuration
        listOf(null)
      } else {
        descriptor.predefinedConfigurations
      }
      logger.debug { "Preparing demo descriptor <${descriptor.name}> with ${predefinedConfigurations.size} configurations" }

      predefinedConfigurations.fastForEachIndexed { index, predefinedConfig ->
        val navigationButton = document.createElement("DIV") as HTMLElement
        navigationButton.classList.add("navButton")

        descriptor.createFullDemoDescription(predefinedConfig).let {
          navigationButton.innerText = it
          navigationButton.setAttribute("title", it)
        }

        if (index != 0 && predefinedConfig != null) {
          navigationButton.classList.add("config")
        }

        navigationButton.addEventListener("click", {
          lastActiveNavigationButton?.classList?.remove("navButtonActive")
          navigationButton.classList.add("navButtonActive")
          lastActiveNavigationButton = navigationButton
          startDemo(descriptor, predefinedConfig, showConfigurationPane.get())

          //Save the selection
          localStorage["lastDemoDescriptor"] = descriptor::class.simpleName.orEmpty()
          localStorage["lastDemoDescriptorConfigIndex"] = index.toString()
        })

        //Check if this button should be restored
        if (descriptor::class.simpleName == demoDescriptorClassNameToRestore
          && index == demoDescriptorConfigIndexToRestore
        ) {
          navigationButton.click()
        }

        navigationTree.appendChild(navigationButton)
      }
    }

    val byCategory = getDemoDescriptors().groupBy {
      it.category
    }
    DemoCategory.values().forEach { category ->
      val descriptors = byCategory[category] ?: return@forEach
      val categoryLabel = document.createElement("H5") as HTMLElement
      categoryLabel.classList.add("categoryLabel")
      categoryLabel.innerText = category.name
      navigationTree.appendChild(categoryLabel)

      descriptors.sortedBy {
        it.name
      }.forEach(demoCreator)
    }

  }


  /**
   * Hide all navigation buttons that do not contain the given [searchFieldValue]
   */
  private fun hideNavigationButtons(navigationElement: Element, searchFieldValue: String) {
    logger.info {
      "hideNavigationButtons: $searchFieldValue"
    }
    for (i in 0 until navigationElement.children.length) {
      val navChildElement = navigationElement.children.item(i)
      if (navChildElement !is HTMLElement) {
        continue
      }
      if (navChildElement.classList.contains("navButton")) {
        if (navChildElement.textContent?.contains(searchFieldValue, true) == true) {
          navChildElement.style.display = "block"
        } else {
          navChildElement.style.display = "none"
        }
      }
    }
  }


}

