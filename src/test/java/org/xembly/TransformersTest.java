/*
 * SPDX-FileCopyrightText: Copyright (c) 2013-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package org.xembly;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link Transformers}.
 *
 * @since 0.32.3
 */
final class TransformersTest {

    @Test
    void prettyPrintsByDefault() throws Exception {
        MatcherAssert.assertThat(
            "Default Xembler must produce indented XML",
            new Xembler(
                new Directives().add("a").add("b").set("hello")
            ).xml(),
            Matchers.containsString("<a>\n")
        );
    }

    @Test
    void disablesPrettyPrintWithCompact() throws Exception {
        MatcherAssert.assertThat(
            "Compact transformer must produce non-indented XML",
            new Xembler(
                new Directives().add("a").add("b").set("hello"),
                new Transformers.Compact()
            ).xml(),
            Matchers.allOf(
                Matchers.containsString("<a><b>hello</b></a>"),
                Matchers.not(Matchers.containsString("\n   ")),
                Matchers.not(Matchers.containsString("\n    "))
            )
        );
    }

    @Test
    void rendersDeclarationInCompactMode() throws Exception {
        MatcherAssert.assertThat(
            "Compact transformer must keep XML declaration",
            new Xembler(
                new Directives().add("root"),
                new Transformers.Compact()
            ).xml(),
            Matchers.startsWith("<?xml version=\"1.0\"")
        );
    }

    @Test
    void omitsDeclarationWithNode() throws Exception {
        MatcherAssert.assertThat(
            "Node transformer must omit XML declaration",
            new Xembler(
                new Directives().add("root"),
                new Transformers.Node()
            ).xml(),
            Matchers.not(Matchers.containsString("<?xml"))
        );
    }
}
