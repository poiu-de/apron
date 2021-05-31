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
package de.poiu.apron.io;

import de.poiu.apron.entry.BasicEntry;
import de.poiu.apron.entry.Entry;
import de.poiu.apron.entry.PropertyEntry;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


/**
 *
 * @author mherrn
 */
public class PropertyFileReaderTest {

  @Test
  public void testSimpleKeyValuePairs() throws IOException {
    // - preparation
    final File propertyFile= this.createTestFile(""
        + "keyA1=valueA1\n"
        + "keyA2 = value A2\n"
        + " keyA3 : value A3\n"
        + "\tkeyA4   value A 4\n");

    final Properties javaUtilProperties= new Properties();
    try (final FileInputStream fis= new FileInputStream(propertyFile);) {
      javaUtilProperties.load(fis);
    }

    // assert our assumptions about the java.util.Properties implementation
    assertThat(javaUtilProperties.size()).as("Check assumption about java.util.Properties size").isEqualTo(4);
    assertThat(javaUtilProperties.containsKey("keyA1")).as("Check assumption about java.util.Properties keys").isTrue();
    assertThat(javaUtilProperties.containsKey("keyA2")).as("Check assumption about java.util.Properties keys").isTrue();
    assertThat(javaUtilProperties.containsKey("keyA3")).as("Check assumption about java.util.Properties keys").isTrue();
    assertThat(javaUtilProperties.containsKey("keyA4")).as("Check assumption about java.util.Properties keys").isTrue();
    assertThat(javaUtilProperties.getProperty("keyA1")).as("Check assumption about java.util.Properties values").isEqualTo("valueA1");
    assertThat(javaUtilProperties.getProperty("keyA2")).as("Check assumption about java.util.Properties values").isEqualTo("value A2");
    assertThat(javaUtilProperties.getProperty("keyA3")).as("Check assumption about java.util.Properties values").isEqualTo("value A3");
    assertThat(javaUtilProperties.getProperty("keyA4")).as("Check assumption about java.util.Properties values").isEqualTo("value A 4");

    // - execution
    final List<Entry> readEntries= new ArrayList<>();
    try (final PropertyFileReader reader= new PropertyFileReader(propertyFile);) {
      Entry entry;
      while ((entry= reader.readEntry()) != null) {
        readEntries.add(entry);
      }
    }

    // - validation
    assertThat(readEntries.size()).isEqualTo(javaUtilProperties.size());
    assertThat(readEntries).allMatch(e -> e instanceof PropertyEntry);
    assertThat(readEntries.get(0)).isEqualTo(new PropertyEntry("", "keyA1", "=", "valueA1", "\n"));
    assertThat(readEntries.get(1)).isEqualTo(new PropertyEntry("", "keyA2", " = ", "value A2", "\n"));
    assertThat(readEntries.get(2)).isEqualTo(new PropertyEntry(" ", "keyA3", " : ", "value A3", "\n"));
    assertThat(readEntries.get(3)).isEqualTo(new PropertyEntry("\t", "keyA4", "   ", "value A 4", "\n"));
  }


