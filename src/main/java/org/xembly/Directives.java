/**
 * Copyright (c) 2013, xembly.org
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

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.immutable.Array;
import java.util.AbstractCollection;
import java.util.Iterator;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.TokenStream;

/**
 * Collection of {@link Directive}s, instantiable from {@link String}.
 *
 * <p>For example, to fetch directives from a string and apply to the
 * DOM document:
 *
 * <pre>Document dom = DocumentBuilderFactory.newInstance()
 *   .newDocumentBuilder().newDocument();
 * dom.appendChild(dom.createElement("root"));
 * new Xembler(
 *   new Directives("XPATH 'root'; ADD 'employee';")
 * ).exec(dom);</pre>
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.1
 */
@Immutable
@ToString
@EqualsAndHashCode(callSuper = false, of = "array")
@Loggable(Loggable.DEBUG)
public final class Directives extends AbstractCollection<Directive> {

    /**
     * Array of directives.
     */
    private final transient Array<Directive> array;

    /**
     * Public ctor.
     * @param text Xembly script
     * @throws XemblySyntaxException If syntax is broken
     */
    public Directives(final String text) throws XemblySyntaxException {
        super();
        final CharStream input = new ANTLRStringStream(text);
        final XemblyLexer lexer = new XemblyLexer(input);
        final TokenStream tokens = new CommonTokenStream(lexer);
        final XemblyParser parser = new XemblyParser(tokens);
        try {
            this.array = new Array<Directive>(parser.directives());
        } catch (RecognitionException ex) {
            throw new XemblySyntaxException(text, ex);
        } catch (IllegalArgumentException ex) {
            throw new XemblySyntaxException(text, ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<Directive> iterator() {
        return this.array.iterator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        return this.array.size();
    }

}
