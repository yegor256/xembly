/*
 * SPDX-FileCopyrightText: Copyright (c) 2013-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package org.xembly;

import java.util.ArrayList;
import java.util.Collection;
import lombok.EqualsAndHashCode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * ADD directive.
 *
 * <p>The class is immutable and thread-safe.
 *
 * @since 0.1
 */
@EqualsAndHashCode(of = "name")
final class AddDirective implements Directive {

    /**
     * Name of node to add.
     */
    private final Arg name;

    /**
     * Public ctor.
     * @param node Name of node to add
     * @throws XmlContentException If invalid input
     */
    AddDirective(final String node) throws XmlContentException {
        this.name = new Arg(node);
    }

    @Override
    public String toString() {
        return String.format("ADD %s", this.name);
    }

    @Override
    @SuppressWarnings("aibolit.P13")
    public Directive.Cursor exec(final Node dom,
        final Directive.Cursor cursor, final Directive.Stack stack) {
        final Collection<Node> targets = new ArrayList<>(cursor.size());
        final String label = this.name.raw();
        final Document doc;
        if (dom.getOwnerDocument() == null) {
            doc = Document.class.cast(dom);
        } else {
            doc = dom.getOwnerDocument();
        }
        for (final Node node : cursor) {
            final Element element = doc.createElement(label);
            node.appendChild(element);
            targets.add(element);
        }
        return new DomCursor(targets);
    }

}