  @Test
  public void testDifferentLineEndings() throws IOException {
    // - preparation
    final File propertyFile= this.createTestFile(""
        + "keyA1=valueA1\n"
        + "keyA2 = value A2\r"
        + " keyA3 : value A3\r\n"
        + "\tkeyA4   value A 4"); // no line ending after the last line

    final Properties javaUtilProperties= new Properties();
    try (final FileInputStream fis= new FileInputStream(propertyFile);) {
      javaUtilProperties.load(fis);
    }

    // assert our assumptions about the java.util.Properties implementation
    assertThat(javaUtilProperties.size()).as("Check assumption about java.util.Properties size").isEqualTo(4);
    assertThat(javaUtilProperties.containsKey("keyA1")).as("Check assumption about java.util.Properties keys").isTrue();
    assertThat(javaUtilProperties.containsKey("keyA2")).as("Check assumption about java.util.Properties keys").isTrue();
    assertThat(javaUtilProperties.containsKey("keyA3")).as("Check assumption about java.util.Properties keys").isTrue();
    assertThat(javaUtilProperties.containsKey("keyA4")).as("Check assumption about java.util.Properties keys").isTrue();
    assertThat(javaUtilProperties.getProperty("keyA1")).as("Check assumption about java.util.Properties values").isEqualTo("valueA1");
    assertThat(javaUtilProperties.getProperty("keyA2")).as("Check assumption about java.util.Properties values").isEqualTo("value A2");
    assertThat(javaUtilProperties.getProperty("keyA3")).as("Check assumption about java.util.Properties values").isEqualTo("value A3");
    assertThat(javaUtilProperties.getProperty("keyA4")).as("Check assumption about java.util.Properties values").isEqualTo("value A 4");

    // - execution
    final List<Entry> readEntries= new ArrayList<>();
    try (final PropertyFileReader reader= new PropertyFileReader(propertyFile);) {
      Entry entry;
      while ((entry= reader.readEntry()) != null) {
        readEntries.add(entry);
      }
    }

    // - validation
    assertThat(readEntries.size()).isEqualTo(javaUtilProperties.size());
    assertThat(readEntries).allMatch(e -> e instanceof PropertyEntry);
    assertThat(readEntries.get(0)).isEqualTo(new PropertyEntry("", "keyA1", "=", "valueA1", "\n"));
    assertThat(readEntries.get(1)).isEqualTo(new PropertyEntry("", "keyA2", " = ", "value A2", "\r"));
    assertThat(readEntries.get(2)).isEqualTo(new PropertyEntry(" ", "keyA3", " : ", "value A3", "\r\n"));
    assertThat(readEntries.get(3)).isEqualTo(new PropertyEntry("\t", "keyA4", "   ", "value A 4", "\n")); //a \n is added if not line ending was found
  }

  @Test
  public void testValueOnMultipleLines() throws IOException {
    // - preparation
    final File propertyFile= this.createTestFile(""
      + "keyA1=valueA1\n"
      + "keyA2 = value A2 \\\r"
      + "      on multiple \\\n"
      + "   \t  lines  \n"
      + " keyA3 : value A3\r\n"
      + "");

    final Properties javaUtilProperties= new Properties();
    try (final FileInputStream fis= new FileInputStream(propertyFile);) {
      javaUtilProperties.load(fis);
    }

    // assert our assumptions about the java.util.Properties implementation
    assertThat(javaUtilProperties.size()).as("Check assumption about java.util.Properties size").isEqualTo(3);
    assertThat(javaUtilProperties.containsKey("keyA1")).as("Check assumption about java.util.Properties keys").isTrue();
    assertThat(javaUtilProperties.containsKey("keyA2")).as("Check assumption about java.util.Properties keys").isTrue();
    assertThat(javaUtilProperties.containsKey("keyA3")).as("Check assumption about java.util.Properties keys").isTrue();
    assertThat(javaUtilProperties.getProperty("keyA1")).as("Check assumption about java.util.Properties values").isEqualTo("valueA1");
    assertThat(javaUtilProperties.getProperty("keyA2")).as("Check assumption about java.util.Properties values").isEqualTo("value A2 on multiple lines  ");
    assertThat(javaUtilProperties.getProperty("keyA3")).as("Check assumption about java.util.Properties values").isEqualTo("value A3");

    // - execution
    final List<Entry> readEntries= new ArrayList<>();
    try (final PropertyFileReader reader= new PropertyFileReader(propertyFile);) {
      Entry entry;
      while ((entry= reader.readEntry()) != null) {
        readEntries.add(entry);
      }
    }

    // - validation
    assertThat(readEntries.size()).isEqualTo(javaUtilProperties.size());
    assertThat(readEntries).allMatch(e -> e instanceof PropertyEntry);
    assertThat(readEntries.get(0)).isEqualTo(new PropertyEntry("", "keyA1", "=", "valueA1", "\n"));
    assertThat(readEntries.get(1)).isEqualTo(new PropertyEntry("", "keyA2", " = ", "value A2 \\\r      on multiple \\\n   \t  lines  ", "\n"));
    assertThat(readEntries.get(2)).isEqualTo(new PropertyEntry(" ", "keyA3", " : ", "value A3", "\r\n"));
  }


