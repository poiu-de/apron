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


import de.poiu.apron.PropertyFile;
import de.poiu.apron.entry.BasicEntry;
import de.poiu.apron.entry.PropertyEntry;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.Date;
import java.util.List;
import java.util.function.BiFunction;
import org.junit.Test;

import static de.poiu.apron.java.util.Helper.isJava9OrHigher;
import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 *
 * @author mherrn
 */
public class PropertiesTest {

  //TODO: Alle diese Tests sollten das gleiche mit einem j.u.P durchführen und das Ergebnis vergleichen

  @Test
  public void testCompute() {
    // - preparation
    final PropertyFile propertyFile= new PropertyFile();
    propertyFile.appendEntry(new BasicEntry("# some comment"));
    propertyFile.appendEntry(new PropertyEntry("myKey", "myValue"));
    propertyFile.appendEntry(new PropertyEntry("anotherKey", "anotherValue"));

    final Properties properties= new Properties(propertyFile);

    // assert our assumptions about the java.util.Properties implementation
    final java.util.Properties reference= new java.util.Properties();
    reference.put("myKey", "myValue");
    reference.put("anotherKey", "anotherValue");

    // should alter "myKey" mapping
    final Object rValue1 = reference.compute("myKey", (t, u) ->
      (u == null) ? "FOO" : u + " BAR"
    );
    // should create a new mapping
    final Object rValue2= reference.compute("non-existant", (t, u) ->
      (u == null) ? "FOO" : u + "BAR"
    );

    assertThat(reference).hasSize(3);
    assertThat(reference).containsEntry("myKey", "myValue BAR");
    assertThat(reference).containsEntry("anotherKey", "anotherValue");
    assertThat(reference).containsEntry("non-existant", "FOO");
    assertThat(rValue1).isEqualTo("myValue BAR");
    assertThat(rValue2).isEqualTo("FOO");


    // - execution
    // should alter "myKey" mapping
    final Object value1 = properties.compute("myKey", (t, u) ->
      (u == null) ? "FOO" : u + " BAR"
    );
    // should create a new mapping
    final Object value2= properties.compute("non-existant", (t, u) ->
      (u == null) ? "FOO" : u + "BAR"
    );

    // - validation
    assertThat(propertyFile.entriesSize()).isEqualTo(4);
    assertThat(propertyFile.getAllEntries()).containsExactly(
      new BasicEntry("# some comment"),
      new PropertyEntry("myKey", "myValue BAR"),
      new PropertyEntry("anotherKey", "anotherValue"),
      new PropertyEntry("non-existant", "FOO")
    );
    assertThat(value1).isEqualTo("myValue BAR");
    assertThat(value2).isEqualTo("FOO");
  }


  @Test
  public void testCompute_NonStringKey() {
    // - preparation
    final PropertyFile propertyFile= new PropertyFile();
    propertyFile.appendEntry(new BasicEntry("# some comment"));
    propertyFile.appendEntry(new PropertyEntry("myKey", "myValue"));
    propertyFile.appendEntry(new PropertyEntry("anotherKey", "anotherValue"));

    final Properties properties= new Properties(propertyFile);

    // - execution
    // should throw exception on change nothing
    assertThatThrownBy(() -> properties.compute(new Date(), (t, u) ->
      (u == null) ? "FOO" : u + "BAR"
    ))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("Only strings are allowed as keys");

    // - validation
    assertThat(propertyFile.entriesSize()).isEqualTo(3);
    assertThat(propertyFile.getAllEntries()).containsExactly(
      new BasicEntry("# some comment"),
      new PropertyEntry("myKey", "myValue"),
      new PropertyEntry("anotherKey", "anotherValue")
    );
  }


  @Test
  public void testCompute_NonStringValue() {
    // - preparation
    final PropertyFile propertyFile= new PropertyFile();
    propertyFile.appendEntry(new BasicEntry("# some comment"));
    propertyFile.appendEntry(new PropertyEntry("myKey", "myValue"));
    propertyFile.appendEntry(new PropertyEntry("anotherKey", "anotherValue"));

    final Properties properties= new Properties(propertyFile);

    // - execution && validation
    // should throw exception on change nothing
    assertThatThrownBy(() -> properties.compute("myKey", (t, u) ->
      (u == null) ? new Date() : new Date()
    ))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("Only strings are allowed as values");

    assertThatThrownBy(() -> properties.compute("non-existant", (t, u) ->
      (u == null) ? new Date() : new Date()
    ))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("Only strings are allowed as values");

    // - validation
    assertThat(propertyFile.entriesSize()).isEqualTo(3);
    assertThat(propertyFile.getAllEntries()).containsExactly(
      new BasicEntry("# some comment"),
      new PropertyEntry("myKey", "myValue"),
      new PropertyEntry("anotherKey", "anotherValue")
    );
  }


