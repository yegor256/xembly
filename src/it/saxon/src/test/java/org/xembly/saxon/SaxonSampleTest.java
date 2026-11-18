/*
 * SPDX-FileCopyrightText: Copyright (c) 2013-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package org.xembly.saxon;

import com.jcabi.matchers.XhtmlMatchers;
import com.jcabi.xml.XMLDocument;
import javax.xml.transform.TransformerFactory;
import javax.xml.xpath.XPathFactory;
import net.sf.saxon.TransformerFactoryImpl;
import net.sf.saxon.xpath.XPathFactoryImpl;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Node;
import org.xembly.Directives;
import org.xembly.Xembler;

/**
 * Test of XML features with Saxon.
 * @since 0.17
 */
final class SaxonSampleTest {

    @Test
    void loadsSaxonProviders() {
        MatcherAssert.assertThat(
            "Must use Saxon for transformations",
            TransformerFactory.newInstance(),
            Matchers.instanceOf(TransformerFactoryImpl.class)
        );
        MatcherAssert.assertThat(
            "Must use Saxon for DOM XPath expressions",
            XPathFactory.newInstance(),
            Matchers.instanceOf(XPathFactoryImpl.class)
        );
    }

    @Test
    void buildsDocument() throws Exception {
        MatcherAssert.assertThat(
            "Can't build XML document",
            new Xembler(
                new Directives().add("root")
                    .addIf("first")
                    .add("node").set("привет")
            ).xml(),
            XhtmlMatchers.hasXPath("/root/first[node='привет']")
        );
    }

    @Test
    void appliesChangesToNode() throws Exception {
        final Node node = new XMLDocument("<doc/>").node();
        new Xembler(
            new Directives().xpath("/doc")
                .remove().xpath("/").add("привет-you")
                .add("second")
                .addIf("sub").set("друг")
        ).apply(node);
        MatcherAssert.assertThat(
            "Can't apply changes to a node",
            new XMLDocument(node),
            XhtmlMatchers.hasXPath("/привет-you/second[sub='друг']")
        );
    }

}
