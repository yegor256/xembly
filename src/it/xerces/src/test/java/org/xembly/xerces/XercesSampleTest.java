/*
 * SPDX-FileCopyrightText: Copyright (c) 2013-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package org.xembly.xerces;

import com.jcabi.matchers.XhtmlMatchers;
import com.jcabi.xml.XMLDocument;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Node;
import org.xembly.Directives;
import org.xembly.Xembler;

/**
 * Test of XML features with Xerces.
 * @since 0.17
 */
final class XercesSampleTest {

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
                .remove().xpath("/").add("hello-you")
                .add("second")
                .addIf("sub").set("друг")
        ).apply(node);
        MatcherAssert.assertThat(
            "Can't apply changes to a node",
            new XMLDocument(node),
            XhtmlMatchers.hasXPath("/hello-you/second[sub='друг']")
        );
    }

}
