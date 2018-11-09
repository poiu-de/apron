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

import de.poiu.apron.ApronOptions;
import de.poiu.apron.UnicodeHandling;
import de.poiu.apron.entry.BasicEntry;
import de.poiu.apron.entry.Entry;
import de.poiu.apron.entry.PropertyEntry;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import org.junit.Test;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;


/**
 *
 * @author mherrn
 */
public class PropertyFileWriterTest {


  @Test
  public void testWriteEntries() throws IOException {
    // - preparation
    final Entry[] entries= {
      new BasicEntry("# Starting comment\n"),
      new BasicEntry("\n"),
      new PropertyEntry("", "keyA1", " = ", "valueA1", "\n"),
      new PropertyEntry(" ", "keyA2", ":", "valueA2", "\n"),
    };

    final File file= this.createTestFile();

    // - execution
    try(final PropertyFileWriter propertyFileWriter= new PropertyFileWriter(file);) {
      for (final Entry entry : entries) {
        propertyFileWriter.writeEntry(entry);
      }
    }

    // - verification
    final String fileContent= toString(file);
    assertThat(fileContent).isEqualTo(
          "# Starting comment\n"
        + "\n"
        + "keyA1 = valueA1\n"
        + " keyA2:valueA2\n"
    );
  }


  @Test
  public void testWriteEntries_OnlyKeyAndValue() throws IOException {
    // - preparation
    final Entry[] entries= {
      new BasicEntry("# Starting comment\n"),
      new BasicEntry("\n"),
      new PropertyEntry("keyA1", "valueA1"),     // this one has not leadingWhitespace, separator and line ending
                                                 // and is therefore using the defaults
      new PropertyEntry(" ", "keyA2", ":", "valueA2", "\n"),
    };

    final File file= this.createTestFile();

    // - execution
    try(final PropertyFileWriter propertyFileWriter= new PropertyFileWriter(file);) {
      for (final Entry entry : entries) {
        propertyFileWriter.writeEntry(entry);
      }
    }

    // - verification
    final String fileContent= toString(file);
    assertThat(fileContent).isEqualTo(
          "# Starting comment\n"
        + "\n"
        + "keyA1 = valueA1\n"
        + " keyA2:valueA2\n"
    );
  }


  @Test
  public void testWriteEntries_DifferentLineEndings() throws IOException {
    // - preparation
    final Entry[] entries= {
      new BasicEntry("# Starting comment\n"),
      new BasicEntry("\r"),
      new PropertyEntry("", "keyA1", " = ", "valueA1", "\r\n"),
      new PropertyEntry(" ", "keyA2", ":", "valueA2", ""),
    };

    final File file= this.createTestFile();

    // - execution
    try(final PropertyFileWriter propertyFileWriter= new PropertyFileWriter(file);) {
      for (final Entry entry : entries) {
        propertyFileWriter.writeEntry(entry);
      }
    }

    // - verification
    final String fileContent= toString(file);
    assertThat(fileContent).isEqualTo(
          "# Starting comment\n"
        + "\r"
        + "keyA1 = valueA1\r\n"
        + " keyA2:valueA2"
    );
  }


  @Test
  public void testWriteEntries_Multilines() throws IOException {
    // - preparation
    final Entry[] entries= {
      new BasicEntry("# Starting comment\n"),
      new BasicEntry("\n"),
      new PropertyEntry("", "keyA1\\ \\\n\tover\\ multiple\\ lines", " = ", "valueA1 \r\tover multiple lines", "\n"),
      new PropertyEntry(" ", "keyA2", ":", "valueA2", "\n"),
    };

    final File file= this.createTestFile();

    // - execution
    try(final PropertyFileWriter propertyFileWriter= new PropertyFileWriter(file);) {
      for (final Entry entry : entries) {
        propertyFileWriter.writeEntry(entry);
      }
    }

    // - verification
    final String fileContent= toString(file);
    assertThat(fileContent).isEqualTo(
          "# Starting comment\n"
        + "\n"
        + "keyA1\\ \\\n\tover\\ multiple\\ lines = valueA1 \r\tover multiple lines\n"
        + " keyA2:valueA2\n"
    );
  }


