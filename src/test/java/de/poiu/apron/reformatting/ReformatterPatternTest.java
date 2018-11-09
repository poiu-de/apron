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

import java.util.regex.Matcher;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


/**
 *
 * @author mherrn
 */
public class ReformatterPatternTest {

  @Test
  public void testPatternFormatString_BaseFormat() {
    final Matcher matcher= Reformatter.PATTERN_PROPERTY_FORMAT.matcher("<KEY> = <VALUE>\\n");
    assertThat(matcher.matches()).isTrue();
    assertThat(matcher.group("LEADINGWHITESPACE")).isEqualTo("");
    assertThat(matcher.group("SEPARATOR")).isEqualTo(" = ");
    assertThat(matcher.group("LINEENDING")).isEqualTo("\\n");
  }


  @Test
  public void testPatternFormatString_CaseInsensitive() {
    final Matcher matcher= Reformatter.PATTERN_PROPERTY_FORMAT.matcher("<key> = <value>\\n");
    assertThat(matcher.matches()).isTrue();
    assertThat(matcher.group("LEADINGWHITESPACE")).isEqualTo("");
    assertThat(matcher.group("SEPARATOR")).isEqualTo(" = ");
    assertThat(matcher.group("LINEENDING")).isEqualTo("\\n");
  }


  @Test
  public void testPatternFormatString_DifferentWhitespace() {
    final Matcher matcher= Reformatter.PATTERN_PROPERTY_FORMAT.matcher(" \\t\\f<KEY>\\f:\\t<VALUE>\\r\\n");
    assertThat(matcher.matches()).isTrue();
    assertThat(matcher.group("LEADINGWHITESPACE")).isEqualTo(" \\t\\f");
    assertThat(matcher.group("SEPARATOR")).isEqualTo("\\f:\\t");
    assertThat(matcher.group("LINEENDING")).isEqualTo("\\r\\n");
  }


  @Test
  public void testPatternFormatString_WhitespaceAsSeparator() {
    final Matcher matcher= Reformatter.PATTERN_PROPERTY_FORMAT.matcher(" <KEY>\\f \\t<VALUE>\\r");
    assertThat(matcher.matches()).isTrue();
    assertThat(matcher.group("LEADINGWHITESPACE")).isEqualTo(" ");
    assertThat(matcher.group("SEPARATOR")).isEqualTo("\\f \\t");
    assertThat(matcher.group("LINEENDING")).isEqualTo("\\r");
  }


  @Test
  public void testPatternFormatString_MissingSeparator() {
    final Matcher matcher= Reformatter.PATTERN_PROPERTY_FORMAT.matcher(" <KEY><VALUE>\\n");
    assertThat(matcher.matches()).isFalse();
  }


  @Test
  public void testPatternFormatString_MissingLineEnding() {
    final Matcher matcher= Reformatter.PATTERN_PROPERTY_FORMAT.matcher(" <KEY> = <VALUE>");
    assertThat(matcher.matches()).isFalse();
  }


  @Test
  public void testPatternFormatString_InvalidLineEnding() {
    final Matcher matcher= Reformatter.PATTERN_PROPERTY_FORMAT.matcher(" <KEY> = <VALUE>\\n\\r");
    assertThat(matcher.matches()).isFalse();
  }


  @Test
  public void testPatternFormatString_InvalidSeparator() {
    final Matcher matcher= Reformatter.PATTERN_PROPERTY_FORMAT.matcher(" <KEY> == <VALUE>\\n");
    assertThat(matcher.matches()).isFalse();
  }


  @Test
  public void testPatternFormatString_LeadingWhitespace() {
    final Matcher matcher= Reformatter.PATTERN_PROPERTY_FORMAT.matcher("# <KEY> = <VALUE>\\n");
    assertThat(matcher.matches()).isFalse();
  }

}
