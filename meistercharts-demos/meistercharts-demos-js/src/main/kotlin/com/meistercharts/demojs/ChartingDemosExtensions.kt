/**
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
