/*
 * SPDX-FileCopyrightText: Copyright (c) 2013-2025 Yegor Bugayenko <yegor256@gmail.com>
 * SPDX-License-Identifier: MIT
 */
package org.xembly;

import lombok.EqualsAndHashCode;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * COMMENT directive.
 *
 * <p>The class is immutable and thread-safe.
 *
 * @since 0.23
 */
@EqualsAndHashCode(of = "value")
final class CommentDirective implements Directive {

    /**
     * Text value to set.
     */
    private final Arg value;

    /**
     * Public ctor.
     * @param val Text value to set
     * @throws XmlContentException If invalid input
     */
    CommentDirective(final String val) throws XmlContentException {
        this.value = new Arg(val);
    }

    @Override
    public String toString() {
        return String.format("CDATA %s", this.value);
    }

    @Override
    @SuppressWarnings("aibolit.P13")
    public Directive.Cursor exec(final Node dom,
        final Directive.Cursor cursor, final Directive.Stack stack) {
        final Document doc;
        if (dom.getOwnerDocument() == null) {
            doc = Document.class.cast(dom);
        } else {
            doc = dom.getOwnerDocument();
        }
        final String val = this.value.raw();
        for (final Node node : cursor) {
            final Node cdata = doc.createComment(val);
            node.appendChild(cdata);
        }
        return cursor;
    }

}
