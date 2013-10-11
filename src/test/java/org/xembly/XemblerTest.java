/**
 * Copyright (c) 2013, xembly.org
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

import com.rexsl.test.XhtmlMatchers;
import javax.xml.parsers.DocumentBuilderFactory;
import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Test case for {@link Xembler}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
public final class XemblerTest {

    /**
     * Xembler can change DOM document.
     * @throws Exception If some problem inside
     */
    @Test
    public void makesChangesToDomDocument() throws Exception {
        final Document dom = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder().newDocument();
        final Element root = dom.createElement("root");
        dom.appendChild(root);
        new Xembler(
            new Directives(
                // @checkstyle StringLiteralsConcatenation (4 lines)
                "STRICT '1'; ADD 'order'; ATTR 'tag', 'hello, world!';"
                + "ADD 'price'; SET \"$29.99\"; STRICT '1'; UP; UP;"
                + "XPATH '//order[price=&apos;$29.99&apos;]/price';"
                + "SET '$39.99';"
            )
        ).apply(dom);
        MatcherAssert.assertThat(
            XhtmlMatchers.xhtml(dom),
            XhtmlMatchers.hasXPaths(
                "/root/order[@tag='hello, world!']",
                "/root/order[price='$39.99']"
            )
        );
    }

    /**
     * Xembler can change DOM document from builder.
     * @throws Exception If some problem inside
     */
    @Test
    public void makesChangesToDomDocumentFromBuilder() throws Exception {
        final Document dom = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder().newDocument();
        final Element root = dom.createElement("top");
        dom.appendChild(root);
        final Directives builder = new Directives()
            .add("employees")
            .add("paper")
            .up()
            .xpath("*")
            .remove()
            .addIf("employee")
            .attr("id", "<443>")
            .add("name")
            .strict(1)
            .set("\rСаша\t\nПушкин\n")
            .up()
            .up()
            .xpath("/top/employees/employee[@id='<443>']/name")
            .set("\"Юра Лермонтов\"");
        new Xembler(new Directives(builder.toString())).apply(dom);
        MatcherAssert.assertThat(
            XhtmlMatchers.xhtml(dom),
            XhtmlMatchers.hasXPaths(
                "/top/employees/employee[@id='<443>']",
                "//employee[name='\"Юра Лермонтов\"']"
            )
        );
    }

    /**
     * Xembler can print XML.
     * @throws Exception If some problem inside
     */
    @Test
    public void printsXmlDocument() throws Exception {
        MatcherAssert.assertThat(
            new Xembler(
                new Directives().add("hey-you").add("hoy").set("\u20ac")
            ).xml("bbb"),
            XhtmlMatchers.hasXPath("/bbb/hey-you/hoy[.='\u20ac']")
        );
    }

}