  @Test
  public void testWriteEntries_CharsetUTF8_UnicodeHandlingNOTHING() throws IOException {
    // - preparation
    final Entry[] entries= {
      new PropertyEntry("keyA1","Sch\\u00fcssel\\u069a"),
      new PropertyEntry("UTF-8-key-Äሴ", "UTF-8-value-編Я"),
    };

    final File file= this.createTestFile();

    // - execution
    try(final PropertyFileWriter propertyFileWriter= new PropertyFileWriter(file,
      ApronOptions.create()
        .with(UTF_8)
        .with(UnicodeHandling.DO_NOTHING));) {
      for (final Entry entry : entries) {
        propertyFileWriter.writeEntry(entry);
      }
    }

    // - verification
    final String fileContent= toString(file);
    assertThat(fileContent).isEqualTo(""
        + "keyA1 = Sch\\u00fcssel\\u069a\n"
        + "UTF-8-key-Äሴ = UTF-8-value-編Я\n"
    );
  }


  @Test
  public void testWriteEntries_CharsetUTF8_UnicodeHandlingBYCHARSET() throws IOException {
    // - preparation
    final Entry[] entries= {
      new PropertyEntry("keyA1","Sch\\u00fcssel\\u069a"),
      new PropertyEntry("UTF-8-key-Äሴ", "UTF-8-value-編Я"),
    };

    final File file= this.createTestFile();

    // - execution
    try(final PropertyFileWriter propertyFileWriter= new PropertyFileWriter(file,
      ApronOptions.create()
        .with(UTF_8)
        .with(UnicodeHandling.BY_CHARSET));) {
      for (final Entry entry : entries) {
        propertyFileWriter.writeEntry(entry);
      }
    }

    // - verification
    final String fileContent= toString(file);
    assertThat(fileContent).isEqualTo(""
        + "keyA1 = Schüsselښ\n"
        + "UTF-8-key-Äሴ = UTF-8-value-編Я\n"
    );
  }


  @Test
  public void testWriteEntries_CharsetUTF8_UnicodeHandlingUNICODE() throws IOException {
    // - preparation
    final Entry[] entries= {
      new PropertyEntry("keyA1","Sch\\u00fcssel\\u069a"),
      new PropertyEntry("UTF-8-key-Äሴ", "UTF-8-value-編Я"),
    };

    final File file= this.createTestFile();

    // - execution
    try(final PropertyFileWriter propertyFileWriter= new PropertyFileWriter(file,
      ApronOptions.create()
        .with(UTF_8)
        .with(UnicodeHandling.UNICODE));) {
      for (final Entry entry : entries) {
        propertyFileWriter.writeEntry(entry);
      }
    }

    // - verification
    final String fileContent= toString(file);
    assertThat(fileContent).isEqualTo(""
        + "keyA1 = Schüsselښ\n"
        + "UTF-8-key-Äሴ = UTF-8-value-編Я\n"
    );
  }


  @Test
  public void testWriteEntries_CharsetUTF8_UnicodeHandlingESCAPE() throws IOException {
    // - preparation
    final Entry[] entries= {
      new PropertyEntry("keyA1","Sch\\u00fcssel\\u069a"),
      new PropertyEntry("UTF-8-key-Äሴ", "UTF-8-value-編Я"),
    };

    final File file= this.createTestFile();

    // - execution
    try(final PropertyFileWriter propertyFileWriter= new PropertyFileWriter(file,
      ApronOptions.create()
        .with(UTF_8)
        .with(UnicodeHandling.ESCAPE));) {
      for (final Entry entry : entries) {
        propertyFileWriter.writeEntry(entry);
      }
    }

    // - verification
    final String fileContent= toString(file);
    assertThat(fileContent).isEqualTo(""
        + "keyA1 = Sch\\u00fcssel\\u069a\n"
        + "UTF-8-key-\\u00c4\\u1234 = UTF-8-value-\\u7de8\\u042f\n"
    );
  }


