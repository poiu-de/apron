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
package de.poiu.apron.java.util;

import de.poiu.apron.ApronOptions;
import de.poiu.apron.PropertyFile;
import de.poiu.apron.entry.BasicEntry;
import de.poiu.apron.entry.PropertyEntry;
import de.poiu.apron.escaping.EscapeUtils;
import de.poiu.apron.io.PropertyFileWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.InvalidPropertiesFormatException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import static de.poiu.apron.java.util.Helper.isJava9OrHigher;
import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.UTF_8;


/**
 * A wrapper around {@link PropertyFile} that extends {@link java.util.Properties} to be
 * used as a drop-in-replacement for <code>java.util.Properties</code>.
 *
 * The implementation in this class tries to behave as exactly as possible like
 * {@link  java.util.Properties}. However, since {@link PropertyFile} does not extend
 * {@link java.util.Hashtable} and only allows strings as keys an values it has one major difference
 * in the methods that are specified my Hashtable (or its implemented interfaces).
 * Where it is possible to store non-String keys or values in a {@link java.util.Properties}
 * instance, this is not the case with this class and an IllegalArgumentException will be thrown
 * if a non-String value is provided as an argument to any these methods.
 *
 * @author mherrn
 * @since 2.1.0
 */
public class Properties extends java.util.Properties {

  /////////////////////////////////////////////////////////////////////////////
  //
  // Attributes

  private PropertyFile propertyFile= new PropertyFile();

  private final java.util.Properties defaults;


  /////////////////////////////////////////////////////////////////////////////
  //
  // Constructors

  /**
   * Creates a new empty Properties object with no default values.
   */
  public Properties() {
    this(new java.util.Properties());
  }


  /**
   * Creates a new empty Properties object with the given default values.
   *
   * @param defaults
   */
  public Properties(final java.util.Properties defaults) {
    this.defaults= defaults;
  }


  /**
   * Creates a new Properties object backed by the given PropertyFile with no default values.
   *
   * @param propertyFile
   */
  public Properties(final PropertyFile propertyFile) {
    this();
    this.propertyFile= propertyFile;
  }


  /**
   * Creates a new Properties object backed by the given PropertyFile with the given default values.
   *
   * @param propertyFile
   * @param defaults
   */
  public Properties(final PropertyFile propertyFile, final java.util.Properties defaults) {
    this(defaults);
    this.propertyFile= propertyFile;
  }


  /////////////////////////////////////////////////////////////////////////////
  //
  // Methods

  /**
   * This method is not implemented in {@link PropertyFile} and therefore will <i>always</i> throw
   * an UnsupportedOperationException.
   *
   * @see java.util.Properties#loadFromXML(java.io.InputStream)
   */
  @Override
  public synchronized void loadFromXML(InputStream in) throws IOException, InvalidPropertiesFormatException {
    throw new UnsupportedOperationException("Apron doesn't allow reading and writing XML files.");
  }


  /**
   * This method loads properties from an input stream and always assumes that the
   * properties are stored with an ISO 8859-1 character encoding.
   *
   * @param inStream
   * @throws IOException
   * @see java.util.Properties#load(java.io.InputStream)
   */
  @Override
  public synchronized void load(InputStream inStream) throws IOException {
    this.propertyFile= PropertyFile.from(inStream, ISO_8859_1);
  }


  /**
   * This method loads properties from a Reader and always assumes that the
   * properties are stored with an UTF-8 character encoding.
   * <p>
   * Be aware that this differs from {@link Properties}!
   *
   * @see java.util.Properties#load(java.io.Reader)
   * @param reader
   * @throws IOException
   */
  @Override
  public synchronized void load(final Reader reader) throws IOException {
    final char[] charBuffer = new char[8 * 1024];
    final StringBuilder builder= new StringBuilder();
    int numCharsRead;
    while ((numCharsRead = reader.read(charBuffer, 0, charBuffer.length)) != -1) {
        builder.append(charBuffer, 0, numCharsRead);
    }
    try (InputStream targetStream = new ByteArrayInputStream(builder.toString().getBytes(UTF_8))) {
      this.propertyFile= PropertyFile.from(targetStream, UTF_8);
      reader.close();
    }
  }


