/*
 * Copyright (c) 2013-2022, xembly.org
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

import com.jcabi.aspects.Parallel;
import com.jcabi.aspects.Tv;
import com.jcabi.immutable.ArrayMap;
import com.jcabi.matchers.XhtmlMatchers;
import com.jcabi.xml.XMLDocument;
import java.util.concurrent.Callable;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

/**
 * Test case for {@link Directives}.
 *
 * @since 0.1
 */
@SuppressWarnings({"PMD.TooManyMethods", "PMD.AvoidDuplicateLiterals"})
public final class DirectivesTest {

    @Test
    public void makesXmlDocument() throws Exception {
        MatcherAssert.assertThat(
            XhtmlMatchers.xhtml(
                new Xembler(
                    new Directives()
                        .pi("xml-stylesheet", "none")
                        .add("page")
                        .attr("the-name", "with \u20ac")
                        .add("child-node").set(" the text\n").up()
                        .add("big_text").cdata("<<hello\n\n!!!>>").up()
                ).xml()
            ),
            XhtmlMatchers.hasXPaths(
                "/page[@the-name]",
                "/page/big_text[.='<<hello\n\n!!!>>']"
            )
        );
    }

    @Test
    public void parsesIncomingGrammar() throws Exception {
        final Iterable<Directive> dirs = new Directives(
            "XPATH '//orders[@id=\"152\"]'; SET 'test';"
        );
        MatcherAssert.assertThat(dirs, Matchers.iterableWithSize(2));
    }

    @Test
    public void throwsOnBrokenGrammar() {
        Assertions.assertThrows(
            SyntaxException.class,
            () -> new Directives("not a xembly at all")
        );
    }

    @Test
    public void throwsOnBrokenXmlContent() {
        Assertions.assertThrows(
            SyntaxException.class,
            () -> new Directives("ADD '\u001b';")
        );
    }

    @Test
    public void throwsOnBrokenEscapedXmlContent() {
        Assertions.assertThrows(
            SyntaxException.class,
            () -> new Directives("ADD '&#27;';")
        );
    }

    @Test
    public void addsMapOfValues() throws Exception {
        final Document dom = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder().newDocument();
        dom.appendChild(dom.createElement("root"));
        new Xembler(
            new Directives().xpath("/root").add(
                new ArrayMap<String, Object>()
                    .with("first", 1)
                    .with("second", "two")
            ).add("third")
        ).apply(dom);
        MatcherAssert.assertThat(
            XhtmlMatchers.xhtml(dom),
            XhtmlMatchers.hasXPaths(
                "/root/first[.=1]",
                "/root/second[.='two']",
                "/root/third"
            )
        );
    }

    @Test
    public void ingoresEmptyInput() throws Exception {
        MatcherAssert.assertThat(
            new Directives("\n\t   \r"),
            Matchers.emptyIterable()
        );
    }

    @Test
    public void performsFullScaleModifications() throws Exception {
        final String script = new Directives()
            // @checkstyle MultipleStringLiteralsCheck (1 line)
            .add("html").attr("xmlns", "http://www.w3.org/1999/xhtml")
            .add("body")
            .add("p")
            .set("\u20ac \\")
            .toString();
        final Document dom = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder().newDocument();
        new Xembler(new Directives(script)).apply(dom);
        MatcherAssert.assertThat(
            XhtmlMatchers.xhtml(dom),
            XhtmlMatchers.hasXPaths(
                "/xhtml:html",
                "/xhtml:html/body/p[.='\u20ac \\']"
            )
        );
    }

    @Test
    public void copiesExistingNode() throws Exception {
        final Document dom = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder().newDocument();
        final Iterable<Directive> copy = Directives.copyOf(
            new XMLDocument(
                StringUtils.join(
                    "<jeff name='Jeffrey'><first/><second/>",
                    "<?some-pi test?>",
                    "<file a='x'><f><name>\u20ac</name></f></file>",
                    "<!-- some comment -->",
                    "<x><![CDATA[hey you]]></x>  </jeff>"
                )
            ).node()
        );
        MatcherAssert.assertThat(copy, Matchers.iterableWithSize(19));
        new Xembler(new Directives().add("dudes").append(copy)).apply(dom);
        MatcherAssert.assertThat(
            XhtmlMatchers.xhtml(dom),
            XhtmlMatchers.hasXPaths(
                "/dudes/jeff[@name = 'Jeffrey']",
                "/dudes/jeff[first and second]",
                "/dudes/jeff/file[@a='x']/f[name='\u20ac']"
            )
        );
    }

