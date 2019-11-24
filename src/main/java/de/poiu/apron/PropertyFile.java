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
package de.poiu.apron;

import de.poiu.apron.entry.BasicEntry;
import de.poiu.apron.entry.Entry;
import de.poiu.apron.entry.PropertyEntry;
import de.poiu.apron.escaping.EscapeUtils;
import de.poiu.apron.io.PropertyFileReader;
import de.poiu.apron.io.PropertyFileWriter;
import de.poiu.apron.java.util.Properties;
import de.poiu.apron.reformatting.ReformatOptions;
import de.poiu.apron.reformatting.Reformatter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Implementation of a PropertyFile that retains the order of entries and also empty and
 * comment lines when writing back to file.
 * <p>
 * This class is mainly intended to for updating actual .properties files without losing manual
 * formatting in the files.
 * <p>
 * This class provides the content of the properties file in two different ways:
 * <ul>
 *   <li>as a map of key-value-pairs (as a normal {@link java.util.Properties} class would do)</li>
 *   <li>as a list of {@link Entry Entries} that does not only contain key-value-pairs, but
 *       also all empty and comment lines</li>
 * </ul>
 * <p>
 * The Entries contain the <i>unescaped</i> content read from a .properties file. This allows
 * writing them back in exactly the same format as they are written (with all escapings, necessary
 * or not, with all line breaks and with all leading spaces).
 * <p>
 * The map contains the <i>escaped</i> key-value-pairs like a {@link java.util.Properties} object.
 * <p>
 * Be aware that this class is not threadsafe!
 * <p>
 *
 * @author mherrn
 */
public class PropertyFile {

  /////////////////////////////////////////////////////////////////////////////
  //
  // Attributes

  /** The Entries in this PropertyFile in their correct order. */
  private final List<Entry> entries= new ArrayList<>();

  /** A mapping of property keys to their corresponding Entries. */
  private final Map<String, PropertyEntry> propertyEntries= new LinkedHashMap<>();


  /////////////////////////////////////////////////////////////////////////////
  //
  // Constructors

  /**
   * Creates a new empty PropertyFile object.
   */
  public PropertyFile() {
  }


  /////////////////////////////////////////////////////////////////////////////
  //
  // Methods

  /**
   * Returns the key-value-pairs in this PropertyFile as an unmodifiable map.
   * This map is only a snapshot of the current state.
   * <p>
   * If a modifiable map is needed use the method {@link #toMap(java.util.Map)} to provide
   * the map to fill.
   *
   * @return a map with the key-value-pairs of this PropertyFile
   */
  public Map<String, String> toMap() {
    return this.toMap(new LinkedHashMap<>(this.propertiesSize()));
  }


  /**
   * Returns the key-value-pairs in this PropertyFile as a map.
   * This map is only a snapshot of the current state and will not be updated if the contents of
   * this PropertyFile change. Also changes to the map are not reflected in this PropertyFile.
   *
   * @param map the map in which to store the key-value-pairs of this PropertyFile
   * @return the given map with the key-value-pairs of this PropertyFile
   */
  public Map<String, String> toMap(final Map<String, String> map) {
    this.propertyEntries.entrySet().forEach((e) -> {
      final String unescapedValue= EscapeUtils.unescape(e.getValue().getValue()).toString();
      map.put(e.getKey(), unescapedValue);
    });

    return map;
  }


  /**
   * Returns a new java.util.Properties wrapper around this PropertyFile.
   *
   * This is actually the same as calling <code>new Properties(propertyFile)</code>.
   *
   * @return a wrapper around this PropertyFile
   * @since 2.1.0
   */
  public Properties asProperties() {
    return new Properties(this);
  }


  /**
   * Append a new entry to this PropertyFile.
   * Be aware that this method does not check for duplicate keys. Adding a new entry with the same
   * key as an already existing entry may lead to unexpected behaviour.
   *
   * @param entry the new Entry to append
   */
  public void appendEntry(final Entry entry) {
    Objects.requireNonNull(entry, "entry may not be null");

    if (entry instanceof BasicEntry) {
      this.appendEntry((BasicEntry) entry);
    } else if (entry instanceof PropertyEntry) {
      this.appendEntry((PropertyEntry) entry);
    } else {
      throw new IllegalArgumentException("Unexpected entry type "+entry.getClass()+" for entry "+entry);
    }
  }


