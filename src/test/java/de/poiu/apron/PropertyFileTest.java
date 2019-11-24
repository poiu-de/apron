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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.junit.Test;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


/**
 *
 * @author mherrn
 */
public class PropertyFileTest {

  @Test
  public void test_CompareFullExample() throws IOException {
    // - preparation
    final File propertyFile= this.createTestFile(""
      + "keyA1=valueA1\n"
      + " keyA2  =  valueA2\n"
      + "\tkeyA3\t=\tvalue A3\t\n"
      + "keyA4 = very long\\\n"
      + "value A4 over \\\n"
      + "multiple lines\n"
      + "        \n"
      + "keyB1:valueB1\n"
      + " keyB2 : valueB2\n"
      + "\t keyB3\t:\t value B3 \n"
      + "keyB4 : very long\\\n"
      + "value B4 over \\\n"
      + "multiple lines\\\n"
      + "\n"
      + "keyC1 valueC1\n"
      + "  keyC2   valueC2\n"
      + "\t keyC3\t\tvalue C3 \n"
      + "keyC4   very long\\\n"
      + "value C4 over \\\n"
      + "\t \tmultiple lines");

    final Properties javaUtilProperties= new Properties();
    try (final FileInputStream fis= new FileInputStream(propertyFile);) {
      javaUtilProperties.load(fis);
    }

    // assert our assumptions about the java.util.Properties implementation
    assertThat(javaUtilProperties.size()).as("Check assumption about java.util.Properties size").isEqualTo(12);
    assertThat(javaUtilProperties.containsKey("keyA1")).as("Check assumption about java.util.Properties keys").isTrue();
    assertThat(javaUtilProperties.containsKey("keyA2")).as("Check assumption about java.util.Properties keys").isTrue();
    assertThat(javaUtilProperties.containsKey("keyA3")).as("Check assumption about java.util.Properties keys").isTrue();
    assertThat(javaUtilProperties.containsKey("keyA4")).as("Check assumption about java.util.Properties keys").isTrue();
    assertThat(javaUtilProperties.containsKey("keyB1")).as("Check assumption about java.util.Properties keys").isTrue();
    assertThat(javaUtilProperties.containsKey("keyB2")).as("Check assumption about java.util.Properties keys").isTrue();
    assertThat(javaUtilProperties.containsKey("keyB3")).as("Check assumption about java.util.Properties keys").isTrue();
    assertThat(javaUtilProperties.containsKey("keyB4")).as("Check assumption about java.util.Properties keys").isTrue();
    assertThat(javaUtilProperties.containsKey("keyC1")).as("Check assumption about java.util.Properties keys").isTrue();
    assertThat(javaUtilProperties.containsKey("keyC2")).as("Check assumption about java.util.Properties keys").isTrue();
    assertThat(javaUtilProperties.containsKey("keyC3")).as("Check assumption about java.util.Properties keys").isTrue();
    assertThat(javaUtilProperties.containsKey("keyC4")).as("Check assumption about java.util.Properties keys").isTrue();
    assertThat(javaUtilProperties.getProperty("keyA1")).as("Check assumption about java.util.Properties values").isEqualTo("valueA1");
    assertThat(javaUtilProperties.getProperty("keyA2")).as("Check assumption about java.util.Properties values").isEqualTo("valueA2");
    assertThat(javaUtilProperties.getProperty("keyA3")).as("Check assumption about java.util.Properties values").isEqualTo("value A3\t");
    assertThat(javaUtilProperties.getProperty("keyA4")).as("Check assumption about java.util.Properties values").isEqualTo("very longvalue A4 over multiple lines");
    assertThat(javaUtilProperties.getProperty("keyB1")).as("Check assumption about java.util.Properties values").isEqualTo("valueB1");
    assertThat(javaUtilProperties.getProperty("keyB2")).as("Check assumption about java.util.Properties values").isEqualTo("valueB2");
    assertThat(javaUtilProperties.getProperty("keyB3")).as("Check assumption about java.util.Properties values").isEqualTo("value B3 ");
    assertThat(javaUtilProperties.getProperty("keyB4")).as("Check assumption about java.util.Properties values").isEqualTo("very longvalue B4 over multiple lines");
    assertThat(javaUtilProperties.getProperty("keyC1")).as("Check assumption about java.util.Properties values").isEqualTo("valueC1");
    assertThat(javaUtilProperties.getProperty("keyC2")).as("Check assumption about java.util.Properties values").isEqualTo("valueC2");
    assertThat(javaUtilProperties.getProperty("keyC3")).as("Check assumption about java.util.Properties values").isEqualTo("value C3 ");
    assertThat(javaUtilProperties.getProperty("keyC4")).as("Check assumption about java.util.Properties values").isEqualTo("very longvalue C4 over multiple lines");

    // - execution
    final PropertyFile readPropertyFile= PropertyFile.from(propertyFile);

    // - validation
    assertThat(readPropertyFile.propertiesSize()).isEqualTo(javaUtilProperties.size());
    for (final Map.Entry<Object, Object> e : javaUtilProperties.entrySet()) {
      final String key = (String) e.getKey();
      final String value = (String) e.getValue();
      assertThat(readPropertyFile.containsKey(key)).as("key %s contains value %s", key, value).isTrue();
    }
  }


  @Test
  public void test_SeparatorCharInValue() throws IOException {
    // - preparation
    final File propertyFile= this.createTestFile(""
        + "keyA1 =  valueA1\n"
        + "keyA2 = value A=2\n"
        + "keyA3 : value A:3\n"
        + "keyA4   value A 4");

    final Properties javaUtilProperties= new Properties();
    try (final FileInputStream fis= new FileInputStream(propertyFile);) {
      javaUtilProperties.load(fis);
    }

    // assert our assumptions about the java.util.Properties implementation
    assertThat(javaUtilProperties.size()).as("Check assumption about java.util.Properties size").isEqualTo(4);
    assertThat(javaUtilProperties).containsOnlyKeys("keyA1", "keyA2", "keyA3", "keyA4");
    assertThat(javaUtilProperties.getProperty("keyA1")).as("Check assumption about java.util.Properties values").isEqualTo("valueA1");
    assertThat(javaUtilProperties.getProperty("keyA2")).as("Check assumption about java.util.Properties values").isEqualTo("value A=2");
    assertThat(javaUtilProperties.getProperty("keyA3")).as("Check assumption about java.util.Properties values").isEqualTo("value A:3");
    assertThat(javaUtilProperties.getProperty("keyA4")).as("Check assumption about java.util.Properties values").isEqualTo("value A 4");

    // - execution
    final PropertyFile readPropertyFile= PropertyFile.from(propertyFile);

    // - validation
    assertThat(readPropertyFile.propertiesSize()).isEqualTo(javaUtilProperties.size());
    for (final Map.Entry<Object, Object> e : javaUtilProperties.entrySet()) {
      final String key = (String) e.getKey();
      final String value = (String) e.getValue();
      assertThat(readPropertyFile.containsKey(key)).as("key %s contains value %s", key, value).isTrue();
    }
  }


  @Test
  public void test_MultilinesWithAdditionalSeparatorChars() throws IOException {
    // - preparation
    final File propertyFile= this.createTestFile(""
      + "   keyA1 =  my very \t \\\n"
      + "   long value that \\\n"
      + "   \tspans several lines = \\\n"
      + " and contains = characters \t \n"
      + "keyA2 = some simple value \t ");

    final Properties javaUtilProperties= new Properties();
    try (final FileInputStream fis= new FileInputStream(propertyFile);) {
      javaUtilProperties.load(fis);
    }

    // assert our assumptions about the java.util.Properties implementation
    assertThat(javaUtilProperties.size()).as("Check assumption about java.util.Properties size").isEqualTo(2);
    assertThat(javaUtilProperties).containsOnlyKeys("keyA1", "keyA2");
    assertThat(javaUtilProperties.getProperty("keyA1")).as("Check assumption about java.util.Properties values").isEqualTo("my very \t long value that spans several lines = and contains = characters \t ");
    assertThat(javaUtilProperties.getProperty("keyA2")).as("Check assumption about java.util.Properties values").isEqualTo("some simple value \t ");

    // - execution
    final PropertyFile readPropertyFile= PropertyFile.from(propertyFile);

    // - validation
    assertThat(readPropertyFile.propertiesSize()).isEqualTo(javaUtilProperties.size());
    for (final Map.Entry<Object, Object> e : javaUtilProperties.entrySet()) {
      final String key = (String) e.getKey();
      final String value = (String) e.getValue();
      assertThat(readPropertyFile.containsKey(key)).as("propertyFile contains key %s", key).isTrue();
      assertThat(readPropertyFile.get(key)).as("key %s contains value %s", key, value).isEqualTo(value);
    }
  }


  @Test
  public void test_Commentlines() throws IOException {
    // - preparation
    final File propertyFile= this.createTestFile(""
      + "   keyA1 =  valueA1\n"
      + " #Kommen=tar\n"
      + "!Auch ein : Kommentar\n"
      + " keyA2= valueA2");

    final Properties javaUtilProperties= new Properties();
    try (final FileInputStream fis= new FileInputStream(propertyFile);) {
      javaUtilProperties.load(fis);
    }

    // assert our assumptions about the java.util.Properties implementation
    assertThat(javaUtilProperties.size()).as("Check assumption about java.util.Properties size").isEqualTo(2);
    assertThat(javaUtilProperties).containsOnlyKeys("keyA1", "keyA2");
    assertThat(javaUtilProperties.getProperty("keyA1")).as("Check assumption about java.util.Properties values").isEqualTo("valueA1");
    assertThat(javaUtilProperties.getProperty("keyA2")).as("Check assumption about java.util.Properties values").isEqualTo("valueA2");

    // - execution
    final PropertyFile readPropertyFile= PropertyFile.from(propertyFile);

    // - validation
    assertThat(readPropertyFile.entriesSize()).isEqualTo(4);
    assertThat(readPropertyFile.propertiesSize()).isEqualTo(javaUtilProperties.size());
    for (final Map.Entry<Object, Object> e : javaUtilProperties.entrySet()) {
      final String key = (String) e.getKey();
      final String value = (String) e.getValue();
      assertThat(readPropertyFile.containsKey(key)).as("propertyFile contains key %s", key).isTrue();
      assertThat(readPropertyFile.get(key)).as("key %s contains value %s", key, value).isEqualTo(value);
    }
  }


