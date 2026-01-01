/*
 * SPDX-FileCopyrightText: Copyright (c) 2013-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package org.xembly;

import com.jcabi.matchers.XhtmlMatchers;
import javax.xml.parsers.DocumentBuilderFactory;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

/**
 * Test case for {@link CommentDirective}.
 *
 * @since 0.1
 */
final class CommentDirectiveTest {

    @Test
    void addsComment() throws Exception {
        final Iterable<Directive> dirs = new Directives(
            "ADD 'root'; ADD 'foo'; COMMENT 'How are you?';"
        );
        final Document dom = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder().newDocument();
        new Xembler(dirs).apply(dom);
        MatcherAssert.assertThat(
            "fails to add comment",
            XhtmlMatchers.xhtml(dom),
            XhtmlMatchers.hasXPath("//comment()")
        );
    }

}
