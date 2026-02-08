/*
 * SPDX-FileCopyrightText: Copyright (c) 2013-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package org.xembly;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
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
        for (final Node node : cursor) {
            try {
                values.put(
                    node,
                    XsetDirective.FACTORY.newXPath().evaluate(
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