  /**
   * Append a new BasicEntry to this PropertyFile.
   *
   * @param entry the new Entry to append
   */
  public void appendEntry(final BasicEntry entry) {
    this.entries.add(entry);
  }


  /**
   * Append a new PropertyEntry to this PropertyFile.
   * Be aware that this method does not check for duplicate keys. Adding a new entry with the same
   * key as an already existing entry may lead to unexpected behaviour.
   *
   * @param entry the new Entry to append
   */
  public void appendEntry(final PropertyEntry entry) {
    final String key= EscapeUtils.unescape(entry.getKey()).toString();
    //FIXME: What to do if the of this entry already exists?
    //       Throw a DuplicateKeyException?
    this.entries.add(entry);
    this.propertyEntries.put(key, entry);
  }


  /**
   * Sets the value for a specific key.
   * If this PropertyFile already contains the given key, its value will get updated.
   * Otherwise a new entry will be appended to the end of thie PropertyFile.
   *
   * @param key the key
   * @param value the (new) value for the given key
   * @since 2.1.0
   */
  // The given strings must already be unescaped.
  // The corresponding PropertyEntry will be updated or created with the escaped values.
  public void set(final String key, final String value) {
    if (this.propertyEntries.containsKey(key)) {
      final PropertyEntry propertyEntry= this.propertyEntries.get(key);
      final String escapedValue= EscapeUtils.escapePropertyValue(value).toString();
      propertyEntry.setValue(escapedValue);
      this.propertyEntries.put(key, propertyEntry);
    } else {
      final String escapedKey= EscapeUtils.escapePropertyKey(key).toString();
      final String escapedValue= EscapeUtils.escapePropertyValue(value).toString();
      final PropertyEntry entry= new PropertyEntry(escapedKey, escapedValue);
      this.propertyEntries.put(key, entry);
      this.entries.add(entry);
    }
  }

  /**
   * Sets the value for a specific key.
   * If this PropertyFile already contains the given key, its value will get updated.
   * Otherwise a new entry will be appended to the end of thie PropertyFile.
   *
   * @param key the key
   * @param value the (new) value for the given key
   * @deprecated Use {@link #set(java.lang.String, java.lang.String)} instead
   */
  // The given strings must already be unescaped.
  // The corresponding PropertyEntry will be updated or created with the escaped values.
  @Deprecated
  public void setValue(final String key, final String value) {
    this.set(key, value);
  }


  /**
   * Removes a key-value-pair and the corresponding PropertyEntry from this
   * PropertyFile.
   *
   * @param key the key of the key-value-pair to remove
   */
  public void remove(final String key) {
    final PropertyEntry removedEntry= this.propertyEntries.remove(key);
    this.entries.remove(removedEntry);
  }


  /**
   * Removes an Entry from this PropertyFile.
   * <p>
   * If this PropertyFile contains the Entry multiple times (which is possible for BasicEntries
   * like comments and blank lines) all occurrences will be removed.
   *
   * @param entry the Entry to remove from this PropertyFile
   */
  public void remove(final Entry entry) {
    final boolean wasRemoved= this.entries.removeAll(Collections.singleton(entry));
    if (wasRemoved && entry instanceof PropertyEntry) {
      for (final Iterator<PropertyEntry> it= this.propertyEntries.values().iterator(); it.hasNext(); ) {
        final PropertyEntry existingEntry= it.next();
        if (existingEntry.equals(entry)) {
          it.remove();
        }
      }
    }
  }


  /**
   * Replaces an Entry in this PropertyFile with another Entry.
   * The Entries do not need to be of the same type.
   * <p>
   * Be aware that this replaces only the first occurrence of <code>entry</code>.
   *
   * @param entry the Entry to replace
   * @param newEntry the Entry to replace it with
   * @return if the given Entry was found in this PropertyFile and replaced
   */
  public boolean replace(final Entry entry, final Entry newEntry) {
    //FIXME: What to do if a PropertyEntry with an already existing key is added this way?
    //       Throw a DuplicateKeyException?

    if (entry instanceof PropertyEntry) {
      this.propertyEntries.values().remove((PropertyEntry)entry);
    }

    if (newEntry instanceof PropertyEntry) {
      final PropertyEntry propertyEntry= (PropertyEntry) newEntry;
      final String key= EscapeUtils.unescape(propertyEntry.getKey()).toString();
      this.propertyEntries.put(key, propertyEntry);
    }

    for (final ListIterator<Entry> it= this.entries.listIterator(); it.hasNext(); ) {
      final Entry current = it.next();
      if (current.equals(entry)) {
        it.set(newEntry);
        return true;
      }
    }
    return false;
  }