  @Test
  public void test_EscapedCommentChar() throws IOException {
    // - preparation
    final File propertyFile= this.createTestFile(""
      + "   keyA1 =  valueA1\n"
      + " # This is a real comment\n"
      + " \\#This_is = not a comment\n"
      + " keyA2= valueA2");

    final Properties javaUtilProperties= new Properties();
    try (final FileInputStream fis= new FileInputStream(propertyFile);) {
      javaUtilProperties.load(fis);
    }

    // assert our assumptions about the java.util.Properties implementation
    assertThat(javaUtilProperties.size()).as("Check assumption about java.util.Properties size").isEqualTo(3);
    assertThat(javaUtilProperties).containsOnlyKeys("keyA1", "#This_is", "keyA2");
    assertThat(javaUtilProperties.getProperty("keyA1")).as("Check assumption about java.util.Properties values").isEqualTo("valueA1");
    assertThat(javaUtilProperties.getProperty("#This_is")).as("Check assumption about java.util.Properties values").isEqualTo("not a comment");
    assertThat(javaUtilProperties.getProperty("keyA2")).as("Check assumption about java.util.Properties values").isEqualTo("valueA2");

    // - execution
    final PropertyFile readPropertyFile= PropertyFile.from(propertyFile);

    // - validation
    assertThat(readPropertyFile.propertiesSize()).isEqualTo(javaUtilProperties.size());
    for (final Map.Entry<Object, Object> e : javaUtilProperties.entrySet()) {
      final String key = (String) e.getKey();
      final String value = (String) e.getValue();
      assertThat(readPropertyFile.containsKey(key)).as("propertyFile contains key %s", key).isTrue();
      assertThat(readPropertyFile.get(key)).as("key %s contains value %s", key, value).isEqualTo(value);
    }
  }


  @Test
  public void test_CommentMultilinesNotSupported() throws IOException {
    // - preparation
    final File propertyFile= this.createTestFile(""
      + "   keyA1 =  valueA1\n"
      + " #Commentlines can not be continued \\\n"
      + "with backslashes\n"
      + " keyA2= valueA2\n");

    final Properties javaUtilProperties= new Properties();
    try (final FileInputStream fis= new FileInputStream(propertyFile);) {
      javaUtilProperties.load(fis);
    }

    // assert our assumptions about the java.util.Properties implementation
    assertThat(javaUtilProperties.size()).as("Check assumption about java.util.Properties size").isEqualTo(3);
    assertThat(javaUtilProperties.containsKey("keyA1")).as("Check assumption about java.util.Properties keys").isTrue();
    assertThat(javaUtilProperties.containsKey("with")).as("Check assumption about java.util.Properties keys").isTrue();
    assertThat(javaUtilProperties.containsKey("keyA2")).as("Check assumption about java.util.Properties keys").isTrue();
    assertThat(javaUtilProperties.getProperty("keyA1")).as("Check assumption about java.util.Properties values").isEqualTo("valueA1");
    assertThat(javaUtilProperties.getProperty("with")).as("Check assumption about java.util.Properties values").isEqualTo("backslashes");
    assertThat(javaUtilProperties.getProperty("keyA2")).as("Check assumption about java.util.Properties values").isEqualTo("valueA2");

    // - execution
    final PropertyFile readPropertyFile= PropertyFile.from(propertyFile);

    // - validation
    assertThat(readPropertyFile.propertiesSize()).isEqualTo(javaUtilProperties.size());
    for (final Map.Entry<Object, Object> e : javaUtilProperties.entrySet()) {
      final String key = (String) e.getKey();
      final String value = (String) e.getValue();
      assertThat(readPropertyFile.containsKey(key)).as("propertyFile contains key %s", key).isTrue();
      assertThat(readPropertyFile.get(key)).as("key %s contains value %s", key, value).isEqualTo(value);
    }
  }

  @Test
  public void test_EmptyLines() throws IOException {
    // - preparation
    final File propertyFile= this.createTestFile(""
      + "   keyA1 =  valueA1\n"        // -> PropertyEntry key: 'keyA1', value; 'valueA1'
      + "   keyOhneValue\n"            // -> PropertyEntry key: 'keyOhneValue', value: ''
      + "   keyOhneValue2 \n"          // -> PropertyEntry key: 'keyOhneValue2 ', value: ''
      + "   : value ohne key \n"       // -> PropertyEntry key: '', value: 'value ohne key '
      + " \t\n"                        // -> BasicEntry value: ' \t'
      + " \t\\\\\\\n"                  // -> PropertyEntry key: '\keyA2', value: 'valueA2' -- odd number of backslashes (but more than one) -> the backslashes are the key and the line is continued
      + " keyA2= valueA2");

    final Properties javaUtilProperties= new Properties();
    try (final FileInputStream fis= new FileInputStream(propertyFile);) {
      javaUtilProperties.load(fis);
    }

    // assert our assumptions about the java.util.Properties implementation
    assertThat(javaUtilProperties.size()).as("Check assumption about java.util.Properties size").isEqualTo(5);
    assertThat(javaUtilProperties.containsKey("keyA1")).as("Check assumption about java.util.Properties keys").isTrue();
    assertThat(javaUtilProperties.containsKey("\\keyA2")).as("Check assumption about java.util.Properties keys").isTrue();
    assertThat(javaUtilProperties.containsKey("keyOhneValue")).as("Check assumption about java.util.Properties keys").isTrue();
    assertThat(javaUtilProperties.containsKey("keyOhneValue2")).as("Check assumption about java.util.Properties keys").isTrue();
    assertThat(javaUtilProperties.containsKey("")).as("Check assumption about java.util.Properties keys").isTrue();
    assertThat(javaUtilProperties.getProperty("keyA1")).as("Check assumption about java.util.Properties values").isEqualTo("valueA1");
    assertThat(javaUtilProperties.getProperty("\\keyA2")).as("Check assumption about java.util.Properties values").isEqualTo("valueA2");
    assertThat(javaUtilProperties.getProperty("keyOhneValue")).as("Check assumption about java.util.Properties values").isEqualTo("");
    assertThat(javaUtilProperties.getProperty("keyOhneValue2")).as("Check assumption about java.util.Properties values").isEqualTo("");
    assertThat(javaUtilProperties.getProperty("")).as("Check assumption about java.util.Properties values").isEqualTo("value ohne key ");

    // - execution
    final PropertyFile readPropertyFile= PropertyFile.from(propertyFile);


    // - validation
    assertThat(readPropertyFile.entriesSize()).isEqualTo(6);
    assertThat(readPropertyFile.getAllEntries().size()).isEqualTo(6);

    assertThat(readPropertyFile.getAllEntries()).containsExactly(
      new PropertyEntry("   ", "keyA1", " =  ", "valueA1", "\n"),
      new PropertyEntry("   ", "keyOhneValue", "", "", "\n"),
      new PropertyEntry("   ", "keyOhneValue2", " ", "", "\n"),
      new PropertyEntry("", "", "   : ", "value ohne key ", "\n"),
      new BasicEntry(" \t\n"),
      new PropertyEntry(" \t", "\\\\\\\n keyA2", "= ", "valueA2", "\n")
    );

    assertThat(readPropertyFile.propertiesSize()).isEqualTo(javaUtilProperties.size());
    for (final Map.Entry<Object, Object> e : javaUtilProperties.entrySet()) {
      final String key = (String) e.getKey();
      final String value = (String) e.getValue();
      assertThat(readPropertyFile.containsKey(key)).as("propertyFile contains key %s", key).isTrue();
      assertThat(readPropertyFile.get(key)).as("key '%s' contains value %s", key, value).isEqualTo(value);
    }
  }

  @Test
//  @Ignore("very rare situations that differ from the behaviour of java.util.Properties")
  public void test_OnlyBackslashesInLine() throws IOException {
    // - preparation
    final File propertyFile= this.createTestFile(""
      + " keyA1 = valueA1\n"
      + " \t\\\n"                      // -> BasicEntry value: ' \t\'  -- no continuation on empty lines
      + " \t\\\\\n"                    // -> PropertyEntry key: '\', value: '' -- even number of backslashes -> the backslashes are the key
      + " keyA2 = valueA2\n"
      );

    final Properties javaUtilProperties= new Properties();
    try (final FileInputStream fis= new FileInputStream(propertyFile);) {
      javaUtilProperties.load(fis);
    }

    // assert our assumptions about the java.util.Properties implementation
    assertThat(javaUtilProperties.size()).as("Check assumption about java.util.Properties size").isEqualTo(3);
    assertThat(javaUtilProperties).containsOnlyKeys("keyA1", "\\", "keyA2");
    assertThat(javaUtilProperties.getProperty("keyA1")).as("Check assumption about java.util.Properties values").isEqualTo("valueA1");
    assertThat(javaUtilProperties.getProperty("\\")).as("Check assumption about java.util.Properties values").isEmpty();
    assertThat(javaUtilProperties.getProperty("keyA2")).as("Check assumption about java.util.Properties values").isEqualTo("valueA2");

    // - execution
    final PropertyFile readPropertyFile= PropertyFile.from(propertyFile);

    // - validation
    assertThat(readPropertyFile.getAllEntries().size()).isEqualTo(4);
    assertThat(readPropertyFile.getAllEntries()).containsExactly(
      new PropertyEntry(" ", "keyA1", " = ", "valueA1", "\n"),
      new BasicEntry(" \t\\\n"),
      new PropertyEntry(" \t", "\\\\", "", "", "\n"),
      new PropertyEntry(" ", "keyA2", " = ", "valueA2", "\n")
    );

    assertThat(readPropertyFile.propertiesSize()).isEqualTo(javaUtilProperties.size());
    for (final Map.Entry<Object, Object> e : javaUtilProperties.entrySet()) {
      final String key = (String) e.getKey();
      final String value = (String) e.getValue();
      assertThat(readPropertyFile.containsKey(key)).as("propertyFile contains key %s", key).isTrue();
      assertThat(readPropertyFile.get(key)).as("key '%s' contains value %s", key, value).isEqualTo(value);
    }
  }


  @Test
  public void test_KeyOverMultipleLines() throws IOException {
    // - preparation
    final File propertyFile= this.createTestFile(""
      + "   key\\\n"
      + "   A1 =  valueA1\n"        // -> PropertyEntry key: 'keyA1', value; 'valueA1'
      + " \t\\\\\\\n"              // -> PropertyEntry key: '\keyA2', value: 'valueA2' -- odd number of backslashes (but more than one) -> the backslashes are the key and the line is continued
      + " keyA2= valueA2");

    final Properties javaUtilProperties= new Properties();
    try (final FileInputStream fis= new FileInputStream(propertyFile);) {
      javaUtilProperties.load(fis);
    }

    // assert our assumptions about the java.util.Properties implementation
    assertThat(javaUtilProperties.size()).as("Check assumption about java.util.Properties size").isEqualTo(2);
    assertThat(javaUtilProperties.containsKey("keyA1")).as("Check assumption about java.util.Properties keys").isTrue();
    assertThat(javaUtilProperties.containsKey("\\keyA2")).as("Check assumption about java.util.Properties keys").isTrue();
    assertThat(javaUtilProperties.getProperty("keyA1")).as("Check assumption about java.util.Properties values").isEqualTo("valueA1");
    assertThat(javaUtilProperties.getProperty("\\keyA2")).as("Check assumption about java.util.Properties values").isEqualTo("valueA2");

    // - execution
    final PropertyFile readPropertyFile= PropertyFile.from(propertyFile);


    // - validation
    assertThat(readPropertyFile.getAllEntries().size()).isEqualTo(2);
    assertThat(readPropertyFile.propertiesSize()).isEqualTo(javaUtilProperties.size());
    for (final Map.Entry<Object, Object> e : javaUtilProperties.entrySet()) {
      final String key = (String) e.getKey();
      final String value = (String) e.getValue();
      assertThat(readPropertyFile.containsKey(key)).as("propertyFile contains key %s", key).isTrue();
      assertThat(readPropertyFile.get(key)).as("key %s contains value %s", key, value).isEqualTo(value);
    }
  }


