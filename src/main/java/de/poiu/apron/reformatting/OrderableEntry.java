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
import de.poiu.apron.entry.PropertyEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;


/**
 * Encapsulates an (optional) PropertyEntry and a list of BasicEntries that are attached to that
 * PropertyEntry or stand on their own.
 *
 * @author mherrn
 */
class OrderableEntry {

  /**
   * The PropertyEntry of this OrderableEntry. In the case of BasicEntries that do not belong
   * to a PropertyEntry this will be empty.
   */
  final Optional<PropertyEntry> propertyEntry;

  /**
   * The list of entries that comprise this OrderableEntry. At max 1 PropertyEntry may be contained
   * in this list.
   */
  final List<Entry> entries;


  /**
   * Creates a new OrderableEntry with the given list of Entries.
   * <p>
   * This list may contain at max 1 PropertyEntry, but an arbitrary number of BasicEntries.
   * However an empty list is not accepted.
   *
   * @param entries the Entries that comprise this OrderableEntry
   */
  public OrderableEntry(final List<Entry> entries) {
    Objects.requireNonNull(entries);
    if (entries.isEmpty()) {
      throw new IllegalArgumentException("The given list of entries may not be empty.");
    }

    PropertyEntry pe= null;
    for (final Entry e : entries) {
      if (e instanceof PropertyEntry) {
        if (pe != null) {
          throw new RuntimeException("At max one PropertyEntry is allowed in the list of entries.");
        }

        pe= (PropertyEntry) e;
      }
    }

    this.propertyEntry= Optional.ofNullable(pe);
    this.entries= new ArrayList<>(entries);
  }


  @Override
  public String toString() {
    return "OrderableEntry{" + "propertyEntry=" + propertyEntry + ", entries=" + entries + '}';
  }
}
