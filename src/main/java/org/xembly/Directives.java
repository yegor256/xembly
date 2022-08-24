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

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import lombok.EqualsAndHashCode;
import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Collection of {@link Directive}s, instantiable from {@link String}.
 *
 * <p>For example, to fetch directives from a string and apply to the
 * DOM document:
 *
 * <pre> Document dom = DocumentBuilderFactory.newInstance()
 *   .newDocumentBuilder().newDocument();
 * dom.appendChild(dom.createElement("root"));
 * new Xembler(
 *   new Directives("XPATH 'root'; ADD 'employee';")
 * ).apply(dom);</pre>
 *
 * <p>{@link Directives} can be used as a builder of Xembly script:
 *
 * <pre> Document dom = DocumentBuilderFactory.newInstance()
 *   .newDocumentBuilder().newDocument();
 * dom.appendChild(dom.createElement("root"));
 * new Xembler(
 *   new Directives()
 *     .xpath("/root")
 *     .addIf("employees")
 *     .add("employee")
 *     .attr("id", 6564)
 *     .up()
 *     .xpath("employee[&#64;id='100']")
 *     .strict(1)
 *     .remove()
 * ).apply(dom);</pre>
 *
 * <p>The class is mutable and thread-safe.
 *
 * @since 0.1
 * @checkstyle ClassFanOutComplexity (500 lines)
 */
@EqualsAndHashCode(callSuper = false, of = "all")
@SuppressWarnings
    (
        {
            "PMD.TooManyMethods",
            "PMD.CyclomaticComplexity",
            "PMD.GodClass",
            "PMD.StdCyclomaticComplexity"
        }
    )
public final class Directives implements Iterable<Directive> {

    /**
     * List of directives.
     */
    private final transient Collection<Directive> all;

    /**
     * Public ctor.
     */
    public Directives() {
        this(Collections.emptyList());
    }

    /**
     * Public ctor.
     * @param text Xembly script
     */
    public Directives(final String text) {
        this(new Verbs(text).directives());
    }

    /**
     * Public ctor.
     * @param dirs Directives
     */
    @SuppressWarnings("PMD.ConstructorOnlyInitializesOrCallOtherConstructors")
    public Directives(final Iterable<Directive> dirs) {
        this.all = Directives.toCollection(dirs);
    }

    @Override
    public String toString() {
        return new Print(this.all).toString();
    }

    @Override
    public Iterator<Directive> iterator() {
        return this.all.iterator();
    }

    /**
     * Create a collection of directives, which can create a copy
     * of provided node.
     *
     * <p>For example, you already have a node in an XML document,
     * which you'd like to add to another XML document:
     *
     * <pre> Document target = parse("&lt;root/&gt;");
     * Node node = parse("&lt;user name='Jeffrey'/&gt;");
     * new Xembler(
     *   new Directives()
     *     .xpath("/*")
     *     .add("jeff")
     *     .append(Directives.copyOf(node))
     * ).apply(target);
     * assert print(target).equals(
     *   "&lt;root&gt;&lt;jeff name='Jeffrey'&gt;&lt;/root&gt;"
     * );
     * </pre>
     *
     * @param node Node to analyze
     * @return Collection of directives
     * @since 0.13
     * @checkstyle CyclomaticComplexity (50 lines)
     */
    @SuppressWarnings(
        {
            "PMD.StdCyclomaticComplexity",
            "PMD.InefficientEmptyStringCheck",
            "aibolit.P20_5"
        }
    )
    public static Iterable<Directive> copyOf(final Node node) {
        final Directives dirs = new Directives();
        if (node.hasAttributes()) {
            final NamedNodeMap attrs = node.getAttributes();
            final int len = attrs.getLength();
            for (int idx = 0; idx < len; ++idx) {
                final Attr attr = Attr.class.cast(attrs.item(idx));
                dirs.attr(attr.getNodeName(), attr.getNodeValue());
            }
        }
        if (node.hasChildNodes()) {
            final NodeList children = node.getChildNodes();
            final int len = children.getLength();
            for (int idx = 0; idx < len; ++idx) {
                final Node child = children.item(idx);
                switch (child.getNodeType()) {
                    case Node.ELEMENT_NODE:
                        dirs.add(child.getNodeName())
                            .append(Directives.copyOf(child))
                            .up();
                        break;
                    case Node.ATTRIBUTE_NODE:
                        dirs.attr(child.getNodeName(), child.getNodeValue());
                        break;
                    case Node.TEXT_NODE:
                    case Node.CDATA_SECTION_NODE:
                        if (len == 1) {
                            dirs.set(child.getTextContent());
                        } else if (!child.getTextContent().trim().isEmpty()) {
                            throw new IllegalArgumentException(
                                String.format(
                                    "TEXT node #%d is not allowed together with other %d nodes in %s",
                                    idx, len, child.getNodeName()
                                )
                            );
                        }
                        break;
                    case Node.PROCESSING_INSTRUCTION_NODE:
                        dirs.pi(child.getNodeName(), child.getNodeValue());
                        break;
                    case Node.ENTITY_NODE:
                    case Node.COMMENT_NODE:
                        break;
                    default:
                        throw new IllegalArgumentException(
                            String.format(
                                "Unsupported type %d of node %s",
                                child.getNodeType(), child.getNodeName()
                            )
                        );
                }
            }
        }
        return dirs;
    }

