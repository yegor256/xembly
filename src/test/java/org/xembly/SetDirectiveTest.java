/**
 * Copyright (c) 2013-2014, xembly.org
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
import java.util.Arrays;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Test case for {@link SetDirective}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
public final class SetDirectiveTest {

    /**
     * SetDirective can set text content of nodes.
     * @throws Exception If some problem inside
     */
    @Test
    public void setsTextContentOfNodes() throws Exception {
        final Iterable<Directive> dirs = new Directives(
            StringUtils.join(
                new String[] {
                    "ADD 'root'; ADD 'foo';",
                    "SET '&quot;Bonnie &amp; Clyde&quot;';",
                    "UP; ADD 'cops'; SET 'everywhere';",
                }
            )
        );
        final Document dom = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder().newDocument();
        new Xembler(dirs).apply(dom);
        MatcherAssert.assertThat(
            XhtmlMatchers.xhtml(dom),
            XhtmlMatchers.hasXPaths(
                "/root/foo[.='\"Bonnie & Clyde\"']",
                "/root/cops[.='everywhere']"
            )
        );
    }

    /**
     * SetDirective can reject invalid content.
     * @throws Exception If some problem inside
     */
    @Test(expected = SyntaxException.class)
    public void rejectsContentWithInvalidXmlCharacters() throws Exception {
        new Directives("ADD 'alpha'; SET 'illegal: &#27;&#00;&#03;';");
    }

    /**
     * SetDirective can set text content of nodes.
     * @throws Exception If some problem inside
     * @since 0.7
     */
    @Test
    public void setsTextDirectlyIntoDomNodes() throws Exception {
        final Document dom = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder().newDocument();
        final Element root = dom.createElement("xxx");
        final Element first = dom.createElement("a");
        root.appendChild(first);
        final Element second = dom.createElement("b");
        root.appendChild(second);
        dom.appendChild(root);
        new SetDirective("alpha").exec(
            dom, new DomPointer(Arrays.<Node>asList(first, second)),
            new DomStack()
        );
        MatcherAssert.assertThat(
            XhtmlMatchers.xhtml(dom),
            XhtmlMatchers.hasXPaths(
                "/xxx/a[.='alpha']",
                "/xxx/b[.='alpha']"
            )
        );
    }

}