  @Test
  public void test_DifferentSeparatorsOverMultipleLines() throws IOException {
    // - preparation
    final File propertyFile= this.createTestFile(""
      + "   keyA1 =  valueA1\n"        // -> PropertyEntry key: 'keyA1', value; 'valueA1'
      + " \t\\\\ \\\n"                 // -> PropertyEntry key: '\', value: 'keyA3= valueA3'
      + " keyA3= valueA3");

    final Properties javaUtilProperties= new Properties();
    try (final FileInputStream fis= new FileInputStream(propertyFile);) {
      javaUtilProperties.load(fis);
    }

    // assert our assumptions about the java.util.Properties implementation
    assertThat(javaUtilProperties.size()).as("Check assumption about java.util.Properties size").isEqualTo(2);
    assertThat(javaUtilProperties.containsKey("keyA1")).as("Check assumption about java.util.Properties keys").isTrue();;
    assertThat(javaUtilProperties.containsKey("\\")).as("Check assumption about java.util.Properties keys").isTrue();
    assertThat(javaUtilProperties.getProperty("keyA1")).as("Check assumption about java.util.Properties values").isEqualTo("valueA1");
    assertThat(javaUtilProperties.getProperty("\\")).as("Check assumption about java.util.Properties values").isEqualTo("keyA3= valueA3");

    // - execution
    final PropertyFile readPropertyFile= PropertyFile.from(propertyFile);

    // - validation
    assertThat(readPropertyFile.getAllEntries().size()).isEqualTo(javaUtilProperties.size());
    assertThat(readPropertyFile.propertiesSize()).isEqualTo(javaUtilProperties.size());
    for (final Map.Entry<Object, Object> e : javaUtilProperties.entrySet()) {
      final String key = (String) e.getKey();
      final String value = (String) e.getValue();
      assertThat(readPropertyFile.containsKey(key)).as("propertyFile contains key %s", key).isTrue();
      assertThat(readPropertyFile.get(key)).as("key %s contains value %s", key, value).isEqualTo(value);
    }
  }


  @Test
  public void test_KeyWithoutValue() throws IOException {
    // - preparation
    final File propertyFile= this.createTestFile(""
      + "   keyA1 =  valueA1\n"
      + " keyWithoutValue  \n"
      + " keyA2= valueA2");

    final Properties javaUtilProperties= new Properties();
    try (final FileInputStream fis= new FileInputStream(propertyFile);) {
      javaUtilProperties.load(fis);
    }

    // assert our assumptions about the java.util.Properties implementation
    assertThat(javaUtilProperties.size()).as("Check assumption about java.util.Properties size").isEqualTo(3);
    assertThat(javaUtilProperties.containsKey("keyA1")).as("Check assumption about java.util.Properties keys").isTrue();
    assertThat(javaUtilProperties.containsKey("keyWithoutValue")).as("Check assumption about java.util.Properties keys").isTrue();
    assertThat(javaUtilProperties.containsKey("keyA2")).as("Check assumption about java.util.Properties keys").isTrue();
    assertThat(javaUtilProperties.getProperty("keyWithoutValue")).as("Check assumption about java.util.Properties values").isEmpty();
    assertThat(javaUtilProperties.getProperty("keyA2")).as("Check assumption about java.util.Properties values").isEqualTo("valueA2");

    // - execution
    final PropertyFile readPropertyFile= PropertyFile.from(propertyFile);

    // - validation
    assertThat(readPropertyFile.propertiesSize()).isEqualTo(javaUtilProperties.size());
    for (final Map.Entry<Object, Object> e : javaUtilProperties.entrySet()) {
      final String key = (String) e.getKey();
      final String value = (String) e.getValue();
      assertThat(readPropertyFile.containsKey(key)).as("propertyFile contains key %s", key).isTrue();
      assertThat(readPropertyFile.get(key)).as("key %s contains value %s", key, value).isEqualTo(value);
    }
  }


  @Test
  public void test_DuplicateKeys() throws IOException {
    // - preparation
    final File propertyFile= this.createTestFile(""
      + "   myKey =  myValue\n"
      + "   myKey =  otherValue\n");

    final Properties javaUtilProperties= new Properties();
    try (final FileInputStream fis= new FileInputStream(propertyFile);) {
      javaUtilProperties.load(fis);
    }

    // assert our assumptions about the java.util.Properties implementation
    assertThat(javaUtilProperties.size()).as("Check assumption about java.util.Properties size").isEqualTo(1);
    assertThat(javaUtilProperties.containsKey("myKey")).as("Check assumption about java.util.Properties keys").isTrue();
    assertThat(javaUtilProperties.getProperty("myKey")).as("Check assumption about java.util.Properties values").isEqualTo("otherValue");

    // - execution
    final PropertyFile readPropertyFile= PropertyFile.from(propertyFile);

    // - validation
    assertThat(readPropertyFile.entriesSize()).isEqualTo(2);
    assertThat(readPropertyFile.propertiesSize()).isEqualTo(javaUtilProperties.size());
    for (final Map.Entry<Object, Object> e : javaUtilProperties.entrySet()) {
      final String key = (String) e.getKey();
      final String value = (String) e.getValue();
      assertThat(readPropertyFile.containsKey(key)).as("propertyFile contains key %s", key).isTrue();
      assertThat(readPropertyFile.get(key)).as("key %s contains value %s", key, value).isEqualTo(value);
    }
  }


  @Test
  public void test_KeysWithSpaces() throws IOException {
    // - preparation
    final File propertyFile= this.createTestFile(""
      + " keyA1 =  valueA1\n"
      + " key\\ A2 = valueA2\n");

    final Properties javaUtilProperties= new Properties();
    try (final FileInputStream fis= new FileInputStream(propertyFile);) {
      javaUtilProperties.load(fis);
    }

    // assert our assumptions about the java.util.Properties implementation
    assertThat(javaUtilProperties.size()).as("Check assumption about java.util.Properties size").isEqualTo(2);
    assertThat(javaUtilProperties.containsKey("keyA1")).as("Check assumption about java.util.Properties keys").isTrue();
    assertThat(javaUtilProperties.containsKey("key A2")).as("Check assumption about java.util.Properties keys").isTrue();
    assertThat(javaUtilProperties.getProperty("keyA1")).as("Check assumption about java.util.Properties values").isEqualTo("valueA1");
    assertThat(javaUtilProperties.getProperty("key A2")).as("Check assumption about java.util.Properties values").isEqualTo("valueA2");

    // - execution
    final PropertyFile readPropertyFile= PropertyFile.from(propertyFile);

    // - validation
    assertThat(readPropertyFile.propertiesSize()).isEqualTo(javaUtilProperties.size());
    for (final Map.Entry<Object, Object> e : javaUtilProperties.entrySet()) {
      final String key = (String) e.getKey();
      final String value = (String) e.getValue();
      assertThat(readPropertyFile.containsKey(key)).as("propertyFile contains key %s", key).isTrue();
      assertThat(readPropertyFile.get(key)).as("key %s contains value %s", key, value).isEqualTo(value);
    }
  }


  @Test
  public void test_KeysWithEscapedSpecialChars() throws IOException {
    // - preparation
    final File propertyFile= this.createTestFile(""
      + " key\\:A1 =  valueA1\n"
      + " key\\=A2 = valueA2\n");

    final Properties javaUtilProperties= new Properties();
    try (final FileInputStream fis= new FileInputStream(propertyFile);) {
      javaUtilProperties.load(fis);
    }

    // assert our assumptions about the java.util.Properties implementation
    assertThat(javaUtilProperties.size()).as("Check assumption about java.util.Properties size").isEqualTo(2);
    assertThat(javaUtilProperties.containsKey("key:A1")).as("Check assumption about java.util.Properties keys").isTrue();
    assertThat(javaUtilProperties.containsKey("key=A2")).as("Check assumption about java.util.Properties keys").isTrue();
    assertThat(javaUtilProperties.getProperty("key:A1")).as("Check assumption about java.util.Properties values").isEqualTo("valueA1");
    assertThat(javaUtilProperties.getProperty("key=A2")).as("Check assumption about java.util.Properties values").isEqualTo("valueA2");

    // - execution
    final PropertyFile readPropertyFile= PropertyFile.from(propertyFile);

    // - validation
    assertThat(readPropertyFile.propertiesSize()).isEqualTo(javaUtilProperties.size());
    for (final Map.Entry<Object, Object> e : javaUtilProperties.entrySet()) {
      final String key = (String) e.getKey();
      final String value = (String) e.getValue();
      assertThat(readPropertyFile.containsKey(key)).as("propertyFile contains key %s", key).isTrue();
      assertThat(readPropertyFile.get(key)).as("key %s contains value %s", key, value).isEqualTo(value);
    }
  }


  @Test
  public void test_EscapeNonSpecialChar() throws IOException {
    // - preparation
    final File propertyFile= this.createTestFile(""
      + " key\\A1 =  valueA1\n"
      + " key\\\\A2 = valueA2\n");

    final Properties javaUtilProperties= new Properties();
    try (final FileInputStream fis= new FileInputStream(propertyFile);) {
      javaUtilProperties.load(fis);
    }

    // assert our assumptions about the java.util.Properties implementation
    assertThat(javaUtilProperties.size()).as("Check assumption about java.util.Properties size").isEqualTo(2);
    assertThat(javaUtilProperties.containsKey("keyA1")).as("Check assumption about java.util.Properties keys").isTrue();
    assertThat(javaUtilProperties.containsKey("key\\A2")).as("Check assumption about java.util.Properties keys").isTrue();
    assertThat(javaUtilProperties.getProperty("keyA1")).as("Check assumption about java.util.Properties values").isEqualTo("valueA1");
    assertThat(javaUtilProperties.getProperty("key\\A2")).as("Check assumption about java.util.Properties values").isEqualTo("valueA2");

    // - execution
    final PropertyFile readPropertyFile= PropertyFile.from(propertyFile);

    // - validation
    assertThat(readPropertyFile.propertiesSize()).isEqualTo(javaUtilProperties.size());
    for (final Map.Entry<Object, Object> e : javaUtilProperties.entrySet()) {
      final String key = (String) e.getKey();
      final String value = (String) e.getValue();
      assertThat(readPropertyFile.containsKey(key)).as("propertyFile contains key %s", key).isTrue();
      assertThat(readPropertyFile.get(key)).as("key %s contains value %s", key, value).isEqualTo(value);
    }
  }