  @Override
  public Object getOrDefault(Object key, Object defaultValue) {
    if (this.propertyFile.containsKey((String) key)) {
      return this.propertyFile.get((String) key);
    } else {
      return defaultValue;
    }
  }


  @Override
  public Collection<Object> values() {
    return new ArrayList<>(this.propertyFile.values());
  }


  @Override
  public Set<Object> keySet() {
    return new LinkedHashSet<>(this.propertyFile.keys());
  }


  @Override
  public synchronized void clear() {
    this.propertyFile.getAllEntries().clear();
  }


  @Override
  public synchronized Object put(Object key, Object value) {
    final String previousValue= this.propertyFile.get((String) key);
    this.propertyFile.set((String) key, (String) value);
    return previousValue;
  }


  @Override
  public Object get(Object key) {
    return this.propertyFile.get((String) key);
  }


  @Override
  public boolean containsKey(Object key) {
    return this.propertyFile.containsKey((String) key);
  }


  @Override
  public boolean containsValue(Object value) {
    return this.propertyFile.values().contains(value);
  }


  @Override
  public boolean contains(Object value) {
    return this.containsValue(value);
  }


  @Override
  public Enumeration<Object> keys() {
    return Collections.enumeration(this.keySet());
  }


  @Override
  public boolean isEmpty() {
    return this.propertyFile.keys().isEmpty();
  }


  @Override
  public int size() {
    return this.propertyFile.propertiesSize();
  }


  @Override
  public String getProperty(String key) {
    if (this.propertyFile.containsKey(key)) {
      return this.propertyFile.get(key);
    } else {
      return this.defaults.getProperty(key);
    }
  }


  @Override
  public synchronized Object replace(Object key, Object value) {
    if (this.containsKey(key)) {
      return this.put(key, value);
    } else {
      return null;
    }
  }


  @Override
  public synchronized boolean replace(Object key, Object oldValue, Object newValue) {
    if (this.containsKey(key) && Objects.equals(this.get(key), oldValue)) {
      this.put(key, newValue);
      return true;
    } else {
      return false;
    }
  }


  @Override
  public synchronized boolean remove(Object key, Object value) {
    if (this.containsKey(key) && Objects.equals(this.get(key), value)) {
      this.remove(key);
      return true;
    } else {
      return false;
    }
  }


  @Override
  public synchronized Object putIfAbsent(Object key, Object value) {
    Object v = this.get(key);
    if (v == null) {
      v = this.put(key, value);
    }

    return v;
  }


  @Override
  public synchronized void putAll(Map<?, ?> t) {
    t.forEach((k, v) -> this.put(k, v));
  }


  @Override
  public synchronized Object remove(Object key) {
    final String previousValue= this.propertyFile.get((String) key);
    this.propertyFile.remove((String) key);
    return previousValue;
  }


  @Override
  public Enumeration<Object> elements() {
    return Collections.enumeration(this.values());
  }


  @Override
  public synchronized Object setProperty(String key, String value) {
    final String previousValue= this.propertyFile.get(key);
    this.propertyFile.set(key, value);
    return previousValue;
  }


  @Override
  public Enumeration<?> propertyNames() {
    final Enumeration<?> defaultPropertyName= this.defaults.propertyNames();
    final HashSet<String> propertyNames= new HashSet<>();
    propertyNames.addAll(this.propertyFile.keys());
    while (defaultPropertyName.hasMoreElements()) {
      propertyNames.add((String) defaultPropertyName.nextElement());
    }
    return Collections.enumeration(propertyNames);
  }


  @Override
  public String getProperty(String key, String defaultValue) {
    if (this.propertyFile.containsKey(key)) {
      return this.propertyFile.get(key);
    } else {
      return this.defaults.getProperty(key, defaultValue);
    }
  }


  @Override
  public synchronized void forEach(BiConsumer<? super Object, ? super Object> action) {
    this.propertyFile.toMap().forEach(action);
    final java.util.Properties g= new java.util.Properties();
  }


