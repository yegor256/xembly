/**
 * Copyright (c) 2013-2015, xembly.org
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

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link Arg}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
public final class ArgTest {

    /**
     * Arg can escape and unescape.
     * @throws Exception If some problem inside
     */
    @Test
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public void escapesAndUnescaped() throws Exception {
        final String[] texts = new String[] {
            "",
            "123",
            "test \u20ac привет & <>'\"\\",
            "how are you there,\t\n\rтоварищ? &#0D;",
        };
        for (final String text : texts) {
            MatcherAssert.assertThat(
                Arg.unescape(new Arg(text).toString()),
                Matchers.equalTo(text)
            );
        }
    }

    /**
     * Arg can reject to escape invalid text.
     * @throws Exception If some problem inside
     */
    @Test(expected = XmlContentException.class)
    public void rejectsToEscapeInvalidXmlChars() throws Exception {
        new Arg("\u001b\u0000").toString();
    }

    /**
     * Arg can reject to unescape invalid text.
     * @throws Exception If some problem inside
     */
    @Test(expected = XmlContentException.class)
    public void rejectsToUnEscapeInvalidXmlChars() throws Exception {
        Arg.unescape("&#27;&#0000;");
    }

}
