/*
 * SPDX-FileCopyrightText: Copyright (c) 2013-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package org.xembly;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link Verbs}.
 * @since 0.29
 */
final class VerbsTest {

    @Test
    void throwsOnBrokenSyntax() {
        Assertions.assertThrows(
            SyntaxException.class,
            () -> new Verbs(
                "ADD 't';" + System.lineSeparator() + "ADD 'x';broken"
            ).directives(),
            "Can't throw on broken syntax"
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
                new Directives(
                    System.lineSeparator() + System.lineSeparator()
                        + "ADD 'o';" + System.lineSeparator()
                        + "ATTR 'base','int';" + System.lineSeparator()
                        + System.lineSeparator()
                )
            ).xml(),
            "Can't work with new lines"
        );
    }
}
