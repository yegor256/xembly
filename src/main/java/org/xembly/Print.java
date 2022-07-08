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

/**
 * It prints directives in one line.
 *
 * @since 0.25
 */
final class Print {

    /**
     * Right margin.
     */
    private static final int MARGIN = 80;

    /**
     * List of directives.
     */
    private final Iterable<Directive> all;

    /**
     * Public ctor.
     * @param dirs Directives
     */
    Print(final Iterable<Directive> dirs) {
        this.all = dirs;
    }

    @Override
    @SuppressWarnings("aibolit.P21")
    public String toString() {
        final StringBuilder text = new StringBuilder(0);
        int width = 0;
        int idx = 0;
        for (final Directive dir : this.all) {
            if (idx > 0 && width == 0) {
                text.append('\n').append(idx).append(':');
            }
            final String txt = dir.toString();
            text.append(txt).append(';');
            width += txt.length();
            if (width > Print.MARGIN) {
                width = 0;
            }
            ++idx;
        }
        return text.toString().trim();
    }

}