  @Test
  public void test_EvenNumberOfBackslashes() throws IOException {
    // - preparation
    final File propertyFile= this.createTestFile(""
      + " keyA1 =  value with \\\\ backslashes as part \\\\\\\\ of value\n"
      + " key\\ with\\ \\\\= valueA2\n");

    final Properties javaUtilProperties= new Properties();
    try (final FileInputStream fis= new FileInputStream(propertyFile);) {
      javaUtilProperties.load(fis);
    }

    // assert our assumptions about the java.util.Properties implementation
    assertThat(javaUtilProperties.size()).as("Check assumption about java.util.Properties size").isEqualTo(2);
    assertThat(javaUtilProperties.containsKey("keyA1")).as("Check assumption about java.util.Properties keys").isTrue();
    assertThat(javaUtilProperties.containsKey("key with \\")).as("Check assumption about java.util.Properties keys").isTrue();
    assertThat(javaUtilProperties.getProperty("keyA1")).as("Check assumption about java.util.Properties values").isEqualTo("value with \\ backslashes as part \\\\ of value");
    assertThat(javaUtilProperties.getProperty("key with \\")).as("Check assumption about java.util.Properties values").isEqualTo("valueA2");

    // - execution
    final PropertyFile readPropertyFile= PropertyFile.from(propertyFile);

    // - validation
    assertThat(readPropertyFile.propertiesSize()).isEqualTo(javaUtilProperties.size());
    for (final Map.Entry<Object, Object> e : javaUtilProperties.entrySet()) {
      final String key = (String) e.getKey();
      final String value = (String) e.getValue();
      assertThat(readPropertyFile.containsKey(key)).as("propertyFile contains key %s", key).isTrue();
      assertThat(readPropertyFile.get(key)).as("key %s contains value %s", key, value).isEqualTo(value);
    }
  }


  @Test
  public void test_OddNumberOfBackslashes() throws IOException {
    // - preparation
    final File propertyFile= this.createTestFile(""
      + " keyA1 =  value with \\ backslashes as part \\\\\\ of value\n"
      + " key\\\\\\ with\\ \\backslashes= valueA2\n");

    final Properties javaUtilProperties= new Properties();
    try (final FileInputStream fis= new FileInputStream(propertyFile);) {
      javaUtilProperties.load(fis);
    }

    // assert our assumptions about the java.util.Properties implementation
    assertThat(javaUtilProperties.size()).as("Check assumption about java.util.Properties size").isEqualTo(2);
    assertThat(javaUtilProperties.containsKey("keyA1")).as("Check assumption about java.util.Properties keys").isTrue();
    assertThat(javaUtilProperties.containsKey("key\\ with backslashes")).as("Check assumption about java.util.Properties keys").isTrue();
    assertThat(javaUtilProperties.getProperty("keyA1")).as("Check assumption about java.util.Properties values").isEqualTo("value with  backslashes as part \\ of value");
    assertThat(javaUtilProperties.getProperty("key\\ with backslashes")).as("Check assumption about java.util.Properties values").isEqualTo("valueA2");

    // - execution
    final PropertyFile readPropertyFile= PropertyFile.from(propertyFile);

    // - validation
    assertThat(readPropertyFile.propertiesSize()).isEqualTo(javaUtilProperties.size());
    for (final Map.Entry<Object, Object> e : javaUtilProperties.entrySet()) {
      final String key = (String) e.getKey();
      final String value = (String) e.getValue();
      assertThat(readPropertyFile.containsKey(key)).as("propertyFile contains key %s", key).isTrue();
      assertThat(readPropertyFile.get(key)).as("key %s contains value %s", key, value).isEqualTo(value);
    }
  }


  @Test
  public void test_LeadingWhitespaceOnContinuationLinesIsIgnored() throws IOException {
    // - preparation
    final File propertyFile= this.createTestFile(""
      + " key\\ \\\n"
      + "\t A1 =  value over\\ \\\n"
      + "\t  multiple\\ \\\n "
      + "\t\f lines\n");

    final Properties javaUtilProperties= new Properties();
    try (final FileInputStream fis= new FileInputStream(propertyFile);) {
      javaUtilProperties.load(fis);
    }

    // assert our assumptions about the java.util.Properties implementation
    assertThat(javaUtilProperties.size()).as("Check assumption about java.util.Properties size").isEqualTo(1);
    assertThat(javaUtilProperties.containsKey("key A1")).as("Check assumption about java.util.Properties keys").isTrue();
    assertThat(javaUtilProperties.getProperty("key A1")).as("Check assumption about java.util.Properties values").isEqualTo("value over multiple lines");

    // - execution
    final PropertyFile readPropertyFile= PropertyFile.from(propertyFile);

    // - validation
    assertThat(readPropertyFile.propertiesSize()).isEqualTo(javaUtilProperties.size());
    for (final Map.Entry<Object, Object> e : javaUtilProperties.entrySet()) {
      final String key = (String) e.getKey();
      final String value = (String) e.getValue();
      assertThat(readPropertyFile.containsKey(key)).as("propertyFile contains key %s", key).isTrue();
      assertThat(readPropertyFile.get(key)).as("key %s contains value %s", key, value).isEqualTo(value);
    }
  }


  @Test
  public void test_Multiline_SeparatorOnNewLine() throws IOException {
    // - preparation
    final File propertyFile= this.createTestFile(""
      + "\tkey\\ \\\n"
      + "  one\\\n"
      + "  : value \\\n"
      + "    1\n"
      + "key\\ \\\n"
      + "  two\\\r"
      + "  = \t value \\\r"
      + "    2\n"
      );

    final Properties javaUtilProperties= new Properties();
    try (final FileInputStream fis= new FileInputStream(propertyFile);) {
      javaUtilProperties.load(fis);
    }

    // assert our assumptions about the java.util.Properties implementation
    assertThat(javaUtilProperties.size()).as("Check assumption about java.util.Properties size").isEqualTo(2);
    assertThat(javaUtilProperties.containsKey("key one")).as("Check assumption about java.util.Properties keys").isTrue();
    assertThat(javaUtilProperties.containsKey("key two")).as("Check assumption about java.util.Properties keys").isTrue();
    assertThat(javaUtilProperties.getProperty("key one")).as("Check assumption about java.util.Properties values").isEqualTo("value 1");
    assertThat(javaUtilProperties.getProperty("key two")).as("Check assumption about java.util.Properties values").isEqualTo("value 2");

    // - execution
    final PropertyFile readPropertyFile= PropertyFile.from(propertyFile);


    // - validation
    assertThat(readPropertyFile.getAllEntries().size()).isEqualTo(2);
    assertThat(readPropertyFile.propertiesSize()).isEqualTo(javaUtilProperties.size());
    for (final Map.Entry<Object, Object> e : javaUtilProperties.entrySet()) {
      final String key = (String) e.getKey();
      final String value = (String) e.getValue();
      assertThat(readPropertyFile.containsKey(key)).as("propertyFile contains key %s", key).isTrue();
      assertThat(readPropertyFile.get(key)).as("key %s contains value %s", key, value).isEqualTo(value);
    }

    final PropertyEntry pE= new PropertyEntry("\t", "key\\ \\\n  two\\\r", "  = ", "value \\\r    2", "\n");
    final PropertyEntry pA= (PropertyEntry) readPropertyFile.getAllEntries().get(1);

    assertThat(readPropertyFile.getAllEntries()).containsExactly(
      new PropertyEntry("\t", "key\\ \\\n  one\\\n", "  : ", "value \\\n    1", "\n"),
      new PropertyEntry("", "key\\ \\\n  two\\\r", "  = \t ", "value \\\r    2", "\n")
    );
  }


  @Test
  public void test_DifferentCharsets() throws IOException {
    // - preparation
    final File propertyFile= this.createTestFile(""
      + " UTF-8-key-Äሴ = UTF-8-value-編Я");

    //We need to explicitly use a reader, otherwise ISO-8859-1 would be used
    final Properties javaUtilProperties= new Properties();
    try (final BufferedReader reader= new BufferedReader(new InputStreamReader(new FileInputStream(propertyFile), Charset.forName("UTF-8")));) {
      javaUtilProperties.load(reader);
    }

    // assert our assumptions about the java.util.Properties implementation
    assertThat(javaUtilProperties.size()).as("Check assumption about java.util.Properties size").isEqualTo(1);
    assertThat(javaUtilProperties.containsKey("UTF-8-key-Äሴ")).as("Check assumption about java.util.Properties keys").isTrue();
    assertThat(javaUtilProperties.getProperty("UTF-8-key-Äሴ")).as("Check assumption about java.util.Properties values").isEqualTo("UTF-8-value-編Я");

    // - execution
    final PropertyFile readPropertyFile= PropertyFile.from(propertyFile);

    // - validation
    assertThat(readPropertyFile.propertiesSize()).isEqualTo(javaUtilProperties.size());
    for (final Map.Entry<Object, Object> e : javaUtilProperties.entrySet()) {
      final String key = (String) e.getKey();
      final String value = (String) e.getValue();
      assertThat(readPropertyFile.containsKey(key)).as("propertyFile contains key %s", key).isTrue();
      assertThat(readPropertyFile.get(key)).as("key %s contains value %s", key, value).isEqualTo(value);
    }
  }


  @Test
  public void test_UnicodeEscapeSequences() throws IOException {
    // - preparation
    final File propertyFile= this.createTestFile(""
      + " keyA1 =  valueWith\\u00dcmlaut\n"
      + " key\\u00c42 = valueA2\n");

    final Properties javaUtilProperties= new Properties();
    try (final FileInputStream fis= new FileInputStream(propertyFile);) {
      javaUtilProperties.load(fis);
    }

    // assert our assumptions about the java.util.Properties implementation
    assertThat(javaUtilProperties.size()).as("Check assumption about java.util.Properties size").isEqualTo(2);
    assertThat(javaUtilProperties.containsKey("keyA1")).as("Check assumption about java.util.Properties keys").isTrue();
    assertThat(javaUtilProperties.containsKey("keyÄ2")).as("Check assumption about java.util.Properties keys").isTrue();
    assertThat(javaUtilProperties.getProperty("keyA1")).as("Check assumption about java.util.Properties values").isEqualTo("valueWithÜmlaut");
    assertThat(javaUtilProperties.getProperty("keyÄ2")).as("Check assumption about java.util.Properties values").isEqualTo("valueA2");

    // - execution
    final PropertyFile readPropertyFile= PropertyFile.from(propertyFile);

    // - validation
    assertThat(readPropertyFile.propertiesSize()).isEqualTo(javaUtilProperties.size());
    for (final Map.Entry<Object, Object> e : javaUtilProperties.entrySet()) {
      final String key = (String) e.getKey();
      final String value = (String) e.getValue();
      assertThat(readPropertyFile.containsKey(key)).as("propertyFile contains key %s", key).isTrue();
      assertThat(readPropertyFile.get(key)).as("key %s contains value %s", key, value).isEqualTo(value);
    }
  }


