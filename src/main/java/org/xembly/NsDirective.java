/*
 * SPDX-FileCopyrightText: Copyright (c) 2013-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package org.xembly;

import lombok.EqualsAndHashCode;
import org.w3c.dom.Node;

/**
 * Namespace directive.
 *
 * <p>The class is immutable and thread-safe.
 *
 * @since 0.19.3
 */
@EqualsAndHashCode(of = "namespace")
final class NsDirective implements Directive {
    /**
     * Namespace, which should be added to a node.
     */
    private final Arg namespace;

    /**
     * Creates an instance of NsDirective.
     * @param nsp Namespace, which should be added to a node.
     */
    NsDirective(final Arg nsp) {
        this.namespace = nsp;
    }

    @Override
    public String toString() {
        return String.format(
            "NS %s",
            this.namespace
        );
    }

    @Override
    public Directive.Cursor exec(final Node dom, final Directive.Cursor cursor,
        final Directive.Stack stack) {
        try {
            final AttrDirective attr = new AttrDirective(
                "xmlns",
                this.namespace.raw()
            );
            return attr.exec(dom, cursor, stack);
        } catch (final XmlContentException exception) {
            throw new IllegalArgumentException(exception);
        }
    }
}
