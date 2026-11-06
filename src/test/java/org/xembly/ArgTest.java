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
}
