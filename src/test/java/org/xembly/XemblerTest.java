/*
 * SPDX-FileCopyrightText: Copyright (c) 2013-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package org.xembly;

import com.jcabi.matchers.XPathMatcher;
import com.jcabi.matchers.XhtmlMatchers;
import com.jcabi.xml.XML;
import com.jcabi.xml.XMLDocument;
import com.jcabi.xml.XPathContext;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Stream;
import javax.xml.parsers.DocumentBuilderFactory;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import org.apache.commons.lang3.StringUtils;
import org.cactoos.experimental.Threads;
import org.cactoos.iterable.Mapped;
import org.cactoos.list.ListOf;
import org.cactoos.scalar.LengthOf;
import org.cactoos.scalar.Repeated;
import org.eolang.jucs.ClasspathSource;
import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.hamcrest.core.AllOf;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.yaml.snakeyaml.Yaml;

/**
 * Test case for {@link Xembler}.
 *
 * @since 0.1
 */
@SuppressWarnings("PMD.TooManyMethods")
final class XemblerTest {

    @TestFactory
    Stream<DynamicTest> parsesDifferentScripts() {
        final String[] scripts = {
            "ADD 'a'; ADD 'b'; ADD 'c'; SET 'привет';",
            "ADD \"a\"; ADD \"b\"; ADD \"c\"; SET \"привет\";",
            "ADD 'x'; SET 'hello';",
            "ADD 'x'; STRICT '1';",
            "ADD 'x'; ATTR 'y', 'z'; PI 'foo', 'bar';",
        };
        return Stream.of(scripts).map(
            script -> DynamicTest.dynamicTest(
                script,
                () -> new Xembler(new Directives(script)).xmlQuietly()
            )
        );
    }

    @Test
    void printsNicely() throws Exception {
        final Document dom = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder().newDocument();
        new Xembler(
            new Directives("ADD 'a'; ADD 'b'; ADD 'c'; SET 'привет';")
        ).apply(dom);
        MatcherAssert.assertThat(
            "Can't print XML nicely",
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
            "Can't make changes to DOM document",
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
            "Can't make changes to DOM document from builder",
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
            "Can't print XML document",
            new Xembler(
                new Directives().add("hey-you").add("hoy").set("\u20ac")
            ).xml(),
            XhtmlMatchers.hasXPath("/hey-you/hoy[.='\u20ac']")
        );
    }

    @Test
    void rendersXmlDeclaration() {
        MatcherAssert.assertThat(
            "Can't render XML declaration",
            new Xembler(new Directives("ADD 'f';")).xmlQuietly(),
            Matchers.equalTo("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<f/>")
        );
    }

    @Test
    void escapesBrokenText() {
        MatcherAssert.assertThat(
            "Can't escape broken text",
            Xembler.escape("привет hello \u0000"),
            Matchers.equalTo("привет hello \\u0000")
        );
    }

    @Property
    void escapesEverything(@ForAll final String value) {
        MatcherAssert.assertThat(
            "Can't escape text",
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
            "Can't modify cloned node",
            new XMLDocument(node),
            XhtmlMatchers.hasXPath("/t/x")
        );
    }

    @Test
    void replacesRootNode() throws Exception {
        final Node node = new XMLDocument("<e/>").deepCopy();
        new Xembler(new Directives().xpath("/e").remove().add("p")).apply(node);
        MatcherAssert.assertThat(
            "Can't replace root node",
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
            "Can't remove attribute",
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
            "Can't remove root node",
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
            "Can't run Xembler concurrently",
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

    @ParameterizedTest
    @ClasspathSource(value = "org/xembly/stories/", glob = "**.yml")
    void checksYamlStories(final String story) {
        MatcherAssert.assertThat(
            "modifies XML document with a few directives",
            XemblerTest.outcomeOf(story),
            XemblerTest.matchersOf(story)
        );
    }

    @SuppressWarnings("unchecked")
    private static XML outcomeOf(final String story) {
        final Map<String, Object> yaml = new Yaml().load(story);
        final Node before = new XMLDocument(
            yaml.get("before").toString()
        ).inner();
        return new XMLDocument(
            new Xembler(
                new Directives(
                    String.join(
                        "",
                        (Iterable<String>) yaml.get("directives")
                    )
                )
            ).applyQuietly(before)
        );
    }

    @SuppressWarnings("unchecked")
    private static Matcher<XML> matchersOf(final String story) {
        return new AllOf<>(
            new ListOf<>(
                new Mapped<>(
                    str -> new XPathMatcher<>(str, new XPathContext()),
                    (Collection<String>) new Yaml()
                        .<Map<String, Object>>load(story)
                        .get("xpaths")
                )
            )
        );
    }

}
