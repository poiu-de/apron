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
package de.poiu.apron.escaping;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


/**
 *
 * @author mherrn
 */
public class EscapeUtilsTest {

  @Test
  public void testUnescape() {
    assertThat(EscapeUtils.unescape("some normal string without escaping").toString())
      .isEqualTo("some normal string without escaping");
    assertThat(EscapeUtils.unescape("string\\ with\\ escaped\\ spaces").toString())
      .isEqualTo("string with escaped spaces");
    assertThat(EscapeUtils.unescape("key\\:\\=value").toString())
      .isEqualTo("key:=value");
    assertThat(EscapeUtils.unescape("double\\\\escaping").toString())
      .isEqualTo("double\\escaping");
    assertThat(EscapeUtils.unescape("escaped newline \\").toString())
      .isEqualTo("escaped newline ");
    assertThat(EscapeUtils.unescape("non-escaped newline \nsecond line").toString())
      .isEqualTo("non-escaped newline second line");
    assertThat(EscapeUtils.unescape("escaped newline \\\nsecond line").toString())
      .isEqualTo("escaped newline second line");
    assertThat(EscapeUtils.unescape("literal newline \\n").toString())
      .isEqualTo("literal newline \n");
  }

  @Test
  public void testUnescapeUnicode() {
      assertThat(EscapeUtils.unescapeUnicode("\\u00fc").toString())
        .isEqualTo("ü");
      assertThat(EscapeUtils.unescapeUnicode("\\u1234").toString())
        .isEqualTo("ሴ");
      assertThat(EscapeUtils.unescapeUnicode("\\u7de8").toString())
        .isEqualTo("編");
      assertThat(EscapeUtils.unescapeUnicode("\\u042f").toString())
        .isEqualTo("Я");
      assertThat(EscapeUtils.unescapeUnicode("\\u0061").toString())
        .isEqualTo("a");

      assertThat(EscapeUtils.unescapeUnicode("abcd\\u1234abcd").toString())
        .isEqualTo("abcdሴabcd");

      //invalid unicode values are left as is
      assertThat(EscapeUtils.unescapeUnicode("\\u123").toString())
        .isEqualTo("\\u123");
      assertThat(EscapeUtils.unescapeUnicode("\\u123T").toString())
        .isEqualTo("\\u123T");

      //all other escapes need to remain as is
      assertThat(EscapeUtils.unescapeUnicode("some\\ test \\# with \\=\\: escaped\\ chars\\b\\n\\\\n").toString())
        .isEqualTo("some\\ test \\# with \\=\\: escaped\\ chars\\b\\n\\\\n");
  }


  @Test
  public void testUnescape_withUnicodeValues() {
      assertThat(EscapeUtils.unescape("hinzuf\\u00fcgen").toString())
        .isEqualTo("hinzufügen");
      assertThat(EscapeUtils.unescape("hinzuf\\u00FCgen").toString())
        .isEqualTo("hinzufügen");
      assertThat(EscapeUtils.unescape("Soll nicht ersetzt werden: \\\\u00fc!").toString())
        .isEqualTo("Soll nicht ersetzt werden: \\u00fc!");
  }


  @Test
  public void testUnescape_withInvalidUnicodeValues() {
    assertThat(EscapeUtils.unescape("hinzuf\\uTTTTgen").toString())
      .isEqualTo("hinzuf\\uTTTTgen");

    assertThat(EscapeUtils.unescape("hinzuf\\uu00fcgen").toString())
      .isEqualTo("hinzuf\\uu00fcgen");
  }


  @Test
  public void testEscapeUnicode() {
      assertThat(EscapeUtils.escapeUnicode('ü').toString())
        .isEqualTo("\\u00fc");
      assertThat(EscapeUtils.escapeUnicode('ሴ').toString())
        .isEqualTo("\\u1234");
      assertThat(EscapeUtils.escapeUnicode('編').toString())
        .isEqualTo("\\u7de8");
      assertThat(EscapeUtils.escapeUnicode('Я').toString())
        .isEqualTo("\\u042f");
      assertThat(EscapeUtils.escapeUnicode('a').toString())
        .isEqualTo("\\u0061");
  }


  @Test
  public void testTranslateUnicode() throws InvalidUnicodeCharacterException {
      assertThat(EscapeUtils.translateUnicode("\\u00fc"))
        .isEqualTo('ü').toString();
      assertThat(EscapeUtils.translateUnicode("\\u1234"))
        .isEqualTo('ሴ').toString();
      assertThat(EscapeUtils.translateUnicode("\\u7de8"))
        .isEqualTo('編').toString();
      assertThat(EscapeUtils.translateUnicode("\\u042f"))
        .isEqualTo('Я').toString();
      assertThat(EscapeUtils.translateUnicode("\\u0061"))
        .isEqualTo('a').toString();
  }


  @Test
  public void testRemoveLeadingWhitespace() {
    assertThat(EscapeUtils.removeLeadingWhitespace("   first line \n"
        + "  \tsecond line \r\n"
        + " \t\f third line").toString())
      .isEqualTo("first line \n"
        + "second line \r\n"
        + "third line");
  }


  @Test
  public void testUnescape_LiteralNewlineToRealNewline() {
    assertThat(EscapeUtils.unescape("first line \\n"
        + "second line").toString())
      .isEqualTo("first line \n"
        + "second line");
    assertThat(EscapeUtils.unescape("first line \\r"
        + "second line").toString())
      .isEqualTo("first line \r"
        + "second line");
    assertThat(EscapeUtils.unescape("first line \\r\\n"
        + "second line").toString())
      .isEqualTo("first line \r\n"
        + "second line");
  }


