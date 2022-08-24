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

import lombok.EqualsAndHashCode;

/**
 * Argument properly escaped.
 *
 * <p>The class is immutable and thread-safe.
 *
 * @since 0.1
 */
@EqualsAndHashCode(of = "value")
final class Arg {

    /**
     * Value of it.
     */
    private final transient String value;

    /**
     * Public ctor.
     * @param val Value of it
     * @throws XmlContentException If fails
     */
    Arg(final String val) throws XmlContentException {
        this.value = Arg.ifValid(val);
    }

    @Override
    public String toString() {
        final String escaped = Arg.escape(this.value);
        return new StringBuilder(this.value.length() + 2 + escaped.length())
            .append('"').append(escaped).append('"').toString();
    }

    /**
     * Get it's raw value.
     * @return Value
     */
    public String raw() {
        return this.value;
    }

    /**
     * Un-escape all XML symbols.
     * @param text XML text
     * @return Clean text
     * @throws XmlContentException If fails
     */
    @SuppressWarnings({ "PMD.AvoidInstantiatingObjectsInLoops", "aibolit.P32" })
    public static String unescape(final String text)
        throws XmlContentException {
        final char[] chars = text.toCharArray();
        if (chars.length < 2) {
            throw new IllegalArgumentException(
                "Internal error, argument can't be shorter than 2 chars"
            );
        }
        final int len = chars.length - 1;
        final StringBuilder output = new StringBuilder(text.length());
        for (int idx = 1; idx < len; ++idx) {
            if (chars[idx] == '&') {
                final StringBuilder sbuf = new StringBuilder(0);
                while (chars[idx] != ';') {
                    // @checkstyle ModifiedControlVariable (1 line)
                    ++idx;
                    if (idx == chars.length) {
                        throw new XmlContentException(
                            "Reached EOF while parsing XML symbol"
                        );
                    }
                    sbuf.append(chars[idx]);
                }
                output.append(Arg.symbol(sbuf.substring(0, sbuf.length() - 1)));
            } else {
                output.append(chars[idx]);
            }
        }
        return output.toString();
    }

    /**
     * Escape all unprintable characters.
     * @param text Raw text
     * @return Clean text
     */
    private static String escape(final String text) {
        final StringBuilder output = new StringBuilder(text.length());
        for (final char chr : text.toCharArray()) {
            if (chr < ' ') {
                output.append("&#").append((int) chr).append(';');
            } else if (chr == '"') {
                output.append("&quot;");
            } else if (chr == '&') {
                output.append("&amp;");
            } else if (chr == '\'') {
                output.append("&apos;");
            } else if (chr == '<') {
                output.append("&lt;");
            } else if (chr == '>') {
                output.append("&gt;");
            } else {
                output.append(chr);
            }
        }
        return output.toString();
    }

    /**
     * Convert XML symbol to char.
     * @param symbol XML symbol, without leading ampersand
     * @return Character
     * @throws XmlContentException If fails
     */
    private static char symbol(final String symbol) throws XmlContentException {
        final char chr;
        if ('#' == symbol.charAt(0)) {
            final int num = Integer.parseInt(symbol.substring(1));
            chr = Arg.legal((char) num);
        } else if ("apos".equalsIgnoreCase(symbol)) {
            chr = '\'';
        } else if ("quot".equalsIgnoreCase(symbol)) {
            chr = '"';
        } else if ("lt".equalsIgnoreCase(symbol)) {
            chr = '<';
        } else if ("gt".equalsIgnoreCase(symbol)) {
            chr = '>';
        } else if ("amp".equalsIgnoreCase(symbol)) {
            chr = '&';
        } else {
            throw new XmlContentException(
                String.format("Unknown XML symbol &%s;", symbol)
            );
        }
        return chr;
    }

    /**
     * Validate char number and throw exception if it's not legal.
     * @param chr Char number
     * @return The same number
     * @throws XmlContentException If illegal
     */
    private static char legal(final char chr) throws XmlContentException {
        Arg.range(chr, 0x00, 0x08);
        Arg.range(chr, 0x0B, 0x0C);
        Arg.range(chr, 0x0E, 0x1F);
        Arg.range(chr, 0x7F, 0x84);
        Arg.range(chr, 0x86, 0x9F);
        return chr;
    }

    /**
     * Throw if number is in the range.
     * @param chr Char number
     * @param left Left number (inclusive)
     * @param right Right number (inclusive)
     * @throws XmlContentException If illegal
     */
    private static void range(final char chr, final int left, final int right)
        throws XmlContentException {
        if (chr >= left && chr <= right) {
            throw new XmlContentException(
                String.format(
                    "Character #%02X is in the restricted XML range #%02X-#%02X, see http://www.w3.org/TR/2004/REC-xml11-20040204/#charsets",
                    (int) chr, left, right
                )
            );
        }
    }

    /**
     * Check it for validity and return.
     * @param val The XML string
     * @return Itself
     * @throws XmlContentException If fails
     */
    private static String ifValid(final String val) throws XmlContentException {
        for (final char chr : val.toCharArray()) {
            Arg.legal(chr);
        }
        return val;
    }

}
