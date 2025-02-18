/*
 * SPDX-FileCopyrightText: 2013-2025 Yegor Bugayenko <yegor256@gmail.com>
 * SPDX-License-Identifier: MIT
 */
package org.xembly;

import com.jcabi.matchers.XhtmlMatchers;
import javax.xml.parsers.DocumentBuilderFactory;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

/**
 * Test case for {@link AddDirective}.
 *
 * @since 0.1
 */
final class AddDirectiveTest {

    @Test
    void addsNodesToCurrentNodes() throws Exception {
        final Iterable<Directive> dirs = new Directives(
            "ADD 'root'; ADD 'foo'; ADD 'x'; UP; UP; ADD 'bar'; UP; ADD 'bar';"
        );
        final Document dom = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder().newDocument();
        new Xembler(dirs).apply(dom);
        MatcherAssert.assertThat(
            "fails to add nodes to current nodes",
            XhtmlMatchers.xhtml(dom),
            XhtmlMatchers.hasXPaths(
                "/root/foo/x",
                "/root[count(bar) = 2]"
            )
        );
    }

}
