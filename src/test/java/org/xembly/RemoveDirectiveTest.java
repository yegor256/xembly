/*
 * SPDX-FileCopyrightText: Copyright (c) 2013-2026 Yegor Bugayenko
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
        final Document dom = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder().newDocument();
        new Xembler(
            new Directives(
                StringUtils.join(
                    "ADD 'root'; ADD 'foo'; ADD 'bar'; ADD 'boom';",
                    "XPATH '/*/foo//*'; REMOVE; ADD 'x';"
                )
            )
        ).apply(dom);
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
        root.appendChild(dom.createElement("b"));
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
