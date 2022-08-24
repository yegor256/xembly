/*
 * Copyright (c) 2013-2022, xembly.org
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

import java.util.Collection;
import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

/**
 * Directives in plain text.
 *
 * @since 0.24
 */
final class Verbs {

    /**
     * Errors listener.
     */
    private static final ANTLRErrorListener ERRORS = new BaseErrorListener() {
        // @checkstyle ParameterNumberCheck (10 lines)
        @Override
        public void syntaxError(final Recognizer<?, ?> recognizer,
            final Object symbol, final int line,
            final int position, final String msg,
            final RecognitionException error) {
            throw new SyntaxException(
                String.format(
                    "\"%s\" at line #%d, position #%d, symbol %s",
                    msg, line, position, symbol
                ),
                error
            );
        }
    };

    /**
     * The text.
     */
    private final String text;

    /**
     * Public ctor.
     * @param txt The script
     */
    Verbs(final String txt) {
        this.text = txt;
    }

    /**
     * Parse script.
     * @return Collection of directives
     */
    public Collection<Directive> directives() {
        final XemblyLexer lexer = new XemblyLexer(
            CharStreams.fromString(this.text)
        );
        final XemblyParser parser =
            new XemblyParser(
                new CommonTokenStream(lexer)
            );
        lexer.removeErrorListeners();
        lexer.addErrorListener(Verbs.ERRORS);
        parser.removeErrorListeners();
        parser.addErrorListener(Verbs.ERRORS);
        try {
            return parser.directives().ret;
        } catch (final ParsingException ex) {
            throw new SyntaxException(
                String.format(
                    "Parsing failed as %s: \"%s\"",
                    ex.getClass().getCanonicalName(),
                    ex.getLocalizedMessage()
                ),
                ex
            );
        }
    }

}