  @Test
  public void testCompute_throwsUncheckedException() {
    // - preparation
    final PropertyFile propertyFile= new PropertyFile();
    propertyFile.appendEntry(new BasicEntry("# some comment"));
    propertyFile.appendEntry(new PropertyEntry("myKey", "myValue"));
    propertyFile.appendEntry(new PropertyEntry("anotherKey", "anotherValue"));

    final Properties properties= new Properties(propertyFile);

    final RuntimeException thrown= new RuntimeException("lorem ipsum");

    // assert our assumptions about the java.util.Properties implementation
    final java.util.Properties reference= new java.util.Properties();
    reference.put("myKey", "myValue");
    reference.put("anotherKey", "anotherValue");

    // should throw exception on change nothing
    assertThatThrownBy(() -> reference.compute("myKey", (t, u) -> {
      throw thrown;
    }))
      .isSameAs(thrown)
      .hasMessage("lorem ipsum");
    assertThat(reference).hasSize(2);
    assertThat(reference).containsEntry("myKey", "myValue");
    assertThat(reference).containsEntry("anotherKey", "anotherValue");

    // - execution && validation
    assertThatThrownBy(() -> properties.compute("myKey", (t, u) -> {
      throw thrown;
    }))
      .isSameAs(thrown)
      .hasMessage("lorem ipsum");

    // assert that nothing has changed
    assertThat(propertyFile.entriesSize()).isEqualTo(3);
    assertThat(propertyFile.getAllEntries()).containsExactly(
      new BasicEntry("# some comment"),
      new PropertyEntry("myKey", "myValue"),
      new PropertyEntry("anotherKey", "anotherValue")
    );
  }

  @Test
  public void testCompute_ReturnsNull() {
    // - preparation
    final PropertyFile propertyFile= new PropertyFile();
    propertyFile.appendEntry(new BasicEntry("# some comment"));
    propertyFile.appendEntry(new PropertyEntry("myKey", "myValue"));
    propertyFile.appendEntry(new PropertyEntry("anotherKey", "anotherValue"));

    final Properties properties= new Properties(propertyFile);

    // assert our assumptions about the java.util.Properties implementation
    final java.util.Properties reference= new java.util.Properties();
    reference.put("myKey", "myValue");
    reference.put("anotherKey", "anotherValue");

    // should remove "myKey"
    reference.compute("myKey", (t, u) -> null);
    assertThat(reference).containsOnlyKeys("anotherKey");
    // should remove nothing
    properties.compute("non-existant", (t, u) -> null);
    assertThat(reference).containsOnlyKeys("anotherKey");


    // - execution
    // should remove "myKey"
    properties.compute("myKey", (t, u) -> null);
    // should remove nothing
    properties.compute("non-existant", (t, u) -> null);

    // - validation
    assertThat(propertyFile.entriesSize()).isEqualTo(2);
    assertThat(propertyFile.getAllEntries()).containsExactly(
      new BasicEntry("# some comment"),
      new PropertyEntry("anotherKey", "anotherValue")
    );
  }


  @Test
  public void testComputeIfAbsent() {
    // - preparation
    final PropertyFile propertyFile= new PropertyFile();
    propertyFile.appendEntry(new BasicEntry("# some comment"));
    propertyFile.appendEntry(new PropertyEntry("myKey", "myValue"));
    propertyFile.appendEntry(new PropertyEntry("anotherKey", "anotherValue"));

    final Properties properties= new Properties(propertyFile);

    // assert our assumptions about the java.util.Properties implementation
    final java.util.Properties reference= new java.util.Properties();
    reference.put("myKey", "myValue");
    reference.put("anotherKey", "anotherValue");

    // should do nothing and return the existing value
    final Object rValue1 = reference.computeIfAbsent("myKey", (t) ->
      "BAR"
    );
    // should create a new mapping and return its value
    final Object rValue2= reference.computeIfAbsent("non-existant", (t) ->
      "BAR"
    );

    assertThat(reference).hasSize(3);
    assertThat(reference).containsEntry("myKey", "myValue");
    assertThat(reference).containsEntry("anotherKey", "anotherValue");
    assertThat(reference).containsEntry("non-existant", "BAR");
    assertThat(rValue1).isEqualTo("myValue");
    assertThat(rValue2).isEqualTo("BAR");


    // - execution
    // should do nothing and return the existing value
    final Object value1 = properties.computeIfAbsent("myKey", (t) ->
      "BAR"
    );
    // should create a new mapping and return its value
    final Object value2= properties.computeIfAbsent("non-existant", (t) ->
      "BAR"
    );

    // - validation
    assertThat(propertyFile.entriesSize()).isEqualTo(4);
    assertThat(propertyFile.getAllEntries()).containsExactly(
      new BasicEntry("# some comment"),
      new PropertyEntry("myKey", "myValue"),
      new PropertyEntry("anotherKey", "anotherValue"),
      new PropertyEntry("non-existant", "BAR")
    );
    assertThat(value1).isEqualTo("myValue");
    assertThat(value2).isEqualTo("BAR");
  }


  @Test
  public void testComputeIfAbsent_NonStringKey() {
    // - preparation
    final PropertyFile propertyFile= new PropertyFile();
    propertyFile.appendEntry(new BasicEntry("# some comment"));
    propertyFile.appendEntry(new PropertyEntry("myKey", "myValue"));
    propertyFile.appendEntry(new PropertyEntry("anotherKey", "anotherValue"));

    final Properties properties= new Properties(propertyFile);

    // - execution
    // should throw exception on change nothing
    assertThatThrownBy(() -> properties.computeIfAbsent(new Date(), (t) ->
      "BAR"
    ))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("Only strings are allowed as keys");

    // - validation
    assertThat(propertyFile.entriesSize()).isEqualTo(3);
    assertThat(propertyFile.getAllEntries()).containsExactly(
      new BasicEntry("# some comment"),
      new PropertyEntry("myKey", "myValue"),
      new PropertyEntry("anotherKey", "anotherValue")
    );
  }