    /**
     * Append all directives.
     * @param dirs Directives to append
     * @return This object
     * @since 0.11
     */
    public Directives append(final Iterable<Directive> dirs) {
        synchronized (this.all) {
            this.all.addAll(Directives.toCollection(dirs));
        }
        return this;
    }

    /**
     * Appends the {@link Node node}.
     * @param node The node to append
     * @return This object
     * @see #append(Iterable)
     * @see Directives#copyOf(Node)
     * @since 0.23
     */
    public Directives append(final Node node) {
        return this.append(Directives.copyOf(node));
    }

    /**
     * Add node to all current nodes.
     * @param name Name of the node to add
     * @return This object
     * @since 0.5
     */
    public Directives add(final Object name) {
        try {
            this.all.add(new AddDirective(name.toString()));
        } catch (final XmlContentException ex) {
            throw new IllegalArgumentException(
                String.format(
                    "Failed to understand XML content, ADD(%s)",
                    name
                ),
                ex
            );
        }
        return this;
    }

    /**
     * Add multiple nodes and set their text values.
     *
     * <p>Every pair in the provided map will be treated as a new
     * node name and value. It's a convenient utility method that simplifies
     * the process of adding a collection of nodes with pre-set values. For
     * example:
     *
     * <pre> new Directives()
     *   .add("first", "hello, world!")
     *   .add(
     *     new ArrayMap&lt;String, Object&gt;()
     *       .with("alpha", 1)
     *       .with("beta", "2")
     *       .with("gamma", new Date())
     *   )
     *   .add("second");
     * </pre>
     *
     * <p>If a value provided contains illegal XML characters, a runtime
     * exception will be thrown. To avoid this, it is recommended to use
     * {@link Xembler#escape(String)}.
     *
     * @param nodes Names and values of nodes to add
     * @param <K> Type of key
     * @param <V> Type of value
     * @return This object
     * @since 0.8
     */
    public <K, V> Directives add(final Map<K, V> nodes) {
        for (final Map.Entry<K, V> entry : nodes.entrySet()) {
            this.add(entry.getKey().toString())
                .set(entry.getValue().toString())
                .up();
        }
        return this;
    }

    /**
     * Add node if it's absent.
     * @param name Name of the node to add
     * @return This object
     * @since 0.5
     */
    public Directives addIf(final Object name) {
        try {
            this.all.add(new AddIfDirective(name.toString()));
        } catch (final XmlContentException ex) {
            throw new IllegalArgumentException(
                String.format(
                    "Failed to understand XML content, ADDIF(%s)",
                    name
                ),
                ex
            );
        }
        return this;
    }

    /**
     * Remove all current nodes and move cursor to their parents.
     * @return This object
     * @since 0.5
     */
    public Directives remove() {
        this.all.add(new RemoveDirective());
        return this;
    }

    /**
     * Set attribute.
     *
     * <p>If a value provided contains illegal XML characters, a runtime
     * exception will be thrown. To avoid this, it is recommended to use
     * {@link Xembler#escape(String)}.
     *
     * @param name Name of the attribute
     * @param value Value to set
     * @return This object
     * @since 0.5
     */
    public Directives attr(final Object name, final Object value) {
        try {
            this.all.add(new AttrDirective(name.toString(), value.toString()));
        } catch (final XmlContentException ex) {
            throw new IllegalArgumentException(
                String.format(
                    "Failed to understand XML content, ATTR(%s, %s)",
                    name, value
                ),
                ex
            );
        }
        return this;
    }

    /**
     * Add processing instruction.
     *
     * <p>If a value provided contains illegal XML characters, a runtime
     * exception will be thrown. To avoid this, it is recommended to use
     * {@link Xembler#escape(String)}.
     *
     * @param target PI name
     * @param data Data to set
     * @return This object
     * @since 0.9
     * @checkstyle MethodName (3 lines)
     */
    @SuppressWarnings("PMD.ShortMethodName")
    public Directives pi(final Object target, final Object data) {
        try {
            this.all.add(new PiDirective(target.toString(), data.toString()));
        } catch (final XmlContentException ex) {
            throw new IllegalArgumentException(
                String.format(
                    "Failed to understand XML content, PI(%s, %s)",
                    target, data
                ),
                ex
            );
        }
        return this;
    }