  /**
   * {@inheritDoc}
   *
   * <p>This method differs from the implementation in {@link java.util.Properties}, since it does
   * only allow Strings as keys and values. If either of them is not a String, an
   * {@link java.lang.IllegalArgumentException} will be thrown.
   *
   * @throws IllegalArgumentException if either the key or the value is not of type String
   */
  @Override
  public synchronized Object compute(Object key, BiFunction<? super Object, ? super Object, ?> remappingFunction) {
    if (! (key instanceof String)) {
      throw new IllegalArgumentException("Only strings are allowed as keys");
    }

    final String k= (String) key;
    final String oldValue = this.propertyFile.get(k);
    final Object newValue = remappingFunction.apply(key, oldValue);

    if (newValue != null && !(newValue instanceof String)) {
      throw new IllegalArgumentException("Only strings are allowed as values");
    }

    final String v= (String) newValue;

    if (oldValue != null ) {
      if (newValue != null) {
        this.propertyFile.set(k, v);
      } else {
        this.propertyFile.remove(k);
      }
    } else {
      if (newValue != null) {
        this.propertyFile.set(k, v);
      }
    }

    return v;
  }


  /**
   * {@inheritDoc}
   *
   * <p>This method differs from the implementation in {@link java.util.Properties}, since it does
   * only allow Strings as keys and values. If either of them is not a String, an
   * {@link java.lang.IllegalArgumentException} will be thrown.
   *
   * @throws IllegalArgumentException if either the key or the value is not of type String
   */
  @Override
  public synchronized Object merge(Object key, Object value, BiFunction<? super Object, ? super Object, ?> remappingFunction) {
    if (! (key instanceof String)) {
      throw new IllegalArgumentException("Only strings are allowed as keys");
    }

    if (isJava9OrHigher()) {
      if (value == null) {
        throw new NullPointerException("value may not be null");
      }
    }

    if (value != null && !(value instanceof String)) {
      throw new IllegalArgumentException("Only strings are allowed as values");
    }

    final String k= (String) key;
    final String v= (String) value;

    final String oldValue = this.propertyFile.get(k);
    final Object newValue = (oldValue == null) ? value : remappingFunction.apply(oldValue, v);

    if (newValue != null && !(newValue instanceof String)) {
      throw new IllegalArgumentException("Only strings are allowed as values");
    }

    final String nv= (String) newValue;

    if (newValue == null) {
      this.propertyFile.remove(k);
      return null;
    } else {
      this.propertyFile.set(k, nv);
      return nv;
    }
  }


  /**
   * {@inheritDoc}
   *
   * <p>This method differs from the implementation in {@link java.util.Properties}, since it does
   * only allow Strings as keys and values. If the computed value is not a String, an
   * {@link java.lang.IllegalArgumentException} will be thrown.
   * However if the key is not a String, then this method just returns <code>null</code> and does
   * <i>not</i> throw an Exception.
   *
   * @throws IllegalArgumentException if the computed value is not of type String
   */
  @Override
  public synchronized Object computeIfPresent(Object key, BiFunction<? super Object, ? super Object, ?> remappingFunction) {
    // a non-String is always absent, therefore we return null
    if (!(key instanceof String)) {
      return null;
    }

    final String k= (String) key;
    final String oldValue = this.propertyFile.get(k);
    final Object newValue = remappingFunction.apply(key, oldValue);

    if (newValue != null && !(newValue instanceof String)) {
      throw new IllegalArgumentException("Only strings are allowed as values");
    }

    if (this.propertyFile.get(k) == null) {
      return null;
    } else {

      final String v= (String) newValue;
      if (newValue != null) {
        this.propertyFile.set(k, v);
        return v;
      } else {
        this.propertyFile.remove(k);
        return null;
      }
    }
  }


