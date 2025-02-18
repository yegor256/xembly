/*
 * SPDX-FileCopyrightText: Copyright (c) 2013-2025 Yegor Bugayenko <yegor256@gmail.com>
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
            "test \u20ac привет & <>'\"\\",
            "how are you there,\t\n\rтоварищ? &#0D;",
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
            () -> new Arg("\u001b\u0000").toString(),
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

}
