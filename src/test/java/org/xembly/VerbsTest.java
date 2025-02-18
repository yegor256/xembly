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
 * Test case for {@link Verbs}.
 *
 * @since 0.29
 */
final class VerbsTest {

    @Test
    void throwsOnBrokenSyntax() {
        final Verbs verbs = new Verbs("ADD 't';\nADD 'x';broken");
        MatcherAssert.assertThat(
            "Can't throw on broken syntax",
            Assertions.assertThrows(
                SyntaxException.class,
                verbs::directives
            ).getMessage(),
            Matchers.containsString("near [broken;]")
        );
    }

    @Test
    void worksWithSpacesAfterLastCommand() {
        Assertions.assertDoesNotThrow(
            () -> new Xembler(
                new Directives("ADD 'o'; ATTR 'base', 'int';    ")
            ).xml(),
            "Can't work with spaces after last command"
        );
    }

    @Test
    void worksWithNewLines() {
        Assertions.assertDoesNotThrow(
            () -> new Xembler(
                new Directives("\n\nADD 'o';\nATTR 'base','int';\n\n")
            ).xml(),
            "Can't work with new lines"
        );
    }
}
