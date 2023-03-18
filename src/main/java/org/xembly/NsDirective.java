/*
 * Copyright (c) 2013-2023, xembly.org
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
import org.w3c.dom.Node;

/**
 * Namespace directive.
 *
 * <p>The class is immutable and thread-safe.
 *
 * @since 0.19.3
 */
@EqualsAndHashCode(of = { "namespace" })
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
