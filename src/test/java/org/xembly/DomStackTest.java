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

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test case for {@link DomStack}.
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.21
 */
public final class DomStackTest {

    /**
     * DomStack can push and pop.
     * @throws Exception If some problem inside
     */
    @Test
    public void addsAndRetrieves() throws Exception {
        final Directive.Stack stack = new DomStack();
        final Directive.Cursor first = Mockito.mock(Directive.Cursor.class);
        final Directive.Cursor second = Mockito.mock(Directive.Cursor.class);
        stack.push(first);
        stack.push(second);
        MatcherAssert.assertThat(
            stack.pop(),
            Matchers.equalTo(second)
        );
        MatcherAssert.assertThat(
            stack.pop(),
            Matchers.equalTo(first)
        );
    }

    /**
     * DomStack throws ImpossibleModificationException when
     * trying to pop an empty stack.
     * @throws Exception If some problem inside
     */
    @Test(expected = ImpossibleModificationException.class)
    public void throwsExceptionOnEmpty() throws Exception {
        new DomStack().pop();
    }
}