  @Test
  public void testKeyWithoutValue() throws IOException {
    // - preparation
    final File propertyFile= this.createTestFile(""
      + "keyA1=\n"
      + "keyA2 = \n"
      + " keyA3 : \n"
      + "\tkeyA4  \n"
      + "key\\ with\\ spaces\n"
      + "");

    final Properties javaUtilProperties= new Properties();
    try (final FileInputStream fis= new FileInputStream(propertyFile);) {
      javaUtilProperties.load(fis);
    }

    // assert our assumptions about the java.util.Properties implementation
    assertThat(javaUtilProperties.size()).as("Check assumption about java.util.Properties size").isEqualTo(5);
    assertThat(javaUtilProperties.containsKey("keyA1")).as("Check assumption about java.util.Properties keys").isTrue();
    assertThat(javaUtilProperties.containsKey("keyA2")).as("Check assumption about java.util.Properties keys").isTrue();
    assertThat(javaUtilProperties.containsKey("keyA3")).as("Check assumption about java.util.Properties keys").isTrue();
    assertThat(javaUtilProperties.containsKey("keyA4")).as("Check assumption about java.util.Properties keys").isTrue();
    assertThat(javaUtilProperties.containsKey("key with spaces")).as("Check assumption about java.util.Properties keys").isTrue();
    assertThat(javaUtilProperties.getProperty("keyA1")).as("Check assumption about java.util.Properties values").isEmpty();
    assertThat(javaUtilProperties.getProperty("keyA2")).as("Check assumption about java.util.Properties values").isEmpty();
    assertThat(javaUtilProperties.getProperty("keyA3")).as("Check assumption about java.util.Properties values").isEmpty();
    assertThat(javaUtilProperties.getProperty("keyA4")).as("Check assumption about java.util.Properties values").isEmpty();
    assertThat(javaUtilProperties.getProperty("key with spaces")).as("Check assumption about java.util.Properties values").isEmpty();

    // - execution
    final List<Entry> readEntries= new ArrayList<>();
    try (final PropertyFileReader reader= new PropertyFileReader(propertyFile);) {
      Entry entry;
      while ((entry= reader.readEntry()) != null) {
        readEntries.add(entry);
      }
    }

    // - validation
    assertThat(readEntries.size()).isEqualTo(javaUtilProperties.size());
    assertThat(readEntries).allMatch(e -> e instanceof PropertyEntry);
    assertThat(readEntries.get(0)).isEqualTo(new PropertyEntry("", "keyA1", "=", "", "\n"));
    assertThat(readEntries.get(1)).isEqualTo(new PropertyEntry("", "keyA2", " = ", "", "\n"));
    assertThat(readEntries.get(2)).isEqualTo(new PropertyEntry(" ", "keyA3", " : ", "", "\n"));
    assertThat(readEntries.get(3)).isEqualTo(new PropertyEntry("\t", "keyA4", "  ", "", "\n"));
    assertThat(readEntries.get(4)).isEqualTo(new PropertyEntry("", "key\\ with\\ spaces", "", "", "\n"));
  }


  @Test
  public void testValueWithoutKey() throws IOException {
    // - preparation
    final File propertyFile= this.createTestFile(""
      + "=valueA1\n"
      + " = valueA2\n"
      + "  : value A3\n"
      + "");

    final Properties javaUtilProperties= new Properties();
    try (final FileInputStream fis= new FileInputStream(propertyFile);) {
      javaUtilProperties.load(fis);
    }

    // assert our assumptions about the java.util.Properties implementation
    assertThat(javaUtilProperties.size()).as("Check assumption about java.util.Properties size").isEqualTo(1);
    assertThat(javaUtilProperties.containsKey("")).as("Check assumption about java.util.Properties keys").isTrue();
    assertThat(javaUtilProperties.getProperty("")).as("Check assumption about java.util.Properties values").isEqualTo("value A3");

    // - execution
    final List<Entry> readEntries= new ArrayList<>();
    try (final PropertyFileReader reader= new PropertyFileReader(propertyFile);) {
      Entry entry;
      while ((entry= reader.readEntry()) != null) {
        readEntries.add(entry);
      }
    }

    // - validation
    assertThat(readEntries.size()).isEqualTo(3); // we have no map here, therefore we get 3 entries
    assertThat(readEntries).allMatch(e -> e instanceof PropertyEntry);
    assertThat(readEntries.get(0)).isEqualTo(new PropertyEntry("", "", "=", "valueA1", "\n"));
    assertThat(readEntries.get(1)).isEqualTo(new PropertyEntry("", "", " = ", "valueA2", "\n"));
    assertThat(readEntries.get(2)).isEqualTo(new PropertyEntry("", "", "  : ", "value A3", "\n"));
  }