  @Test
  public void testUnescape_SilentlyDropUnnecessaryBackslashes() {
    assertThat(EscapeUtils.unescape("\\a\\b\\c\\d\\e").toString())
      .isEqualTo("abcde");
  }


  @Test
  public void testUnescape_RemoveNewlines() {
    assertThat(EscapeUtils.unescape("first line \n"
      + "second line").toString())
      .isEqualTo("first line second line");
    assertThat(EscapeUtils.unescape("first line \r"
      + "second line").toString())
      .isEqualTo("first line second line");
    assertThat(EscapeUtils.unescape("first line \r\n"
      + "second line").toString())
      .isEqualTo("first line second line");
  }

  @Test
  public void testUnescape_RemoveNewlinesAndLeadingWhitespace() {
    assertThat(EscapeUtils.unescape("first line \n"
      + "\tsecond line").toString())
      .isEqualTo("first line second line");
  }


  @Test
  public void testUnescapePropertyValue_UnescapeUnicodeDifferentLengths() {
    assertThat(EscapeUtils.unescape("Nudel\\u123456").toString())
      .isEqualTo("Nudelሴ56");
    assertThat(EscapeUtils.unescape("Nudel\\u12345").toString())
      .isEqualTo("Nudelሴ5");
    assertThat(EscapeUtils.unescape("Nudel\\u1234").toString())
      .isEqualTo("Nudelሴ");
    assertThat(EscapeUtils.unescape("Nudel\\u123").toString())
      .isEqualTo("Nudel\\u123");
    assertThat(EscapeUtils.unescape("Nudel\\u12").toString())
      .isEqualTo("Nudel\\u12");
    assertThat(EscapeUtils.unescape("Nudel\\u1").toString())
      .isEqualTo("Nudel\\u1");
    assertThat(EscapeUtils.unescape("Nudel\\u").toString())
      .isEqualTo("Nudel\\u");
  }


  @Test
  public void testEscapePropertyKey() {
    assertThat(EscapeUtils.escapePropertyKey("key with whitespace").toString())
      .isEqualTo("key\\ with\\ whitespace");

    assertThat(EscapeUtils.escapePropertyKey("key with \nnewline").toString())
      .isEqualTo("key\\ with\\ \\\nnewline");

    assertThat(EscapeUtils.escapePropertyKey("key with \rnewline").toString())
      .isEqualTo("key\\ with\\ \\\rnewline");

    assertThat(EscapeUtils.escapePropertyKey("key with \r\nnewline").toString())
      .isEqualTo("key\\ with\\ \\\r\nnewline");

    assertThat(EscapeUtils.escapePropertyKey("#key with commentchar").toString())
      .isEqualTo("\\#key\\ with\\ commentchar");

    assertThat(EscapeUtils.escapePropertyKey("key with #commentchar inside").toString())
      .isEqualTo("key\\ with\\ \\#commentchar\\ inside");

    assertThat(EscapeUtils.escapePropertyKey("key with :=").toString())
      .isEqualTo("key\\ with\\ \\:\\=");
  }


  @Test
  public void testEscapePropertyValue() {
    assertThat(EscapeUtils.escapePropertyValue("value with whitespace").toString())
      .isEqualTo("value with whitespace");

    assertThat(EscapeUtils.escapePropertyValue("value with \nnewline").toString())
      .isEqualTo("value with \\nnewline");

    assertThat(EscapeUtils.escapePropertyValue("value with \rnewline").toString())
      .isEqualTo("value with \\rnewline");

    assertThat(EscapeUtils.escapePropertyValue("value with \r\nnewline").toString())
      .isEqualTo("value with \\r\\nnewline");

    assertThat(EscapeUtils.escapePropertyValue("#value with commentchar").toString())
      .isEqualTo("#value with commentchar");

    assertThat(EscapeUtils.escapePropertyValue("value with #commentchar inside").toString())
      .isEqualTo("value with #commentchar inside");

    assertThat(EscapeUtils.escapePropertyValue("value with :=").toString())
      .isEqualTo("value with :=");
  }


  /**
   * This test checks bug #5: https://github.com/hupfdule/apron/issues/5
   */
  @Test
  public void testEscape_Backslashes() {
    assertThat(EscapeUtils.escapePropertyKey("my\\key").toString())
      .isEqualTo("my\\\\key");
    assertThat(EscapeUtils.escapePropertyKey("my\\value").toString())
      .isEqualTo("my\\\\value");
  }


  @Test
  public void testComment() {
    assertThat(EscapeUtils.comment(""
      + "my key = my value \\\n"
      + "   over \\\r"
      + "   multiple \\\r\n"
      + "   lines").toString())
      .isEqualTo(""
      + "#my key = my value \\\n"
      + "#   over \\\r"
      + "#   multiple \\\r\n"
      + "#   lines");

    assertThat(EscapeUtils.comment(""
      + "my key = my value \n"
      + "   over \r"
      + "   multiple \r\n"
      + "   lines").toString())
      .isEqualTo(""
      + "#my key = my value \n"
      + "#   over \r"
      + "#   multiple \r\n"
      + "#   lines");

    assertThat(EscapeUtils.comment(""
      + "my key = my value \n"
      + "   over \r"
      + "   multiple \r\n"
      + "   lines\n").toString())
      .isEqualTo(""
      + "#my key = my value \n"
      + "#   over \r"
      + "#   multiple \r\n"
      + "#   lines\n");
  }


  @Test
  public void testUnescape_withUnicodeValuesAndRorN() {
      assertThat(EscapeUtils.unescape("Die Kommunikation ist gest\\u00f6rt.").toString())
        .isEqualTo("Die Kommunikation ist gestört.");
      assertThat(EscapeUtils.unescape("\\u00c4ndern").toString())
        .isEqualTo("Ändern");
  }
}