  @Test
  public void testComputeIfAbsent_NonStringValue() {

    // - preparation
    final PropertyFile propertyFile= new PropertyFile();
    propertyFile.appendEntry(new BasicEntry("# some comment"));
    propertyFile.appendEntry(new PropertyEntry("myKey", "myValue"));
    propertyFile.appendEntry(new PropertyEntry("anotherKey", "anotherValue"));

    final Properties properties= new Properties(propertyFile);

    // - execution
    // should throw exception on change nothing
    assertThatThrownBy(() -> properties.computeIfAbsent("non-existant", (t) ->
      new Date()
    ))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("Only strings are allowed as values");

    // - validation
    assertThat(propertyFile.entriesSize()).isEqualTo(3);
    assertThat(propertyFile.getAllEntries()).containsExactly(
      new BasicEntry("# some comment"),
      new PropertyEntry("myKey", "myValue"),
      new PropertyEntry("anotherKey", "anotherValue")
    );
  }


  @Test
  public void testComputeIfAbsent_throwsUncheckedException() {
    // - preparation
    final PropertyFile propertyFile= new PropertyFile();
    propertyFile.appendEntry(new BasicEntry("# some comment"));
    propertyFile.appendEntry(new PropertyEntry("myKey", "myValue"));
    propertyFile.appendEntry(new PropertyEntry("anotherKey", "anotherValue"));

    final Properties properties= new Properties(propertyFile);

    final RuntimeException thrown= new RuntimeException("lorem ipsum");

    // assert our assumptions about the java.util.Properties implementation
    final java.util.Properties reference= new java.util.Properties();
    reference.put("myKey", "myValue");
    reference.put("anotherKey", "anotherValue");

    // should do nothing and return null
    final Object rValue1= reference.computeIfAbsent("myKey", (t) -> {
      throw thrown;
    });
    // should throw exception and change nothing
    assertThatThrownBy(() -> reference.computeIfAbsent("non-existant", (t) -> {
      throw thrown;
    }))
      .isSameAs(thrown)
      .hasMessage("lorem ipsum");
    assertThat(reference).hasSize(2);
    assertThat(reference).containsEntry("myKey", "myValue");
    assertThat(reference).containsEntry("anotherKey", "anotherValue");
    assertThat(rValue1).isEqualTo("myValue");

    // - execution && validation
    // should do nothing and return null
    final Object value1= properties.computeIfAbsent("myKey", (t) -> {
      throw thrown;
    });
    // should throw exception and change nothing
    assertThatThrownBy(() -> properties.computeIfAbsent("non-existant", (t) -> {
      throw thrown;
    }))
      .isSameAs(thrown)
      .hasMessage("lorem ipsum");

    // assert that nothing has changed
    assertThat(propertyFile.entriesSize()).isEqualTo(3);
    assertThat(propertyFile.getAllEntries()).containsExactly(
      new BasicEntry("# some comment"),
      new PropertyEntry("myKey", "myValue"),
      new PropertyEntry("anotherKey", "anotherValue")
    );
    assertThat(value1).isEqualTo("myValue");
  }


  @Test
  public void testComputeIfAbsent_ReturnsNull() {
    // - preparation
    final PropertyFile propertyFile= new PropertyFile();
    propertyFile.appendEntry(new BasicEntry("# some comment"));
    propertyFile.appendEntry(new PropertyEntry("myKey", "myValue"));
    propertyFile.appendEntry(new PropertyEntry("anotherKey", "anotherValue"));

    final Properties properties= new Properties(propertyFile);

    // assert our assumptions about the java.util.Properties implementation
    final java.util.Properties reference= new java.util.Properties();
    reference.put("myKey", "myValue");
    reference.put("anotherKey", "anotherValue");

    // should remove nothing
    reference.computeIfAbsent("myKey", (t) -> null);
    assertThat(reference).containsOnlyKeys("myKey", "anotherKey");
    // should remove nothing
    properties.computeIfAbsent("non-existant", (t) -> null);
    assertThat(reference).containsOnlyKeys("myKey", "anotherKey");


    // - execution
    // should remove nothing
    properties.computeIfAbsent("myKey", (t) -> null);
    // should remove nothing
    properties.computeIfAbsent("non-existant", (t) -> null);

    // - validation
    assertThat(propertyFile.entriesSize()).isEqualTo(3);
    assertThat(propertyFile.getAllEntries()).containsExactly(
      new BasicEntry("# some comment"),
      new PropertyEntry("myKey", "myValue"),
      new PropertyEntry("anotherKey", "anotherValue")
    );
  }


  @Test
  public void testComputeIfPresent() {
    // - preparation
    final PropertyFile propertyFile= new PropertyFile();
    propertyFile.appendEntry(new BasicEntry("# some comment"));
    propertyFile.appendEntry(new PropertyEntry("myKey", "myValue"));
    propertyFile.appendEntry(new PropertyEntry("anotherKey", "anotherValue"));

    final Properties properties= new Properties(propertyFile);

    // assert our assumptions about the java.util.Properties implementation
    final java.util.Properties reference= new java.util.Properties();
    reference.put("myKey", "myValue");
    reference.put("anotherKey", "anotherValue");

    // should alter "myKey" mapping
    final Object rValue1 = reference.computeIfPresent("myKey", (t, u) ->
      (u == null) ? "FOO" : u + " BAR"
    );
    // should create a new mapping
    final Object rValue2= reference.computeIfPresent("non-existant", (t, u) ->
      (u == null) ? "FOO" : u + "BAR"
    );

    assertThat(reference).hasSize(2);
    assertThat(reference).containsEntry("myKey", "myValue BAR");
    assertThat(reference).containsEntry("anotherKey", "anotherValue");
    assertThat(rValue1).isEqualTo("myValue BAR");
    assertThat(rValue2).isEqualTo(null);


    // - execution
    // should alter "myKey" mapping
    final Object value1 = properties.computeIfPresent("myKey", (t, u) ->
      (u == null) ? "FOO" : u + " BAR"
    );
    // should do nothing and return null
    final Object value2= properties.computeIfPresent("non-existant", (t, u) ->
      (u == null) ? "FOO" : u + "BAR"
    );

    // - validation
    assertThat(propertyFile.entriesSize()).isEqualTo(3);
    assertThat(propertyFile.getAllEntries()).containsExactly(
      new BasicEntry("# some comment"),
      new PropertyEntry("myKey", "myValue BAR"),
      new PropertyEntry("anotherKey", "anotherValue")
    );
    assertThat(value1).isEqualTo("myValue BAR");
    assertThat(value2).isEqualTo(null);
  }