  @Test
  public void testCommentLines() throws IOException {
    // - preparation
    final File propertyFile= this.createTestFile(""
      + "keyA1=valueA1\n"
      + "# some comment\n"
      + "  ! some other comment    \n"
      + "keyA2 = valueA2\n"
      + " keyA3 : value A3\n"
      + "  # comment lines may not be continued \\\n"
      + "\t keyA4 = value A4  \n"
      + "");

    final Properties javaUtilProperties= new Properties();
    try (final FileInputStream fis= new FileInputStream(propertyFile);) {
      javaUtilProperties.load(fis);
    }

    // assert our assumptions about the java.util.Properties implementation
    assertThat(javaUtilProperties.size()).as("Check assumption about java.util.Properties size").isEqualTo(4);
    assertThat(javaUtilProperties.containsKey("keyA1")).as("Check assumption about java.util.Properties keys").isTrue();
    assertThat(javaUtilProperties.containsKey("keyA2")).as("Check assumption about java.util.Properties keys").isTrue();
    assertThat(javaUtilProperties.containsKey("keyA3")).as("Check assumption about java.util.Properties keys").isTrue();
    assertThat(javaUtilProperties.containsKey("keyA4")).as("Check assumption about java.util.Properties keys").isTrue();
    assertThat(javaUtilProperties.getProperty("keyA1")).as("Check assumption about java.util.Properties values").isEqualTo("valueA1");
    assertThat(javaUtilProperties.getProperty("keyA2")).as("Check assumption about java.util.Properties values").isEqualTo("valueA2");
    assertThat(javaUtilProperties.getProperty("keyA3")).as("Check assumption about java.util.Properties values").isEqualTo("value A3");
    assertThat(javaUtilProperties.getProperty("keyA4")).as("Check assumption about java.util.Properties values").isEqualTo("value A4  ");

    // - execution
    final List<Entry> readEntries= new ArrayList<>();
    try (final PropertyFileReader reader= new PropertyFileReader(propertyFile);) {
      Entry entry;
      while ((entry= reader.readEntry()) != null) {
        readEntries.add(entry);
      }
    }

    // - validation
    assertThat(readEntries.size()).isEqualTo(7); // we have no map here, therefore we get 3 entries
    assertThat(readEntries.get(0)).isExactlyInstanceOf(PropertyEntry.class);
    assertThat(readEntries.get(0)).isEqualTo(new PropertyEntry("", "keyA1", "=", "valueA1", "\n"));
    assertThat(readEntries.get(1)).isExactlyInstanceOf(BasicEntry.class);
    assertThat(readEntries.get(1)).isEqualTo(new BasicEntry("# some comment\n"));
    assertThat(readEntries.get(2)).isExactlyInstanceOf(BasicEntry.class);
    assertThat(readEntries.get(2)).isEqualTo(new BasicEntry("  ! some other comment    \n"));
    assertThat(readEntries.get(3)).isExactlyInstanceOf(PropertyEntry.class);
    assertThat(readEntries.get(3)).isEqualTo(new PropertyEntry("", "keyA2", " = ", "valueA2", "\n"));
    assertThat(readEntries.get(4)).isExactlyInstanceOf(PropertyEntry.class);
    assertThat(readEntries.get(4)).isEqualTo(new PropertyEntry(" ", "keyA3", " : ", "value A3", "\n"));
    assertThat(readEntries.get(5)).isExactlyInstanceOf(BasicEntry.class);
    assertThat(readEntries.get(5)).isEqualTo(new BasicEntry("  # comment lines may not be continued \\\n"));
    assertThat(readEntries.get(6)).isExactlyInstanceOf(PropertyEntry.class);
    assertThat(readEntries.get(6)).isEqualTo(new PropertyEntry("\t ", "keyA4", " = ", "value A4  ", "\n"));
  }


