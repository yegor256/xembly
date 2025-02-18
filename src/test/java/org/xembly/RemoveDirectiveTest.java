/*
 * SPDX-FileCopyrightText: 2013-2025 Yegor Bugayenko <yegor256@gmail.com>
 * SPDX-License-Identifier: MIT
 */
package org.xembly;

import com.jcabi.matchers.XhtmlMatchers;
import java.util.Collections;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Test case for {@link RemoveDirective}.
 *
 * @since 0.1
 */
final class RemoveDirectiveTest {

    @Test
    void removesCurrentNodes() throws Exception {
        final Iterable<Directive> dirs = new Directives(
            StringUtils.join(
                "ADD 'root'; ADD 'foo'; ADD 'bar'; ADD 'boom';",
                "XPATH '/*/foo//*'; REMOVE; ADD 'x';"
            )
        );
        final Document dom = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder().newDocument();
        new Xembler(dirs).apply(dom);
        MatcherAssert.assertThat(
            "Can't remove current nodes",
            XhtmlMatchers.xhtml(dom),
            XhtmlMatchers.hasXPaths(
                "/root/foo/x",
                "/root/foo[not(bar)]"
            )
        );
    }

    @Test
    void removesDomNodesDirectly() throws Exception {
        final Document dom = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder().newDocument();
        final Element root = dom.createElement("xxx");
        final Element first = dom.createElement("a");
        root.appendChild(first);
        final Element second = dom.createElement("b");
        root.appendChild(second);
        dom.appendChild(root);
        new RemoveDirective().exec(
            dom, new DomCursor(Collections.singletonList(first)),
            new DomStack()
        );
        MatcherAssert.assertThat(
            "Can't remove DOM nodes directly",
            XhtmlMatchers.xhtml(dom),
            XhtmlMatchers.hasXPath("/xxx[b and not(a)]")
        );
    }

}
