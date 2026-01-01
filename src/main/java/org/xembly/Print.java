/*
 * SPDX-FileCopyrightText: Copyright (c) 2013-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
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