  @Test
  public void testEmptyLines() throws IOException {
    // - preparation
    final File propertyFile= this.createTestFile(""
      + "keyA1=valueA1\n"
      + "\n"
      + "       \n"
      + "keyA2 = valueA2\n"
      + " keyA3 : value A3\n"
      + "");

    final Properties javaUtilProperties= new Properties();
    try (final FileInputStream fis= new FileInputStream(propertyFile);) {
      javaUtilProperties.load(fis);
    }

    // assert our assumptions about the java.util.Properties implementation
    assertThat(javaUtilProperties.size()).as("Check assumption about java.util.Properties size").isEqualTo(3);
    assertThat(javaUtilProperties.containsKey("keyA1")).as("Check assumption about java.util.Properties keys").isTrue();
    assertThat(javaUtilProperties.containsKey("keyA2")).as("Check assumption about java.util.Properties keys").isTrue();
    assertThat(javaUtilProperties.containsKey("keyA3")).as("Check assumption about java.util.Properties keys").isTrue();
    assertThat(javaUtilProperties.getProperty("keyA1")).as("Check assumption about java.util.Properties values").isEqualTo("valueA1");
    assertThat(javaUtilProperties.getProperty("keyA2")).as("Check assumption about java.util.Properties values").isEqualTo("valueA2");
    assertThat(javaUtilProperties.getProperty("keyA3")).as("Check assumption about java.util.Properties values").isEqualTo("value A3");

    // - execution
    final List<Entry> readEntries= new ArrayList<>();
    try (final PropertyFileReader reader= new PropertyFileReader(propertyFile);) {
      Entry entry;
      while ((entry= reader.readEntry()) != null) {
        readEntries.add(entry);
      }
    }

    // - validation
    assertThat(readEntries.size()).isEqualTo(5); // we have no map here, therefore we get 3 entries
    assertThat(readEntries.get(0)).isExactlyInstanceOf(PropertyEntry.class);
    assertThat(readEntries.get(0)).isEqualTo(new PropertyEntry("", "keyA1", "=", "valueA1", "\n"));
    assertThat(readEntries.get(1)).isExactlyInstanceOf(BasicEntry.class);
    assertThat(readEntries.get(1)).isEqualTo(new BasicEntry("\n"));
    assertThat(readEntries.get(2)).isExactlyInstanceOf(BasicEntry.class);
    assertThat(readEntries.get(2)).isEqualTo(new BasicEntry("       \n"));
    assertThat(readEntries.get(3)).isExactlyInstanceOf(PropertyEntry.class);
    assertThat(readEntries.get(3)).isEqualTo(new PropertyEntry("", "keyA2", " = ", "valueA2", "\n"));
    assertThat(readEntries.get(4)).isExactlyInstanceOf(PropertyEntry.class);
    assertThat(readEntries.get(4)).isEqualTo(new PropertyEntry(" ", "keyA3", " : ", "value A3", "\n"));
  }

  @Test
  public void testOnlyEmptyLines() throws IOException {
    // - preparation
    final File propertyFile= this.createTestFile(""
      + "\n"
      + "       \n"
      + "");

    final Properties javaUtilProperties= new Properties();
    try (final FileInputStream fis= new FileInputStream(propertyFile);) {
      javaUtilProperties.load(fis);
    }

    // assert our assumptions about the java.util.Properties implementation
    assertThat(javaUtilProperties.size()).as("Check assumption about java.util.Properties size").isEqualTo(0);

    // - execution
    final List<Entry> readEntries= new ArrayList<>();
    try (final PropertyFileReader reader= new PropertyFileReader(propertyFile);) {
      Entry entry;
      while ((entry= reader.readEntry()) != null) {
        readEntries.add(entry);
      }
    }
    // - validation
    assertThat(readEntries.size()).isEqualTo(2);
    assertThat(readEntries.get(0)).isExactlyInstanceOf(BasicEntry.class);
    assertThat(readEntries.get(0)).isEqualTo(new BasicEntry("\n"));
    assertThat(readEntries.get(1)).isExactlyInstanceOf(BasicEntry.class);
    assertThat(readEntries.get(1)).isEqualTo(new BasicEntry("       \n"));
  }


