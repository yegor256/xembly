/*
 * SPDX-FileCopyrightText: Copyright (c) 2013-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package org.xembly;

import com.jcabi.matchers.XhtmlMatchers;
import javax.xml.parsers.DocumentBuilderFactory;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

/**
 * Test case for {@link UpDirective}.
 *
 * @since 0.1
 */
final class UpDirectiveTest {

    @Test
    void jumpsToParentsWhenTheyExist() throws Exception {
        final Iterable<Directive> dirs = new Directives(
            "ADD 'root'; ADD 'foo'; ADD 'bar'; UP; UP; STRICT '1';"
        );
        final Document dom = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder().newDocument();
        new Xembler(dirs).apply(dom);
        MatcherAssert.assertThat(
            "Can't jump to parents when they exist",
            XhtmlMatchers.xhtml(dom),
            XhtmlMatchers.hasXPath("/root/foo/bar")
        );
    }

    @Test
    void throwsWhenNoParents() {
        Assertions.assertThrows(
            ImpossibleModificationException.class,
            () -> {
                final Iterable<Directive> dirs = new Directives(
                    "ADD 'foo'; UP; UP; UP;"
                );
                final Document dom = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder().newDocument();
                dom.appendChild(dom.createElement("boom"));
                new Xembler(dirs).apply(dom);
            },
            "Can't jump to parents when they don't exist"
        );
    }

}
