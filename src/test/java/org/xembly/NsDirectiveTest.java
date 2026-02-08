/*
 * SPDX-FileCopyrightText: Copyright (c) 2013-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package org.xembly;

import com.jcabi.matchers.XhtmlMatchers;
import com.jcabi.xml.XMLDocument;
import java.util.Collections;
import javax.xml.parsers.DocumentBuilderFactory;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Validates NsDirective class.
 * @since 0.19.3
 */
final class NsDirectiveTest {
    @Test
    void setsNsAttr() throws Exception {
        final Document dom = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder().newDocument();
        final Element root = dom.createElement("f");
        dom.appendChild(root);
        new NsDirective(new Arg("somens")).exec(
            dom, new DomCursor(Collections.singletonList(root)),
            new DomStack()
        );
        MatcherAssert.assertThat(
            "fails to set namespace attribute",
            new XMLDocument(dom).toString(),
            XhtmlMatchers.hasXPath("/ns1:f", "somens")
        );
    }
}
