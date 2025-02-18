/*
 * SPDX-FileCopyrightText: 2013-2025 Yegor Bugayenko <yegor256@gmail.com>
 * SPDX-License-Identifier: MIT
 */
package org.xembly;

import java.util.ArrayList;
import java.util.Collection;
import lombok.EqualsAndHashCode;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * ADDIF directive.
 *
 * <p>The class is immutable and thread-safe.
 *
 * @since 0.1
 */
@EqualsAndHashCode(of = "name")
final class AddIfDirective implements Directive {

    /**
     * Name of node to add.
     */
    private final Arg name;

    /**
     * Public ctor.
     * @param node Name of node to add
     * @throws XmlContentException If invalid input
     */
    AddIfDirective(final String node) throws XmlContentException {
        this.name = new Arg(node);
    }

    @Override
    public String toString() {
        return String.format("ADDIF %s", this.name);
    }

    @Override
    @SuppressWarnings("aibolit.P32")
    public Directive.Cursor exec(final Node dom,
        final Directive.Cursor cursor, final Directive.Stack stack) {
        final Collection<Node> targets = new ArrayList<>(cursor.size());
        final String label = this.name.raw();
        for (final Node node : cursor) {
            final NodeList kids = node.getChildNodes();
            Node target = null;
            final int len = kids.getLength();
            for (int idx = 0; idx < len; ++idx) {
                if (kids.item(idx).getNodeName()
                    .compareToIgnoreCase(label) == 0) {
                    target = kids.item(idx);
                    break;
                }
            }
            if (target == null) {
                final Document doc;
                if (dom.getOwnerDocument() == null) {
                    doc = Document.class.cast(dom);
                } else {
                    doc = dom.getOwnerDocument();
                }
                target = doc.createElement(this.name.raw());
                node.appendChild(target);
            }
            targets.add(target);
        }
        return new DomCursor(targets);
    }

}
