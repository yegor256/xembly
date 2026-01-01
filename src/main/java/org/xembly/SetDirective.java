/*
 * SPDX-FileCopyrightText: Copyright (c) 2013-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package org.xembly;

import lombok.EqualsAndHashCode;
import org.w3c.dom.Node;

/**
 * SET directive.
 *
 * <p>The class is immutable and thread-safe.
 *
 * @since 0.1
 */
@EqualsAndHashCode(of = "value")
final class SetDirective implements Directive {

    /**
     * Text value to set.
     */
    private final Arg value;

    /**
     * Public ctor.
     * @param val Text value to set
     * @throws XmlContentException If invalid input
     */
    SetDirective(final String val) throws XmlContentException {
        this.value = new Arg(val);
    }

    @Override
    public String toString() {
        return String.format("SET %s", this.value);
    }

    @Override
    public Directive.Cursor exec(final Node dom,
        final Directive.Cursor cursor, final Directive.Stack stack) {
        final String val = this.value.raw();
        for (final Node node : cursor) {
            node.setTextContent(val);
        }
        return cursor;
    }

}
