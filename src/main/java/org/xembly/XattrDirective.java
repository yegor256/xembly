/*
 * SPDX-FileCopyrightText: Copyright (c) 2013-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
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
