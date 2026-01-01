/*
 * SPDX-FileCopyrightText: Copyright (c) 2013-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package org.xembly;

import java.util.Locale;
import lombok.EqualsAndHashCode;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * PI directive.
 *
 * <p>The class is immutable and thread-safe.
 *
 * @since 0.9
 */
@EqualsAndHashCode(of = { "target", "data" })
final class PiDirective implements Directive {

    /**
     * Target name.
     */
    private final Arg target;

    /**
     * Data.
     */
    private final Arg data;

    /**
     * Public ctor.
     * @param tgt Target
     * @param dat Data
     * @throws XmlContentException If invalid input
     */
    PiDirective(final String tgt, final String dat)
        throws XmlContentException {
        this.target = new Arg(tgt.toLowerCase(Locale.ENGLISH));
        this.data = new Arg(dat);
    }

    @Override
    public String toString() {
        return String.format("PI %s, %s", this.target, this.data);
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
        final Node instr = doc.createProcessingInstruction(
            this.target.raw(), this.data.raw()
        );
        if (cursor.isEmpty()) {
            dom.insertBefore(instr, doc.getDocumentElement());
        } else {
            for (final Node node : cursor) {
                node.appendChild(instr);
            }
        }
        return cursor;
    }

}
