/*
 * SPDX-FileCopyrightText: Copyright (c) 2013-2025 Yegor Bugayenko <yegor256@gmail.com>
 * SPDX-License-Identifier: MIT
 */
package org.xembly;

import java.util.Collection;
import java.util.HashSet;
import lombok.EqualsAndHashCode;
import org.w3c.dom.Node;

/**
 * UP directive.
 *
 * <p>The class is immutable and thread-safe.
 *
 * @since 0.1
 */
@EqualsAndHashCode
final class UpDirective implements Directive {

    @Override
    public String toString() {
        return "UP";
    }

    @Override
    @SuppressWarnings("aibolit.P13")
    public Directive.Cursor exec(final Node dom,
        final Directive.Cursor cursor, final Directive.Stack stack)
        throws ImpossibleModificationException {
        final Collection<Node> parents = new HashSet<>(cursor.size());
        for (final Node node : cursor) {
            final Node parent = node.getParentNode();
            if (parent == null) {
                throw new ImpossibleModificationException(
                    String.format(
                        "There is no parent node of '%s' (%s), can't go UP",
                        node.getNodeName(), node.getNodeType()
                    )
                );
            }
            parents.add(parent);
        }
        return new DomCursor(parents);
    }

}