  @Test
  public void testComputeIfPresent_NonStringKey() {
    // - preparation
    final PropertyFile propertyFile= new PropertyFile();
    propertyFile.appendEntry(new BasicEntry("# some comment"));
    propertyFile.appendEntry(new PropertyEntry("myKey", "myValue"));
    propertyFile.appendEntry(new PropertyEntry("anotherKey", "anotherValue"));

    final Properties properties= new Properties(propertyFile);

    // - execution
    // should do nothing and return 'null'
    final Object value1= properties.computeIfPresent(new Date(), (t, u) ->
      (u == null) ? "FOO" : u + "BAR"
    );

    // - validation
    assertThat(propertyFile.entriesSize()).isEqualTo(3);
    assertThat(propertyFile.getAllEntries()).containsExactly(
      new BasicEntry("# some comment"),
      new PropertyEntry("myKey", "myValue"),
      new PropertyEntry("anotherKey", "anotherValue")
    );
    assertThat(value1).isNull();
  }

  @Test
  public void testComputeIfPresent_NonStringValue() {
    // - preparation
    final PropertyFile propertyFile= new PropertyFile();
    propertyFile.appendEntry(new BasicEntry("# some comment"));
    propertyFile.appendEntry(new PropertyEntry("myKey", "myValue"));
    propertyFile.appendEntry(new PropertyEntry("anotherKey", "anotherValue"));

    final Properties properties= new Properties(propertyFile);

    // - execution && validation
    // should throw exception on change nothing
    assertThatThrownBy(() -> properties.computeIfPresent("myKey", (t, u) ->
      (u == null) ? new Date() : new Date()
    ))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("Only strings are allowed as values");

    assertThatThrownBy(() -> properties.computeIfPresent("non-existant", (t, u) ->
      (u == null) ? new Date() : new Date()
    ))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("Only strings are allowed as values");

    // - validation
    assertThat(propertyFile.entriesSize()).isEqualTo(3);
    assertThat(propertyFile.getAllEntries()).containsExactly(
      new BasicEntry("# some comment"),
      new PropertyEntry("myKey", "myValue"),
      new PropertyEntry("anotherKey", "anotherValue")
    );
  }

  @Test
  public void testComputeIfPresent_throwsUncheckedException() {
    // - preparation
    final PropertyFile propertyFile= new PropertyFile();
    propertyFile.appendEntry(new BasicEntry("# some comment"));
    propertyFile.appendEntry(new PropertyEntry("myKey", "myValue"));
    propertyFile.appendEntry(new PropertyEntry("anotherKey", "anotherValue"));

    final Properties properties= new Properties(propertyFile);

    final RuntimeException thrown= new RuntimeException("lorem ipsum");

    // assert our assumptions about the java.util.Properties implementation
    final java.util.Properties reference= new java.util.Properties();
    reference.put("myKey", "myValue");
    reference.put("anotherKey", "anotherValue");

    // should throw exception on change nothing
    assertThatThrownBy(() -> reference.computeIfPresent("myKey", (t, u) -> {
      throw thrown;
    }))
      .isSameAs(thrown)
      .hasMessage("lorem ipsum");
    assertThat(reference).hasSize(2);
    assertThat(reference).containsEntry("myKey", "myValue");
    assertThat(reference).containsEntry("anotherKey", "anotherValue");

    // - execution && validation
    assertThatThrownBy(() -> properties.computeIfPresent("myKey", (t, u) -> {
      throw thrown;
    }))
      .isSameAs(thrown)
      .hasMessage("lorem ipsum");

    // assert that nothing has changed
    assertThat(propertyFile.entriesSize()).isEqualTo(3);
    assertThat(propertyFile.getAllEntries()).containsExactly(
      new BasicEntry("# some comment"),
      new PropertyEntry("myKey", "myValue"),
      new PropertyEntry("anotherKey", "anotherValue")
    );
  }


