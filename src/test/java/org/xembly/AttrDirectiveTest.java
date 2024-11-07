/*
 * Copyright (c) 2013-2024, xembly.org
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
        final Iterable<Directive> dirs = new Directives(
            StringUtils.join(
                "ADD 'root'; ADD 'foo'; UP; ADD 'foo';",
                "XPATH '//*'; ATTR 'bar', 'привет';"
            )
        );
        final Document dom = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder().newDocument();
        new Xembler(dirs).apply(dom);
        MatcherAssert.assertThat(
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
        final Element first = dom.createElement("a");
        root.appendChild(first);
        final Element second = dom.createElement("b");
        root.appendChild(second);
        dom.appendChild(root);
        new AttrDirective("x", "y").exec(
            dom, new DomCursor(Collections.singletonList(second)),
            new DomStack()
        );
        MatcherAssert.assertThat(
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
            XhtmlMatchers.xhtml(dom),
            XhtmlMatchers.hasXPath("/f[@Price='\u20ac50']")
        );
    }
}