  /**
   * Removes all Entries from this PropertyFile.
   *
   * @since 2.0.0
   */
  public void clear() {
    this.entries.clear();
    this.propertyEntries.clear();
  }


  /**
   * Sets the Entries for this PropertyFile.
   * All existing entries will be dropped.
   *
   * @param entries the new entries for this PropertyFile
   * @since 2.0.0
   */
  public void setEntries(final List<Entry> entries) {
    this.entries.clear();
    this.propertyEntries.clear();
    entries.forEach((entry) -> {
      this.appendEntry(entry);
    });
  }


  /**
   * Returns a set with the keys of all PropertyEntries in this PropertyFile.
   * Thie returned set is only a snapshot of the current state and will not be updated if the contents of
   * this PropertyFile change. Also changes to the set are not reflected in this PropertyFile.
   *
   * @return the keys in this PropertyFile
   */
  public LinkedHashSet<String> keys() {
    return new LinkedHashSet<>(this.propertyEntries.keySet());
  }


  /**
   * Returns a list with the values of all PropertyEntries in this PropertyFile.
   * Thie returned set is only a snapshot of the current state and will not be updated if the contents of
   * this PropertyFile change. Also changes to the list are not reflected in this PropertyFile.
   *
   * @return the values in this PropertyFile
   */
  public List<String> values() {
    final List<String> values= new ArrayList<>(this.propertyEntries.size());

    this.propertyEntries.forEach((key, propertyEntry) -> {
      values.add(EscapeUtils.unescape(propertyEntry.getValue()).toString());
    });

    return values;
  }


  /**
   * Returns the number of key-value-pairs in this property file.
   *
   * @return the number of key-value-pairs in this PropertyFile
   * @see #entriesSize()
   */
  public int propertiesSize() {
    return propertyEntries.size();
  }


  /**
   * Returns the number of Entries in this property file.
   * This includes also non-key-value-pairs like empty lines and comment lines.
   *
   * @return the number of entries in this PropertyFile
   * @see #propertiesSize()
   */
  public int entriesSize() {
    return this.entries.size();
  }


  /**
   * Checks whether this PropertyFile contains a PropertyEntry with the given key.
   * <p>
   * The given key must be unescaped (the actual key), not the escaped version like in the
   * actual .properties file (where spaces and other special characters need to be escaped).
   *
   * @param key the key to search for
   * @return whether this PropertyFile contains a PropertyEntry with the given key
   */
  public boolean containsKey(final String key) {
    return this.propertyEntries.containsKey(key);
  }


  /**
   * Returns the value for the given key.
   * <p>
   * The given key must be unescaped (the actual key), not the escaped version like in the
   * actual .properties file (where spaces and other special characters need to be escaped).
   * <p>
   * The returned value will also be the unescaped value.
   *
   * @param key the key whose value to return
   * @return the value of the given key or <code>null</code> if the key does not exist.
   */
  public String get(final String key) {
    final PropertyEntry propertyEntry= this.propertyEntries.get(key);
    if (propertyEntry != null) {
      return EscapeUtils.unescape(propertyEntry.getValue()).toString();
    } else {
      return null;
    }
  }


  /**
   * Returns the PropertyEntry for the given key.
   * <p>
   * The given key must be unescaped (the actual key), not the escaped version like in the
   * actual .properties file (where spaces and other special characters need to be escaped).
   * <p>
   * The returned PropertyEntry will contain the <i>escaped</i> key and value!
   *
   * @param key the key for which to return the PropertyEntry
   * @return the PropertyEntry for the given key or <code>null</code> if no PropertyEntry with the
   *         given key exists.
   * @since 2.0.1
   */
  public PropertyEntry getPropertyEntry(final String key) {
    return this.propertyEntries.get(key);
  }


