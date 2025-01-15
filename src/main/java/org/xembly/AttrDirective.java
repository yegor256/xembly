/*
 * Copyright (c) 2013-2025 Yegor Bugayenko
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the xembly.org nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
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
