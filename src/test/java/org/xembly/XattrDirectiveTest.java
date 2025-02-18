/*
 * SPDX-FileCopyrightText: 2013-2025 Yegor Bugayenko <yegor256@gmail.com>
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
        final Iterable<Directive> dirs = new Directives(
            StringUtils.join(
                "ADD 'root'; ADD 'foo'; UP; ADD 'foo';",
                "XPATH '//*'; XATTR 'bar', 'count(//foo)';"
            )
        );
        final Document dom = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder().newDocument();
        new Xembler(dirs).apply(dom);
        MatcherAssert.assertThat(
            "Can't set attributes to current nodes",
            XhtmlMatchers.xhtml(dom),
            XhtmlMatchers.hasXPaths("/root[@bar = 2]")
        );
    }
}
