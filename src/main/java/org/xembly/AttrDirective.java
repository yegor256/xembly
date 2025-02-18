/*
 * SPDX-FileCopyrightText: Copyright (c) 2013-2025 Yegor Bugayenko <yegor256@gmail.com>
 * SPDX-License-Identifier: MIT
 */
package org.xembly;

import lombok.EqualsAndHashCode;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * ATTR directive.
 *
 * <p>The class is immutable and thread-safe.
 *
 * @since 0.1
 */
@EqualsAndHashCode(of = { "name", "value" })
final class AttrDirective implements Directive {

    /**
     * Attribute name.
     */
    private final Arg name;

    /**
     * Text value to set.
     */
    private final Arg value;

    /**
     * Public ctor.
     * @param attr Attribute name
     * @param val Text value to set
     * @throws XmlContentException If invalid input
     */
    AttrDirective(final String attr, final String val)
        throws XmlContentException {
        this.name = new Arg(attr);
        this.value = new Arg(val);
    }

    @Override
    public String toString() {
        return String.format("ATTR %s, %s", this.name, this.value);
    }

    @Override
    public Directive.Cursor exec(final Node dom,
        final Directive.Cursor cursor, final Directive.Stack stack) {
        final String key = this.name.raw();
        final String val = this.value.raw();
        final String[] parts = key.split(" ");
        for (final Node node : cursor) {
            if (parts.length == 3) {
                Element.class.cast(node).setAttributeNS(
                    parts[2], String.format("%s:%s", parts[1], parts[0]), val
                );
            } else {
                Element.class.cast(node).setAttribute(key, val);
            }
        }
        return cursor;
    }

}
