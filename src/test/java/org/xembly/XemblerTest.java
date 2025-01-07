/*
 * Copyright (c) 2013-2025, Yegor Bugayenko
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
import com.jcabi.xml.XMLDocument;
import javax.xml.parsers.DocumentBuilderFactory;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import org.apache.commons.lang3.StringUtils;
import org.cactoos.experimental.Threads;
import org.cactoos.scalar.LengthOf;
import org.cactoos.scalar.Repeated;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Test case for {@link Xembler}.
 *
 * @since 0.1
 */
@SuppressWarnings("PMD.TooManyMethods")
final class XemblerTest {

    @Test
    void printsNicely() throws Exception {
        final Document dom = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder().newDocument();
        new Xembler(
            new Directives("ADD 'a'; ADD 'b'; ADD 'c'; SET 'привет';")
        ).apply(dom);
        MatcherAssert.assertThat(
            new XMLDocument(dom).toString(),
            Matchers.containsString("<a>\n   <b>\n      <c>")
        );
    }

    @Test
    void makesChangesToDomDocument() throws Exception {
        final Document dom = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder().newDocument();
        new Xembler(
            new Directives(
                StringUtils.join(
                    "ADD 'root'; STRICT '1'; ADD 'order';",
                    "ATTR 'tag', 'hello, друг!';",
                    "ADD 'price'; SET \"$29.99\"; STRICT '1'; UP; UP;",
                    "XPATH '//order[price=&apos;$29.99&apos;]/price';",
                    "SET ' $39.99 ';"
                )
            )
        ).apply(dom);
        MatcherAssert.assertThat(
            XhtmlMatchers.xhtml(dom),
            XhtmlMatchers.hasXPaths(
                "/root/order[@tag='hello, друг!']",
                "/root/order[price=' $39.99 ']"
            )
        );
    }

    @Test
    void makesChangesToDomDocumentFromBuilder() throws Exception {
        final Document dom = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder().newDocument();
        final Directives builder = new Directives()
            .add("top")
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
        new Xembler(new Directives(builder.toString())).applyQuietly(dom);
        MatcherAssert.assertThat(
            XhtmlMatchers.xhtml(dom),
            XhtmlMatchers.hasXPaths(
                "/top/employees/employee[@id='<443>']",
                "//employee[name='\"Юра Лермонтов\"']"
            )
        );
    }

    @Test
    void printsXmlDocument() throws Exception {
        MatcherAssert.assertThat(
            new Xembler(
                new Directives().add("hey-you").add("hoy").set("\u20ac")
            ).xml(),
            XhtmlMatchers.hasXPath("/hey-you/hoy[.='\u20ac']")
        );
    }

    @Test
    void rendersXmlDeclaration() {
        MatcherAssert.assertThat(
            new Xembler(new Directives("ADD 'f';")).xmlQuietly(),
            Matchers.equalTo("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<f/>")
        );
    }

    @Test
    void escapesBrokenText() {
        MatcherAssert.assertThat(
            Xembler.escape("привет hello \u0000"),
            Matchers.equalTo("привет hello \\u0000")
        );
    }

    @Property
    void escapesEverything(@ForAll final String value) {
        MatcherAssert.assertThat(
            new XMLDocument(
                new Xembler(
                    new Directives().add("r").attr("a", Xembler.escape(value))
                ).domQuietly()
            ),
            XhtmlMatchers.hasXPath("/r/@a")
        );
    }

    @Test
    void modifiesClonedNode() throws Exception {
        final Node node = new XMLDocument("<t/>").deepCopy();
        new Xembler(new Directives().xpath("/t").add("x")).apply(node);
        MatcherAssert.assertThat(
            new XMLDocument(node),
            XhtmlMatchers.hasXPath("/t/x")
        );
    }

    @Test
    void replacesRootNode() throws Exception {
        final Node node = new XMLDocument("<e/>").deepCopy();
        new Xembler(new Directives().xpath("/e").remove().add("p")).apply(node);
        MatcherAssert.assertThat(
            new XMLDocument(node),
            XhtmlMatchers.hasXPath("/p")
        );
    }

    @Test
    void removesAttribute() throws Exception {
        final Node node = new XMLDocument("<i8 a6='foo'/>").deepCopy();
        new Xembler(
            new Directives()
                .xpath("/i8/@a6")
                .strict(1)
                .remove()
                .attr("a7", "foo-9")
        ).apply(node);
        MatcherAssert.assertThat(
            new XMLDocument(node),
            XhtmlMatchers.hasXPath("/i8[@a7='foo-9' and not(@a6)]")
        );
    }

    @Test
    void removesRootNode() throws Exception {
        final Node node = new XMLDocument("<old/>").deepCopy();
        new Xembler(
            new Directives().xpath("/old").remove().add("new")
        ).apply(node);
        MatcherAssert.assertThat(
            new XMLDocument(node),
            XhtmlMatchers.hasXPath("/new")
        );
    }

    @Test
    void concurrentInvocationWithNoExceptions() throws Exception {
        final Document dom = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder().newDocument();
        dom.appendChild(dom.createElement("root"));
        final Xembler xembler = new Xembler(
            new Directives(
                "XPATH '/root';ADDIF 'blow';"
            )
        );
        final int tasks = 10;
        new LengthOf(
            new Threads<>(
                tasks,
                new Repeated<>(
                    () -> {
                        xembler.apply(dom);
                        return null;
                    },
                    tasks
                )
            )
        ).value();
        MatcherAssert.assertThat(
            XhtmlMatchers.xhtml(dom),
            XhtmlMatchers.hasXPaths(
                "/root[count(blow) = 1]"
            )
        );
    }

    @Test
    void omitsHeader() {
        MatcherAssert.assertThat(
            "Xembler should omit XML header for Node output type",
            new Xembler(
                new Directives()
                    .add("animals")
                    .add("cow")
                    .set("му-му"),
                new Transformers.Node()
            ).xmlQuietly(),
            Matchers.equalTo("<animals><cow>му-му</cow></animals>")
        );
    }

    @Test
    void printsNamespaces() {
        MatcherAssert.assertThat(
            "XML output must have namespace prefixes",
            new Xembler(
                new Directives().add("flower").attr("color x html", "green")
            ).xmlQuietly(),
            Matchers.containsString("x:color=\"green\"")
        );
    }
}