  /**
   * Creates a new PropertyFile as an exact copy of the given PropertyFile.
   * <p>
   * If the given PropertyFile is null this will create a new empty PropertyFile.
   *
   * @param propertyFile the PropertyFile to copy
   * @return a new PropertyFile with the same content as the given one
   * @since 2.1.0
   */
  public static PropertyFile from(final PropertyFile propertyFile) {
    final PropertyFile copy= new PropertyFile();

    if (propertyFile == null) {
      return copy;
    }

    for (Entry e : propertyFile.getAllEntries()) {
      if (e instanceof BasicEntry) {
        copy.appendEntry(new BasicEntry(e.toCharSequence()));
      } else if (e instanceof PropertyEntry) {
        final PropertyEntry pe= (PropertyEntry) e;
        copy.appendEntry(new PropertyEntry(
          pe.getLeadingWhitespace(),
          pe.getKey(),
          pe.getSeparator(),
          pe.getValue(),
          pe.getLineEnding()));
      } else {
        throw new RuntimeException("Invalid Entry type: "+e.getClass().getName());
      }
    }

    return copy;
  }


  /**
   * Reads a PropertyFile from the given file.
   * <p>
   * This methods assumes the encoding of the file to be UTF-8.
   *
   * @param file the .properties file to read
   * @return a PropertyFile with the content from the given file
   */
  public static PropertyFile from(final File file) {
    return from(file, Charset.forName("UTF-8"));
  }


  /**
   * Reads a PropertyFile from the given file.
   * <p>
   * The .properties file must be in the correct encoding of the given charset.
   *
   * @param file the .properties file to read
   * @param charset the encoding of the .properties file
   * @return a PropertyFile with the content from the given file
   */
  public static PropertyFile from(final File file, final Charset charset) {
    try(final PropertyFileReader propertyFileReader= new PropertyFileReader(file, charset);) {
      final PropertyFile propertyFile= new PropertyFile();
      Entry entry;
      while ((entry= propertyFileReader.readEntry()) != null) {
        propertyFile.appendEntry(entry);
      }
      return propertyFile;
    } catch (IOException ex) {
      throw new RuntimeException("Error reading propertyFile "+file, ex);
    }
  }


  /**
   * Reads a PropertyFile from a Reader.
   *
   * @param reader the Reader to read the .properties file content from
   * @return a PropertyFile with the content from the given Reader
   */
  //FIXME: We cannot guess the charset when using a reader. Should we avoid this method?
  private static PropertyFile from(final Reader reader) {
    try(final PropertyFileReader propertyFileReader= new PropertyFileReader(reader);) {
      final PropertyFile propertyFile= new PropertyFile();
      Entry entry;
      while ((entry= propertyFileReader.readEntry()) != null) {
        propertyFile.appendEntry(entry);
      }
      return propertyFile;
    } catch (IOException ex) {
      throw new RuntimeException("Error reading from reader", ex);
    }
  }


  /**
   * Reads a PropertyFile from an InputStream.
   * <p>
   * This methods assumes the encoding of the file to be UTF-8.
   *
   * @param inputStream the InputStream to read the .properties file content from
   * @return a PropertyFile with the content from the given InputStream
   */
  public static PropertyFile from(final InputStream inputStream) {
    return from(inputStream, Charset.forName("UTF-8"));
  }


  /**
   * Reads a PropertyFile from an InputStream.
   * <p>
   * The .properties file must be in the correct encoding of the given charset.
   *
   * @param inputStream the InputStream to read the .properties file content from
   * @param charset the encoding of the InputStream
   * @return a PropertyFile with the content from the given InputStream
   */
  public static PropertyFile from(final InputStream inputStream, final Charset charset) {
    try(final PropertyFileReader propertyFileReader= new PropertyFileReader(inputStream, charset);) {
      final PropertyFile propertyFile= new PropertyFile();
      Entry entry;
      while ((entry= propertyFileReader.readEntry()) != null) {
        propertyFile.appendEntry(entry);
      }
      return propertyFile;
    } catch (IOException ex) {
      throw new RuntimeException("Error reading from input stream", ex);
    }
  }


  /**
   * Returns all entries in this PropertyFile in the correct order.
   * <p>
   * The returned list is the actual list of this PropertyFile. Changes to this PropertyFile are
   * therefore reflected in the returned list and changes to the returned list are reflected in
   * this PropertyFile.
   * <p>
   * Pay special attention to the fact that PropertyFile is not thread safe and the returned list
   * isn't also. Be sure to not modify or iterate over the returned list in one and accessing this
   * PropertyFile in another thread without synchronization.
   *
   * @return all entries in this PropertyFile
   */
  public List<Entry> getAllEntries() {
    return this.entries;
  }


