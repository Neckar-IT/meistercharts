/*
 * Copyright (C) 2013-2026 Neckar IT GmbH, Mössingen, Germany
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Linking this library statically or dynamically with other modules is
 * making a combined work based on this library. Thus, the terms and
 * conditions of the GNU General Public License cover the whole combination.
 *
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules, regardless of
 * the license terms of these independent modules, and to copy and distribute
 * the resulting combined work under terms of your choice, provided that every
 * copy of the combined work is accompanied by a complete copy of the source
 * code of this library.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package it.neckar.open.collections

/**
 * Generic interface for "item + quantity" collections with aggregate operations.
 *
 * Models a list of [entries] where each [Entry] pairs an item of type [I] with an [Int] amount.
 * Typical use: shopping carts, material/BOM lists, equipment selections, resource allocations.
 *
 * # Conventions for concrete implementations
 *
 * 1. Implement `Selection<I, MySelection.Entry>` and add a nested `Entry` data class implementing `Selection.Entry<I>`.
 * 2. Allow `amount = 0` as a legitimate UI intermediate state. Use [entriesNonEmpty] to filter when displaying or persisting.
 * 3. Provide `with(item: I, amount: Int): Self` that upserts (`amount = 0` keeps the entry, `amount < 0` throws).
 * 4. Provide `companion object { fun empty(); operator fun invoke(item: I, amount: Int = 1) }`.
 * 5. Domain-specific aggregates (`totalPriceAt`, `totalWeight`, ...) are added per Selection — this interface only carries universal aggregates.
 *
 * See `internal/patterns/docs/kotlin/selection-with-aggregates.md` and the
 * `BookOrder` living example in the patterns project.
 *
 * @param I the type of items selected
 * @param E the concrete entry type
 */
interface Selection<I, E : Selection.Entry<I>> {

  /**
   * The entries of this selection in insertion order.
   *
   * Entries with `amount = 0` are legitimate intermediate states (e.g., a UI temporarily
   * setting an entry to zero before the user picks a new value). Use [entriesNonEmpty] to
   * filter them out when iterating for display or persistence.
   */
  val entries: List<E>

  /**
   * One entry of a [Selection]: a single [item] paired with an [amount].
   */
  interface Entry<ITEM> {
    /**
     * The item this entry refers to.
     */
    val item: ITEM

    /**
     * The selected amount. Must be `>= 0`. `0` is a legitimate intermediate state.
     */
    val amount: Int

    /**
     * `true` if [amount] is `0`.
     *
     * An entry with `amount = 0` is a legitimate UI intermediate state; this property
     * names that state at the entry level so call sites can read `it.isEmpty` instead
     * of `it.amount == 0`.
     */
    val isEmpty: Boolean
      get() = amount == 0

    val isNotEmpty: Boolean
      get() = isEmpty.not()
  }

  /**
   * `true` if no entry has an [amount][Entry.amount] greater than zero.
   *
   * An empty [entries] list and a list of entries that are all [Entry.isEmpty] are both considered empty.
   */
  val isEmpty: Boolean
    get() = entries.all { it.isEmpty }

  val isNotEmpty: Boolean
    get() = isEmpty.not()

  /**
   * The non-empty entries (those with `amount > 0`).
   *
   * Use this when displaying or persisting the selection; entries with `amount = 0` are
   * intermediate UI states and typically should not appear in totals or summaries.
   */
  val entriesNonEmpty: List<E>
    get() = entries.filter { it.isNotEmpty }

  /**
   * The sum of all entry amounts. Entries with `amount = 0` contribute nothing.
   */
  val totalAmount: Int
    get() = entries.sumOf { it.amount }

  /**
   * The items of all entries in entry order.
   *
   * Note: includes items of entries with `amount = 0`.
   */
  val items: List<I>
    get() = entries.map { it.item }

  /**
   * The amount selected for [item], or `0` if the item is not in the selection.
   *
   * O(n) lookup over [entries].
   */
  operator fun get(item: I): Int =
    entries.firstOrNull { it.item == item }?.amount ?: 0
}
