/*
 * Copyright (c) 2013-2025, Yegor Bugayenko
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
package org.xembly.prof;

import com.jcabi.aspects.Loggable;
import com.jcabi.matchers.XhtmlMatchers;
import javax.xml.parsers.DocumentBuilderFactory;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xembly.Directives;
import org.xembly.Xembler;

/**
 * Prof case for {@link Xembler}.
 * @since 0.10.1
 */
@Loggable
final class XemblerProfTest {

    @Test
    @SuppressWarnings("PMD.InsufficientStringBufferDeclaration")
    void modifiesDom() throws Exception {
        final StringBuilder program = new StringBuilder(1000)
            .append("ADD 'root';");
        for (int idx = 0; idx < 50_000; ++idx) {
            program.append(
                "XPATH '/root'; ADDIF 'node';ADD 'temp'; REMOVE;SET '"
            ).append(idx).append("';");
        }
        final Directives dirs = new Directives(program.toString());
        final Document dom = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder().newDocument();
        new Xembler(dirs).apply(dom);
        MatcherAssert.assertThat(
            XhtmlMatchers.xhtml(dom),
            XhtmlMatchers.hasXPath("/root/node[.='49999']")
        );
    }
}