  /**
   * Reformats this PropertyFiles Entries according to the default ReformatOptions.
   *
   * @throws de.poiu.apron.reformatting.InvalidFormatException if the given format string is invalid
   * @since 2.0.0
   */
  public void reformat() {
    final Reformatter reformatter= new Reformatter();
    reformatter.reformat(this);
  }


  /**
   * Reformats this PropertyFiles Entries according to the format string in the given ReformatOptions.
   * <p>
   * Please refer to the javadoc of {@link de.poiu.apron.reformatting.ReformatOptions#withFormat(java.lang.String)}
   * for a detailed description of the valid format strings.
   *
   * @param reformatOptions the reformat options to use when reformatting this PropertyFile
   * @throws de.poiu.apron.reformatting.InvalidFormatException if the given format string is invalid
   * @since 2.0.0
   */
  public void reformat(final ReformatOptions reformatOptions) {
    final Reformatter reformatter= new Reformatter(reformatOptions);
    reformatter.reformat(this);
  }


  /**
   * Reorders this PropertyFiles Entries alphabetically by the names of their keys.
   * <p>
   * Comments and empty lines will be attached to the key-value pair that follows them (according
   * to the default {@link de.poiu.apron.reformatting.ReformatOptions}).
   *
   * @since 2.0.0
   */
  public void reorderByKey() {
    final Reformatter reformatter= new Reformatter();
    reformatter.reorderByKey(this);
  }


  /**
   * Reorders this PropertyFiles Entries alphabetically by the names of their keys.
   * <p>
   * Comments and empty lines will be handled according to the given ReformatOptions.
   *
   * @param reformatOptions the ReformatOptions to use when reordering. Actually only the
   *                        {@link de.poiu.apron.reformatting.AttachCommentsTo} value is respected on reordering.
   * @since 2.0.0
   */
  public void reorderByKey(final ReformatOptions reformatOptions) {
    final Reformatter reformatter= new Reformatter(reformatOptions);
    reformatter.reorderByKey(this);
  }


  /**
   * Reorders this PropertyFiles Entries according to the order of those Entries keys in the given
   * reference file.
   * <p>
   * Keys that only exist in the file to reorder, but not in the reference file will be put to the
   * end of the file to reorder. Those entries are <code>not</code> reordered.
   * <p>
   * Comments and empty lines will be attached to the key-value pair that follows them (according
   * to the default {@link de.poiu.apron.reformatting.ReformatOptions}).
   *
   * @param template the reference file to be used as template for the reordering
   * @since 2.0.0
   */
  public void reorderByTemplate(final PropertyFile template) {
    final Reformatter reformatter= new Reformatter();
    reformatter.reorderByTemplate(template, this);
  }


  /**
   * Reorders this PropertyFiles Entries according to the order of those Entries keys in the given
   * reference file.
   * <p>
   * Keys that only exist in the file to reorder, but not in the reference file will be put to the
   * end of the file to reorder. Those entries are <code>not</code> reordered.
   * <p>
   * Comments and empty lines will handled according to the {@link de.poiu.apron.reformatting.AttachCommentsTo} value in the given
   * ReformatOptions.
   *
   * @param template the reference file to be used as template for the reordering
   * @param reformatOptions the ReformatOptions to use when reordering. Actually only the
   *                        {@link de.poiu.apron.reformatting.AttachCommentsTo} value is respected on reordering.
   * @since 2.0.0
   */
  public void reorderByTemplate(final PropertyFile template, final ReformatOptions reformatOptions) {
    final Reformatter reformatter= new Reformatter(reformatOptions);
    reformatter.reorderByTemplate(template, this);
  }


  /**
   * This method does exactly the same as {@link #saveTo(java.io.File, de.poiu.apron.ApronOptions)}
   * using default options.
   * <p>
   * See {@link de.poiu.apron.ApronOptions} for a desciption of the default values.
   *
   * @param file the file to write to
   * @see #update(java.io.File)
   * @see #overwrite(java.io.File)
   */
  public void saveTo(final File file) {
    this.saveTo(file, ApronOptions.create());
  }