  @Test
  public void test_UnicodeEscapeSequencesAndUTF8charsInSameFile() throws IOException {
    // java.util.Properties doesn't support UTF-8 characters and Unicode escape sequences in the same file
    // However PropertyFile does.

    // - preparation
    final File propertyFile= this.createTestFile(""
      + " keyA1 =  valüeWith\\u00dcmlaut\n"
      + " key\\u00c42 = välueA2\n");

    // - execution
    final PropertyFile readPropertyFile= PropertyFile.from(propertyFile);

    // - validation
    assertThat(readPropertyFile.propertiesSize()).as("Check assumption about java.util.Properties size").isEqualTo(2);
    assertThat(readPropertyFile.containsKey("keyA1")).as("Check assumption about java.util.Properties keys").isTrue();
    assertThat(readPropertyFile.containsKey("keyÄ2")).as("Check assumption about java.util.Properties keys").isTrue();
    assertThat(readPropertyFile.get("keyA1")).as("Check assumption about java.util.Properties values").isEqualTo("valüeWithÜmlaut");
    assertThat(readPropertyFile.get("keyÄ2")).as("Check assumption about java.util.Properties values").isEqualTo("välueA2");
  }


  @Test
  public void test_LiteralNewline() throws IOException {
    // - preparation
    // \n as readable two-character sequence should lead to a real line break
    // - preparation
    final File propertyFile= this.createTestFile(""
      + " keyA1 =  value over multiple lines \\\n- linebreak ignored\n"
      + " keyA2 =  value over multiple lines \\n- literal linebreak remains\n"
      + " keyA3 =  value over multiple lines \\n\\\n- both; literal remains\n"
      + " keyA4 =  value over multiple lines \\\\n- literal linebreak remains\n"
    );

    final Properties javaUtilProperties= new Properties();
    try (final FileInputStream fis= new FileInputStream(propertyFile);) {
      javaUtilProperties.load(fis);
    }

    // assert our assumptions about the java.util.Properties implementation
    assertThat(javaUtilProperties.size()).as("Check assumption about java.util.Properties size").isEqualTo(4);
    assertThat(javaUtilProperties).containsOnlyKeys("keyA1", "keyA2", "keyA3", "keyA4");
    assertThat(javaUtilProperties.getProperty("keyA1")).as("Check assumption about java.util.Properties values").isEqualTo("value over multiple lines - linebreak ignored");
    assertThat(javaUtilProperties.getProperty("keyA2")).as("Check assumption about java.util.Properties values").isEqualTo("value over multiple lines \n- literal linebreak remains");
    assertThat(javaUtilProperties.getProperty("keyA3")).as("Check assumption about java.util.Properties values").isEqualTo("value over multiple lines \n- both; literal remains");
    assertThat(javaUtilProperties.getProperty("keyA4")).as("Check assumption about java.util.Properties values").isEqualTo("value over multiple lines \\n- literal linebreak remains");

    // - execution
    final PropertyFile readPropertyFile= PropertyFile.from(propertyFile);

    // - validation
    assertThat(readPropertyFile.propertiesSize()).isEqualTo(javaUtilProperties.size());
    for (final Map.Entry<Object, Object> e : javaUtilProperties.entrySet()) {
      final String key = (String) e.getKey();
      final String value = (String) e.getValue();
      assertThat(readPropertyFile.containsKey(key)).as("propertyFile contains key %s", key).isTrue();
      assertThat(readPropertyFile.get(key)).as("key %s contains value %s", key, value).isEqualTo(value);
    }
  }


  @Test
  public void test_UnicodeEscapeAndRorN() throws IOException {
    // - preparation
    final File propertyFile= this.createTestFile(""
      + " kommerror =  Die Kommunikation ist gest\\u00f6rt.\n"
      + " btn.modify =  \\u00c4ndern\n"
      + " rivers = Fl\\u00fcsse"
      , ISO_8859_1
    );

    final Properties javaUtilProperties= new Properties();
    try (final FileInputStream fis= new FileInputStream(propertyFile);) {
      javaUtilProperties.load(fis);
    }

    // assert our assumptions about the java.util.Properties implementation
    assertThat(javaUtilProperties.size()).as("Check assumption about java.util.Properties size").isEqualTo(3);
    assertThat(javaUtilProperties).containsOnlyKeys("kommerror", "btn.modify", "rivers");
    assertThat(javaUtilProperties.getProperty("kommerror")).as("Check assumption about java.util.Properties values").isEqualTo("Die Kommunikation ist gestört.");
    assertThat(javaUtilProperties.getProperty("btn.modify")).as("Check assumption about java.util.Properties values").isEqualTo("Ändern");
    assertThat(javaUtilProperties.getProperty("rivers")).as("Check assumption about java.util.Properties values").isEqualTo("Flüsse");

    // - execution
    final PropertyFile readPropertyFile= PropertyFile.from(propertyFile, ISO_8859_1);

    // - validation
    assertThat(readPropertyFile.propertiesSize()).isEqualTo(javaUtilProperties.size());
    for (final Map.Entry<Object, Object> e : javaUtilProperties.entrySet()) {
      final String key = (String) e.getKey();
      final String value = (String) e.getValue();
      assertThat(readPropertyFile.containsKey(key)).as("propertyFile contains key %s", key).isTrue();
      assertThat(readPropertyFile.get(key)).as("key %s contains value %s", key, value).isEqualTo(value);
    }
  }


  @Test
  public void testClonePropertyFile() {
    // - preparation
    final PropertyFile orig= new PropertyFile();
    orig.appendEntry(new BasicEntry("# some Comment\n"));
    orig.appendEntry(new PropertyEntry(" \t", "BmyKey1", " = ", "myValue1", "\n"));
    orig.appendEntry(new BasicEntry("\n"));
    orig.appendEntry(new BasicEntry("# another Comment\n"));
    orig.appendEntry(new PropertyEntry("\t\t", "AmyKey2", ": ", "myValue2", "\rn"));

    // Check that the clone equals the original

    // - execution
    final PropertyFile copy= PropertyFile.from(orig);

    // - validation
    assertThat(copy.getAllEntries()).isEqualTo(orig.getAllEntries());

    // Check that modifications in the clone do not affect the original

    // - execution
    copy.reorderByKey();
    copy.getAllEntries().remove(0);
    copy.setValue("AmyKey1", "a changed value");
    copy.appendEntry(new BasicEntry("# Closing comment\n"));

    // - verification
    assertThat(orig.entriesSize()).isEqualTo(5);
    assertThat(orig.getAllEntries()).containsExactly(
      new BasicEntry("# some Comment\n"),
      new PropertyEntry(" \t", "BmyKey1", " = ", "myValue1", "\n"),
      new BasicEntry("\n"),
      new BasicEntry("# another Comment\n"),
      new PropertyEntry("\t\t", "AmyKey2", ": ", "myValue2", "\rn")
    );
  }


  @Test
  public void testCloneFromNull() {
    // - preparation
    final PropertyFile orig= null;

    // - execution
    final PropertyFile clone= PropertyFile.from(orig);

    // - verification
    assertThat(clone).isNotNull();
    assertThat(clone.getAllEntries()).hasSize(0);
  }


  @Test
  public void testWritePropertyFile() throws IOException {
    // - preparation
    final File propertyFile= this.createTestFile(""
        + "keyA1 =  valueA1\n"
        + "keyA2 = value A=2\n"
        + "keyA3 : value A:3\n"
        + "keyA4   value A 4"
    );
    final PropertyFile readPropertyFile= PropertyFile.from(propertyFile);

    final File targetFile= this.createTestFile("");
    targetFile.delete();

    // - execution
    readPropertyFile.setValue("keyA1", "NEW valueA1");
    readPropertyFile.setValue("keyA3", "NEW value A:3");
    readPropertyFile.saveTo(targetFile);

    // - validation
    final String newFileContent= toString(targetFile);
    assertThat(newFileContent).isEqualTo(""
        + "keyA1 =  NEW valueA1\n"
        + "keyA2 = value A=2\n"
        + "keyA3 : NEW value A:3\n"
        + "keyA4   value A 4\n"
    );
  }


  @Test
  public void testWritePropertyFile_ChangedValueFormatButSameContent() throws IOException {
    // - preparation
    final File propertyFile= this.createTestFile(""
        + "strange\\ \\\n"
      + "  \tkey= strange \\\n"
      + "  \tvalue"
    );
    final PropertyFile readPropertyFile= PropertyFile.from(propertyFile);

    assertThat(readPropertyFile.propertiesSize()).isEqualTo(1);
    assertThat(readPropertyFile.toMap()).containsOnlyKeys("strange key");
    assertThat(readPropertyFile.get("strange key")).isEqualTo("strange value");

    // - execution
    readPropertyFile.setValue("strange key", "sane value");
    readPropertyFile.saveTo(propertyFile);

    // - validationassertThat(readPropertyFile.size()).isEqualTo(1);
    assertThat(readPropertyFile.toMap()).containsOnlyKeys("strange key");
    assertThat(readPropertyFile.get("strange key")).isEqualTo("sane value");
    assertThat(readPropertyFile.getAllEntries()).hasSize(1);
    assertThat(readPropertyFile.getAllEntries()).containsExactly(new PropertyEntry("", "strange\\ \\\n  \tkey", "= ", "sane value", "\n"));

    final String newFileContent= toString(propertyFile);
    assertThat(newFileContent).isEqualTo(""
        + "strange\\ \\\n"
      + "  \tkey= sane value\n"
    );
  }


  @Test
  public void testWritePropertyFile_SomeUpdateEntries_SomeAppendedEntries() throws IOException {
    // - preparation
    final File propertyFile= this.createTestFile(""
        + "stable\\ key = not updated\n"
        + "nextKey = Schüsselښ"
    );
    final PropertyFile readPropertyFile= PropertyFile.from(propertyFile);

    final File targetFile= this.createTestFile("");
    targetFile.delete();

    // - execution
    readPropertyFile.setValue("UTF-8-key-Äሴ", "UTF-8-value-編Я");
    readPropertyFile.setValue("nextKey", "was updated");
    readPropertyFile.saveTo(targetFile);

    // - validation
    final String newFileContent= toString(targetFile);
    assertThat(newFileContent).isEqualTo(""
        + "stable\\ key = not updated\n"
        + "nextKey = was updated\n"
        + "UTF-8-key-Äሴ = UTF-8-value-編Я\n"
    );
  }


  @Test
  public void testWritePropertyFile_UTF8_to_ISO88591() throws IOException {
    // - preparation
    final File propertyFile= this.createTestFile(""
        + "nextKey = Schüsselښ"
      , UTF_8
    );
    final PropertyFile readPropertyFile= PropertyFile.from(propertyFile, UTF_8);

    final File targetFile= this.createTestFile("");
    targetFile.delete();

    // - execution
    readPropertyFile.setValue("UTF-8-key-Äሴ", "UTF-8-value-編Я");
    readPropertyFile.saveTo(targetFile, ApronOptions.create().with(ISO_8859_1));

    // - validation
    final String newFileContent= toString(targetFile, ISO_8859_1);
    assertThat(newFileContent).isEqualTo(""
        + "nextKey = Sch\\u00fcssel\\u069a\n"
        + "UTF-8-key-\\u00c4\\u1234 = UTF-8-value-\\u7de8\\u042f\n"
    );
  }


