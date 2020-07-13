/**
 * Copyright (c) 2013-2020, xembly.org
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the xembly.org nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
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
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 */
public final class XpathDirectiveTest {

    /**
     * XpathDirective can find nodes.
     * @throws Exception If some problem inside
     */
    @Test
    public void findsNodesWithXpathExpression() throws Exception {
        final Iterable<Directive> dirs = new Directives(
            StringUtils.join(
                new String[] {
                    "ADD 'root'; ADD 'foo'; ATTR 'bar', '1'; UP; ADD 'bar';",
                    "XPATH '//*[@bar=1]'; ADD 'test';",
                }
            )
        );
        final Document dom = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder().newDocument();
        new Xembler(dirs).apply(dom);
        MatcherAssert.assertThat(
            XhtmlMatchers.xhtml(dom),
            XhtmlMatchers.hasXPaths(
                "/root/foo[@bar=1]/test",
                "/root/bar"
            )
        );
    }

    /**
     * XpathDirective can ignore empty searches.
     * @throws Exception If some problem inside
     */
    @Test
    public void ignoresEmptySearches() throws Exception {
        final Iterable<Directive> dirs = new Directives(
            "XPATH '/nothing'; XPATH '/top'; STRICT '1'; ADD 'hey';"
        );
        final Document dom = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder().newDocument();
        dom.appendChild(dom.createElement("top"));
        new Xembler(dirs).apply(dom);
        MatcherAssert.assertThat(
            XhtmlMatchers.xhtml(dom),
            XhtmlMatchers.hasXPath("/top/hey")
        );
    }

    /**
     * XpathDirective can find nodes by XPath.
     * @throws Exception If some problem inside
     * @since 0.7
     */
    @Test
    public void findsNodesByXpathDirectly() throws Exception {
        final Document dom = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder().newDocument();
        final Element root = dom.createElement("xxx");
        final Element first = dom.createElement("a");
        root.appendChild(first);
        final Element second = dom.createElement("b");
        root.appendChild(second);
        dom.appendChild(root);
        MatcherAssert.assertThat(
            new XpathDirective("/*").exec(
                dom,
                new DomCursor(Collections.<Node>singletonList(first)),
                new DomStack()
            ),
            Matchers.hasItem(root)
        );
    }

    /**
     * XpathDirective can find nodes in empty DOM.
     * @throws Exception If some problem inside
     */
    @Test
    public void findsNodesInEmptyDom() throws Exception {
        final Document dom = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder().newDocument();
        MatcherAssert.assertThat(
            new XpathDirective("/some-root").exec(
                dom,
                new DomCursor(Collections.<Node>emptyList()),
                new DomStack()
            ),
            Matchers.emptyIterable()
        );
    }

    /**
     * XpathDirective can find root in cloned document.
     * @throws Exception If some problem inside
     */
    @Test
    public void findsRootInClonedNode() throws Exception {
        final Iterable<Directive> dirs = new Directives(
            "XPATH '/*'; STRICT '1'; ADD 'boom-5';"
        );
        final Document dom = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder().newDocument();
        dom.appendChild(dom.createElement("high"));
        final Node clone = dom.cloneNode(true);
        new Xembler(dirs).apply(clone);
        MatcherAssert.assertThat(
            XhtmlMatchers.xhtml(clone),
            XhtmlMatchers.hasXPath("/high/boom-5")
        );
    }

}