  @Test
  public void testComputeIfPresent_ReturnsNull() {
    // - preparation
    final PropertyFile propertyFile= new PropertyFile();
    propertyFile.appendEntry(new BasicEntry("# some comment"));
    propertyFile.appendEntry(new PropertyEntry("myKey", "myValue"));
    propertyFile.appendEntry(new PropertyEntry("anotherKey", "anotherValue"));

    final Properties properties= new Properties(propertyFile);

    // assert our assumptions about the java.util.Properties implementation
    final java.util.Properties reference= new java.util.Properties();
    reference.put("myKey", "myValue");
    reference.put("anotherKey", "anotherValue");

    // should remove "myKey"
    reference.computeIfPresent("myKey", (t, u) -> null);
    assertThat(reference).containsOnlyKeys("anotherKey");
    // should remove nothing
    properties.computeIfPresent("non-existant", (t, u) -> null);
    assertThat(reference).containsOnlyKeys("anotherKey");


    // - execution
    // should remove "myKey"
    properties.computeIfPresent("myKey", (t, u) -> null);
    // should remove nothing
    properties.computeIfPresent("non-existant", (t, u) -> null);

    // - validation
    assertThat(propertyFile.entriesSize()).isEqualTo(2);
    assertThat(propertyFile.getAllEntries()).containsExactly(
      new BasicEntry("# some comment"),
      new PropertyEntry("anotherKey", "anotherValue")
    );
  }


  @Test
  public void testMerge() {
    // - preparation
    final PropertyFile propertyFile= new PropertyFile();
    propertyFile.appendEntry(new BasicEntry("# some comment"));
    propertyFile.appendEntry(new PropertyEntry("myKey", "myValue"));
    propertyFile.appendEntry(new PropertyEntry("anotherKey", "anotherValue"));

    final Properties properties= new Properties(propertyFile);

    // assert our assumptions about the java.util.Properties implementation
    final java.util.Properties reference= new java.util.Properties();
    reference.put("myKey", "myValue");
    reference.put("anotherKey", "anotherValue");

    // should alter "myKey" mapping
    final Object rValue1 = reference.merge("myKey", "SPAM", (t, u) -> (String)t + (String)u);
    // should create a new mapping
    final Object rValue2= reference.merge("non-existant", "SPAM", (t, u) -> (String)t + (String)u);

    assertThat(reference).hasSize(3);
    //ATTENTION! The JDK implementation doesn't really conform to its specification.
    //           The javadoc tells "replaces the associated value with the results of the given
    //           remapping function, or removes if the result is null". But actually it merges
    //           the _given_ value with the result of the function. Not the _existing_ value.
    //assertThat(reference).containsEntry("myKey", "myValueSPAM");
    assertThat(reference).containsEntry("myKey", "myValueSPAM");
    assertThat(reference).containsEntry("anotherKey", "anotherValue");
    assertThat(reference).containsEntry("non-existant", "SPAM");
    assertThat(rValue1).isEqualTo("myValueSPAM");
    assertThat(rValue2).isEqualTo("SPAM");


    // - execution
    // should alter "myKey" mapping
    final Object value1 = properties.merge("myKey", "SPAM", (t, u) -> (String)t + (String)u);
    // should create a new mapping
    final Object value2= properties.merge("non-existant", "SPAM", (t, u) -> (String)t + (String)u);

    // - validation
    assertThat(propertyFile.entriesSize()).isEqualTo(4);
    assertThat(propertyFile.getAllEntries()).containsExactly(
      new BasicEntry("# some comment"),
      new PropertyEntry("myKey", "myValueSPAM"),
      new PropertyEntry("anotherKey", "anotherValue"),
      new PropertyEntry("non-existant", "SPAM")
    );
    assertThat(value1).isEqualTo("myValueSPAM");
    assertThat(value2).isEqualTo("SPAM");
  }


  @Test
  public void testMerge_NonStringKey() {
    // - preparation
    final PropertyFile propertyFile= new PropertyFile();
    propertyFile.appendEntry(new BasicEntry("# some comment"));
    propertyFile.appendEntry(new PropertyEntry("myKey", "myValue"));
    propertyFile.appendEntry(new PropertyEntry("anotherKey", "anotherValue"));

    final Properties properties= new Properties(propertyFile);

    // - execution
    // should throw exception and change nothing
    assertThatThrownBy(() -> properties.merge(new Date(), "SPAM", (t, u) ->
      (u == null) ? "FOO" : u + "BAR"
    ))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("Only strings are allowed as keys");

    // - validation
    assertThat(propertyFile.entriesSize()).isEqualTo(3);
    assertThat(propertyFile.getAllEntries()).containsExactly(
      new BasicEntry("# some comment"),
      new PropertyEntry("myKey", "myValue"),
      new PropertyEntry("anotherKey", "anotherValue")
    );
  }


  @Test
  public void testMerge_NonStringValue() {
    // - preparation
    final PropertyFile propertyFile= new PropertyFile();
    propertyFile.appendEntry(new BasicEntry("# some comment"));
    propertyFile.appendEntry(new PropertyEntry("myKey", "myValue"));
    propertyFile.appendEntry(new PropertyEntry("anotherKey", "anotherValue"));

    final Properties properties= new Properties(propertyFile);

    // - execution && validation
    // should throw exception on change nothing
    assertThatThrownBy(() -> properties.merge("myKey", new Date(), (t, u) ->
      (u == null) ? new Date() : new Date()
    ))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("Only strings are allowed as values");

    assertThatThrownBy(() -> properties.merge("non-existant", new Date(), (t, u) ->
      (u == null) ? new Date() : new Date()
    ))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("Only strings are allowed as values");

    // - validation
    assertThat(propertyFile.entriesSize()).isEqualTo(3);
    assertThat(propertyFile.getAllEntries()).containsExactly(
      new BasicEntry("# some comment"),
      new PropertyEntry("myKey", "myValue"),
      new PropertyEntry("anotherKey", "anotherValue")
    );
  }


