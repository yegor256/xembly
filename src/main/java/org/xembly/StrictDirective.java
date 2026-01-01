/*
 * SPDX-FileCopyrightText: Copyright (c) 2013-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package org.xembly;

import lombok.EqualsAndHashCode;
import org.w3c.dom.Node;

/**
 * STRICT directive.
 *
 * <p>The class is immutable and thread-safe.
 *
 * @since 0.3
 */
@EqualsAndHashCode(of = "number")
final class StrictDirective implements Directive {

    /**
     * Number of nodes we're expecting.
     */
    private final int number;

    /**
     * Public ctor.
     * @param nodes Number of node expected
     */
    StrictDirective(final int nodes) {
        this.number = nodes;
    }

    @Override
    public String toString() {
        return String.format("STRICT \"%d\"", this.number);
    }

    @Override
    public Directive.Cursor exec(final Node dom,
        final Directive.Cursor cursor, final Directive.Stack stack)
        throws ImpossibleModificationException {
        if (cursor.size() != this.number) {
            if (cursor.isEmpty()) {
                throw new ImpossibleModificationException(
                    String.format(
                        "No current nodes while %d expected", this.number
                    )
                );
            }
            if (cursor.size() == 1) {
                throw new ImpossibleModificationException(
                    String.format(
                        "One current node '%s' while strictly %d expected",
                        cursor.iterator().next().getNodeName(), this.number
                    )
                );
            }
            throw new ImpossibleModificationException(
                String.format(
                    "%d current nodes [%s] while strictly %d expected",
                    cursor.size(), StrictDirective.names(cursor), this.number
                )
            );
        }
        return cursor;
    }

    /**
     * Get node names as a string.
     * @param nodes Collection of nodes
     * @return Text presentation of them
     */
    @SuppressWarnings("aibolit.P13")
    private static String names(final Iterable<Node> nodes) {
        final StringBuilder text = new StringBuilder(0);
        for (final Node node : nodes) {
            if (text.length() > 0) {
                text.append(", ");
            }
            final Node parent = node.getParentNode();
            if (parent != null) {
                text.append(parent.getNodeName());
            }
            text.append('/').append(node.getNodeName());
        }
        return text.toString();
    }

}
