/*
 * SPDX-FileCopyrightText: Copyright (c) 2013-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package org.xembly;

import com.jcabi.matchers.XhtmlMatchers;
import java.util.Collections;
import javax.xml.parsers.DocumentBuilderFactory;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Test case for {@link AddIfDirective}.
 *
 * @since 0.1
 */
final class AddIfDirectiveTest {

    @Test
    void addsNodesToCurrentNodes() throws Exception {
        final Iterable<Directive> dirs = new Directives(
            "ADD 'root'; ADD 'foo'; UP; ADDIF 'друг'; UP; ADDIF 'друг';"
        );
        final Document dom = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder().newDocument();
        new Xembler(dirs).apply(dom);
        MatcherAssert.assertThat(
            "fails to add nodes to current nodes",
            XhtmlMatchers.xhtml(dom),
            XhtmlMatchers.hasXPaths(
                "/root/foo",
                "/root[count(друг) = 1]"
            )
        );
    }

    @Test
    void addsDomNodesDirectly() throws Exception {
        final Document dom = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder().newDocument();
        final Element root = dom.createElement("xxx");
        root.appendChild(dom.createElement("a"));
        root.appendChild(dom.createTextNode("привет"));
        root.appendChild(dom.createComment("some comment"));
        root.appendChild(dom.createCDATASection("CDATA"));
        root.appendChild(dom.createProcessingInstruction("a12", "22"));
        dom.appendChild(root);
        new AddIfDirective("b").exec(
            dom, new DomCursor(Collections.singleton(root)),
            new DomStack()
        );
        MatcherAssert.assertThat(
            "fails to add nodes to current nodes",
            XhtmlMatchers.xhtml(dom),
            XhtmlMatchers.hasXPath("/xxx/b")
        );
    }

}
