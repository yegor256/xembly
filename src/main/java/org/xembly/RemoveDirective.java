/*
 * SPDX-FileCopyrightText: 2013-2025 Yegor Bugayenko <yegor256@gmail.com>
 * SPDX-License-Identifier: MIT
 */
package org.xembly;

import java.util.Collection;
import java.util.HashSet;
import lombok.EqualsAndHashCode;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * REMOVE directive.
 *
 * <p>The class is immutable and thread-safe.
 *
 * @since 0.2
 */
@EqualsAndHashCode
final class RemoveDirective implements Directive {

    @Override
    public String toString() {
        return "REMOVE";
    }

    @Override
    public Directive.Cursor exec(final Node dom,
        final Directive.Cursor cursor, final Directive.Stack stack) {
        final Collection<Node> parents = new HashSet<>(cursor.size());
        for (final Node node : cursor) {
            parents.add(RemoveDirective.parent(node));
        }
        return new DomCursor(parents);
    }

    /**
     * Convert it to the parent.
     * @param node The node
     * @return Its parent
     */
    @SuppressWarnings("aibolit.P13")
    private static Node parent(final Node node) {
        final Node parent;
        if (node.getNodeType() == Node.ATTRIBUTE_NODE) {
            final Attr attr = Attr.class.cast(node);
            parent = attr.getOwnerElement();
            Element.class.cast(parent).removeAttributeNode(attr);
        } else {
            parent = node.getParentNode();
            if (parent == null) {
                throw new IllegalArgumentException(
                    "You can't delete root element from the XML"
                );
            }
            parent.removeChild(node);
        }
        return parent;
    }

}
