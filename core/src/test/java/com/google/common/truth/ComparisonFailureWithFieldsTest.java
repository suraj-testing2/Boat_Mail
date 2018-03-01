/*
 * Copyright (c) 2018 Google, Inc.
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

package com.google.common.truth;

import static com.google.common.base.Strings.repeat;
import static com.google.common.truth.ComparisonFailureWithFields.formatExpectedAndActual;
import static com.google.common.truth.Truth.assertThat;

import com.google.common.annotations.GwtIncompatible;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Test for {@link ComparisonFailureWithFields}. */
@RunWith(JUnit4.class)
public class ComparisonFailureWithFieldsTest {
  @Test
  public void formatAllDifferent() {
    runFormatTest(
        "foo", "bar",
        "foo", "bar");
  }

  @Test
  public void formatShortOverlap() {
    runFormatTest(
        "bar", "baz",
        "bar", "baz");
  }

  @Test
  public void formatLongOverlapStart() {
    runFormatTest(
        repeat("b", 100) + "aa",
        repeat("b", 100) + "oo",
        "…" + repeat("b", 20) + "aa",
        "…" + repeat("b", 20) + "oo");
  }

  @Test
  public void formatLongOverlapEnd() {
    runFormatTest(
        "ba" + repeat("r", 100),
        "fu" + repeat("r", 100),
        "ba" + repeat("r", 20) + "…",
        "fu" + repeat("r", 20) + "…");
  }

  @Test
  public void formatLongOverlapStartAlsoSmallAtEnd() {
    runFormatTest(
        repeat("b", 100) + "aa" + repeat("t", 7),
        repeat("b", 100) + "oo" + repeat("t", 7),
        "…" + repeat("b", 20) + "aattttttt",
        "…" + repeat("b", 20) + "oottttttt");
  }

  @Test
  public void formatLongOverlapEndAlsoSmallAtStart() {
    runFormatTest(
        repeat("a", 7) + "ba" + repeat("r", 100),
        repeat("a", 7) + "fu" + repeat("r", 100),
        "aaaaaaaba" + repeat("r", 20) + "…",
        "aaaaaaafu" + repeat("r", 20) + "…");
  }

  @Test
  public void formatLongOverlapBoth() {
    runFormatTest(
        repeat("r", 60) + "a" + repeat("g", 60),
        repeat("r", 60) + "u" + repeat("g", 60),
        "…" + repeat("r", 20) + "a" + repeat("g", 20) + "…",
        "…" + repeat("r", 20) + "u" + repeat("g", 20) + "…");
  }

  @Test
  public void formatLongOverlapBothDifferentLength() {
    runFormatTest(
        repeat("r", 60) + "aaaaa" + repeat("g", 60),
        repeat("r", 60) + "u" + repeat("g", 60),
        "…" + repeat("r", 20) + "aaaaa" + repeat("g", 20) + "…",
        "…" + repeat("r", 20) + "u" + repeat("g", 20) + "…");
  }

  @Test
  public void prefixAndSuffixWouldOverlapSimple() {
    runFormatTest(
        repeat("a", 40) + "lmnopqrstuv" + repeat("a", 40),
        repeat("a", 40) + "lmnopqrstuvlmnopqrstuv" + repeat("a", 40),
        "…aaaaaaaaalmnopqrstuvaaaaaaaaa…",
        "…aaaaaaaaalmnopqrstuvlmnopqrstuvaaaaaaaaa…");
  }

  @Test
  public void prefixAndSuffixWouldOverlapAllSame() {
    runFormatTest(repeat("a", 100), repeat("a", 102), "…" + repeat("a", 20), "…" + repeat("a", 22));
  }

  @Test
  public void formatNoSplitSurrogateStart() {
    runFormatTest(
        repeat("b", 100) + "\uD8AB\uDCAB" + repeat("b", 19) + "aa",
        repeat("b", 100) + "\uD8AB\uDCAB" + repeat("b", 19) + "oo",
        "…\uD8AB\uDCAB" + repeat("b", 19) + "aa",
        "…\uD8AB\uDCAB" + repeat("b", 19) + "oo");
  }

