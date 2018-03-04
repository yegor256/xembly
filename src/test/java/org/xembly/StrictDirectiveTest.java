/**
 * Copyright (c) 2013-2018, xembly.org
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

import com.jcabi.matchers.XhtmlMatchers;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.w3c.dom.Document;

/**
 * Test case for {@link StrictDirective}.
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 */
public final class StrictDirectiveTest {

    /**
     * StrictDirective can check the number of current nodes.
     * @throws Exception If some problem inside
     */
    @Test
    public void checksNumberOfCurrentNodes() throws Exception {
        final Iterable<Directive> dirs = new Directives(
            StringUtils.join(
                new String[]{
                    "ADD 'root'; ADD 'foo'; ADD 'bar';",
                    "ADD 'boom'; XPATH '//*'; STRICT '4';",
                }
            )
        );
        final Document dom = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder().newDocument();
        new Xembler(dirs).apply(dom);
        MatcherAssert.assertThat(
            XhtmlMatchers.xhtml(dom),
            XhtmlMatchers.hasXPaths(
                "/root/foo",
                "/root/foo/bar/boom"
            )
        );
    }

    /**
     * StrictDirective can fail when number of current nodes is too big.
     * @throws Exception If some problem inside
     */
    @Test(expected = ImpossibleModificationException.class)
    public void failsWhenNumberOfCurrentNodesIsTooBig() throws Exception {
        final Iterable<Directive> dirs = new Directives(
            "ADD 'bar'; UP; ADD 'bar'; XPATH '/f/bar'; STRICT '1';"
        );
        final Document dom = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder().newDocument();
        dom.appendChild(dom.createElement("f"));
        new Xembler(dirs).apply(dom);
    }

    /**
     * StrictDirective can fail when number of current nodes is zero.
     * @throws Exception If some problem inside
     */
    @Test(expected = ImpossibleModificationException.class)
    public void failsWhenNumberOfCurrentNodesIsZero() throws Exception {
        final Iterable<Directive> dirs = new Directives(
            "ADD 'foo'; ADD 'x'; XPATH '/foo/absent'; STRICT '1';"
        );
        final Document dom = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder().newDocument();
        dom.appendChild(dom.createElement("z"));
        new Xembler(dirs).apply(dom);
    }

    /**
     * StrictDirective can fail when number of current nodes is too small.
     * @throws Exception If some problem inside
     */
    @Test(expected = ImpossibleModificationException.class)
    public void failsWhenNumberOfCurrentNodesIsTooSmall() throws Exception {
        final Iterable<Directive> dirs = new Directives(
            "ADD 'bar'; STRICT '2';"
        );
        final Document dom = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder().newDocument();
        dom.appendChild(dom.createElement("x"));
        new Xembler(dirs).apply(dom);
    }

}