  @Test
  public void testWriteEntries_CharsetISO88591_UnicodeHandlingESCAPE() throws IOException {
    // - preparation
    final Entry[] entries= {
      new PropertyEntry("keyA1","Sch\\u00fcssel\\u069a"),
      new PropertyEntry("UTF-8-key-Äሴ", "UTF-8-value-編Я"),
    };

    final File file= this.createTestFile();

    // - execution
    try(final PropertyFileWriter propertyFileWriter= new PropertyFileWriter(file,
      ApronOptions.create()
        .with(ISO_8859_1)
        .with(UnicodeHandling.ESCAPE));) {
      for (final Entry entry : entries) {
        propertyFileWriter.writeEntry(entry);
      }
    }

    // - verification
    final String fileContent= toString(file);
    assertThat(fileContent).isEqualTo(""
        + "keyA1 = Sch\\u00fcssel\\u069a\n"
        + "UTF-8-key-\\u00c4\\u1234 = UTF-8-value-\\u7de8\\u042f\n"
    );
  }


  @Test
  public void testWriteEntries_CharsetISO88591_UnicodeHandlingUNICODE() throws IOException {
    // UnicodeHandling needs to be ignored, since ISO-8859-1 does not allow unicode values
    // - preparation
    final Entry[] entries= {
      new PropertyEntry("keyA1","Sch\\u00fcssel\\u069a"),
      new PropertyEntry("UTF-8-key-Äሴ", "UTF-8-value-編Я"),
    };

    final File file= this.createTestFile();

    // - execution
    try(final PropertyFileWriter propertyFileWriter= new PropertyFileWriter(file,
      ApronOptions.create()
        .with(ISO_8859_1)
        .with(UnicodeHandling.ESCAPE));) {
      for (final Entry entry : entries) {
        propertyFileWriter.writeEntry(entry);
      }
    }

    // - verification
    final String fileContent= toString(file);
    assertThat(fileContent).isEqualTo(""
        + "keyA1 = Sch\\u00fcssel\\u069a\n"
        + "UTF-8-key-\\u00c4\\u1234 = UTF-8-value-\\u7de8\\u042f\n"
    );
  }


  @Test
  public void testWriteEntries_CharsetISO88591_UnicodeHandlingNOTHING() throws IOException {
    // UnicodeHandling needs to be ignored, since ISO-8859-1 does not allow unicode values
    // - preparation
    final Entry[] entries= {
      new PropertyEntry("keyA1","Sch\\u00fcssel\\u069a"),
      new PropertyEntry("UTF-8-key-Äሴ", "UTF-8-value-編Я"),
    };

    final File file= this.createTestFile();

    // - execution
    try(final PropertyFileWriter propertyFileWriter= new PropertyFileWriter(file,
      ApronOptions.create()
        .with(ISO_8859_1)
        .with(UnicodeHandling.DO_NOTHING));) {
      for (final Entry entry : entries) {
        propertyFileWriter.writeEntry(entry);
      }
    }

    // - verification
    final String fileContent= toString(file);
    assertThat(fileContent).isEqualTo(""
        + "keyA1 = Sch\\u00fcssel\\u069a\n"
        + "UTF-8-key-\\u00c4\\u1234 = UTF-8-value-\\u7de8\\u042f\n"
    );
  }



  private File createTestFile() {
    try {
      final File propertyTestFile= File.createTempFile("propertyFile", ".properties");
      propertyTestFile.deleteOnExit();

      return propertyTestFile;
    } catch (IOException ex) {
      throw new RuntimeException("Error in test preparation", ex);
    }
  }


  private String toString(final File file) throws IOException {
    final StringBuilder sb= new StringBuilder();

    try(final BufferedReader reader= new BufferedReader(new InputStreamReader(new FileInputStream(file)));) {
      int cInt;
      while((cInt= reader.read()) != -1) {
        final char c= (char) cInt;
        sb.append(c);
      }
    }

    return sb.toString();
  }


  private static void debugPrint(String fileContent) {
    System.out.println("debug ------");
    System.out.println(">"+fileContent+"<");
    System.out.println("debug end --");
  }
}
