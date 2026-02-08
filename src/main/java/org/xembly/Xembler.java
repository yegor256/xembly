/*
 * SPDX-FileCopyrightText: Copyright (c) 2013-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package org.xembly;

import java.io.StringWriter;
import java.util.Collections;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Processor of Xembly directives, main entry point to the package.
 *
 * <p>For example, to modify a DOM document:
 *
 * <pre> Document dom = DocumentBuilderFactory.newInstance()
 *   .newDocumentBuilder().newDocument();
 * dom.appendChild(dom.createElement("root"));
 * new Xembler(
 *   new Directives()
 *     .xpath("/root")
 *     .addIfAbsent("employees")
 *     .add("employee")
 *     .attr("id", 6564)
 * ).apply(dom);</pre>
 *
 * <p>You can also convert your Xembly directives directly to XML document:
 *
 * <pre> String xml = new Xembler(
 *   new Directives()
 *     .xpath("/root")
 *     .addIfAbsent("employees")
 *     .add("employee")
 *     .attr("id", 6564)
 * ).xml("root");</pre>
 *
 * <p>Since version 0.18 you can convert directives to XML without
 * a necessity to catch checked exceptions.
 * Use {@code *Quietly()} methods for that:
 * {@link #xmlQuietly()}, {@link #domQuietly()},
 * and {@link #applyQuietly(Node)}.
 *
 * @since 0.1
 */
@ToString
@EqualsAndHashCode(of = "directives")
public final class Xembler {

    /**
     * Builder factory.
     */
    private static final DocumentBuilderFactory BFACTORY =
        DocumentBuilderFactory.newInstance();

    /**
     * Array of directives.
     */
    private final Iterable<Directive> directives;

    /**
     * Transformers factory.
     */
    private final Transformers transformers;

    static {
        Xembler.BFACTORY.setNamespaceAware(true);
        Xembler.BFACTORY.setValidating(false);
        Xembler.BFACTORY.setCoalescing(false);
    }

    /**
     * Public ctor.
     * @param dirs Directives
     */
    public Xembler(final Iterable<Directive> dirs) {
        this(dirs, new Transformers.Document());
    }

    /**
     * Public ctor.
     * @param directives Directives
     * @param transformers Transformers
     */
    public Xembler(final Iterable<Directive> directives, final Transformers transformers) {
        this.directives = directives;
        this.transformers = transformers;
    }

    /**
     * Apply all changes to the document/node, without any checked exceptions.
     * @param dom DOM document/node
     * @return The same document/node
     * @since 0.18
     */
    public Node applyQuietly(final Node dom) {
        try {
            return this.apply(dom);
        } catch (final ImpossibleModificationException ex) {
            throw new IllegalArgumentException(
                String.format(
                    "Failed to apply to DOM quietly: %s",
                    this.directives
                ),
                ex
            );
        }
    }

    /**
     * Apply all changes to the document/node.
     * @param dom DOM document/node
     * @return The same document/node
     * @throws ImpossibleModificationException If can't modify
     */
    @SuppressWarnings({"aibolit.P15", "PMD.UnnecessaryLocalRule"})
    public Node apply(final Node dom) throws ImpossibleModificationException {
        Directive.Cursor cursor = new DomCursor(
            Collections.singletonList(dom)
        );
        int pos = 1;
        final Directive.Stack stack = new DomStack();
        for (final Directive dir : this.directives) {
            try {
                cursor = dir.exec(dom, cursor, stack);
            } catch (final ImpossibleModificationException ex) {
                throw new ImpossibleModificationException(
                    String.format("Directive #%d: %s", pos, dir),
                    ex
                );
            } catch (final DOMException ex) {
                throw new ImpossibleModificationException(
                    String.format("DOM exception at dir #%d: %s", pos, dir),
                    ex
                );
            }
            ++pos;
        }
        return dom;
    }

    /**
     * Apply all changes to an empty DOM, without checked exceptions.
     * @return DOM created
     * @since 0.18
     */
    public Document domQuietly() {
        try {
            return this.dom();
        } catch (final ImpossibleModificationException ex) {
            throw new IllegalStateException(
                String.format(
                    "Failed to create DOM quietly: %s",
                    this.directives
                ),
                ex
            );
        }
    }

    /**
     * Apply all changes to an empty DOM.
     * @return DOM created
     * @throws ImpossibleModificationException If can't modify
     * @since 0.9
     */
    public Document dom() throws ImpossibleModificationException {
        final Document dom;
        try {
            dom = Xembler.BFACTORY.newDocumentBuilder().newDocument();
        } catch (final ParserConfigurationException ex) {
            throw new IllegalStateException(
                String.format(
                    "Failed to obtain a new DOM document from %s",
                    Xembler.BFACTORY.getClass().getCanonicalName()
                ),
                ex
            );
        }
        this.apply(dom);
        return dom;
    }

    /**
     * Convert to XML document, without checked exceptions.
     * @return XML document
     * @since 0.18
     */
    public String xmlQuietly() {
        try {
            return this.xml();
        } catch (final ImpossibleModificationException ex) {
            throw new IllegalStateException(
                String.format(
                    "Failed to build XML quietly: %s",
                    this.directives
                ),
                ex
            );
        }
    }

    /**
     * Convert to XML document.
     * @return XML document
     * @throws ImpossibleModificationException If can't modify
     * @since 0.9
     */
    public String xml() throws ImpossibleModificationException {
        final Transformer transformer = this.transformers.create();
        final StringWriter writer = new StringWriter();
        try {
            transformer.transform(
                new DOMSource(this.dom()),
                new StreamResult(writer)
            );
        } catch (final TransformerException ex) {
            throw new IllegalArgumentException(
                String.format(
                    "Failed to transform DOM to text by %s",
                    transformer.getClass().getCanonicalName()
                ),
                ex
            );
        }
        return writer.toString();
    }

    /**
     * Utility method to escape text before using it as a text value
     * in XML.
     *
     * <p>Use it like this, in order to avoid runtime exceptions:
     *
     * <pre>new Directives().xpath("/test")
     *   .set(Xembler.escape("illegal: \u0000"));</pre>
     *
     * @param text Text to escape
     * @return The same text with escaped characters, which are not XML-legal
     * @since 0.14
     * @checkstyle CyclomaticComplexity (20 lines)
     * @checkstyle BooleanExpressionComplexity (20 lines)
     */
    @SuppressWarnings("PMD.ProhibitPublicStaticMethods")
    public static String escape(final String text) {
        final StringBuilder output = new StringBuilder(text.length());
        final char[] chars = text.toCharArray();
        for (final char chr : chars) {
            final boolean illegal = chr >= 0x00 && chr <= 0x08
                || chr >= 0x0B && chr <= 0x0C
                || chr >= 0x0E && chr <= 0x1F
                || chr >= 0x7F && chr <= 0x84
                || chr >= 0x86 && chr <= 0x9F;
            if (illegal) {
                output.append(String.format("\\u%04x", (int) chr));
            } else {
                output.append(chr);
            }
        }
        return output.toString();
    }

}