  @Test
  public void testUpdatePropertyFile_ISO88591_to_UTF8() throws IOException {
    // - preparation
    final File propertyFile= this.createTestFile(""
        + "nextKey = Sch\\u00fcssel\\u069a"
      , ISO_8859_1
    );
    final PropertyFile readPropertyFile= PropertyFile.from(propertyFile, ISO_8859_1);

    // - execution
    readPropertyFile.setValue("UTF-8-key-Äሴ", "UTF-8-value-編Я");
    readPropertyFile.saveTo(propertyFile, ApronOptions.create().with(UTF_8));

    // - validation
    final String newFileContent= toString(propertyFile, UTF_8);
    assertThat(newFileContent).isEqualTo(""
        + "nextKey = Sch\\u00fcssel\\u069a\n" // escape sequences in original file remain as they are, unless the actual content has changed
        + "UTF-8-key-Äሴ = UTF-8-value-編Я\n"
    );
  }


  @Test
  public void testOverwritePropertyFile_ISO88591_to_UTF8() throws IOException {
    // - preparation
    final File propertyFile= this.createTestFile(""
        + "nextKey = Sch\\u00fcssel\\u069a"
      , ISO_8859_1
    );
    final PropertyFile readPropertyFile= PropertyFile.from(propertyFile, ISO_8859_1);

    final File targetFile= this.createTestFile("");
    targetFile.delete();

    // - execution
    readPropertyFile.setValue("UTF-8-key-Äሴ", "UTF-8-value-編Я");
    readPropertyFile.saveTo(targetFile, ApronOptions.create().with(UTF_8));

    // - validation
    final String newFileContent= toString(targetFile, UTF_8);
    assertThat(newFileContent).isEqualTo(""
        + "nextKey = Schüsselښ\n" // escape sequences is written in target encoding, since we write to a new file (or request an overwrite)
        + "UTF-8-key-Äሴ = UTF-8-value-編Я\n"
    );
  }


  @Test
  public void testWritePropertyFile_ISO88591_to_UTF8_updateContent() throws IOException {
    // - preparation
    final File propertyFile= this.createTestFile(""
        + "nextKey = Sch\\u00fcssel\\u069a"
      , ISO_8859_1
    );
    final PropertyFile readPropertyFile= PropertyFile.from(propertyFile, ISO_8859_1);

    // - execution
    readPropertyFile.setValue("UTF-8-key-Äሴ", "UTF-8-value-編Я");
    readPropertyFile.setValue("nextKey", "Schlüsselښ");  //overwrite value with a different content (added 'l' between h and 'ü')
    readPropertyFile.saveTo(propertyFile, ApronOptions.create().with(UTF_8));

    // - validation
    final String newFileContent= toString(propertyFile, UTF_8);
    assertThat(newFileContent).isEqualTo(""
        + "nextKey = Schlüsselښ\n" // escape sequences are now written as UTF-8, since the content has changed
        + "UTF-8-key-Äሴ = UTF-8-value-編Я\n"
    );
  }


  @Test
  public void testWritePropertyFile_ISO88591_to_UTF8_updateContentWithoutRealChanges() throws IOException {
    // - preparation
    final File propertyFile= this.createTestFile(""
        + "nextKey = Sch\\u00fcssel\\u069a"
      , ISO_8859_1
    );
    final PropertyFile readPropertyFile= PropertyFile.from(propertyFile, ISO_8859_1);

    // - execution
    readPropertyFile.setValue("UTF-8-key-Äሴ", "UTF-8-value-編Я");
    readPropertyFile.setValue("nextKey", "Schüsselښ");  //overwrite value with the same content
    readPropertyFile.saveTo(propertyFile, ApronOptions.create().with(UTF_8));

    // - validation
    final String newFileContent= toString(propertyFile, UTF_8);
    assertThat(newFileContent).isEqualTo(""
        + "nextKey = Sch\\u00fcssel\\u069a\n" // escape sequences are still there, since the content has not changed
        + "UTF-8-key-Äሴ = UTF-8-value-編Я\n"
    );
  }


  @Test
//  @Ignore("This is postponed for later. When doing PropertyFileWriter#writeEntry() we need to tell the writer to convert everything to UTF-8")
  public void testWritePropertyFile_ISO88591_to_UTF8_toNewFile() throws IOException {
    // - preparation
    // all UTF-8 characters should be written as is, not as escape sequences
    final File propertyFile= this.createTestFile(""
        + "nextKey = Sch\\u00fcssel\\u069a"
      , ISO_8859_1
    );
    final PropertyFile readPropertyFile= PropertyFile.from(propertyFile, ISO_8859_1);

    final File targetFile= this.createTestFile("");
    targetFile.delete();

    // - execution
    readPropertyFile.setValue("UTF-8-key-Äሴ", "UTF-8-value-編Я");
    readPropertyFile.saveTo(targetFile, ApronOptions.create().with(UTF_8));

    // - validation
    final String newFileContent= toString(targetFile, UTF_8);
    assertThat(newFileContent).isEqualTo(""
        + "nextKey = Schüsselښ\n"
        + "UTF-8-key-Äሴ = UTF-8-value-編Я\n"
    );
  }


  @Test
  public void testWritePropertyFile_ISO88591_to_UTF8_updateExistingFile() throws IOException {
    // - preparation
    // only new entries are written as UTF-8 characters. Existing ones remain as they are (maybe as unicode escape sequences)
    final File propertyFile= this.createTestFile(""
        + "nextKey = Sch\\u00fcssel\\u069a"
      , ISO_8859_1
    );
    final PropertyFile readPropertyFile= PropertyFile.from(propertyFile, ISO_8859_1);

    // - execution
    readPropertyFile.setValue("UTF-8-key-Äሴ", "UTF-8-value-編Я");
    readPropertyFile.saveTo(propertyFile, ApronOptions.create().with(UTF_8));

    // - validation
    final String newFileContent= toString(propertyFile, UTF_8);
    assertThat(newFileContent).isEqualTo(""
        + "nextKey = Sch\\u00fcssel\\u069a\n"
        + "UTF-8-key-Äሴ = UTF-8-value-編Я\n"
    );
  }



  @Test
  public void testWritePropertyFile_ReadAndWriteISO88591() throws IOException {
    // - preparation
    final File propertyFile= this.createTestFile(""
        + "UTF-8-key-\\u00c4\\u1234 = UTF-8-value-\\u7de8\\u042f"
      , ISO_8859_1
    );
    final PropertyFile readPropertyFile= PropertyFile.from(propertyFile, ISO_8859_1);

    final File targetFile= this.createTestFile("");
    targetFile.delete();

    // - execution
    readPropertyFile.setValue("nextKey", "Schüssel");
    readPropertyFile.saveTo(targetFile, ApronOptions.create().with(ISO_8859_1));

    // - validation
    final String newFileContent= toString(targetFile, ISO_8859_1);
    assertThat(newFileContent).isEqualTo(""
        + "UTF-8-key-\\u00c4\\u1234 = UTF-8-value-\\u7de8\\u042f\n"
        + "nextKey = Sch\\u00fcssel\n"
    );
  }


  @Test
  public void testWritePropertyFile_ReadAndWriteUTF8() throws IOException {
    // - preparation
    final File propertyFile= this.createTestFile(""
        + "UTF-8-key-Äሴ = UTF-8-value-編Я"
      , UTF_8
    );
    final PropertyFile readPropertyFile= PropertyFile.from(propertyFile, UTF_8);

    final File targetFile= this.createTestFile("");
    targetFile.delete();

    // - execution
    readPropertyFile.setValue("nextKey", "Schüssel");
    readPropertyFile.saveTo(targetFile, ApronOptions.create().with(UTF_8));

    // - validation
    final String newFileContent= toString(targetFile, UTF_8);
    assertThat(newFileContent).isEqualTo(""
        + "UTF-8-key-Äሴ = UTF-8-value-編Я\n"
        + "nextKey = Schüssel\n"
    );
  }


  @Test
  public void testWritePropertyFile_UnicodeValuesAsUTF8() throws IOException {
    // - preparation
    final PropertyFile propertyFile= new PropertyFile();

    final File targetFile= this.createTestFile("");
    targetFile.delete();

    // - execution
    propertyFile.setValue("nextKey", "Schüsselښ");
    propertyFile.setValue("UTF-8-key-Äሴ", "UTF-8-value-編Я");
    propertyFile.saveTo(targetFile, ApronOptions.create().with(UTF_8));

    // - validation
    final String newFileContent= toString(targetFile, UTF_8);
    assertThat(newFileContent).isEqualTo(""
        + "nextKey = Schüsselښ\n"
        + "UTF-8-key-Äሴ = UTF-8-value-編Я\n"
    );
  }


  @Test
  public void testWritePropertyFile_UnicodeValuesAsEscapeSequence() throws IOException {
    // - preparation
    final PropertyFile propertyFile= new PropertyFile();

    final File targetFile= this.createTestFile("");
    targetFile.delete();

    // - execution
    propertyFile.setValue("nextKey", "Schüsselښ");
    propertyFile.setValue("UTF-8-key-Äሴ", "UTF-8-value-編Я");
    propertyFile.saveTo(targetFile, ApronOptions.create().with(ISO_8859_1));

    // - validation
    final String newFileContent= toString(targetFile, ISO_8859_1);
    assertThat(newFileContent).isEqualTo(""
        + "nextKey = Sch\\u00fcssel\\u069a\n"
        + "UTF-8-key-\\u00c4\\u1234 = UTF-8-value-\\u7de8\\u042f\n"
    );
  }


  @Test
  public void testAppendEntry_DuplicateKey() throws IOException {
    // - preparation
    final PropertyFile propertyFile= new PropertyFile();

    final File targetFile= this.createTestFile("");
    targetFile.delete();

    // - execution
    propertyFile.appendEntry(new PropertyEntry("myKey", "myValue"));
    propertyFile.appendEntry(new PropertyEntry("someOtherKey", "withAnotherValue"));
    propertyFile.appendEntry(new PropertyEntry("myKey", "shadowingValue"));
    propertyFile.saveTo(targetFile);

    // - validation
    assertThat(propertyFile.getAllEntries()).hasSize(3);  // 3 entries
    assertThat(propertyFile.propertiesSize()).isEqualTo(2); // but only 2 keys
    assertThat(propertyFile.toMap()).hasSize(2);
    assertThat(propertyFile.toMap()).containsOnlyKeys("myKey", "someOtherKey");
    assertThat(propertyFile.get("myKey")).isEqualTo("shadowingValue");
    assertThat(propertyFile.get("someOtherKey")).isEqualTo("withAnotherValue");

    assertThat(propertyFile.getAllEntries()).containsExactly(
      new PropertyEntry("myKey", "myValue"),
      new PropertyEntry("someOtherKey", "withAnotherValue"),
      new PropertyEntry("myKey", "shadowingValue")
    );

    final String newFileContent= toString(targetFile);
    assertThat(newFileContent).isEqualTo(""
      + "myKey = myValue\n"
      + "someOtherKey = withAnotherValue\n"
      + "myKey = shadowingValue\n"
    );
  }


