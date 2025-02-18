/*
 * SPDX-FileCopyrightText: 2013-2025 Yegor Bugayenko <yegor256@gmail.com>
 * SPDX-License-Identifier: MIT
 */
package org.xembly;

import com.jcabi.immutable.ArrayMap;
import com.jcabi.matchers.XhtmlMatchers;
import com.jcabi.xml.XMLDocument;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.lang3.StringUtils;
import org.cactoos.experimental.Threads;
import org.cactoos.scalar.LengthOf;
import org.cactoos.scalar.Repeated;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

/**
 * Test case for {@link Directives}.
 *
 * @since 0.1
 */
@SuppressWarnings({"PMD.TooManyMethods", "PMD.AvoidDuplicateLiterals"})
final class DirectivesTest {

    @Test
    void makesXmlDocument() throws Exception {
        MatcherAssert.assertThat(
            "Can't build XML document",
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
    void parsesIncomingGrammar() {
        final Iterable<Directive> dirs = new Directives(
            "XPATH '//orders[@id=\"152\"]'; SET 'test';"
        );
        MatcherAssert.assertThat(
            "Can't parse directives",
            dirs,
            Matchers.iterableWithSize(2)
        );
    }

    @Test
    void throwsOnBrokenGrammar() {
        Assertions.assertThrows(
            SyntaxException.class,
            () -> new Directives("not a xembly at all"),
            "Can't detect broken grammar"
        );
    }

    @Test
    void throwsOnBrokenXmlContent() {
        MatcherAssert.assertThat(
            "Can't detect broken XML content",
            Assertions.assertThrows(
                SyntaxException.class,
                () -> new Directives("ADD 't';\nADD '\u001b';")
            ).getMessage(),
            Matchers.containsString("Character #1B is in the restricted XML")
        );
    }

    @Test
    void throwsOnBrokenEscapedXmlContent() {
        Assertions.assertThrows(
            SyntaxException.class,
            () -> new Directives("ADD '&#27;';"),
            "Can't detect broken escaped XML content"
        );
    }

    @Test
    void addsMapOfValues() throws Exception {
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
            "Can't add map of values",
            XhtmlMatchers.xhtml(dom),
            XhtmlMatchers.hasXPaths(
                "/root/first[.=1]",
                "/root/second[.='two']",
                "/root/third"
            )
        );
    }

    @Test
    void ingoresEmptyInput() {
        MatcherAssert.assertThat(
            "Can't ignore empty input",
            new Directives("\n\t   \r"),
            Matchers.emptyIterable()
        );
    }

    @Test
    void performsFullScaleModifications() throws Exception {
        final String script = new Directives()
            .add("html").attr("xmlns", "http://www.w3.org/1999/xhtml")
            .add("body")
            .add("p")
            .set("\u20ac \\")
            .toString();
        final Document dom = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder().newDocument();
        new Xembler(new Directives(script)).apply(dom);
        MatcherAssert.assertThat(
            "Can't perform full-scale modifications",
            XhtmlMatchers.xhtml(dom),
            XhtmlMatchers.hasXPaths(
                "/xhtml:html",
                "/xhtml:html/body/p[.='\u20ac \\']"
            )
        );
    }

    @Test
    void copiesExistingNode() throws Exception {
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
            ).deepCopy()
        );
        MatcherAssert.assertThat(
            "Can't copy existing node",
            copy,
            Matchers.iterableWithSize(19)
        );
        new Xembler(new Directives().add("dudes").append(copy)).apply(dom);
        MatcherAssert.assertThat(
            "Can't copy existing node",
            XhtmlMatchers.xhtml(dom),
            XhtmlMatchers.hasXPaths(
                "/dudes/jeff[@name = 'Jeffrey']",
                "/dudes/jeff[first and second]",
                "/dudes/jeff/file[@a='x']/f[name='\u20ac']"
            )
        );
    }

    @Test
    void appendsExistingNode() throws Exception {
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
                ).deepCopy()
            )
        ).apply(dom);
        MatcherAssert.assertThat(
            "Can't append existing node",
            XhtmlMatchers.xhtml(dom),
            XhtmlMatchers.hasXPaths(
                "/guys/joe[@name = 'Joey']",
                "/guys/joe[first and second]",
                "/guys/joe/io[@a='x']/f[name='\u20ac']"
            )
        );
    }

    @Test
    void addsElementsCaseSensitively() throws Exception {
        MatcherAssert.assertThat(
            "Can't add elements case-sensitively",
            new Xembler(new Directives().add("XHtml").addIf("Body")).xml(),
            XhtmlMatchers.hasXPaths(
                "/XHtml",
                "/XHtml/Body"
            )
        );
    }

    @Test
    void convertsToString() {
        final Directives dirs = new Directives();
        for (int idx = 0; idx < 10; ++idx) {
            dirs.add("HELLO");
        }
        MatcherAssert.assertThat(
            "Can't convert to string",
            dirs,
            Matchers.hasToString(Matchers.containsString("8:"))
        );
        MatcherAssert.assertThat(
            "Can't convert to string",
            new Directives(dirs.toString()),
            Matchers.not(Matchers.emptyIterable())
        );
    }

    @Test
    void pushesAndPopsCursor() throws Exception {
        MatcherAssert.assertThat(
            "Can't push and pop cursor",
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
    void prefixesItemsWithNamespaces() throws Exception {
        MatcherAssert.assertThat(
            "Can't prefix items with namespaces",
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
    void acceptsFromMultipleThreads() throws Exception {
        final Directives dirs = new Directives().add("mt6");
        final int tasks = 50;
        new LengthOf(
            new Threads<>(
                tasks / 10,
                new Repeated<>(
                    () -> {
                        dirs.append(
                            new Directives()
                                .add("fo9").attr("yu", "").set("some text 90").up()
                                .add("tr4").attr("s2w3", "").set("some other text 76")
                                .up()
                        );
                        return null;
                    },
                    tasks
                )
            )
        ).value();
        MatcherAssert.assertThat(
            "Can't accept from multiple threads",
            XhtmlMatchers.xhtml(new Xembler(dirs).xml()),
            XhtmlMatchers.hasXPath("/mt6[count(fo9[@yu])=50]")
        );
    }

    @Test
    void addsComments() throws Exception {
        MatcherAssert.assertThat(
            "Can't add comments",
            new Xembler(
                new Directives()
                    .add("victory")
                    .comment(Xembler.escape("Yes, we <win>!"))
            ).xml(),
            XhtmlMatchers.hasXPath("//comment()")
        );
    }

    @Test
    void appendsDirs() {
        MatcherAssert.assertThat(
            "Can't append directives",
            new Directives().add("0").append(
                new Directives().add("1")
            ).add("2").toString(),
            Matchers.equalTo("ADD \"0\";ADD \"1\";ADD \"2\";")
        );
    }

    @Test
    void hasUnknownCommand() {
        MatcherAssert.assertThat(
            "Can't detect unknown command",
            Assertions.assertThrows(
                SyntaxException.class,
                () -> new Directives("hello 'hey';")
            ).getMessage(),
            Matchers.containsString("Unknown command")
        );
    }

    @Test
    void doesNotHaveQuoteInSimpleCommand() {
        MatcherAssert.assertThat(
            "Can't detect missing quote in simple command",
            Assertions.assertThrows(
                SyntaxException.class,
                () -> new Directives("ADD ;")
            ).getMessage(),
            Matchers.containsString("Couldn't find quote in the part of the command")
        );
    }

    @Test
    void containsWrongSymbolsAfterSimpleCommandArgument() {
        MatcherAssert.assertThat(
            "Can't detect wrong symbols after simple command argument",
            Assertions.assertThrows(
                SyntaxException.class,
                () -> new Directives("ADD 'o' wrong;")
            ).getMessage(),
            Matchers.containsString("Unexpected symbols after command argument")
        );
    }

    @Test
    void hasManyArgumentsForSimpleCommand() {
        MatcherAssert.assertThat(
            "Can't detect many arguments for simple command",
            Assertions.assertThrows(
                SyntaxException.class,
                () -> new Directives("ADD 'o' ' ;")
            ).getMessage(),
            Matchers.containsString("Unexpected behaviour when searching for command arguments")
        );
    }

    @Test
    void containsSemicolonAfterFirstArgumentInComplexCommand() {
        MatcherAssert.assertThat(
            "Can't detect semicolon after first argument in complex command",
            Assertions.assertThrows(
                SyntaxException.class,
                () -> new Directives("ATTR 'o';")
            ).getMessage(),
            Matchers.containsString("Unexpected last quote")
        );
    }

    @Test
    void doesNotContainCommaAfterFirstArgumentInComplexCommand() {
        MatcherAssert.assertThat(
            "Can't detect missing comma after first argument in complex command",
            Assertions.assertThrows(
                SyntaxException.class,
                () -> new Directives("ATTR 'o' .;")
            ).getMessage(),
            Matchers.containsString("Comma after first argument is expected")
        );
    }

    @Test
    void containsUnexpectedSemicolonAfterCommaInComplexCommand() {
        MatcherAssert.assertThat(
            "Can't detect unexpected semicolon after comma in complex command",
            Assertions.assertThrows(
                SyntaxException.class,
                () -> new Directives("ATTR 'o' , ;")
            ).getMessage(),
            Matchers.containsString("Unexpected last semicolon")
        );
    }

    @Test
    void doesNotContainQuoteAfterFirstArgumentInComplexCommand() {
        MatcherAssert.assertThat(
            "Can't detect missing quote after first argument in complex command",
            Assertions.assertThrows(
                SyntaxException.class,
                () -> new Directives("ATTR 'o' , x ;")
            ).getMessage(),
            Matchers.containsString("Single or double quote is expected after comma")
        );
    }

    @Test
    void containsUnexpectedSymbolsAfterSecondArgumentInComplexCommand() {
        MatcherAssert.assertThat(
            "Can't detect unexpected symbols after second argument in complex command",
            Assertions.assertThrows(
                SyntaxException.class,
                () -> new Directives("ATTR 'o' , \"x\" y ;")
            ).getMessage(),
            Matchers.containsString("Unexpected symbols after second argument")
        );
    }
}
