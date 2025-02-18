/*
 * SPDX-FileCopyrightText: 2013-2025 Yegor Bugayenko <yegor256@gmail.com>
 * SPDX-License-Identifier: MIT
 */
package org.xembly;

import com.jcabi.matchers.XhtmlMatchers;
import java.util.Arrays;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Test case for {@link SetDirective}.
 *
 * @since 0.1
 */
final class SetDirectiveTest {

    @Test
    void setsTextContentOfNodes() throws Exception {
        final Iterable<Directive> dirs = new Directives(
            StringUtils.join(
                "ADD 'root'; ADD 'foo';",
                "SET '&quot;Bonnie &amp; Clyde&quot;';",
                "UP; ADD 'cops'; SET 'everywhere';"
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
                "/root/cops[.='everywhere']"
            )
        );
    }

    @Test
    void rejectsContentWithInvalidXmlCharacters() {
        Assertions.assertThrows(
            SyntaxException.class,
            () -> new Directives("ADD 'alpha'; SET 'illegal: &#27;&#00;&#03;';"),
            "Invalid XML characters are not detected"
        );
    }

    @Test
    void setsTextDirectlyIntoDomNodes() throws Exception {
        final Document dom = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder().newDocument();
        final Element root = dom.createElement("xxx");
        final Element first = dom.createElement("a");
        root.appendChild(first);
        final Element second = dom.createElement("b");
        root.appendChild(second);
        dom.appendChild(root);
        new SetDirective("alpha").exec(
            dom, new DomCursor(Arrays.asList(first, second)),
            new DomStack()
        );
        MatcherAssert.assertThat(
            "Can't set text directly into DOM nodes",
            XhtmlMatchers.xhtml(dom),
            XhtmlMatchers.hasXPaths(
                "/xxx/a[.='alpha']",
                "/xxx/b[.='alpha']"
            )
        );
    }

}
