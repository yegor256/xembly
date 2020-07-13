/*
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
import com.jcabi.xml.XMLDocument;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Test case for {@link Xembler}.
 *
 * @since 0.1
 */
@SuppressWarnings("PMD.TooManyMethods")
public final class XemblerTest {

    /**
     * Xembler can change DOM document.
     * @throws Exception If some problem inside
     */
    @Test
    public void makesChangesToDomDocument() throws Exception {
        final Document dom = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder().newDocument();
        new Xembler(
            new Directives(
                StringUtils.join(
                    "ADD 'root'; STRICT '1'; ADD 'order';",
                    "ATTR 'tag', 'hello, world!';",
                    "ADD 'price'; SET \"$29.99\"; STRICT '1'; UP; UP;",
                    "XPATH '//order[price=&apos;$29.99&apos;]/price';",
                    "SET ' $39.99 ';"
                )
            )
        ).apply(dom);
        MatcherAssert.assertThat(
            XhtmlMatchers.xhtml(dom),
            XhtmlMatchers.hasXPaths(
                "/root/order[@tag='hello, world!']",
                "/root/order[price=' $39.99 ']"
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

    /**
     * Xembler can print XML.
     * @throws Exception If some problem inside
     */
    @Test
    public void printsXmlDocument() throws Exception {
        MatcherAssert.assertThat(
            new Xembler(
                new Directives().add("hey-you").add("hoy").set("\u20ac")
            ).xml(),
            XhtmlMatchers.hasXPath("/hey-you/hoy[.='\u20ac']")
        );
    }

    /**
     * Xembler can show XML declaration line.
     * @throws Exception If some problem inside
     */
    @Test
    public void rendersXmlDeclaration() throws Exception {
        MatcherAssert.assertThat(
            new Xembler(new Directives("ADD 'f';")).xmlQuietly(),
            Matchers.equalTo("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<f/>")
        );
    }

    /**
     * Xembler can escape broken text.
     * @throws Exception If some problem inside
     * @since 0.14
     */
    @Test
    public void escapesBrokenText() throws Exception {
        MatcherAssert.assertThat(
            Xembler.escape("привет hello \u0000"),
            Matchers.equalTo("привет hello \\u0000")
        );
    }

    /**
     * Xembler can modify a cloned node.
     * @throws Exception If some problem inside
     */
    @Test
    public void modifiesClonedNode() throws Exception {
        final Node node = new XMLDocument("<t/>").node().cloneNode(true);
        new Xembler(new Directives().xpath("/t").add("x")).apply(node);
        MatcherAssert.assertThat(
            new XMLDocument(node),
            XhtmlMatchers.hasXPath("/t/x")
        );
    }

    /**
     * Xembler can remove root node and add a new one.
     * @throws Exception If some problem inside
     */
    @Test
    public void replacesRootNode() throws Exception {
        final Node node = new XMLDocument("<e/>").node();
        new Xembler(new Directives().xpath("/e").remove().add("p")).apply(node);
        MatcherAssert.assertThat(
            new XMLDocument(node),
            XhtmlMatchers.hasXPath("/p")
        );
    }

    /**
     * Xembler can remove attribute node.
     * @throws Exception If some problem inside
     */
    @Test
    public void removesAttribute() throws Exception {
        final Node node = new XMLDocument("<i8 a6='foo'/>").node();
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

    /**
     * Xembler can remove root node.
     * @throws Exception If some problem inside
     */
    @Test
    public void removesRootNode() throws Exception {
        final Node node = new XMLDocument("<old/>").node();
        new Xembler(
            new Directives().xpath("/old").remove().add("new")
        ).apply(node);
        MatcherAssert.assertThat(
            new XMLDocument(node),
            XhtmlMatchers.hasXPath("/new")
        );
    }

    /**
     * Test that concurrent invocations on shared DOM.
     * doesn't ruin executing thread
     * @todo #34
     *  I assumed that Xembler should be a thread-safe and
     *  this test must pass, but this assumption might be wrong and
     *  this test case is not an issue.
     *  Modify <code>int capacity</code> to play with results:
     *  case 1: change capacity to 1 to get test work within 1 thread
     *  case 2: change capacity to any positive number greater than 1
     *  to get test broken
     * @throws Exception If some problem inside
     */
    @Test
    @Disabled
    public void concurrentInvocationWithNoExceptions() throws Exception {
        final ExecutorService service = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors()
        );
        final Document dom = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder().newDocument();
        final Node root = dom.appendChild(dom.createElement("root"));
        final Xembler xembler = new Xembler(
            new Directives(
                "ADDIF 'blow';REMOVE;ADDIF 'blow';"
            )
        );
        final int capacity = 10_000;
        final Collection<Callable<Node>> tasks = new ArrayList<>(capacity);
        for (int idx = 0; idx < capacity; ++idx) {
            final Callable<Node> callable = XemblerTest.callable(
                xembler, root
            );
            tasks.add(callable);
        }
        final List<Future<Node>> futures = service.invokeAll(tasks);
        for (final Future<Node> future : futures) {
            future.get();
        }
        MatcherAssert.assertThat(
            XhtmlMatchers.xhtml(dom),
            XhtmlMatchers.hasXPaths(
                "/root[count(blow) = 1]"
            )
        );
        service.shutdown();
    }

    /**
     * Builds up callable object.
     * @param xembler Xembler's instance
     * @param document DOM object
     * @return Callable object
     */
    private static Callable<Node> callable(
        final Xembler xembler, final Node document
    ) {
        return new Callable<Node>() {
            @Override
            public Node call() throws Exception {
                return xembler.apply(document);
            }
        };
    }
}