  /**
   * {@inheritDoc}
   *
   * <p>This method differs from the implementation in {@link java.util.Properties}, since it does
   * only allow Strings as keys and values. If either of them is not a String, an
   * {@link java.lang.IllegalArgumentException} will be thrown.
   *
   * @throws IllegalArgumentException if either the key or the value is not of type String
   */
  @Override
  public synchronized Object computeIfAbsent(Object key, Function<? super Object, ?> mappingFunction) {
    if (! (key instanceof String)) {
      throw new IllegalArgumentException("Only strings are allowed as keys");
    }

    final String k= (String) key;

    if (this.propertyFile.containsKey(k)) {
      return this.propertyFile.get(k);
    }

    final Object newValue = mappingFunction.apply(key);

    if (newValue != null && !(newValue instanceof String)) {
      throw new IllegalArgumentException("Only strings are allowed as values");
    }

    final String v= (String) newValue;

    if (this.propertyFile.get(k) == null) {
     if (newValue != null) {
         this.propertyFile.set(k, v);
         return v;
     }
    }

    return null;
  }


  /**
   * {@inheritDoc}
   *
   * <p>This method differs from the implementation in {@link java.util.Properties}, since it does
   * only allow Strings as values. If one of them is not a String, an
   * {@link java.lang.IllegalArgumentException} will be thrown.
   *
   * @throws IllegalArgumentException if the value is not of type String
   */
  @Override
  public synchronized void replaceAll(BiFunction<? super Object, ? super Object, ?> function) {
    // store all the results of the function in a shadow store to not
    // remain in a half-modified state if one of those function calls
    // fails.
    // TODO: Write a test to verify this. Does j.u.P behave the same?
    final Map<String, String> shadowStore= new HashMap<>();

    for (final String key : this.propertyFile.keys()) {
      final Object newValue= function.apply(key, this.propertyFile.get(key));
      if (newValue == null) {
        throw new NullPointerException("The value may not be null (Current key is: "+key+")");
      }else if (!(newValue instanceof String)) {
        throw new IllegalArgumentException("Only strings are allowed as values");
      } else {
        shadowStore.put(key, (String) newValue);
      }
    }
    for (final Map.Entry<String, String> e : shadowStore.entrySet()) {
      this.propertyFile.set(e.getKey(), e.getValue());
    }
  }


  @Override
  public Set<Map.Entry<Object, Object>> entrySet() {
    final Map<Object, Object> resultMap= new LinkedHashMap<>();
    this.propertyFile.toMap().forEach((k, v) -> resultMap.put(k, v));
    return resultMap.entrySet();
  }


  @Override
  public Set<String> stringPropertyNames() {
    final Set<String> propertyNames= new LinkedHashSet<>(this.defaults.stringPropertyNames());
    propertyNames.addAll(this.propertyFile.keys());
    return propertyNames;
  }


  @Override
  public void list(PrintWriter out) {
    out.println("-- listing properties --");
    final Map<String, String> h= new LinkedHashMap<>();
    this.defaults.forEach((k, v) -> h.put((String) k, (String) v));
    h.putAll(this.propertyFile.toMap());
    h.entrySet().forEach((e) -> {
      final String key = e.getKey();
      String val = e.getValue();
      if (val.length() > 40) {
        val = val.substring(0, 37) + "...";
      }
      out.println(key + "=" + val);
    });
  }


  @Override
  public void list(PrintStream out) {
    out.println("-- listing properties --");
    final Map<String, String> h= new LinkedHashMap<>();
    this.defaults.forEach((k, v) -> h.put((String) k, (String) v));
    h.putAll(this.propertyFile.toMap());
    h.entrySet().forEach((e) -> {
      final String key = e.getKey();
      String val = e.getValue();
      if (val.length() > 40) {
        val = val.substring(0, 37) + "...";
      }
      out.println(key + "=" + val);
    });
  }


  /**
   * This method is not implemented in {@link PropertyFile} and therefore will <i>always</i> throw
   * an UnsupportedOperationException.
   *
   * @see java.util.Properties#storeToXML(java.io.OutputStream, String, Charset)
   */
  //@Override
  public void storeToXML(OutputStream os, String comment, Charset charset) throws IOException {
    throw new UnsupportedOperationException("Apron doesn't allow reading and writing XML files.");
  }


