/*
 * SPDX-FileCopyrightText: Copyright (c) 2013-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package org.xembly;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Test case for {@link DomStack}.
 *
 * @since 0.21
 */
final class DomStackTest {

    @Test
    void addsAndRetrieves() throws Exception {
        final Directive.Stack stack = new DomStack();
        final Directive.Cursor first = Mockito.mock(Directive.Cursor.class);
        final Directive.Cursor second = Mockito.mock(Directive.Cursor.class);
        stack.push(first);
        stack.push(second);
        MatcherAssert.assertThat(
            "Can't retrieve the second element",
            stack.pop(),
            Matchers.equalTo(second)
        );
        MatcherAssert.assertThat(
            "Can't retrieve the first element",
            stack.pop(),
            Matchers.equalTo(first)
        );
    }

    @Test
    void throwsExceptionOnEmpty() {
        Assertions.assertThrows(
            ImpossibleModificationException.class,
            () -> new DomStack().pop(),
            "empty stack"
        );
    }
}
