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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import lombok.EqualsAndHashCode;
import org.w3c.dom.Node;

/**
 * XSET directive.
 *
 * <p>The class is immutable and thread-safe.
 *
 * @since 0.1
 */
@EqualsAndHashCode(of = "expr")
final class XsetDirective implements Directive {

    /**
     * XPath factory.
     */
    private static final XPathFactory FACTORY = XPathFactory.newInstance();

    /**
     * XPath to use.
     */
    private final Arg expr;

    /**
     * Public ctor.
     * @param val Text value to set
     * @throws XmlContentException If invalid input
     */
    XsetDirective(final String val) throws XmlContentException {
        this.expr = new Arg(val);
    }

    @Override
    public String toString() {
        return String.format("XSET %s", this.expr);
    }

    @Override
    public Directive.Cursor exec(final Node dom,
        final Directive.Cursor cursor, final Directive.Stack stack)
        throws ImpossibleModificationException {
        final ConcurrentMap<Node, String> values =
            new ConcurrentHashMap<>(0);
        final XPath xpath = XsetDirective.FACTORY.newXPath();
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
                    String.format("Invalid XPath expression '%s'", this.expr), ex
                );
            }
        }
        for (final Map.Entry<Node, String> entry : values.entrySet()) {
            entry.getKey().setTextContent(entry.getValue());
        }
        return cursor;
    }

}
