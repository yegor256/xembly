/*
 * SPDX-FileCopyrightText: Copyright (c) 2013-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package org.xembly;

import lombok.EqualsAndHashCode;
import org.w3c.dom.Node;

/**
 * PUSH directive.
 *
 * <p>The class is immutable and thread-safe.
 *
 * @since 0.16
 */
@EqualsAndHashCode
final class PushDirective implements Directive {

    @Override
    public String toString() {
        return "PUSH";
    }

    @Override
    public Directive.Cursor exec(final Node dom,
        final Directive.Cursor cursor, final Directive.Stack stack)
        throws ImpossibleModificationException {
        stack.push(cursor);
        return cursor;
    }

}