  @Test
  public void testSingleBackslashOnLine() throws IOException {
    // - preparation
    final File propertyFile= this.createTestFile(""
      + "\n"
      + "   \\    \n"
      + "");

    final Properties javaUtilProperties= new Properties();
    try (final FileInputStream fis= new FileInputStream(propertyFile);) {
      javaUtilProperties.load(fis);
    }

    // assert our assumptions about the java.util.Properties implementation
    assertThat(javaUtilProperties.size()).as("Check assumption about java.util.Properties size").isEqualTo(1);
    assertThat(javaUtilProperties.containsKey(" ")).as("Check assumption about java.util.Properties keys").isTrue();
    assertThat(javaUtilProperties.getProperty(" ")).as("Check assumption about java.util.Properties values").isEmpty();

    // - execution
    final List<Entry> readEntries= new ArrayList<>();
    try (final PropertyFileReader reader= new PropertyFileReader(propertyFile);) {
      Entry entry;
      while ((entry= reader.readEntry()) != null) {
        readEntries.add(entry);
      }
    }

    // - validation
    assertThat(readEntries.size()).isEqualTo(2);
    assertThat(readEntries.get(0)).isExactlyInstanceOf(BasicEntry.class);
    assertThat(readEntries.get(0)).isEqualTo(new BasicEntry("\n"));
    assertThat(readEntries.get(1)).isExactlyInstanceOf(PropertyEntry.class);
    assertThat(readEntries.get(1)).isEqualTo(new PropertyEntry("   ", "\\ ", "   ", "", "\n"));
  }

  /**
   * This test verifies bug #11: https://github.com/hupfdule/apron/issues/11
   * @throws IOException
   */
  @Test
  public void test_CrLfEndings() throws IOException {
    // - preparation
    final File propertyFile= this.createTestFile(""
      + "lf                       = One\\\nTwo\n"
      + "cr                       = One\\\rTwo\r"
      + "crlf_both_escaped        = One\\\r\\\nTwo\r\n"
      + "crlf_only_first_escaped  = One\\\r\nTwo\r\n");

    final Properties javaUtilProperties= new Properties();
    try (final FileInputStream fis= new FileInputStream(propertyFile);) {
      javaUtilProperties.load(fis);
    }

    // assert our assumptions about the java.util.Properties implementation
    assertThat(javaUtilProperties.size()).as("Check assumption about java.util.Properties size").isEqualTo(4);
    assertThat(javaUtilProperties.containsKey("lf")).as("Check assumption about java.util.Properties keys").isTrue();
    assertThat(javaUtilProperties.containsKey("cr")).as("Check assumption about java.util.Properties keys").isTrue();
    assertThat(javaUtilProperties.containsKey("crlf_both_escaped")).as("Check assumption about java.util.Properties keys").isTrue();
    assertThat(javaUtilProperties.containsKey("crlf_only_first_escaped")).as("Check assumption about java.util.Properties keys").isTrue();
    assertThat(javaUtilProperties.getProperty("lf")).as("Check assumption about java.util.Properties values").isEqualTo("OneTwo");
    assertThat(javaUtilProperties.getProperty("cr")).as("Check assumption about java.util.Properties values").isEqualTo("OneTwo");
    assertThat(javaUtilProperties.getProperty("crlf_both_escaped")).as("Check assumption about java.util.Properties values").isEqualTo("OneTwo");
    assertThat(javaUtilProperties.getProperty("crlf_only_first_escaped")).as("Check assumption about java.util.Properties values").isEqualTo("OneTwo");

    // - execution
    final List<Entry> readEntries= new ArrayList<>();
    try (final PropertyFileReader reader= new PropertyFileReader(propertyFile);) {
      Entry entry;
      while ((entry= reader.readEntry()) != null) {
        readEntries.add(entry);
      }
    }

    // - validation
    assertThat(readEntries.size()).isEqualTo(javaUtilProperties.size());
    assertThat(readEntries.get(0)).isEqualTo(new PropertyEntry("", "lf", "                       = ", "One\\\nTwo", "\n"));
    assertThat(readEntries.get(1)).isEqualTo(new PropertyEntry("", "cr", "                       = ", "One\\\rTwo", "\r"));
    assertThat(readEntries.get(2)).isEqualTo(new PropertyEntry("", "crlf_both_escaped", "        = ", "One\\\r\\\nTwo", "\r\n"));
    assertThat(readEntries.get(3)).isEqualTo(new PropertyEntry("", "crlf_only_first_escaped", "  = ", "One\\\r\nTwo", "\r\n"));
  }

  private File createTestFile(final String content) {
    try {
      final File propertyTestFile= File.createTempFile("propertyFile", ".properties");
      propertyTestFile.deleteOnExit();

      try (final PrintWriter pw= new PrintWriter(propertyTestFile);) {
        pw.print(content);
      }

      return propertyTestFile;
    } catch (IOException ex) {
      throw new RuntimeException("Error in test preparation", ex);
    }
  }
}
