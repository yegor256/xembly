/*
 * SPDX-FileCopyrightText: Copyright (c) 2013-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package org.xembly;

import com.jcabi.matchers.XhtmlMatchers;
import java.util.Collections;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Test case for {@link XpathDirective}.
 *
 * @since 0.1
 */
final class XpathDirectiveTest {

    @Test
    void findsNodesWithXpathExpression() throws Exception {
        final Iterable<Directive> dirs = new Directives(
            StringUtils.join(
                "ADD 'root'; ADD 'foo'; ATTR 'bar', '1'; UP; ADD 'bar';",
                "XPATH '//*[@bar=1]'; ADD 'test';"
            )
        );
        final Document dom = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder().newDocument();
        new Xembler(dirs).apply(dom);
        MatcherAssert.assertThat(
            "Can't find nodes with XPath expression",
            XhtmlMatchers.xhtml(dom),
            XhtmlMatchers.hasXPaths(
                "/root/foo[@bar=1]/test",
                "/root/bar"
            )
        );
    }

    @Test
    void ignoresEmptySearches() throws Exception {
        final Iterable<Directive> dirs = new Directives(
            "XPATH '/nothing'; XPATH '/top'; STRICT '1'; ADD 'hey';"
        );
        final Document dom = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder().newDocument();
        dom.appendChild(dom.createElement("top"));
        new Xembler(dirs).apply(dom);
        MatcherAssert.assertThat(
            "Can't ignore empty searches",
            XhtmlMatchers.xhtml(dom),
            XhtmlMatchers.hasXPath("/top/hey")
        );
    }

    @Test
    void findsNodesByXpathDirectly() throws Exception {
        final Document dom = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder().newDocument();
        final Element root = dom.createElement("xxx");
        final Element first = dom.createElement("a");
        root.appendChild(first);
        final Element second = dom.createElement("b");
        root.appendChild(second);
        dom.appendChild(root);
        MatcherAssert.assertThat(
            "Can't find nodes by XPath directly",
            new XpathDirective("/*").exec(
                dom,
                new DomCursor(Collections.singletonList(first)),
                new DomStack()
            ),
            Matchers.hasItem(root)
        );
    }

    @Test
    void findsNodesInEmptyDom() throws Exception {
        final Document dom = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder().newDocument();
        MatcherAssert.assertThat(
            "Can't find nodes in an empty DOM",
            new XpathDirective("/some-root").exec(
                dom,
                new DomCursor(Collections.emptyList()),
                new DomStack()
            ),
            Matchers.emptyIterable()
        );
    }

    @Test
    void findsRootInClonedNode() throws Exception {
        final Iterable<Directive> dirs = new Directives(
            "XPATH '/*'; STRICT '1'; ADD 'boom-5';"
        );
        final Document dom = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder().newDocument();
        dom.appendChild(dom.createElement("high"));
        final Node clone = dom.cloneNode(true);
        new Xembler(dirs).apply(clone);
        MatcherAssert.assertThat(
            "Can't find root in a cloned node",
            XhtmlMatchers.xhtml(clone),
            XhtmlMatchers.hasXPath("/high/boom-5")
        );
    }

}