  /**
   * Saves the entries in this PropertyFile to the given file.
   * <p>
   * If the given file already exists, it is treated as a .properties file and the key-value-pairs
   * of this .properties file will only be updated or appended if missing.
   * <p>
   * When updating a .properties file, the unescaped existing value will be compared to the unescaped
   * value in this PropertyFiles entries. The value will only be updated if it differs.
   * This allows retaining the current format if only the formatting has changed, but not the actual
   * value. For example the escaped value
   *
   * <pre>
   * myKey = my\ value\ \
   *     over multiple \
   *     lines
   * </pre>
   *
   * will be treated as equal to
   *
   * <pre>
   * myKey = my value \
   * over multiple lines
   * </pre>
   *
   * because the unescaped value will in both cases be
   *
   * <pre>
   * myKey = my value over multiple lines
   * </pre>
   *
   * <p>
   * If the target file does not exist yet, it will be created and populated with the entries in this
   * PropertyFile.
   * <p>
   * This method will store the entries in the given encoding.
   * <p>
   * In most cases this method should be preferred over the corresponding <code>update(..)</code>
   * and <code>overwrite</code> methods if it is not important whether the target file already
   * exists or not.
   *
   * @param file the file to write to
   * @param options Options to respect when writing the .properties file
   * @see #update(java.io.File, de.poiu.apron.ApronOptions)
   * @see #overwrite(java.io.File, de.poiu.apron.ApronOptions)
   */
  public void saveTo(final File file, final ApronOptions options) {
    if (file.exists()) {
      // if the file already exists, just update the values that have changed
      this.update(file, options);
    } else {
      // if the file does not exist yet, just write out the entries
      this.overwrite(file, options);
    }
  }


  /**
   * Saves the entries in this PropertyFile to the given OutputStream.
   * <p>
   * This method actually only delegates to {@link #overwrite(java.io.OutputStream, de.poiu.apron.ApronOptions)}
   * using default options.
   * <p>
   * See {@link de.poiu.apron.ApronOptions} for a desciption of the default values.
   *
   * @param outputStream the OutputStream to write to
   * @see #overwrite(java.io.OutputStream)
   */
  public void saveTo(final OutputStream outputStream) {
    this.overwrite(outputStream, ApronOptions.create());
  }


  /**
   * Saves the entries in this PropertyFile to the given OutputStream.
   * <p>
   * This method actually only delegates to {@link #overwrite(java.io.OutputStream, de.poiu.apron.ApronOptions)}
   * using default options.
   * <p>
   * See {@link de.poiu.apron.ApronOptions} for a desciption of the default values.
   *
   * @param outputStream the OutputStream to write to
   * @param options Options to respect when writing the .properties file
   * @see #overwrite(java.io.OutputStream, de.poiu.apron.ApronOptions)
   */
  public void saveTo(final OutputStream outputStream, final ApronOptions options) {
    this.overwrite(outputStream, options);
  }


  /**
   * This method does exactly the same as {@link #update(java.io.File, de.poiu.apron.ApronOptions)}
   * using default options.
   * <p>
   * See {@link de.poiu.apron.ApronOptions} for a desciption of the default values.
   *
   * @param file the file to update
   * @see #saveTo(java.io.File)
   * @see #overwrite(java.io.File)
   */
  public void update(final File file) {
    update(file, ApronOptions.create());
  }


