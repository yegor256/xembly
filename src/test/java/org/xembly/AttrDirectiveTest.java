/*
 * SPDX-FileCopyrightText: Copyright (c) 2013-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package org.xembly;

import com.jcabi.matchers.XhtmlMatchers;
import com.jcabi.xml.XMLDocument;
import java.util.Collections;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Test case for {@link AttrDirective}.
 *
 * @since 0.1
 */
final class AttrDirectiveTest {

    @Test
    void addsAttributesToCurrentNodes() throws Exception {
        final Document dom = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder().newDocument();
        new Xembler(
            new Directives(
                StringUtils.join(
                    "ADD 'root'; ADD 'foo'; UP; ADD 'foo';",
                    "XPATH '//*'; ATTR 'bar', 'привет';"
                )
            )
        ).apply(dom);
        MatcherAssert.assertThat(
            "fails to add attributes to current nodes",
            XhtmlMatchers.xhtml(dom),
            XhtmlMatchers.hasXPaths(
                "/root[count(foo) = 2]",
                "/root[count(foo[@bar='привет']) = 2]"
            )
        );
    }

    @Test
    void addsDomAttributesDirectly() throws Exception {
        final Document dom = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder().newDocument();
        final Element root = dom.createElement("xxx");
        root.appendChild(dom.createElement("a"));
        final Element second = dom.createElement("b");
        root.appendChild(second);
        dom.appendChild(root);
        new AttrDirective("x", "y").exec(
            dom, new DomCursor(Collections.singletonList(second)),
            new DomStack()
        );
        MatcherAssert.assertThat(
            "fails to add attributes to current nodes",
            XhtmlMatchers.xhtml(dom),
            XhtmlMatchers.hasXPath("/xxx/b[@x='y']")
        );
    }

    @Test
    void addsCaseSensitiveAttributesDirectly() throws Exception {
        final Document dom = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder().newDocument();
        final Element root = dom.createElement("f");
        dom.appendChild(root);
        new AttrDirective("Price", "\u20ac50").exec(
            dom, new DomCursor(Collections.singletonList(root)),
            new DomStack()
        );
        MatcherAssert.assertThat(
            "fails to add case-sensitive attributes to current nodes",
            XhtmlMatchers.xhtml(dom),
            XhtmlMatchers.hasXPath("/f[@Price='\u20ac50']")
        );
    }

    @Test
    void addAttributeWithNamespace() {
        MatcherAssert.assertThat(
            "fails to add attribute with namespace",
            new XMLDocument(
                new Xembler(
                    new Directives().add("boom").attr(
                        "noNamespaceSchemaLocation xsi http://www.w3.org/2001/XMLSchema-instance",
                        "foo.xsd"
                    )
                ).domQuietly()
            ).toString(),
            Matchers.containsString("xsi:noNamespaceSchemaLocation")
        );
    }

    @Test
    void findsAttributeWithNamespaceByXpath() {
        MatcherAssert.assertThat(
            "fails to find attribute with namespace by xpath",
            new XMLDocument(
                new Xembler(
                    new Directives().add("boom").attr(
                        "noNamespaceSchemaLocation xsi http://www.w3.org/2001/XMLSchema-instance",
                        "foo.xsd"
                    )
                ).domQuietly()
            ).nodes("/boom/@xsi:noNamespaceSchemaLocation"),
            Matchers.not(Matchers.emptyIterable())
        );
    }
}