    /**
     * Set text content.
     *
     * <p>If a value provided contains illegal XML characters, a runtime
     * exception will be thrown. To avoid this, it is recommended to use
     * {@link Xembler#escape(String)}.
     *
     * @param text Text to set
     * @return This object
     * @since 0.5
     */
    public Directives set(final Object text) {
        try {
            this.all.add(new SetDirective(text.toString()));
        } catch (final XmlContentException ex) {
            throw new IllegalArgumentException(
                String.format(
                    "Failed to understand XML content, SET(%s)",
                    text
                ),
                ex
            );
        }
        return this;
    }

    /**
     * Set text content.
     * @param text Text to set
     * @return This object
     * @since 0.7
     */
    public Directives xset(final Object text) {
        try {
            this.all.add(new XsetDirective(text.toString()));
        } catch (final XmlContentException ex) {
            throw new IllegalArgumentException(
                String.format(
                    "Failed to understand XML content, XSET(%s)",
                    text
                ),
                ex
            );
        }
        return this;
    }

    /**
     * Set attribute.
     * @param attr Attribute name
     * @param text Text to set
     * @return This object
     * @since 0.28
     */
    public Directives xattr(final Object attr, final Object text) {
        try {
            this.all.add(new XattrDirective(attr.toString(), text.toString()));
        } catch (final XmlContentException ex) {
            throw new IllegalArgumentException(
                String.format(
                    "Failed to understand XML content, XATTR(%s, %s)",
                    attr, text
                ),
                ex
            );
        }
        return this;
    }

    /**
     * Go one node/level up.
     * @return This object
     * @since 0.5
     * @checkstyle MethodName (3 lines)
     */
    @SuppressWarnings("PMD.ShortMethodName")
    public Directives up() {
        this.all.add(new UpDirective());
        return this;
    }

    /**
     * Go to XPath.
     * @param path Path to go to
     * @return This object
     * @since 0.5
     */
    public Directives xpath(final Object path) {
        try {
            this.all.add(new XpathDirective(path.toString()));
        } catch (final XmlContentException ex) {
            throw new IllegalArgumentException(
                String.format(
                    "Failed to understand XML content, XPATH(%s)",
                    path
                ),
                ex
            );
        }
        return this;
    }

    /**
     * Check that there is exactly this number of current nodes.
     * @param number Number of expected nodes
     * @return This object
     * @since 0.5
     */
    public Directives strict(final int number) {
        this.all.add(new StrictDirective(number));
        return this;
    }

    /**
     * Push current cursor to stack.
     * @return This object
     * @since 0.16
     */
    public Directives push() {
        this.all.add(new PushDirective());
        return this;
    }

    /**
     * Pop cursor to stack and replace current cursor with it.
     * @return This object
     * @since 0.16
     */
    public Directives pop() {
        this.all.add(new PopDirective());
        return this;
    }

    /**
     * Set CDATA section.
     *
     * <p>If a value provided contains illegal XML characters, a runtime
     * exception will be thrown. To avoid this, it is recommended to use
     * {@link Xembler#escape(String)}.
     *
     * @param text Text to set
     * @return This object
     * @since 0.17
     */
    public Directives cdata(final Object text) {
        try {
            this.all.add(new CdataDirective(text.toString()));
        } catch (final XmlContentException ex) {
            throw new IllegalArgumentException(
                String.format(
                    "Failed to understand XML content, CDATA(%s)",
                    text
                ),
                ex
            );
        }
        return this;
    }

    /**
     * Add an XML comment.
     *
     * <p>If a value provided contains illegal XML characters, a runtime
     * exception will be thrown. To avoid this, it is recommended to use
     * {@link Xembler#escape(String)}.
     *
     * @param text Text to set
     * @return This object
     * @since 0.23
     */
    public Directives comment(final Object text) {
        try {
            this.all.add(new CommentDirective(text.toString()));
        } catch (final XmlContentException ex) {
            throw new IllegalArgumentException(
                String.format(
                    "Failed to understand XML content, comment(%s)",
                    text
                ),
                ex
            );
        }
        return this;
    }

    /**
     * Iterable to collection.
     * @param itr Iterable
     * @param <T> The type
     * @return Collection
     */
    private static <T> Collection<T> toCollection(final Iterable<T> itr) {
        final Collection<T> col = new LinkedList<>();
        for (final T item : itr) {
            col.add(item);
        }
        return col;
    }

}
