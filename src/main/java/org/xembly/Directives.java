/**
 * Copyright (c) 2013, xembly.org
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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.TokenStream;

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
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.1
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@EqualsAndHashCode(callSuper = false, of = "all")
@SuppressWarnings("PMD.TooManyMethods")
public final class Directives implements Iterable<Directive> {

    /**
     * Right margin.
     */
    private static final int MARGIN = 80;

    /**
     * List of directives.
     */
    private final transient Collection<Directive> all =
        new CopyOnWriteArrayList<Directive>();

    /**
     * Public ctor.
     */
    public Directives() {
        this(Collections.<Directive>emptyList());
    }

    /**
     * Public ctor.
     * @param text Xembly script
     * @throws SyntaxException If syntax is broken
     */
    public Directives(@NotNull(message = "xembly script can't be NULL")
        final String text) throws SyntaxException {
        this(Directives.parse(text));
    }

    /**
     * Public ctor.
     * @param dirs Directives
     */
    public Directives(@NotNull(message = "directives can't be NULL")
        final Iterable<Directive> dirs) {
        this.append(dirs);
    }

    @Override
    public String toString() {
        final StringBuilder text = new StringBuilder(0);
        int width = 0;
        for (final Directive dir : this.all) {
            final String txt = dir.toString();
            text.append(txt).append(';');
            width += txt.length();
            if (width > Directives.MARGIN) {
                text.append('\n');
                width = 0;
            }
        }
        return text.toString().trim();
    }

    @Override
    public Iterator<Directive> iterator() {
        return this.all.iterator();
    }

    /**
     * Append all directives.
     * @param dirs Directives to append
     * @return This object
     * @since 0.11
     */
    public Directives append(
        @NotNull(message = "list of directives can't be NULL")
        final Iterable<Directive> dirs) {
        for (final Directive dir : dirs) {
            this.all.add(dir);
        }
        return this;
    }

    /**
     * Add node to all current nodes.
     * @param name Name of the node to add
     * @return This object
     * @since 0.5
     */
    public Directives add(
        @NotNull(message = "name can't be NULL") final String name) {
        try {
            this.all.add(new AddDirective(name));
        } catch (XmlContentException ex) {
            throw new IllegalArgumentException(ex);
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
     * @param <K> Type of key
     * @param <V> Type of value
     * @param nodes Names and values of nodes to add
     * @return This object
     * @since 0.8
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public <K, V> Directives add(
        @NotNull(message = "map can't be NULL") final Map<K, V> nodes) {
        try {
            for (final Map.Entry<K, V> entry : nodes.entrySet()) {
                this.all.addAll(
                    Arrays.asList(
                        new AddDirective(entry.getKey().toString()),
                        new SetDirective(entry.getValue().toString()),
                        new UpDirective()
                    )
                );
            }
        } catch (XmlContentException ex) {
            throw new IllegalArgumentException(ex);
        }
        return this;
    }

    /**
     * Add node if it's absent.
     * @param name Name of the node to add
     * @return This object
     * @since 0.5
     */
    public Directives addIf(
        @NotNull(message = "name can't be NULL") final String name) {
        try {
            this.all.add(new AddIfDirective(name));
        } catch (XmlContentException ex) {
            throw new IllegalArgumentException(ex);
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
     * @param name Name of the attribute
     * @param value Value to set
     * @return This object
     * @since 0.5
     */
    public Directives attr(
        @NotNull(message = "attr name can't be NULL") final String name,
        @NotNull(message = "value can't be NULL") final String value) {
        try {
            this.all.add(new AttrDirective(name, value));
        } catch (XmlContentException ex) {
            throw new IllegalArgumentException(ex);
        }
        return this;
    }

    /**
     * Add processing instruction.
     * @param target PI name
     * @param data Data to set
     * @return This object
     * @since 0.9
     * @checkstyle MethodName (3 lines)
     */
    @SuppressWarnings("PMD.ShortMethodName")
    public Directives pi(
        @NotNull(message = "target can't be NULL") final String target,
        @NotNull(message = "data can't be NULL") final String data) {
        try {
            this.all.add(new PiDirective(target, data));
        } catch (XmlContentException ex) {
            throw new IllegalArgumentException(ex);
        }
        return this;
    }

    /**
     * Set text content.
     * @param text Text to set
     * @return This object
     * @since 0.5
     */
    public Directives set(
        @NotNull(message = "content can't be NULL") final String text) {
        try {
            this.all.add(new SetDirective(text));
        } catch (XmlContentException ex) {
            throw new IllegalArgumentException(ex);
        }
        return this;
    }

    /**
     * Set text content.
     * @param text Text to set
     * @return This object
     * @since 0.7
     */
    public Directives xset(
        @NotNull(message = "content can't be NULL") final String text) {
        try {
            this.all.add(new XsetDirective(text));
        } catch (XmlContentException ex) {
            throw new IllegalArgumentException(ex);
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
    public Directives xpath(
        @NotNull(message = "xpath can't be NULL") final String path) {
        try {
            this.all.add(new XpathDirective(path));
        } catch (XmlContentException ex) {
            throw new IllegalArgumentException(ex);
        }
        return this;
    }

    /**
     * Check that there is exactly this number of current nodes.
     * @param number Number of expected nodes
     * @return This object
     * @since 0.5
     */
    public Directives strict(
        @NotNull(message = "number can't be NULL") final int number) {
        this.all.add(new StrictDirective(number));
        return this;
    }

    /**
     * Parse script.
     * @param script Script to parse
     * @return Collection of directives
     * @throws SyntaxException If can't parse
     */
    private static Collection<Directive> parse(final String script)
        throws SyntaxException {
        final CharStream input = new ANTLRStringStream(script);
        final XemblyLexer lexer = new XemblyLexer(input);
        final TokenStream tokens = new CommonTokenStream(lexer);
        final XemblyParser parser = new XemblyParser(tokens);
        try {
            return parser.directives();
        } catch (RecognitionException ex) {
            throw new SyntaxException(script, ex);
        } catch (ParsingException ex) {
            throw new SyntaxException(script, ex);
        }
    }

}
