/*
 * SPDX-FileCopyrightText: Copyright (c) 2013-2025 Yegor Bugayenko <yegor256@gmail.com>
 * SPDX-License-Identifier: MIT
 */
package org.xembly;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link SyntaxException}.
 *
 * @since 1.0
 */
final class SyntaxExceptionTest {

    @Test
    void instantiatesException() {
        MatcherAssert.assertThat(
            "Can't instantiate exception",
            new SyntaxException("", new IllegalStateException("")),
            Matchers.notNullValue()
        );
    }

}
