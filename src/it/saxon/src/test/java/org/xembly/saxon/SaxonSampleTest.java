/*
 * Copyright (c) 2013-2024, xembly.org
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
package org.xembly.saxon;

import com.jcabi.matchers.XhtmlMatchers;
import com.jcabi.xml.XMLDocument;
import org.hamcrest.MatcherAssert;
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
    void buildsDocument() throws Exception {
        MatcherAssert.assertThat(
            new Xembler(
                new Directives().add("root")
                    .addIf("first")
                    .add("node").set("hello")
            ).xml(),
            XhtmlMatchers.hasXPath("/root/first[node='hello']")
        );
    }

    @Test
    void appliesChangesToNode() throws Exception {
        final Node node = new XMLDocument("<doc/>").node();
        new Xembler(
            new Directives().xpath("/doc")
                .remove().xpath("/").add("hello-you")
                .add("second")
                .addIf("sub").set("hey")
        ).apply(node);
        MatcherAssert.assertThat(
            new XMLDocument(node),
            XhtmlMatchers.hasXPath("/hello-you/second[sub='hey']")
        );
    }

}