  @Test
  public void formatNoSplitSurrogateEnd() {
    runFormatTest(
        "ba" + repeat("r", 19) + "\uD8AB\uDCAB" + repeat("r", 100),
        "fu" + repeat("r", 19) + "\uD8AB\uDCAB" + repeat("r", 100),
        "ba" + repeat("r", 19) + "\uD8AB\uDCAB…",
        "fu" + repeat("r", 19) + "\uD8AB\uDCAB…");
  }

  @GwtIncompatible
  @Test
  public void formatDiffOmitStart() {
    runFormatTest(
        repeat("a\n", 100) + "b",
        repeat("a\n", 100) + "c",
        Joiner.on('\n').join(" ⋮", " a", " a", " a", "-b", "+c"));
  }

  @GwtIncompatible
  @Test
  public void formatDiffOmitEnd() {
    runFormatTest(
        "a" + repeat("\nz", 100),
        "b" + repeat("\nz", 100),
        Joiner.on('\n').join("-a", "+b", " z", " z", " z", " ⋮"));
  }

  @GwtIncompatible
  @Test
  public void formatDiffOmitBoth() {
    runFormatTest(
        repeat("a\n", 100) + "m" + repeat("\nz", 100),
        repeat("a\n", 100) + "n" + repeat("\nz", 100),
        Joiner.on('\n').join(" ⋮", " a", " a", " a", "-m", "+n", " z", " z", " z", " ⋮"));
  }

  @GwtIncompatible
  @Test
  public void formatDiffOmitBothMultipleDifferingLines() {
    runFormatTest(
        repeat("a\n", 100) + "m\nn\no\np" + repeat("\nz", 100),
        repeat("a\n", 100) + "q\nr\ns\nt" + repeat("\nz", 100),
        Joiner.on('\n')
            .join(
                " ⋮", " a", " a", " a", "-m", "-n", "-o", "-p", "+q", "+r", "+s", "+t", " z", " z",
                " z", " ⋮"));
  }

  @GwtIncompatible
  @Test
  public void formatDiffOmitBothMultipleDifferingLinesDifferentLength() {
    runFormatTest(
        repeat("a\n", 100) + "m\nn\no\np" + repeat("\nz", 100),
        repeat("a\n", 100) + "q\nr\ns\nt\nu\nv" + repeat("\nz", 100),
        Joiner.on('\n')
            .join(
                " ⋮", " a", " a", " a", "-m", "-n", "-o", "-p", "+q", "+r", "+s", "+t", "+u", "+v",
                " z", " z", " z", " ⋮"));
  }

  @GwtIncompatible
  @Test
  public void formatDiffPrefixAndSuffixWouldOverlapSimple() {
    runFormatTest(
        repeat("a\n", 40) + "l\nm\nn\no\np\n" + repeat("a\n", 40),
        repeat("a\n", 40) + "l\nm\nn\no\np\nl\nm\nn\no\np\n" + repeat("a\n", 40),
        Joiner.on('\n')
            .join(" ⋮", " n", " o", " p", "+l", "+m", "+n", "+o", "+p", " a", " a", " a", " ⋮"));
  }

  @GwtIncompatible
  @Test
  public void formatDiffPrefixAndSuffixWouldOverlapAllSame() {
    runFormatTest(
        repeat("a\n", 80),
        repeat("a\n", 82),
        Joiner.on('\n').join(" ⋮", " a", " a", " a", "-", "+a", "+a", "+"));
  }

  private static void runFormatTest(
      String expected, String actual, String expectedExpected, String expectedActual) {
    ImmutableList<Field> fields = formatExpectedAndActual(expected, actual);
    assertThat(fields).hasSize(2);
    assertThat(fields.get(0).key).isEqualTo("expected");
    assertThat(fields.get(1).key).isEqualTo("but was");
    assertThat(fields.get(0).value).isEqualTo(expectedExpected);
    assertThat(fields.get(1).value).isEqualTo(expectedActual);
  }

  @GwtIncompatible
  private static void runFormatTest(String expected, String actual, String expectedDiff) {
    ImmutableList<Field> fields = formatExpectedAndActual(expected, actual);
    assertThat(fields).hasSize(1);
    assertThat(fields.get(0).key).isEqualTo("diff");
    assertThat(fields.get(0).value).isEqualTo(expectedDiff);
  }
}