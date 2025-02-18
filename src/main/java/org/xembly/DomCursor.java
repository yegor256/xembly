/*
 * SPDX-FileCopyrightText: Copyright (c) 2013-2025 Yegor Bugayenko <yegor256@gmail.com>
 * SPDX-License-Identifier: MIT
 */
package org.xembly;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import lombok.EqualsAndHashCode;
import org.w3c.dom.Node;

/**
 * Cursor at DOM.
 *
 * <p>The class is immutable and thread-safe.
 *
 * @since 0.16
 */
@EqualsAndHashCode(callSuper = false, of = "nodes")
final class DomCursor extends
    AbstractCollection<Node> implements Directive.Cursor {

    /**
     * Nodes.
     */
    private final Collection<Node> nodes;

    /**
     * Public ctor.
     * @param nds Nodes to encapsulate
     */
    DomCursor(final Collection<Node> nds) {
        super();
        this.nodes = Collections.unmodifiableCollection(nds);
    }

    @Override
    public Iterator<Node> iterator() {
        return this.nodes.iterator();
    }

    @Override
    public int size() {
        return this.nodes.size();
    }
}
