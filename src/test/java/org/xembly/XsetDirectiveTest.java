/*
 * SPDX-FileCopyrightText: Copyright (c) 2013-2025 Yegor Bugayenko <yegor256@gmail.com>
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
 * Test case for {@link XsetDirective}.
 *
 * @since 0.1
 */
final class XsetDirectiveTest {

    @Test
    void setsTextContentOfNodes() throws Exception {
        final Iterable<Directive> dirs = new Directives(
            StringUtils.join(
                "ADD 'root'; ADD 'foo';",
                "SET '&quot;Bonnie &amp; Clyde&quot;';",
                "UP; ADD 'length'; XSET 'string-length(/root/foo)';",
                "XSET '. + 10';"
            )
        );
        final Document dom = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder().newDocument();
        new Xembler(dirs).apply(dom);
        MatcherAssert.assertThat(
            "Can't set text content of nodes",
            XhtmlMatchers.xhtml(dom),
            XhtmlMatchers.hasXPaths(
                "/root/foo[.='\"Bonnie & Clyde\"']",
                "/root/length[.='26']"
            )
        );
    }

    @Test
    void setsTextDirectlyIntoDomNodes() throws Exception {
        final Document dom = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder().newDocument();
        final Element root = dom.createElement("xxx");
        final Element first = dom.createElement("first");
        first.setTextContent("15");
        root.appendChild(first);
        final Element second = dom.createElement("second");
        second.setTextContent("13");
        root.appendChild(second);
        dom.appendChild(root);
        new XsetDirective("sum(/xxx/*/text()) + 5").exec(
            dom, new DomCursor(Collections.singletonList(first)),
            new DomStack()
        );
        MatcherAssert.assertThat(
            "Can't set text directly into DOM nodes",
            XhtmlMatchers.xhtml(dom),
            XhtmlMatchers.hasXPaths("/xxx[first='33' and second='13']")
        );
    }

}
