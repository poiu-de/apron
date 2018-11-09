/*
 * Copyright (C) 2018 Marco Herrn
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.poiu.apron.reformatting;

import de.poiu.apron.entry.Entry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * A list of {@link OrderableEntry OrderableEntries}.
 * <p>
 * Be aware that this class does not implement the {@link java.util.List} interface, but instead
 * only provides the methods that are meaningful for this class.
 *
 * @author mherrn
 */
class OrderableEntryList {


  /////////////////////////////////////////////////////////////////////////////
  //
  // Attributes

  /**
   * The actual List of OrderableEntries that comprise this OrderableEntryList.
   */
  private final List<OrderableEntry> orderableEntries= new ArrayList<>();


  /////////////////////////////////////////////////////////////////////////////
  //
  // Constructors

  /**
   * Creates a new OrderableEntryList from the given List of OrderableEntries.
   *
   * @param orderableEntries the OrderableEntries that comprise this OrderableEntryList
   */
  public OrderableEntryList(final List<OrderableEntry> orderableEntries) {
    this.orderableEntries.addAll(orderableEntries);
  }


  /////////////////////////////////////////////////////////////////////////////
  //
  // Methods

  /**
   * Returns the first (and hopefully only) OrderableEntry that contains a PropertyEntry with the
   * given key.
   * <p>
   * If no such OrderableEntry is found the returned Optional is empty.
   *
   * @param propertyKey the property-key to search for
   * @return the first OrderableEntry containing a PropertyEntry with the given key
   */
  //FIXME: This is a very naïve approach and not very performant
  //       To make it more perfomant we need to manage our own mapping
  public Optional<OrderableEntry> get(final CharSequence propertyKey) {
    return orderableEntries.stream()
      .filter(e -> { return e.propertyEntry.isPresent(); })
      .findFirst();
  }


  /**
   * Returns and removes the first (and hopefully only) OrderableEntry that contains a PropertyEntry
   * with the given key.
   * <p>
   * If no such OrderableEntry is found the returned Optional is empty and nothing is removed from
   * this OrderableEntryList.
   * <p>
   *
   * @param propertyKey the property-key to search for
   * @return the first OrderableEntry containing a PropertyEntry with the given key
   */
  public Optional<OrderableEntry> pop(final CharSequence propertyKey) {
    for (final Iterator<OrderableEntry> it= this.orderableEntries.iterator(); it.hasNext(); ) {
      final OrderableEntry e= it.next();

      if (e.propertyEntry.isPresent() && e.propertyEntry.get().getKey().equals(propertyKey)) {
        it.remove();
        return Optional.of(e);
      }
    }

    return Optional.empty();
  }


  /**
   * Returns the OrderableEntries in this OrderableEntryList.
   * <p>
   * The returned list ist the actual list and changes to this list will be reflected.
   *
   * @return the OrderableEntries in this OrderableEntryList
   */
  //FIXME: Should we return a mutable copy instead?
  public List<OrderableEntry> getAll() {
    return this.orderableEntries;
  }


  /**
   * Returns the Entries contained in this OrderableEntryLists OrderableEntries.
   * <p>
   * This is actually a <i>flattened</i> view on this OrderableEntryLists OrderableEntries.
   * Changes to this list will not be reflected in this OrderableEntryList.
   *
   * @return the Entries contained in this OrderableEntryLists OrderableEntries
   */
  public List<Entry> getAsEntries() {
    return Collections.unmodifiableList(
      this.orderableEntries.stream()
        .flatMap(_oe -> { return _oe.entries.stream(); })
        .collect(Collectors.toList())
    );
  }
}
