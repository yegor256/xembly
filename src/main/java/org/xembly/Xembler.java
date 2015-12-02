/**
 * Copyright (c) 2013-2015, xembly.org
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

import java.io.StringWriter;
import java.util.Collections;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
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
 * and {@link #applyQuietly(org.w3c.dom.Node)}.
 *
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id$
 * @since 0.1
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
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
     * Transformer factory.
     */
    private static final TransformerFactory TFACTORY =
        TransformerFactory.newInstance();

    /**
     * Array of directives.
     */
    private final transient Iterable<Directive> directives;

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
        this.directives = dirs;
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
                    "failed to apply to DOM quietly: %s",
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
                    String.format("directive #%d: %s", pos, dir),
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
                    "failed to create DOM quietly: %s",
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
                    "failed to obtain a new DOM document from %s",
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
                    "failed to build XML quietly: %s",
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
        final Transformer transformer;
        try {
            transformer = Xembler.TFACTORY.newTransformer();
        } catch (final TransformerConfigurationException ex) {
            throw new IllegalStateException(
                String.format(
                    "failed to create new Transformer at %s",
                    Xembler.TFACTORY.getClass().getCanonicalName()
                ),
                ex
            );
        }
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        final StringWriter writer = new StringWriter();
        try {
            transformer.transform(
                new DOMSource(this.dom()),
                new StreamResult(writer)
            );
        } catch (final TransformerException ex) {
            throw new IllegalArgumentException(
                String.format(
                    "failed to transform DOM to text by %s",
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
     * @checkstyle MagicNumber (20 lines)
     * @checkstyle CyclomaticComplexity (20 lines)
     * @checkstyle BooleanExpressionComplexity (20 lines)
     */
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
