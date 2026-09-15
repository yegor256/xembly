/*
 * SPDX-FileCopyrightText: Copyright (c) 2013-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package org.xembly;

import com.jcabi.matchers.XhtmlMatchers;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

/**
 * Test case for {@link XattrDirective}.
 *
 * @since 0.28
 */
final class XattrDirectiveTest {

    @Test
    void setsAttributesToCurrentNodes() throws Exception {
        final Document dom = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder().newDocument();
        new Xembler(
            new Directives(
                StringUtils.join(
                    "ADD 'root'; ADD 'foo'; UP; ADD 'foo';",
                    "XPATH '//*'; XATTR 'bar', 'count(//foo)';"
                )
            )
        ).apply(dom);
        MatcherAssert.assertThat(
            "Can't set attributes to current nodes",
            XhtmlMatchers.xhtml(dom),
            XhtmlMatchers.hasXPaths("/root[@bar = 2]")
        );
    }
}
