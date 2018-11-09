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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;


/**
 * Specifies how to handle comment lines and empty lines when reordering the properties in .properties files.
 *
 * @author mherrn
 * @since 2.0.0
 */
public enum AttachCommentsTo {
  /**
   * Comments and empty lines are attached to the key-value pair <i>after</i> them.
   */
  NEXT_PROPERTY {
    /**
     * Creates a list of OrderableEntries where BasicEntries are combined with the PropertyEntry directly
     * following them. BasicEntries at the end of the given list form their own OrderableEntry.
     *
     * @param entries the entries to convert
     * @return the result of the conversion
     */
    @Override
    OrderableEntryList toOrderableEntries(final List<Entry> entries) {
      final List<OrderableEntry> orderableEntries= new ArrayList<>();

      final List<Entry> buffer= new ArrayList<>();
      for (final Entry entry : entries) {
        buffer.add(entry);
        if (entry instanceof PropertyEntry) {
          orderableEntries.add(new OrderableEntry(buffer));
          buffer.clear();
        }
      }

      if (!buffer.isEmpty()) {
        orderableEntries.add(new OrderableEntry(buffer));
      }

      return new OrderableEntryList(orderableEntries);
    }

    /**
     * Sorts a list of OrderableEntries.
     * All BasicEntries that are not connected to a PropertyEntry are put to the end of the list.
     *
     * @param orderableEntries the OrderableEntries to sort
     */
    @Override
    void sort(final List<OrderableEntry> orderableEntries) {
      Collections.sort(orderableEntries, (final OrderableEntry o1, final OrderableEntry o2) -> {
        if (!o1.propertyEntry.isPresent() && !o2.propertyEntry.isPresent()) {
          return 0;
        }

        if (!o1.propertyEntry.isPresent()) {
          return 1;
        }

        if (!o2.propertyEntry.isPresent()) {
          return -1;
        }

        return o1.propertyEntry.get().getKey().toString().compareTo(
          o2.propertyEntry.get().getKey().toString());
      });
    }
  },


  /**
   * Comments and empty lines are attached to the key-value pair <i>before</i> them.
   */
  PREV_PROPERTY {
    /**
     * Creates a list of OrderableEntries where BasicEntries are combined with the PropertyEntry directly
     * preceding them. BasicEntries at the start of the given list form their own OrderableEntry.
     *
     * @param entries the entries to convert
     * @return the result of the conversion
     */
    @Override
    OrderableEntryList toOrderableEntries(final List<Entry> entries) {
      final List<OrderableEntry> orderableEntries= new ArrayList<>();

      final List<Entry> buffer= new ArrayList<>();
      for (final Entry entry : entries) {
        if (entry instanceof PropertyEntry) {
          orderableEntries.add(new OrderableEntry(buffer));
          buffer.clear();
        }
        buffer.add(entry);
      }

      if (!buffer.isEmpty()) {
        orderableEntries.add(new OrderableEntry(buffer));
      }

      return new OrderableEntryList(orderableEntries);
    }

    /**
     * Sorts a list of OrderableEntries.
     * All BasicEntries that are not connected to a PropertyEntry are put to the beginning of the list.
     *
     * @param orderableEntries the OrderableEntries to sort
     */
    @Override
    void sort(final List<OrderableEntry> orderableEntries) {
      Collections.sort(orderableEntries, (final OrderableEntry o1, final OrderableEntry o2) -> {
        if (!o1.propertyEntry.isPresent()&& !o2.propertyEntry.isPresent()) {
          return 0;
        }

        if (!o1.propertyEntry.isPresent()) {
          return -1;
        }

        if (!o2.propertyEntry.isPresent()) {
          return 1;
        }

        return o1.propertyEntry.get().getKey().toString().compareTo(
          o2.propertyEntry.get().getKey().toString());
      });
    }
  },


  /**
   * Comments and empty lines remain at their current position.
   * For example
   *
   * <pre>
   * # Comment 1
   * key F = F
   * key L = L
   *
   * # Comment 2
   * key B = B
   * # Comment 3
   * key A = A
   * </pre>
   *
   * would be changed (by sorting alphabetically) to
   *
   * <pre>
   * # Comment 1
   * key A = A
   * key B = B
   *
   * # Comment 2
   * key F = F
   * # Comment 3
   * key L = L
   * </pre>
   */
  ORIG_LINE {
    /**
     * Converts each entry to a single OrderableEntry. BasicEntries and PropertyEntries are not
     * combined.
     *
     * @param entries the entries to convert
     * @return the result of the conversion
     */
    @Override
    OrderableEntryList toOrderableEntries(final List<Entry> entries) {
      final List<OrderableEntry> orderableEntries= entries.stream()
        .map(e -> { return new OrderableEntry(Arrays.asList(e)); })
        .collect(Collectors.toList());
      return new OrderableEntryList(orderableEntries);
    }

    /**
     * Sorts a list of OrderableEntries by only switching the positions of PropertyEntries.
     * All BasicEntries are left at their position.
     *
     * @param orderableEntries the OrderableEntries to sort
     */
    @Override
    void sort(final List<OrderableEntry> orderableEntries) {
      // only sort the PropertyEntries
      final List<OrderableEntry> sortedPropertyEntries= orderableEntries.stream()
        .filter(_oe -> { return _oe.propertyEntry.isPresent(); })
        .collect(Collectors.toList());

      // sort the PropertyEntries
      Collections.sort(sortedPropertyEntries, (final OrderableEntry o1, final OrderableEntry o2) -> {
        if (!o1.propertyEntry.isPresent()
          || !o2.propertyEntry.isPresent()) {
          throw new IllegalArgumentException("The given OrderableEntries _must_ contain a PropertyEntry.");
        }

        return o1.propertyEntry.get().getKey().toString().compareTo(
          o2.propertyEntry.get().getKey().toString());
      });

      // now replace the existing PropertyEntries with the sorted ones (in the new order)
      for (final ListIterator<OrderableEntry> it= orderableEntries.listIterator(); it.hasNext(); ) {
        final OrderableEntry next= it.next();
        if (next.propertyEntry.isPresent()) {
          it.set(sortedPropertyEntries.remove(0));
        }
      }

      if (!sortedPropertyEntries.isEmpty()) {
        throw new IllegalStateException("sortedPropertyEntries is not empty! This should never happen!");
      }
    }
  },
  ;


  /**
   * Returns an OrderableEntryList for the given list of entries.
   * <p>
   * The OrderableEntryList combines PropertyEntries and BasicEntries according to this enums
   * strategy to attach comments.
   *
   * @param entries the entries for which to create an OrderableEntryList
   * @return the OrderableEntryList for the given list of entries
   */
  abstract OrderableEntryList toOrderableEntries(final List<Entry> entries);


  /**
   * Sorts a list of OrderableEntries according to this enums strategy to attach comments.
   *
   * @param orderableEntries the OrderableEntries to sort
   */
  abstract void sort(final List<OrderableEntry> orderableEntries);
}
