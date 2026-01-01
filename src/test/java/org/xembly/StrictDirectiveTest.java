/*
 * SPDX-FileCopyrightText: Copyright (c) 2013-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package org.xembly;

import com.jcabi.matchers.XhtmlMatchers;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

/**
 * Test case for {@link StrictDirective}.
 *
 * @since 0.1
 */
final class StrictDirectiveTest {

    @Test
    void checksNumberOfCurrentNodes() throws Exception {
        final Iterable<Directive> dirs = new Directives(
            StringUtils.join(
                "ADD 'root'; ADD 'foo'; ADD 'bar';",
                "ADD 'boom'; XPATH '//*'; STRICT '4';"
            )
        );
        final Document dom = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder().newDocument();
        new Xembler(dirs).apply(dom);
        MatcherAssert.assertThat(
            "Can't check number of current nodes",
            XhtmlMatchers.xhtml(dom),
            XhtmlMatchers.hasXPaths(
                "/root/foo",
                "/root/foo/bar/boom"
            )
        );
    }

    @Test
    void failsWhenNumberOfCurrentNodesIsTooBig() {
        Assertions.assertThrows(
            ImpossibleModificationException.class,
            () -> {
                final Iterable<Directive> dirs = new Directives(
                    "ADD 'bar'; UP; ADD 'bar'; XPATH '/f/bar'; STRICT '1';"
                );
                final Document dom = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder().newDocument();
                dom.appendChild(dom.createElement("f"));
                new Xembler(dirs).apply(dom);
            },
            "Number of current nodes is not checked"
        );
    }

    @Test
    void failsWhenNumberOfCurrentNodesIsZero() {
        Assertions.assertThrows(
            ImpossibleModificationException.class,
            () -> {
                final Iterable<Directive> dirs = new Directives(
                    "ADD 'foo'; ADD 'x'; XPATH '/foo/absent'; STRICT '1';"
                );
                final Document dom = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder().newDocument();
                dom.appendChild(dom.createElement("z"));
                new Xembler(dirs).apply(dom);
            },
            "Number of current nodes is not checked"
        );
    }

    @Test
    void failsWhenNumberOfCurrentNodesIsTooSmall() {
        Assertions.assertThrows(
            ImpossibleModificationException.class,
            () -> {
                final Iterable<Directive> dirs = new Directives(
                    "ADD 'bar'; STRICT '2';"
                );
                final Document dom = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder().newDocument();
                dom.appendChild(dom.createElement("x"));
                new Xembler(dirs).apply(dom);
            },
            "Number of current nodes is not checked"
        );
    }

}