  /**
   * This method is not implemented in {@link PropertyFile} and therefore will <i>always</i> throw
   * an UnsupportedOperationException.
   *
   * @see java.util.Properties#storeToXML(java.io.OutputStream, String, String)
   */
  @Override
  public void storeToXML(OutputStream os, String comment, String encoding) throws IOException {
    throw new UnsupportedOperationException("Apron doesn't allow reading and writing XML files.");
  }


  /**
   * This method is not implemented in {@link PropertyFile} and therefore will <i>always</i> throw
   * an UnsupportedOperationException.
   *
   * @see java.util.Properties#storeToXML(java.io.OutputStream, String)
   */
  @Override
  public void storeToXML(OutputStream os, String comment) throws IOException {
    throw new UnsupportedOperationException("Apron doesn't allow reading and writing XML files.");
  }


  @Override
  public void store(OutputStream out, String comments) throws IOException {
    final ApronOptions apronOptions= ApronOptions.create().with(ISO_8859_1);
    try (final PropertyFileWriter writer= new PropertyFileWriter(out, apronOptions)) {
      if (comments != null) {
        writer.writeEntry(new BasicEntry(comments + "\n"));
      }
      for (final de.poiu.apron.entry.Entry e : this.propertyFile.getAllEntries()) {
        writer.writeEntry(e);
      }
    }
  }


  /**
   * This methods always writes in UTF-8 encoding.
   *
   * @param writer
   * @param comments
   * @throws IOException
   */
  @Override
  public void store(Writer writer, String comments) throws IOException {
    final ApronOptions apronOptions= ApronOptions.create().with(UTF_8);
    try (final ByteArrayOutputStream out= new ByteArrayOutputStream();
      final PropertyFileWriter propertyFileWriter= new PropertyFileWriter(out, apronOptions);) {
      if (comments != null) {
        propertyFileWriter.writeEntry(new BasicEntry(comments + "\n"));
      }
      for (final de.poiu.apron.entry.Entry e : this.propertyFile.getAllEntries()) {
        propertyFileWriter.writeEntry(e);
      }

      propertyFileWriter.close();

      final String s= new String(out.toByteArray(), UTF_8);
      writer.append(s);
    }
  }


  @Override
  @Deprecated
  public void save(OutputStream out, String comments) {
    try {
      this.store(out, comments);
    } catch (IOException ex) {
    }
  }


  @Override
  public synchronized Object clone() {
    final Properties clone= new Properties();

    this.propertyFile.getAllEntries().forEach((e) -> {
      if (e instanceof BasicEntry) {
        final BasicEntry basicEntry= (BasicEntry) e;
        clone.propertyFile.appendEntry(new BasicEntry(basicEntry.toCharSequence()));
      } else if (e instanceof PropertyEntry) {
        final PropertyEntry propertyEntry= (PropertyEntry) e;
        clone.propertyFile.appendEntry(
          new PropertyEntry(propertyEntry.getLeadingWhitespace(),
            propertyEntry.getKey(),
            propertyEntry.getSeparator(),
            propertyEntry.getValue(),
            propertyEntry.getLineEnding()));
      } else {
        throw new RuntimeException("Unexpected Entry type: "+e.getClass());
      }
    });

    return clone;
  }


  @Override
  public synchronized int hashCode() {
    return this.propertyFile.getAllEntries().hashCode();
  }


  @Override
  public synchronized boolean equals(Object o) {
    if (o == this) {
      return true;
    }

    if (!(o instanceof Properties)) {
      return false;
    }

    final Properties that= (Properties) o;

    if (this.propertyFile.entriesSize() != (that.propertyFile.entriesSize())) {
      return false;
    }

    return this.propertyFile.getAllEntries()
      .equals(that.propertyFile.getAllEntries());
  }


  @Override
  public synchronized String toString() {
    final StringBuilder sb= new StringBuilder();
    sb.append("{");

    if (!this.propertyFile.getAllEntries().isEmpty()) {
      sb.append("\n");
    }
    this.propertyFile.getAllEntries().forEach(e -> {
      sb.append(EscapeUtils.unescape(e.toCharSequence()));
      sb.append("\n");
    });

    sb.append("}");
    return sb.toString();
  }
}
