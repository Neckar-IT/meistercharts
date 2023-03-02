package com.meistercharts.demojs

import kotlinx.browser.document
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLTableElement


/**
 * Adds a row to this [HTMLTableElement] that spans 2 columns and whose content is [cellContent]
 * @see twoColumnsRow
 */
fun HTMLTableElement.singleColumnRow(cellContent: HTMLElement) {
  appendChild(
    document.tableRow().apply {
      appendChild(
        document.tableCell().apply {
          setAttribute("colspan", "2")
          appendChild(
            cellContent
          )
        }
      )
    }
  )
}

/**
 * Adds a row to this [HTMLTableElement] with the given cell contents
 * @see singleColumnRow
 */
fun HTMLTableElement.twoColumnsRow(firstCell: HTMLElement, secondCell: HTMLElement) {
  appendChild(
    document.tableRow().also { tableRow ->
      tableRow.appendChild(
        document.tableCell().apply {
          appendChild(firstCell)
        }
      )
      tableRow.appendChild(
        document.tableCell().apply {
          appendChild(secondCell)
        }
      )
    }
  )
}

fun HTMLTableElement.threeColumnsRow(firstCell: HTMLElement, secondCell: HTMLElement, thirdCell: HTMLElement) {
  appendChild(
    document.tableRow().also { tableRow ->
      tableRow.appendChild(
        document.tableCell().apply {
          appendChild(firstCell)
        }
      )
      tableRow.appendChild(
        document.tableCell().apply {
          appendChild(secondCell)
        }
      )
      tableRow.appendChild(
        document.tableCell().apply {
          appendChild(thirdCell)
        }
      )
    }
  )
}
