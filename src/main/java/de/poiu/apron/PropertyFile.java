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
import de.poiu.apron.entry.PropertyEntry;
import de.poiu.apron.entry.Entry;
import de.poiu.apron.io.PropertyFileReader;
import de.poiu.apron.io.PropertyFileWriter;
import de.poiu.apron.escaping.EscapeUtils;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;


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
   * If a modifiable map is needed use the method {@link #toMap(java.util.Map) } to provide
   * the map to fill.
   *
   * @return a map with the key-value-pairs of this PropertyFile
   */
  public Map<String, String> toMap() {
    final Map<String, String> map= new LinkedHashMap<>();

    this.propertyEntries.entrySet().forEach((e) -> {
      map.put(e.getKey(), e.getValue().getValue().toString());
    });

    return map;
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
      map.put(e.getKey(), e.getValue().getValue().toString());
    });

    return map;
  }


  /**
   * Append a new entry to this PropertyFile.
   * Be aware that this method does not check for duplicate keys. Adding a new entry with the same
   * key as an already existing entry may lead to unexpected behaviour.
   *
   * @param entry the new Entry to append
   */
  void appendEntry(final Entry entry) {
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
  void appendEntry(final BasicEntry entry) {
    this.entries.add(entry);
  }


  /**
   * Append a new PropertyEntry to this PropertyFile.
   * Be aware that this method does not check for duplicate keys. Adding a new entry with the same
   * key as an already existing entry may lead to unexpected behaviour.
   *
   * @param entry the new Entry to append
   */
  void appendEntry(final PropertyEntry entry) {
    final String key= EscapeUtils.unescape(entry.getKey()).toString();
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
   */
  // The given strings must already be unescaped.
  // The corresponding PropertyEntry will be updated or created with the escaped values.
  public void setValue(final String key, final String value) {
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
   * The returned collection is an unmodifiable snapshot of the current entries.
   *
   * @return all entries in this PropertyFile
   */
  //FIXME: This is not clean. The returned list may be unmodifiable, but the contained entries
  //       are modifiable!
  //       This method should there remain package private
  List<Entry> getAllEntries() {
    return Collections.unmodifiableList(this.entries);
  }


  /**
   * This method does exactly the same as {@link #saveTo(java.io.File, java.nio.charset.Charset) }
   * but writes the file with UTF-8 encoding.
   *
   * @param file the file to write to
   * @see #update(java.io.File)
   * @see #overwrite(java.io.File)
   */
  public void saveTo(final File file) {
    this.saveTo(file, UTF_8);
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
   * FIXME: Option, gelöschte Schlüssel zu entfernen?
   *
   * @param file the file to write to
   * @param charset the encoding to use for writing the entries.
   * @see #update(java.io.File, java.nio.charset.Charset)
   * @see #overwrite(java.io.File, java.nio.charset.Charset)
   */
  public void saveTo(final File file, final Charset charset) {
    if (file.exists()) {
      // if the file already exists, just update the values that have changed
      this.update(file, charset);
    } else {
      // if the file does not exist yet, just write out the entries
      this.overwrite(file, charset);
    }
  }


  /**
   * Saves the entries in this PropertyFile to the given OutputStream.
   * <p>
   * This method actually only delegates to {@link #overwrite(java.io.OutputStream, java.nio.charset.Charset) }.
   *
   * @param outputStream the OutputStream to write to
   * @see #overwrite(java.io.OutputStream)
   */
  public void saveTo(final OutputStream outputStream) {
    this.overwrite(outputStream, UTF_8);
  }


  /**
   * Saves the entries in this PropertyFile to the given OutputStream.
   * <p>
   * This method actually only delegates to {@link #overwrite(java.io.OutputStream, java.nio.charset.Charset) }.
   *
   * @param outputStream the OutputStream to write to
   * @param charset the encoding to use for writing the entries.
   * @see #overwrite(java.io.OutputStream, java.nio.charset.Charset)
   */
  public void saveTo(final OutputStream outputStream, final Charset charset) {
    this.overwrite(outputStream, charset);
  }


  /**
   * This method does exactly the same as {@link #update(java.io.File, java.nio.charset.Charset) },
   * but writes the file with UTF-8 encoding.
   *
   * @param file the file to update
   * @see #saveTo(java.io.File)
   * @see #overwrite(java.io.File)
   */
  public void update(final File file) {
    update(file, UTF_8);
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
   * FIXME: Option, gelöschte Schlüssel zu entfernen?
   *
   * @param file the file to update
   * @param charset the encoding in which to write the file
   * @see #saveTo(java.io.File, java.nio.charset.Charset)
   * @see #overwrite(java.io.File, java.nio.charset.Charset)
   */
  public void update(final File file, final Charset charset) {
    final PropertyFile existing= PropertyFile.from(file, charset);
    for (final Entry entry : this.entries) {
      // only process PropertyEntries when updating
      if (entry instanceof PropertyEntry) {
        final PropertyEntry propertyEntry= (PropertyEntry) entry;
        // if the key exists…
        final String unescapedKey= EscapeUtils.unescape(propertyEntry.getKey()).toString();
        if (existing.containsKey(unescapedKey)) {
          // …and the value is different…
          final String unescapedValue= EscapeUtils.unescape(propertyEntry.getValue()).toString();
          if (!existing.get(unescapedKey).equals(unescapedValue)) {
            // …update it
            existing.setValue(unescapedKey, propertyEntry.getValue().toString());
          }
        } else {
          // …otherwise (if the key does not exist yet)
          // append it
          existing.appendEntry(propertyEntry);
        }
      }
    }

    //TODO: Delete entries that exist in existing file, but not in new?

    // now write the modified PropertyFile to file
    existing.overwrite(file);
  }


  /**
   * This method does exactly the same as {@link #overwrite(java.io.File, java.nio.charset.Charset) },
   * but writes the file with UTF-8 encoding.
   *
   * @param file the file to write to
   * @see #saveTo(java.io.File)
   * @see #update(java.io.File)
   */
  public void overwrite(final File file) {
    overwrite(file, UTF_8);
  }


  /**
   * This method does exactly the same as {@link #overwrite(java.io.OutputStream, java.nio.charset.Charset) },
   * but writes to the stream with UTF-8 encoding.
   *
   * @param outputStream the OutputStream to write to
   * @see #saveTo(java.io.OutputStream)
   */
  public void overwrite(final OutputStream outputStream) {
    overwrite(outputStream, UTF_8);
  }


  /**
   * Saves this PropertyFiles entries to the given file. This method will always write a new
   * file. If the given file already exists it will be overwritten with the entries of this
   * PropertyFile.
   * <p>
   * The file is written in the given encoding.
   *
   * @param file the file to write to
   * @param charset the encoding in which to write the file
   * @see #saveTo(java.io.File)
   * @see #update(java.io.File)
   */
  public void overwrite(final File file, final Charset charset) {
    try(final PropertyFileWriter writer= new PropertyFileWriter(file, charset)) {
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
   * @param charset the encoding in which to write to the stream
   * @see #saveTo(java.io.OutputStream, java.nio.charset.Charset)
   */
  public void overwrite(final OutputStream outputStream, final Charset charset) {
    try(final PropertyFileWriter writer= new PropertyFileWriter(outputStream, charset)) {
      for (final Entry entry : this.entries) {
        writer.writeEntry(entry);
      }
    }catch (IOException ex) {
      throw new RuntimeException("Error writing PropertyFile. ", ex);
    }
  }
}