    @Test
    public void appendsExistingNode() throws Exception {
        final Document dom = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder().newDocument();
        new Xembler(
            new Directives().add("guys").append(
                new XMLDocument(
                    StringUtils.join(
                        "<joe name='Joey'><first/><second/>",
                        "<io a='x'><f><name>\u20ac</name></f></io>",
                        "<x><![CDATA[hey you]]></x>  </joe>"
                    )
                ).node()
            )
        ).apply(dom);
        MatcherAssert.assertThat(
            XhtmlMatchers.xhtml(dom),
            XhtmlMatchers.hasXPaths(
                "/guys/joe[@name = 'Joey']",
                "/guys/joe[first and second]",
                "/guys/joe/io[@a='x']/f[name='\u20ac']"
            )
        );
    }

    @Test
    public void addsElementsCaseSensitively() throws Exception {
        MatcherAssert.assertThat(
            new Xembler(new Directives().add("XHtml").addIf("Body")).xml(),
            XhtmlMatchers.hasXPaths(
                "/XHtml",
                "/XHtml/Body"
            )
        );
    }

    @Test
    public void convertsToString() throws Exception {
        final Directives dirs = new Directives();
        for (int idx = 0; idx < Tv.TEN; ++idx) {
            dirs.add("HELLO");
        }
        MatcherAssert.assertThat(
            dirs,
            Matchers.hasToString(Matchers.containsString("8:"))
        );
        MatcherAssert.assertThat(
            new Directives(dirs.toString()),
            Matchers.not(Matchers.emptyIterable())
        );
    }

    @Test
    public void pushesAndPopsCursor() throws Exception {
        MatcherAssert.assertThat(
            XhtmlMatchers.xhtml(
                new Xembler(
                    new Directives()
                        .add("jeff")
                        .push().add("lebowski")
                        .push().xpath("/jeff").add("dude").pop()
                        .attr("birthday", "today").pop()
                        .add("los-angeles")
                ).xml()
            ),
            XhtmlMatchers.hasXPaths(
                "/jeff/lebowski[@birthday]",
                "/jeff/los-angeles",
                "/jeff/dude"
            )
        );
    }

    @Test
    public void prefixesItemsWithNamespaces() throws Exception {
        MatcherAssert.assertThat(
            new Xembler(
                new Directives()
                    .add("bbb")
                    .attr("xmlns:x", "http://www.w3.org/1999/xhtml")
                    .add("x:node").set("HELLO WORLD!")
            ).xml(),
            XhtmlMatchers.hasXPath("//xhtml:node")
        );
    }

    @Test
    @Disabled
    public void acceptsFromMultipleThreads() throws Exception {
        final Directives dirs = new Directives().add("mt6");
        new Callable<Void>() {
            @Parallel(threads = Tv.FIFTY)
            @Override
            public Void call() throws Exception {
                dirs.append(
                    new Directives()
                        .add("fo9").attr("yu", "").set("some text 90")
                        .add("tr4").attr("s2w3", "").set("some other text 76")
                        .up().up()
                );
                return null;
            }
        } .call();
        MatcherAssert.assertThat(
            XhtmlMatchers.xhtml(new Xembler(dirs).xml()),
            XhtmlMatchers.hasXPath("/mt6[count(fo9[@yu])=50]")
        );
    }

    @Test
    public void addsComments() throws Exception {
        MatcherAssert.assertThat(
            new Xembler(
                new Directives()
                    .add("victory")
                    .comment(Xembler.escape("Yes, we <win>!"))
            ).xml(),
            XhtmlMatchers.hasXPath("//comment()")
        );
    }

    @Test
    public void appendsDirs() throws Exception {
        MatcherAssert.assertThat(
            new Directives().add("0").append(
                new Directives().add("1")
            ).add("2").toString(),
            Matchers.equalTo("ADD \"0\";ADD \"1\";ADD \"2\";")
        );
    }

}