  @Test
  public void testUpdate_NonExistantFile() throws IOException {
    // - preparation
    final PropertyFile propertyFile= new PropertyFile();

    final File targetFile= this.createTestFile("");
    targetFile.delete();

    // - execution and verification
    propertyFile.appendEntry(new PropertyEntry("myKey", "myValue"));
    propertyFile.appendEntry(new PropertyEntry("someOtherKey", "withAnotherValue"));

    assertThatThrownBy(() -> propertyFile.update(targetFile)).hasCauseInstanceOf(FileNotFoundException.class);
  }


  @Test
  public void testUpdate_RetainMissingKeys() throws IOException {
    // - preparation
    final File propertyFile= this.createTestFile(""
      + "keyA1 =  valueA1\n"
      + "keyA2 = value A2 \\\n"
      + "          over multiple \\\n"
      + "          lines\n"
      + "keyA3 : value A3\n"
    );
    final PropertyFile readPropertyFile= PropertyFile.from(propertyFile);

    // - execution
    readPropertyFile.setValue("keyA1", "NEW valueA1");
    readPropertyFile.remove("keyA2");
    readPropertyFile.setValue("keyA4", "NEW value A4");

    readPropertyFile.update(propertyFile, ApronOptions.create().with(MissingKeyAction.NOTHING));

    // - validation
    final String newFileContent= toString(propertyFile);
    assertThat(newFileContent).isEqualTo(""
      + "keyA1 =  NEW valueA1\n"
      + "keyA2 = value A2 \\\n"
      + "          over multiple \\\n"
      + "          lines\n"
      + "keyA3 : value A3\n"
      + "keyA4 = NEW value A4\n"
    );
  }


  @Test
  public void testUpdate_DeleteMissingKeys() throws IOException {
    // - preparation
    final File propertyFile= this.createTestFile(""
      + "keyA1 =  valueA1\n"
      + "keyA2 = value A2 \\\n"
      + "          over multiple \\\n"
      + "          lines\n"
      + "keyA3 : value A3\n"
    );
    final PropertyFile readPropertyFile= PropertyFile.from(propertyFile);

    // - execution
    readPropertyFile.setValue("keyA1", "NEW valueA1");
    readPropertyFile.remove("keyA2");
    readPropertyFile.setValue("keyA4", "NEW value A4");
    readPropertyFile.update(propertyFile, ApronOptions.create().with(MissingKeyAction.DELETE));

    // - validation
    final String newFileContent= toString(propertyFile);
    assertThat(newFileContent).isEqualTo(""
        + "keyA1 =  NEW valueA1\n"
        + "keyA3 : value A3\n"
        + "keyA4 = NEW value A4\n"
    );
  }


  @Test
  public void testUpdate_CommentMissingKeys() throws IOException {
    // - preparation
    final File propertyFile= this.createTestFile(""
      + "keyA1 =  valueA1\n"
      + "keyA2 = value A2 \\\n"
      + "          over multiple \\\n"
      + "          lines\n"
      + "keyA3 : value A3\n"
    );
    final PropertyFile readPropertyFile= PropertyFile.from(propertyFile);

    // - execution
    readPropertyFile.setValue("keyA1", "NEW valueA1");
    readPropertyFile.remove("keyA2");
    readPropertyFile.setValue("keyA4", "NEW value A4");
    readPropertyFile.update(propertyFile, ApronOptions.create().with(MissingKeyAction.COMMENT));

    // - validation
    final String newFileContent= toString(propertyFile);
    assertThat(newFileContent).isEqualTo(""
      + "keyA1 =  NEW valueA1\n"
      + "#keyA2 = value A2 \\\n"
      + "#          over multiple \\\n"
      + "#          lines\n"
      + "keyA3 : value A3\n"
      + "keyA4 = NEW value A4\n"
    );
  }


  /**
   * This test verifies bug #4: https://github.com/hupfdule/apron/issues/4
   */
  @Test
  public void testUpdate_CompareUnescapedValues() throws IOException {
    // - preparation
    final File propertyFile= this.createTestFile(""
      + "keyA2 = value A2 \\n"
      + "          with literal \\n"
      + "          linebreaks\n"
    );
    final PropertyFile readPropertyFile= PropertyFile.from(propertyFile);

    // - execution
    final String origValue= readPropertyFile.get("keyA2");
    readPropertyFile.setValue("keyA2", origValue);
    readPropertyFile.update(propertyFile, ApronOptions.create().with(MissingKeyAction.COMMENT));

    // - validation
    final String newFileContent= toString(propertyFile);
    assertThat(newFileContent).isEqualTo(""
      + "keyA2 = value A2 \\n"
      + "          with literal \\n"
      + "          linebreaks\n"
    );
  }


  /**
   * This test verifies bug #3: https://github.com/hupfdule/apron/issues/3
   */
  @Test
  public void testUpdate_LinebreakInNewValue() throws IOException {
    // - preparation
    final File propertyFile= this.createTestFile(""
      + "keyA1 =  valueA1\n"
    );
    final PropertyFile readPropertyFile= PropertyFile.from(propertyFile);

    // - execution
    readPropertyFile.setValue("keyA1", "new Value with \nlinebreak");
    readPropertyFile.update(propertyFile, ApronOptions.create().with(MissingKeyAction.COMMENT));

    // - validation
    final String newFileContent= toString(propertyFile);
    assertThat(newFileContent).isEqualTo(""
      + "keyA1 =  new Value with \\nlinebreak\n"
    );
  }


  /**
   * This test verifies bug #3: https://github.com/hupfdule/apron/issues/3
   * It didn't trigger the bug (that only occurred on #update()
   */
  @Test
  public void testOverwrite_LinebreakInNewValue() throws IOException {
    // - preparation
    final File propertyFile= this.createTestFile(""
      + "keyA1 =  valueA1\n"
    );
    final PropertyFile readPropertyFile= PropertyFile.from(propertyFile);

    // - execution
    readPropertyFile.setValue("keyA1", "new Value with \nlinebreak");
    readPropertyFile.overwrite(propertyFile, ApronOptions.create().with(MissingKeyAction.COMMENT));

    // - validation
    final String newFileContent= toString(propertyFile);
    assertThat(newFileContent).isEqualTo(""
      + "keyA1 =  new Value with \\nlinebreak\n"
    );
  }


  @Test
  public void testKeys() {
    // - preparation
    final File propertyFile= this.createTestFile(""
      + "keyA1 =  valueA1\n"
      + " \t\n"
      + "keyA2 = value A2 \\\n"
      + "          over multiple \\\n"
      + "          lines\n"
      + " # comment line\n"
      + "keyA3 : value A3\n"
    );
    final PropertyFile readPropertyFile= PropertyFile.from(propertyFile);

    // - execution
    final Set<String> keys= readPropertyFile.keys();

    // - verification
    assertThat(keys).containsExactly("keyA1", "keyA2", "keyA3");
  }


  @Test
  public void testValues() {
    // - preparation
    final File propertyFile= this.createTestFile(""
      + "keyA1 =  valueA1\n"
      + " \t\n"
      + "keyA2 = value A2 \\\n"
      + "          over multiple \\\n"
      + "          lines\n"
      + " # comment line\n"
      + "keyA3 : value A3\n"
    );
    final PropertyFile readPropertyFile= PropertyFile.from(propertyFile);

    // - execution
    final List<String> values= readPropertyFile.values();

    // - verification
    assertThat(values).containsExactly(
      "valueA1",
      "value A2 over multiple lines",
      "value A3");
  }


  @Test
  public void testRemovePropertyEntry() {
    // - preparation
    final File propertyFile= this.createTestFile(""
      + "keyA1 =  valueA1\n"
      + " \t\n"
      + "keyA2 = value A2 \\\n"
      + "          over multiple \\\n"
      + "          lines\n"
      + " # comment line\n"
      + "keyA3 : value A3\n"
    );
    final PropertyFile readPropertyFile= PropertyFile.from(propertyFile);

    // - execution
    readPropertyFile.remove(new PropertyEntry("", "keyA1", " =  ", "valueA1", "\n"));

    // - verification
    assertThat(readPropertyFile.getAllEntries()).containsExactly(
      new BasicEntry(" \t\n"),
      new PropertyEntry("", "keyA2", " = ", "value A2 \\\n"
      + "          over multiple \\\n"
      + "          lines", "\n"),
      new BasicEntry(" # comment line\n"),
      new PropertyEntry("", "keyA3", " : ", "value A3", "\n"));
  }


  @Test
  public void testRemoveBasicEntry() {
    // - preparation
    final File propertyFile= this.createTestFile(""
      + " # comment line to be removed \n"
      + "keyA1 =  valueA1\n"
      + " \t\n"
      + " # comment line to remain\n"
      + "keyA2 = value A2 \\\n"
      + "          over multiple \\\n"
      + "          lines\n"
      + " # comment line to be removed \n"
      + "keyA3 : value A3\n"
    );
    final PropertyFile readPropertyFile= PropertyFile.from(propertyFile);

    // - execution
    readPropertyFile.remove(new BasicEntry(" # comment line to be removed \n"));

    // - verification
    assertThat(readPropertyFile.getAllEntries()).containsExactly(
      new PropertyEntry("", "keyA1", " =  ", "valueA1", "\n"),
      new BasicEntry(" \t\n"),
      new BasicEntry(" # comment line to remain\n"),
      new PropertyEntry("", "keyA2", " = ", "value A2 \\\n"
      + "          over multiple \\\n"
      + "          lines", "\n"),
      new PropertyEntry("", "keyA3", " : ", "value A3", "\n"));
  }


  @Test
  public void testReplaceBasicEntryWithBasicEntry() {
    // - preparation
    final File propertyFile= this.createTestFile(""
      + " # comment line to be removed \n"
      + "keyA1 =  valueA1\n"
      + " \t\n"
      + " # comment line to remain\n"
      + "keyA2 = value A2 \\\n"
      + "          over multiple \\\n"
      + "          lines\n"
      + " # comment line to be removed \n"
      + "keyA3 : value A3\n"
    );
    final PropertyFile readPropertyFile= PropertyFile.from(propertyFile);

    // - execution
    final boolean replaced= readPropertyFile.replace(new BasicEntry(" # comment line to be removed \n"), new BasicEntry(" # the new comment\n"));

    // - verification
    assertThat(replaced).isTrue();
    assertThat(readPropertyFile.getAllEntries()).containsExactly(
      new BasicEntry(" # the new comment\n"),
      new PropertyEntry("", "keyA1", " =  ", "valueA1", "\n"),
      new BasicEntry(" \t\n"),
      new BasicEntry(" # comment line to remain\n"),
      new PropertyEntry("", "keyA2", " = ", "value A2 \\\n"
      + "          over multiple \\\n"
      + "          lines", "\n"),
      new BasicEntry(" # comment line to be removed \n"),  // not replaced, since only the first occurrence is replaced
      new PropertyEntry("", "keyA3", " : ", "value A3", "\n"));
  }


