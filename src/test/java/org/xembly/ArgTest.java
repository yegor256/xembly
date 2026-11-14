/*
 * SPDX-FileCopyrightText: Copyright (c) 2013-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package org.xembly;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link Arg}.
 *
 * @since 0.1
 */
final class ArgTest {

    @Test
    void escapesAndUnescaped() throws Exception {
        final String[] texts = {
            "",
            "123",
            "test € привет & <>'\"\\",
            "😀 🎉 привет",
            String.format("how are you there,\t%c%cтоварищ? &#0D;", '\n', '\r'),
        };
        for (final String text : texts) {
            MatcherAssert.assertThat(
                "Can't escape",
                Arg.unescape(new Arg(text).toString()),
                Matchers.equalTo(text)
            );
        }
    }

    @Test
    void rejectsToEscapeInvalidXmlChars() {
        Assertions.assertThrows(
            XmlContentException.class,
            () -> new Arg("\033\0").toString(),
            "Invalid XML content"
        );
    }

    @Test
    void rejectsToUnEscapeInvalidXmlChars() {
        Assertions.assertThrows(
            XmlContentException.class,
            () -> Arg.unescape("&#27;&#0000;"),
            "Invalid XML content"
        );
    }

    @Test
    void referencesLatestXmlSpecificationInErrorMessage() {
        MatcherAssert.assertThat(
            "Error message doesnt reference the latest XML 1.1 specification",
            Assertions.assertThrows(
                XmlContentException.class,
                () -> new Arg("\007").toString()
            ).getMessage(),
            Matchers.containsString("https://www.w3.org/TR/xml11/#charsets")
        );
    }

    @Test
    void unescapesSupplementaryCharacterReferences() throws Exception {
        MatcherAssert.assertThat(
            "Supplementary characters must survive numeric references",
            Arg.unescape("'&#128512;'"),
            Matchers.equalTo("😀")
        );
    }

    @Test
    void rejectsNumericReferencesAboveUnicodeRange() {
        Assertions.assertThrows(
            XmlContentException.class,
            () -> Arg.unescape("'&#1114112;'"),
            "Values above the Unicode range are not valid XML characters"
        );
        Assertions.assertThrows(
            XmlContentException.class,
            () -> Arg.unescape("'&#1114144;'"),
            "Values above the Unicode range are not valid XML characters"
        );
    }

    @Test
    void rejectsNegativeNumericReferences() {
        Assertions.assertThrows(
            XmlContentException.class,
            () -> Arg.unescape("'&#-1;'"),
            "Negative values are not valid XML characters"
        );
    }

    @Test
    void rejectsMalformedNumericReferences() {
        Assertions.assertThrows(
            XmlContentException.class,
            () -> Arg.unescape("'&#99999999999999;'"),
            "Numbers that don't fit into int are not valid XML characters"
        );
    }

    @Test
    void rejectsEmptySymbolReferences() {
        Assertions.assertThrows(
            XmlContentException.class,
            () -> Arg.unescape("'x&;y'"),
            "Empty XML symbols must be reported as content errors"
        );
    }

    @Test
    void rejectsEmptyNumericReferences() {
        Assertions.assertThrows(
            XmlContentException.class,
            () -> Arg.unescape("'&#;'"),
            "Empty numbers must be reported as content errors"
        );
    }

    @Test
    void unescapesHexCharacterReferences() throws Exception {
        MatcherAssert.assertThat(
            "Hex references must decode by base 16",
            Arg.unescape("'&#x41;'"),
            Matchers.equalTo("A")
        );
    }

    @Test
    void unescapesUppercaseHexPrefixes() throws Exception {
        MatcherAssert.assertThat(
            "Uppercase X prefix must decode too",
            Arg.unescape("'&#X41;'"),
            Matchers.equalTo("A")
        );
    }

    @Test
    void unescapesHexLineFeedReferences() throws Exception {
        MatcherAssert.assertThat(
            "Hex line feed reference must decode",
            Arg.unescape("'&#xA;'"),
            Matchers.equalTo(String.valueOf('\n'))
        );
    }

    @Test
    void unescapesSupplementaryHexReferences() throws Exception {
        MatcherAssert.assertThat(
            "Supplementary hex references must survive",
            Arg.unescape("'&#x1F600;'"),
            Matchers.equalTo("😀")
        );
    }

    @Test
    void rejectsMalformedHexCharacterReferences() {
        Assertions.assertThrows(
            XmlContentException.class,
            () -> Arg.unescape("'&#xZZ;'"),
            "Non-hex digits are not valid XML characters"
        );
        Assertions.assertThrows(
            XmlContentException.class,
            () -> Arg.unescape("'&#x;'"),
            "Empty hex digits are not valid XML characters"
        );
    }

    @Test
    void rejectsHexReferencesAboveUnicodeRange() {
        Assertions.assertThrows(
            XmlContentException.class,
            () -> Arg.unescape("'&#x110000;'"),
            "Hex values above the Unicode range are not valid XML characters"
        );
        Assertions.assertThrows(
            XmlContentException.class,
            () -> Arg.unescape("'&#xFFFFFFFF;'"),
            "Hex values that don't fit into int are not valid XML characters"
        );
    }
}