  @Test
  public void testMerge_throwsUncheckedException() {
    // - preparation
    final PropertyFile propertyFile= new PropertyFile();
    propertyFile.appendEntry(new BasicEntry("# some comment"));
    propertyFile.appendEntry(new PropertyEntry("myKey", "myValue"));
    propertyFile.appendEntry(new PropertyEntry("anotherKey", "anotherValue"));

    final Properties properties= new Properties(propertyFile);

    final RuntimeException thrown= new RuntimeException("lorem ipsum");

    // assert our assumptions about the java.util.Properties implementation
    final java.util.Properties reference= new java.util.Properties();
    reference.put("myKey", "myValue");
    reference.put("anotherKey", "anotherValue");

    // should throw exception on change nothing
    assertThatThrownBy(() -> reference.merge("myKey", "SPAM", (t, u) -> {
      throw thrown;
    }))
      .isSameAs(thrown)
      .hasMessage("lorem ipsum");
    assertThat(reference).hasSize(2);
    assertThat(reference).containsEntry("myKey", "myValue");
    assertThat(reference).containsEntry("anotherKey", "anotherValue");

    // - execution && validation
    assertThatThrownBy(() -> properties.merge("myKey", "SPAM", (t, u) -> {
      throw thrown;
    }))
      .isSameAs(thrown)
      .hasMessage("lorem ipsum");

    // assert that nothing has changed
    assertThat(propertyFile.entriesSize()).isEqualTo(3);
    assertThat(propertyFile.getAllEntries()).containsExactly(
      new BasicEntry("# some comment"),
      new PropertyEntry("myKey", "myValue"),
      new PropertyEntry("anotherKey", "anotherValue")
    );
  }

  @Test
  public void testMerge_ReturnsNull() {
    // - preparation
    final PropertyFile propertyFile= new PropertyFile();
    propertyFile.appendEntry(new BasicEntry("# some comment"));
    propertyFile.appendEntry(new PropertyEntry("myKey", "myValue"));
    propertyFile.appendEntry(new PropertyEntry("anotherKey", "anotherValue"));

    final Properties properties= new Properties(propertyFile);

    // assert our assumptions about the java.util.Properties implementation
    final java.util.Properties reference= new java.util.Properties();
    reference.put("myKey", "myValue");
    reference.put("anotherKey", "anotherValue");

    // should remove "myKey"
    reference.merge("myKey", "SPAM", (t, u) -> null);
    assertThat(reference).containsOnlyKeys("anotherKey");
    // should do nothing
    reference.merge("non-existant", "FOO", (t, u) -> null);
    assertThat(reference).containsOnlyKeys("anotherKey", "non-existant");
    assertThat(reference).containsEntry("anotherKey", "anotherValue");
    assertThat(reference).containsEntry("non-existant", "FOO");


    // - execution
    // should remove "myKey"
    properties.merge("myKey", "SPAM", (t, u) -> null);
    // should remove nothing
    properties.merge("non-existant", "FOO", (t, u) -> null);

    // - validation
    assertThat(propertyFile.entriesSize()).isEqualTo(3);
    assertThat(propertyFile.getAllEntries()).containsExactly(
      new BasicEntry("# some comment"),
      new PropertyEntry("anotherKey", "anotherValue"),
      new PropertyEntry("non-existant", "FOO")
    );
  }


  /*
  This test is necessary since Java 8 bebhves differently than Java 9+ when merge is call with a
  null value for as value, but the mapping function returns a value.
  Java 8 just ignores the null value, if the mapping function should apply and applies the mapping function.
  Java 9+ throws a NullPointerException and does _not_ apply the mapping function.

  Actually I consider the Java 8 behaviour more accurate, but we try to do the same as the current JRE does.
  */
  @Test
  public void testMerge_IgnoreNullValue() {
    // - preparation
    final PropertyFile propertyFile= new PropertyFile();
    propertyFile.appendEntry(new BasicEntry("# some comment"));
    propertyFile.appendEntry(new PropertyEntry("myKey", "myValue"));
    propertyFile.appendEntry(new PropertyEntry("anotherKey", "anotherValue"));

    final Properties properties= new Properties(propertyFile);

    // assert our assumptions about the java.util.Properties implementation
    final java.util.Properties reference= new java.util.Properties();
    reference.put("myKey", "myValue");
    reference.put("anotherKey", "anotherValue");

    if (isJava9OrHigher()) {
      // JAVA 9+ throws an NPE for an existing and a non-existing key and doesn't change anything
      assertThatThrownBy(() -> reference.merge("anotherKey", null, (t, u) -> "SPAM"))
        .isInstanceOf(NullPointerException.class)
        ;
      assertThatThrownBy(() -> reference.merge("non-existant", null, (t, u) -> "SPAM"))
        .isInstanceOf(NullPointerException.class)
        ;
      assertThat(reference).containsOnlyKeys("myKey", "anotherKey");
      assertThat(reference).containsEntry("myKey", "myValue");
      assertThat(reference).containsEntry("anotherKey", "anotherValue");


      // - execution
      assertThatThrownBy(() -> properties.merge("anotherKey", null, (t, u) -> "SPAM"))
        .isInstanceOf(NullPointerException.class)
        ;
      assertThatThrownBy(() -> properties.merge("non-existant", null, (t, u) -> "SPAM"))
        .isInstanceOf(NullPointerException.class)
        ;

      // - validation
      assertThat(propertyFile.entriesSize()).isEqualTo(3);
      assertThat(propertyFile.getAllEntries()).containsExactly(
        new BasicEntry("# some comment"),
        new PropertyEntry("myKey", "myValue"),
        new PropertyEntry("anotherKey", "anotherValue")
      );
    } else {
      // Java 8 ignores the null value and either applies the mapping function (if the key exists)
      // or does nothing (if the key doesn't exist).
      reference.merge("anotherKey", null, (t, u) -> "SPAM");
      assertThat(reference).containsOnlyKeys("myKey", "anotherKey");
      assertThat(reference).containsEntry("myKey", "myValue");
      assertThat(reference).containsEntry("anotherKey", "SPAM");
      reference.merge("non-existant", null, (t, u) -> "SPAM");
      assertThat(reference).containsOnlyKeys("myKey", "anotherKey");
      assertThat(reference).containsEntry("myKey", "myValue");
      assertThat(reference).containsEntry("anotherKey", "SPAM");


      // - execution
      properties.merge("anotherKey", null, (t, u) -> "SPAM");
      properties.merge("non-existant", null, (t, u) -> "SPAM");

      // - validation
      assertThat(propertyFile.entriesSize()).isEqualTo(3);
      assertThat(propertyFile.getAllEntries()).containsExactly(
        new BasicEntry("# some comment"),
        new PropertyEntry("myKey", "myValue"),
        new PropertyEntry("anotherKey", "SPAM")
      );
    }
  }