  /**
   * Saves this PropertyFiles entries to the given file. The given file must already exist.
   * Existing keys are overwritten with their new values if the unescaped existing value differs
   * from the unescaped value in this PropertyFile.
   * <p>
   * If this method is called with a non-existing file a RuntimeException, caused by a
   * FileNotFoundException will be thrown.
   * <p>
   * The file is written in the given encoding.
   *
   * @param file the file to update
   * @param options Options to respect when writing the .properties file
   * @see #saveTo(java.io.File, de.poiu.apron.ApronOptions)
   * @see #overwrite(java.io.File, de.poiu.apron.ApronOptions)
   */
  public void update(final File file, final ApronOptions options) {
    // first update the values of the key-value-pairs
    final PropertyFile existing= PropertyFile.from(file, options.getCharset());
    for (final Entry entry : this.entries) {
      // only process PropertyEntries when updating
      if (entry instanceof PropertyEntry) {
        final PropertyEntry propertyEntry= (PropertyEntry) entry;
        // if the key exists…
        final String unescapedKey= EscapeUtils.unescape(propertyEntry.getKey()).toString();
        if (existing.containsKey(unescapedKey)) {
          // …and the value is different…
          final PropertyEntry existingEntry= existing.getPropertyEntry(unescapedKey);
          final String unescapedValue= EscapeUtils.unescape(propertyEntry.getValue()).toString();
          final String existingUnescapedValue= EscapeUtils.unescape(existingEntry.getValue()).toString();
          if (!existingUnescapedValue.equals(unescapedValue)) {
            // …update it
            existingEntry.setValue(propertyEntry.getValue());
          }
        } else {
          // …otherwise (if the key does not exist yet)
          // append it
          existing.appendEntry(propertyEntry);
        }
      }
    }

    // then remove or comment out keys that are in the existing, but are missing in this PropertyFile
    final Stream<String> missingKeyStream= existing.propertyEntries.entrySet().stream()
      .filter(e -> !this.containsKey(e.getKey()))
      .map(e -> e.getKey());

    switch(options.getMissingKeyAction()) {
      case DELETE:
        missingKeyStream.collect(Collectors.toList()).forEach((missingKey) -> {
          existing.remove(missingKey);
        });
        break;
      case COMMENT:
        for (final String missingKey : missingKeyStream.collect(Collectors.toList())) {
          final PropertyEntry missingPropertyEntry= existing.propertyEntries.get(missingKey);
          final CharSequence commentedEntry= EscapeUtils.comment(missingPropertyEntry.toCharSequence());
          existing.replace(missingPropertyEntry, new BasicEntry(commentedEntry));
        }
        break;
    }

    // now write the modified PropertyFile to file
    try(final PropertyFileWriter writer= new PropertyFileWriter(file, options)) {
      for (final Entry entry : existing.entries) {
        writer.writeEntry(entry);
      }
    } catch (IOException ex) {
      throw new RuntimeException("Error writing PropertyFile "+file.getAbsolutePath(), ex);
    }
  }


  /**
   * This method does exactly the same as {@link #overwrite(java.io.File, de.poiu.apron.ApronOptions)}
   * using default options.
   * <p>
   * See {@link de.poiu.apron.ApronOptions} for a desciption of the default values.
   *
   * @param file the file to write to
   * @see #saveTo(java.io.File)
   * @see #update(java.io.File)
   */
  public void overwrite(final File file) {
    overwrite(file, ApronOptions.create());
  }


  /**
   * This method does exactly the same as {@link #overwrite(java.io.OutputStream, de.poiu.apron.ApronOptions)}
   * using default options.
   * <p>
   * See {@link de.poiu.apron.ApronOptions} for a desciption of the default values.
   *
   * @param outputStream the OutputStream to write to
   * @see #saveTo(java.io.OutputStream)
   */
  public void overwrite(final OutputStream outputStream) {
    overwrite(outputStream, ApronOptions.create());
  }


  /**
   * Saves this PropertyFiles entries to the given file. This method will always write a new
   * file. If the given file already exists it will be overwritten with the entries of this
   * PropertyFile.
   * <p>
   * The file is written in the given encoding.
   *
   * @param file the file to write to
   * @param options Options to respect when writing the .properties file
   * @see #saveTo(java.io.File)
   * @see #update(java.io.File)
   */
  public void overwrite(final File file, final ApronOptions options) {
    this.createPathTo(file);

    try(final PropertyFileWriter writer= new PropertyFileWriter(file, options.with(UnicodeHandling.BY_CHARSET))) {
      for (final Entry entry : this.entries) {
        writer.writeEntry(entry);
      }
    } catch (IOException ex) {
      throw new RuntimeException("Error writing PropertyFile "+file.getAbsolutePath(), ex);
    }
  }


  /**
   * Saves this PropertyFiles entries to the given OutputStream. An OutputStream does not
   * allow updating existing values, so this method always writes the current entries of this
   * PropertyFile to the stream.
   * <p>
   * The given encoding will be used for writing to the stream.
   *
   * @param outputStream the OutputStream to write to
   * @param options Options to respect when writing the .properties file
   * @see #saveTo(java.io.OutputStream, de.poiu.apron.ApronOptions)
   */
  public void overwrite(final OutputStream outputStream, final ApronOptions options) {
    try(final PropertyFileWriter writer= new PropertyFileWriter(outputStream, options)) {
      for (final Entry entry : this.entries) {
        writer.writeEntry(entry);
      }
    }catch (IOException ex) {
      throw new RuntimeException("Error writing PropertyFile. ", ex);
    }
  }


  /**
   * Creates the directories to the given file.
   * <p>
   * This does nothing if the directories already exist.
   *
   * @param file the file for which to create that parent paths
   */
  private void createPathTo(final File file) {
    if (!file.exists()) {
      file.getParentFile().mkdirs();
    }
  }
}