  @Test
  public void testReplaceBasicEntryWithPropertyEntry() {
    // - preparation
    final File propertyFile= this.createTestFile(""
      + " # comment line to be removed \n"
      + "keyA1 =  valueA1\n"
      + " \t\n"
      + " # comment line to remain\n"
      + "keyA2 = value A2 \\\n"
      + "          over multiple \\\n"
      + "          lines\n"
      + " # comment line to be removed \n"
      + "keyA3 : value A3\n"
    );
    final PropertyFile readPropertyFile= PropertyFile.from(propertyFile);

    // - execution
    final boolean replaced= readPropertyFile.replace(new BasicEntry(" # comment line to be removed \n"), new PropertyEntry("newKey", "newValue"));

    // - verification
    assertThat(replaced).isTrue();
    assertThat(readPropertyFile.getAllEntries()).containsExactly(
      new PropertyEntry("newKey", "newValue"),
      new PropertyEntry("", "keyA1", " =  ", "valueA1", "\n"),
      new BasicEntry(" \t\n"),
      new BasicEntry(" # comment line to remain\n"),
      new PropertyEntry("", "keyA2", " = ", "value A2 \\\n"
      + "          over multiple \\\n"
      + "          lines", "\n"),
      new BasicEntry(" # comment line to be removed \n"),
      new PropertyEntry("", "keyA3", " : ", "value A3", "\n"));
  }


  @Test
  public void testReplacePropertyEntryWithBasicEntry() {
    // - preparation
    final File propertyFile= this.createTestFile(""
      + " # comment line to be removed \n"
      + "keyA1 =  valueA1\n"
      + " \t\n"
      + " # comment line to remain\n"
      + "keyA2 = value A2 \\\n"
      + "          over multiple \\\n"
      + "          lines\n"
      + " # comment line to be removed \n"
      + "keyA3 : value A3\n"
    );
    final PropertyFile readPropertyFile= PropertyFile.from(propertyFile);

    // - execution
    final boolean replaced= readPropertyFile.replace(new PropertyEntry("", "keyA3", " : ", "value A3", "\n"), new BasicEntry("# replacement"));

    // - verification
    assertThat(replaced).isTrue();
    assertThat(readPropertyFile.getAllEntries()).containsExactly(
      new BasicEntry(" # comment line to be removed \n"),
      new PropertyEntry("", "keyA1", " =  ", "valueA1", "\n"),
      new BasicEntry(" \t\n"),
      new BasicEntry(" # comment line to remain\n"),
      new PropertyEntry("", "keyA2", " = ", "value A2 \\\n"
      + "          over multiple \\\n"
      + "          lines", "\n"),
      new BasicEntry(" # comment line to be removed \n"),
      new BasicEntry("# replacement"));
  }


  @Test
  public void testReplaceNonexistent() {
    // - preparation
    final File propertyFile= this.createTestFile(""
      + " # comment line to be removed \n"
      + "keyA1 =  valueA1\n"
      + " \t\n"
      + " # comment line to remain\n"
      + "keyA2 = value A2 \\\n"
      + "          over multiple \\\n"
      + "          lines\n"
      + " # comment line to be removed \n"
      + "keyA3 : value A3\n"
    );
    final PropertyFile readPropertyFile= PropertyFile.from(propertyFile);

    // - execution
    final boolean replaced= readPropertyFile.replace(new BasicEntry("# This entry does not exist\n"), new BasicEntry("# replacement"));

    // - verification
    assertThat(replaced).isFalse();
    assertThat(readPropertyFile.getAllEntries()).containsExactly(
      new BasicEntry(" # comment line to be removed \n"),
      new PropertyEntry("", "keyA1", " =  ", "valueA1", "\n"),
      new BasicEntry(" \t\n"),
      new BasicEntry(" # comment line to remain\n"),
      new PropertyEntry("", "keyA2", " = ", "value A2 \\\n"
        + "          over multiple \\\n"
        + "          lines", "\n"),
      new BasicEntry(" # comment line to be removed \n"),
      new PropertyEntry("", "keyA3", " : ", "value A3", "\n"));
  }


  @Test
  public void testToMap(){
    //the map needs to contain the _unescaped_ keys and values
    // - preparation
    final File propertyFile= this.createTestFile(""
      + " # comment line \n"
      + "keyA1 =  valueA1\n"
      + " \t\n"
      + "key編 = valueЯ \\\n"
      + "          over multiple \\\n"
      + "          lines\n"
      + "otherKey\\u7de8 = otherValue\\u042f\n"
    );
    final PropertyFile readPropertyFile= PropertyFile.from(propertyFile);

    // - execution
    final Map<String, String> propertyMap= readPropertyFile.toMap();

    // - verification
    assertThat(propertyMap).hasSize(3);
    assertThat(propertyMap).containsEntry("keyA1", "valueA1");
    assertThat(propertyMap).containsEntry("key編", "valueЯ over multiple lines");
    assertThat(propertyMap).containsEntry("otherKey編", "otherValueЯ");
  }


  @Test
  public void testClear() {
    // - preparation
    final File propertyFile= this.createTestFile(""
      + " # comment line 1 \n"
      + "keyA1 =  valueA1\n"
      + " \t\n"
      + " # comment line 2\n"
      + "keyA2 = value A2\n"
    );
    final PropertyFile readPropertyFile= PropertyFile.from(propertyFile);

    assertThat(readPropertyFile.getAllEntries()).containsExactly(
      new BasicEntry(" # comment line 1 \n"),
      new PropertyEntry("", "keyA1", " =  ", "valueA1", "\n"),
      new BasicEntry(" \t\n"),
      new BasicEntry(" # comment line 2\n"),
      new PropertyEntry("", "keyA2", " = ", "value A2", "\n")
    );

    // - execution
    readPropertyFile.clear();

    // - verification
    assertThat(readPropertyFile.getAllEntries()).isEmpty();
    assertThat(readPropertyFile.toMap()).isEmpty();
  }


  @Test
  public void testSetEntries() {
    // - preparation
    final File propertyFile= this.createTestFile(""
      + " # comment line 1 \n"
      + "keyA1 =  valueA1\n"
      + " \t\n"
      + " # comment line 2\n"
      + "keyA2 = value A2\n"
    );
    final PropertyFile readPropertyFile= PropertyFile.from(propertyFile);

    assertThat(readPropertyFile.getAllEntries()).containsExactly(
      new BasicEntry(" # comment line 1 \n"),
      new PropertyEntry("", "keyA1", " =  ", "valueA1", "\n"),
      new BasicEntry(" \t\n"),
      new BasicEntry(" # comment line 2\n"),
      new PropertyEntry("", "keyA2", " = ", "value A2", "\n")
    );


    // - execution
    final List<Entry> newEntries= Arrays.asList(
      new PropertyEntry("some new property", "with a value"),
      new BasicEntry("    "),
      new PropertyEntry("oh", "my"),
      new BasicEntry("# finish")
    );
    readPropertyFile.setEntries(newEntries);

    // - verification
    assertThat(readPropertyFile.getAllEntries()).containsExactly(
      new PropertyEntry("some new property", "with a value"),
      new BasicEntry("    "),
      new PropertyEntry("oh", "my"),
      new BasicEntry("# finish")
    );
    assertThat(readPropertyFile.toMap()).containsOnlyKeys("some new property", "oh");
    assertThat(readPropertyFile.toMap().get("some new property")).isEqualTo("with a value");
    assertThat(readPropertyFile.toMap().get("oh")).isEqualTo("my");
  }


  /**
   * This test verifies bug #5: https://github.com/hupfdule/apron/issues/5
   */
  @Test
  public void testSetValue_escapedLiteralNewline() throws IOException {
    // - preparation
    // The space needs escaping in the key, the backslash needs escaping in any case.
    final String key1= "my Key1";
    final String value1= "my\\nvalue 1";

    final String key2= "my\\Key2";
    final String value2= "my\\value2";

    final Properties javaUtilProperties= new Properties();
    javaUtilProperties.setProperty(key1, value1);
    javaUtilProperties.setProperty(key2, value2);

    // assert our assumptions about the java.util.Properties implementation
    assertThat(javaUtilProperties.size()).as("Check assumption about java.util.Properties size").isEqualTo(2);
    assertThat(javaUtilProperties).containsOnlyKeys(key1, key2);
    assertThat(javaUtilProperties.getProperty(key1)).as("Check assumption about java.util.Properties values").isEqualTo(value1);
    assertThat(javaUtilProperties.getProperty(key2)).as("Check assumption about java.util.Properties values").isEqualTo(value2);

    // - execution
    final PropertyFile propertyFile= new PropertyFile();
    propertyFile.setValue(key1, value1);
    propertyFile.setValue(key2, value2);

    // - validation
    assertThat(propertyFile.propertiesSize()).isEqualTo(2);
    assertThat(propertyFile.get(key1)).isEqualTo(value1);
    assertThat(propertyFile.get(key2)).isEqualTo(value2);
  }




  private File createTestFile(final String content) {
    return createTestFile(content, Charset.forName("UTF-8"));
  }


  private File createTestFile(final String content, final Charset charset) {
    try {
      final File propertyTestFile= File.createTempFile("propertyFile", ".properties");
      propertyTestFile.deleteOnExit();

      try (final PrintWriter pw= new PrintWriter(new OutputStreamWriter(new FileOutputStream(propertyTestFile), charset));) {
        pw.print(content);
      }

      return propertyTestFile;
    } catch (IOException ex) {
      throw new RuntimeException("Error in test preparation", ex);
    }
  }


  private static void debugPrint(final PropertyFile propertyFile) {
    //debug
    System.out.println("------------- PropertyFile");
    for (final Map.Entry<String, String> e : propertyFile.toMap().entrySet()) {
      final String key = (String) e.getKey();
      final String value = (String) e.getValue();
      System.out.println("'"+key+"' -> \""+value+"\"");
    }
    System.out.println("------------- PropertyFile end\n");
    //debug

    //debug
    System.out.println("------------- PropertyFile Entries");
    for (final Entry e : propertyFile.getAllEntries()) {
      System.out.println(e);
    }
    System.out.println("------------- PropertyFile Entries end\n");
    //debug
  }


  private static void debugPrint(final Properties javaUtilProperties) {
    System.out.println("------------- j.u.P");
    for (final Map.Entry<Object, Object> e : javaUtilProperties.entrySet()) {
      final String key = (String) e.getKey();
      final String value = (String) e.getValue();
      System.out.println("'"+key+"' -> \""+value+"\"");
    }
    System.out.println("------------- j.u.P end\n");
  }


  private String toString(final File file) throws IOException {
    return toString(file, UTF_8);
  }


  private String toString(final File file, final Charset charset) throws IOException {
    final StringBuilder sb= new StringBuilder();

    try(final BufferedReader reader= new BufferedReader(new InputStreamReader(new FileInputStream(file), charset));) {
      int cInt;
      while((cInt= reader.read()) != -1) {
        final char c= (char) cInt;
        sb.append(c);
      }
    }

    return sb.toString();
  }
}