  @Test
  public void testReplaceAll() {
    // - preparation
    final PropertyFile propertyFile= new PropertyFile();
    propertyFile.appendEntry(new BasicEntry("# some comment"));
    propertyFile.appendEntry(new PropertyEntry("myKey", "myValue"));
    propertyFile.appendEntry(new PropertyEntry("anotherKey", "anotherValue"));
    propertyFile.appendEntry(new PropertyEntry("thirdKey", "thirdValue"));

    final Properties properties= new Properties(propertyFile);

    // assert our assumptions about the java.util.Properties implementation
    final java.util.Properties reference= new java.util.Properties();
    reference.put("myKey", "myValue");
    reference.put("anotherKey", "anotherValue");
    reference.put("thirdKey", "thirdValue");

    final BiFunction<Object, Object, Object> f= (Object t, Object u) -> {
      switch((String)t) {
        case "myKey": return "FOO";
        case "anotherKey": return "BAR";
        default: return u;
      }
    };

    reference.replaceAll(f);

    assertThat(reference).hasSize(3);
    assertThat(reference).containsEntry("myKey", "FOO");
    assertThat(reference).containsEntry("anotherKey", "BAR");
    assertThat(reference).containsEntry("thirdKey", "thirdValue");


    // - execution
    properties.replaceAll(f);

    // - validation
    assertThat(propertyFile.entriesSize()).isEqualTo(4);
    assertThat(propertyFile.getAllEntries()).containsExactly(
      new BasicEntry("# some comment"),
      new PropertyEntry("myKey", "FOO"),
      new PropertyEntry("anotherKey", "BAR"),
      new PropertyEntry("thirdKey", "thirdValue")
    );
  }


  @Test
  public void testReplaceAll_NullValue() {
    // - preparation
    final PropertyFile propertyFile= new PropertyFile();
    propertyFile.appendEntry(new BasicEntry("# some comment"));
    propertyFile.appendEntry(new PropertyEntry("myKey", "myValue"));
    propertyFile.appendEntry(new PropertyEntry("anotherKey", "anotherValue"));
    propertyFile.appendEntry(new PropertyEntry("thirdKey", "thirdValue"));

    final Properties properties= new Properties(propertyFile);

    // assert our assumptions about the java.util.Properties implementation
    final java.util.Properties reference= new java.util.Properties();
    reference.put("myKey", "myValue");
    reference.put("anotherKey", "anotherValue");
    reference.put("thirdKey", "thirdValue");

    final BiFunction<Object, Object, Object> f= (Object t, Object u) -> null;

    assertThatThrownBy(() -> reference.replaceAll(f))
      .isInstanceOf(NullPointerException.class)
      ;

    assertThat(reference).hasSize(3);
    assertThat(reference).containsEntry("myKey", "myValue");
    assertThat(reference).containsEntry("anotherKey", "anotherValue");
    assertThat(reference).containsEntry("thirdKey", "thirdValue");


    // - execution
    assertThatThrownBy(() -> properties.replaceAll(f))
      .isInstanceOf(NullPointerException.class)
      ;

    // - validation
    assertThat(propertyFile.entriesSize()).isEqualTo(4);
    assertThat(propertyFile.getAllEntries()).containsExactly(
      new BasicEntry("# some comment"),
      new PropertyEntry("myKey", "myValue"),
      new PropertyEntry("anotherKey", "anotherValue"),
      new PropertyEntry("thirdKey", "thirdValue")
    );
  }


  @Test
  public void testReplaceAll_NonStringValue() {
    // - preparation
    final PropertyFile propertyFile= new PropertyFile();
    propertyFile.appendEntry(new BasicEntry("# some comment"));
    propertyFile.appendEntry(new PropertyEntry("myKey", "myValue"));
    propertyFile.appendEntry(new PropertyEntry("anotherKey", "anotherValue"));
    propertyFile.appendEntry(new PropertyEntry("thirdKey", "thirdValue"));

    final Properties properties= new Properties(propertyFile);

    final BiFunction<Object, Object, Object> f= (Object t, Object u) -> new Date();


    // - execution
    assertThatThrownBy(() -> properties.replaceAll(f))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("Only strings are allowed as values")
      ;

    // - validation
    assertThat(propertyFile.entriesSize()).isEqualTo(4);
    assertThat(propertyFile.getAllEntries()).containsExactly(
      new BasicEntry("# some comment"),
      new PropertyEntry("myKey", "myValue"),
      new PropertyEntry("anotherKey", "anotherValue"),
      new PropertyEntry("thirdKey", "thirdValue")
    );
  }


