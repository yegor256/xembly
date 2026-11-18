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
 * Test case for {@link Verbs}.
 *
 * @since 0.29
 */
final class VerbsTest {

    @Test
    void throwsOnBrokenSyntax() {
        Assertions.assertThrows(
            SyntaxException.class,
            () -> new Verbs(
                String.format("ADD 't';%nADD 'x';broken")
            ).directives(),
            "Can't throw on broken syntax"
        );
    }

    @Test
    void throwsOnMalformedStrictNumber() {
        Assertions.assertThrows(
            SyntaxException.class,
            () -> new Verbs("STRICT '1x';").directives(),
            "Can't throw on malformed number in STRICT"
        );
    }

    @Test
    void throwsOnStrictNumberOverflow() {
        Assertions.assertThrows(
            SyntaxException.class,
            () -> new Verbs("STRICT '99999999999';").directives(),
            "Can't throw on number overflow in STRICT"
        );
    }

    @Test
    void throwsOnStrictNumberWithLeadingSpace() {
        Assertions.assertThrows(
            SyntaxException.class,
            () -> new Verbs("STRICT ' 1';").directives(),
            "Can't throw on number with leading space in STRICT"
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
                    String.format("%n%nADD 'o';%nATTR 'base','int';%n%n")
                )
            ).xml(),
            "Can't work with new lines"
        );
    }

    @Test
    void parsesSingleQuotedArgumentBeginningWithSemicolon() {
        MatcherAssert.assertThat(
            "Single-quoted argument beginning with a semicolon must not be split",
            new Verbs("ADD 'root'; SET ';keep';").directives().toString(),
            Matchers.equalTo("[ADD \"root\", SET \";keep\"]")
        );
    }

    @Test
    void parsesDoubleQuotedArgumentBeginningWithSemicolon() {
        MatcherAssert.assertThat(
            "Double-quoted argument beginning with a semicolon must not be split",
            new Verbs("ADD 'root'; SET \";keep\";").directives().toString(),
            Matchers.equalTo("[ADD \"root\", SET \";keep\"]")
        );
    }
}
