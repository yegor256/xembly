/*
 * SPDX-FileCopyrightText: Copyright (c) 2013-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package org.xembly;

import lombok.EqualsAndHashCode;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * CDATA directive.
 *
 * <p>The class is immutable and thread-safe.
 *
 * @since 0.17
 */
@EqualsAndHashCode(of = "value")
final class CdataDirective implements Directive {

    /**
     * Text value to set.
     */
    private final Arg value;

    /**
     * Public ctor.
     * @param val Text value to set
     * @throws XmlContentException If invalid input
     */
    CdataDirective(final String val) throws XmlContentException {
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
        for (final Node node : cursor) {
            node.appendChild(doc.createCDATASection(this.value.raw()));
        }
        return cursor;
    }

}
