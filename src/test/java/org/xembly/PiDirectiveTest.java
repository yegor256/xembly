/*
 * SPDX-FileCopyrightText: 2013-2025 Yegor Bugayenko <yegor256@gmail.com>
 * SPDX-License-Identifier: MIT
 */
package org.xembly;

import com.jcabi.matchers.XhtmlMatchers;
import java.util.Collections;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Test case for {@link PiDirective}.
 * @since 0.9
 */
final class PiDirectiveTest {

    @Test
    void addsProcessingInstructionsToDom() throws Exception {
        final Iterable<Directive> dirs = new Directives(
            StringUtils.join(
                "XPATH '/root'; PI 'ab', 'boom \u20ac';",
                "ADD 'test'; PI 'foo', 'some data \u20ac';"
            )
        );
        final Document dom = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder().newDocument();
        dom.appendChild(dom.createElement("root"));
        new Xembler(dirs).apply(dom);
        MatcherAssert.assertThat(
            "Can't add processing instructions to DOM",
            XhtmlMatchers.xhtml(dom),
            XhtmlMatchers.hasXPaths(
                "/root/processing-instruction('ab')",
                "/root/test/processing-instruction('foo')"
            )
        );
    }

    @Test
    void addsProcessingInstructionsDirectlyToDom() throws Exception {
        final Document dom = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder().newDocument();
        final Element root = dom.createElement("xxx");
        dom.appendChild(root);
        new PiDirective("x", "y").exec(
            dom, new DomCursor(Collections.emptyList()),
            new DomStack()
        );
        MatcherAssert.assertThat(
            "Can't add processing instructions directly to DOM",
            XhtmlMatchers.xhtml(dom),
            XhtmlMatchers.hasXPath("/processing-instruction('x')")
        );
    }

    @Test
    void addsProcessingInstructionsToDomRoot() throws Exception {
        final Iterable<Directive> dirs = new Directives(
            "XPATH '/'; PI 'alpha', 'beta \u20ac'; ADD 'x4';"
        );
        final Document dom = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder().newDocument();
        new Xembler(dirs).apply(dom);
        MatcherAssert.assertThat(
            "Can't add processing instructions to DOM root",
            XhtmlMatchers.xhtml(dom),
            XhtmlMatchers.hasXPath(
                "/processing-instruction('alpha')[.='beta \u20ac']"
            )
        );
    }

    @Test
    void prependsProcessingInstructionsToDomRoot() throws Exception {
        MatcherAssert.assertThat(
            "Can't prepend processing instructions to DOM root",
            new Xembler(new Directives("PI 'a', 'b'; ADD 'c';")).xml(),
            Matchers.containsString("<?a b?><c/>")
        );
    }

}
