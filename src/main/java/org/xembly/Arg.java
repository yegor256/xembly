/*
 * SPDX-FileCopyrightText: Copyright (c) 2013-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package org.xembly;

import lombok.EqualsAndHashCode;

/**
 * Argument properly escaped.
 *
 * <p>The class is immutable and thread-safe.</p>
 *
 * @since 0.1
 */
@EqualsAndHashCode(of = "value")
final class Arg {

    /**
     * Value of it.
     */
    private final String value;

    /**
     * Public ctor.
     *
     * @param val Value of it
     * @throws XmlContentException If fails
     */
    Arg(final String val) throws XmlContentException {
        this(Arg.ifValid(val).toCharArray());
    }

    /**
     * Primary ctor.
     *
     * @param chars Validated characters
     */
    private Arg(final char... chars) {
        this.value = new String(chars);
    }

    @Override
    public String toString() {
        return String.format("\"%s\"", Arg.escape(this.value));
    }

    /**
     * Get it's raw value.
     *
     * @return Value
     */
    String raw() {
        return this.value;
    }

    /**
     * Un-escape all XML symbols.
     *
     * @param text XML text
     * @return Clean text
     * @throws XmlContentException If fails
     */
    @SuppressWarnings("aibolit.P32")
    static String unescape(final String text) throws XmlContentException {
        final char[] chars = text.toCharArray();
        if (chars.length < 2) {
            throw new IllegalArgumentException(
                "Internal error, argument can't be shorter than 2 chars"
            );
        }
        final int len = chars.length - 1;
        final StringBuilder output = new StringBuilder(text.length());
        int idx = 1;
        while (true) {
            if (idx >= len) {
                break;
            }
            if (chars[idx] == '&') {
                final StringBuilder sbuf = new StringBuilder(0);
                while (chars[idx] != ';') {
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
            ++idx;
        }
        return output.toString();
    }

    private static String escape(final String text) {
        final StringBuilder output = new StringBuilder(text.length());
        for (int idx = 0; idx < text.length(); ++idx) {
            final char chr = text.charAt(idx);
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

    private static String symbol(final String symbol) throws XmlContentException {
        final String chr;
        if ('#' == symbol.charAt(0)) {
            chr = String.valueOf(
                Character.toChars(Arg.legal(Arg.number(symbol.substring(1))))
            );
        } else if ("apos".equalsIgnoreCase(symbol)) {
            chr = "'";
        } else if ("quot".equalsIgnoreCase(symbol)) {
            chr = "\"";
        } else if ("lt".equalsIgnoreCase(symbol)) {
            chr = "<";
        } else if ("gt".equalsIgnoreCase(symbol)) {
            chr = ">";
        } else if ("amp".equalsIgnoreCase(symbol)) {
            chr = "&";
        } else {
            throw new XmlContentException(
                String.format("Unknown XML symbol &%s;", symbol)
            );
        }
        return chr;
    }

    private static int number(final String digits) throws XmlContentException {
        final int code;
        try {
            code = Integer.parseInt(digits);
        } catch (final NumberFormatException ex) {
            throw new XmlContentException(
                String.format("Invalid numeric character reference #%s", digits),
                ex
            );
        }
        return code;
    }

    private static int legal(final int code) throws XmlContentException {
        Arg.range(code, 0x00, 0x08);
        Arg.range(code, 0x0B, 0x0C);
        Arg.range(code, 0x0E, 0x1F);
        Arg.range(code, 0x7F, 0x84);
        Arg.range(code, 0x86, 0x9F);
        if (code < 0x00 || code > 0x10FFFF) {
            throw new XmlContentException(
                String.format(
                    "Character #%X is outside of the valid XML range #00-#10FFFF, see https://www.w3.org/TR/xml11/#charsets",
                    code
                )
            );
        }
        return code;
    }

    private static void range(final int code, final int left, final int right)
        throws XmlContentException {
        if (code >= left && code <= right) {
            throw new XmlContentException(
                String.format(
                    "Character #%02X is in the restricted XML range #%02X-#%02X, see https://www.w3.org/TR/xml11/#charsets",
                    code, left, right
                )
            );
        }
    }

    private static String ifValid(final String val) throws XmlContentException {
        for (int idx = 0; idx < val.length(); ++idx) {
            Arg.legal(val.charAt(idx));
        }
        return val;
    }
}
