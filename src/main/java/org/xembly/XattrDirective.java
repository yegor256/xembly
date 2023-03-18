/*
 * Copyright (c) 2013-2022, xembly.org
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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import lombok.EqualsAndHashCode;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * XATTR directive.
 *
 * <p>The class is immutable and thread-safe.
 *
 * @since 0.28
 */
@EqualsAndHashCode(of = "expr")
final class XattrDirective implements Directive {

    /**
     * XPath factory.
     */
    private static final XPathFactory FACTORY = XPathFactory.newInstance();

    /**
     * Attribute name.
     */
    private final Arg name;

    /**
     * XPath to use.
     */
    private final Arg expr;

    /**
     * Public ctor.
     * @param attr Name of the attr
     * @param val Text value to set
     * @throws XmlContentException If invalid input
     */
    XattrDirective(final String attr, final String val) throws XmlContentException {
        this.name = new Arg(attr);
        this.expr = new Arg(val);
    }

    @Override
    public String toString() {
        return String.format("XATTR %s, %s", this.name, this.expr);
    }

    @Override
    public Cursor exec(final Node dom,
        final Cursor cursor, final Stack stack)
        throws ImpossibleModificationException {
        final ConcurrentMap<Node, String> values =
            new ConcurrentHashMap<>(0);
        final XPath xpath = XattrDirective.FACTORY.newXPath();
        for (final Node node : cursor) {
            try {
                values.put(
                    node,
                    xpath.evaluate(
                        this.expr.raw(), node, XPathConstants.STRING
                    ).toString()
                );
            } catch (final XPathExpressionException ex) {
                throw new ImpossibleModificationException(
                    String.format("Invalid XPath expr '%s'", this.expr), ex
                );
            }
        }
        for (final Map.Entry<Node, String> entry : values.entrySet()) {
            Element.class.cast(entry.getKey()).setAttribute(
                this.name.raw(), entry.getValue()
            );
        }
        return cursor;
    }

}