  @Test
  public void testReplaceAll_throwsUncheckedException() {
    // - preparation
    final PropertyFile propertyFile= new PropertyFile();
    propertyFile.appendEntry(new BasicEntry("# some comment"));
    propertyFile.appendEntry(new PropertyEntry("myKey", "myValue"));
    propertyFile.appendEntry(new PropertyEntry("anotherKey", "anotherValue"));
    propertyFile.appendEntry(new PropertyEntry("thirdKey", "thirdValue"));

    final Properties properties= new Properties(propertyFile);

    // assert our assumptions about the java.util.Properties implementation
    final java.util.Properties reference= new java.util.Properties();
    reference.put("myKey", "myValue");
    reference.put("anotherKey", "anotherValue");
    reference.put("thirdKey", "thirdValue");

    final BiFunction<Object, Object, Object> f= (Object t, Object u) -> {
      throw new RuntimeException("Bail out!");
    };

    assertThatThrownBy(() -> reference.replaceAll(f))
      .isInstanceOf(RuntimeException.class)
      .hasMessage("Bail out!")
      ;

    assertThat(reference).hasSize(3);
    assertThat(reference).containsEntry("myKey", "myValue");
    assertThat(reference).containsEntry("anotherKey", "anotherValue");
    assertThat(reference).containsEntry("thirdKey", "thirdValue");


    // - execution
    assertThatThrownBy(() -> properties.replaceAll(f))
      .isInstanceOf(RuntimeException.class)
      .hasMessage("Bail out!")
      ;

    // - validation
    assertThat(propertyFile.entriesSize()).isEqualTo(4);
    assertThat(propertyFile.getAllEntries()).containsExactly(
      new BasicEntry("# some comment"),
      new PropertyEntry("myKey", "myValue"),
      new PropertyEntry("anotherKey", "anotherValue"),
      new PropertyEntry("thirdKey", "thirdValue")
    );
  }


  @Test
  public void testStore_OutputStream() throws Exception {
    // - preparation
    final PropertyFile propertyFile= new PropertyFile();
    propertyFile.appendEntry(new BasicEntry("# some comment\n"));
    propertyFile.appendEntry(new PropertyEntry("myKey", "myValue"));
    propertyFile.appendEntry(new PropertyEntry("anotherKey", "anotherValue"));

    final Properties properties= new Properties(propertyFile);

    // - execution
    final File outFile= File.createTempFile("apron-jup-test", ".properties");
    outFile.deleteOnExit();

    try (final FileOutputStream fos= new FileOutputStream(outFile);) {
      properties.store(fos, "# Leading comment");
    }

    // - validation
    final List<String> outLines = Files.readAllLines(outFile.toPath(), ISO_8859_1);

    assertThat(outLines).containsExactly(
      "# Leading comment",
      "# some comment",
      "myKey = myValue",
      "anotherKey = anotherValue"
    );
  }


  @Test
  public void testStore_Writer() throws Exception {
    // - preparation
    final PropertyFile propertyFile= new PropertyFile();
    propertyFile.appendEntry(new BasicEntry("# some comment\n"));
    propertyFile.appendEntry(new PropertyEntry("myKey", "myValue"));
    propertyFile.appendEntry(new PropertyEntry("anotherKey", "anotherValue"));

    final Properties properties= new Properties(propertyFile);

    // - execution
    final File outFile= File.createTempFile("apron-jup-test", ".properties");
    outFile.deleteOnExit();

    try (final PrintWriter pw= new PrintWriter(new OutputStreamWriter(new FileOutputStream(outFile), ISO_8859_1));) {
      properties.store(pw, "# Leading comment");
    }

    // - validation
    final List<String> outLines = Files.readAllLines(outFile.toPath(), ISO_8859_1);

    assertThat(outLines).containsExactly(
      "# Leading comment",
      "# some comment",
      "myKey = myValue",
      "anotherKey = anotherValue"
    );
  }


  @Test
  public void testStore_Access_through_jup_and_apron() throws Exception {
    // - preparation
    final PropertyFile propertyFile= new PropertyFile();
    propertyFile.appendEntry(new BasicEntry("# some comment\n"));
    propertyFile.appendEntry(new PropertyEntry("myKey", "myValue"));
    propertyFile.appendEntry(new PropertyEntry("anotherKey", "anotherValue"));

    final Properties properties= new Properties(propertyFile);

    // - execution

    propertyFile.set("apron1", "apronA");
    propertyFile.set("apron2", "apronB");
    propertyFile.remove("myKey");

    properties.setProperty("jup1", "jupA");
    properties.setProperty("jup2", "jupB");
    properties.remove("anotherKey");

    // - validation

    assertThat(properties).hasSize(4);
    assertThat(properties).containsEntry("apron1", "apronA");
    assertThat(properties).containsEntry("apron2", "apronB");
    assertThat(properties).containsEntry("jup1", "jupA");
    assertThat(properties).containsEntry("jup2", "jupB");

    assertThat(propertyFile.getAllEntries()).containsExactly(
      new BasicEntry("# some comment\n"),
      new PropertyEntry("apron1", "apronA"),
      new PropertyEntry("apron2", "apronB"),
      new PropertyEntry("jup1", "jupA"),
      new PropertyEntry("jup2", "jupB")
    );
  }

}
