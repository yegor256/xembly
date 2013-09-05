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

import com.jcabi.aspects.Loggable;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
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
 * <pre>Document dom = DocumentBuilderFactory.newInstance()
 *   .newDocumentBuilder().newDocument();
 * dom.appendChild(dom.createElement("root"));
 * new Xembler(
 *   new Directives("XPATH 'root'; ADD 'employee';")
 * ).exec(dom);</pre>
 *
 * <p>{@link Directives} can be used as a builder of Xembly script:
 *
 * <pre>Document dom = DocumentBuilderFactory.newInstance()
 *   .newDocumentBuilder().newDocument();
 * dom.appendChild(dom.createElement("root"));
 * new Xembler(
 *   new Directives()
 *     .xpath("/root")
 *     .addIfAbsent("employees")
 *     .add("employee")
 *     .attr("id", 6564)
 *     .up()
 *     .xpath("employee[&#64;id='100']")
 *     .strict(1)
 *     .remove()
 * ).exec(dom);</pre>
 *
 * <p>The class is mutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.1
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@EqualsAndHashCode(callSuper = false, of = "all")
@Loggable(Loggable.DEBUG)
@SuppressWarnings("PMD.TooManyMethods")
public final class Directives extends AbstractCollection<Directive> {

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
        this(new ArrayList<Directive>(0));
    }

    /**
     * Public ctor.
     * @param text Xembly script
     * @throws XemblySyntaxException If syntax is broken
     */
    public Directives(@NotNull(message = "xembly script can't be NULL")
        final String text) throws XemblySyntaxException {
        this(Directives.parse(text));
    }

    /**
     * Public ctor.
     * @param dirs Directives
     */
    public Directives(@NotNull(message = "directives can't be NULL")
        final Collection<Directive> dirs) {
        super();
        this.all.addAll(dirs);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final StringBuilder text = new StringBuilder();
        int width = 0;
        for (Directive dir : this.all) {
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

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<Directive> iterator() {
        return this.all.iterator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        return this.all.size();
    }

    /**
     * Add more directives to the end of the list.
     * @param list List of them
     * @return This object
     * @since 0.5
     */
    public Directives append(final Collection<Directive> list) {
        this.all.addAll(list);
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
     * Add node if it's absent.
     * @param name Name of the node to add
     * @return This object
     * @since 0.5
     */
    public Directives addIfAbsent(
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
            this.all.add(new XPathDirective(path));
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
     * @throws XemblySyntaxException If can't parse
     */
    private static Collection<Directive> parse(final String script)
        throws XemblySyntaxException {
        final CharStream input = new ANTLRStringStream(script);
        final XemblyLexer lexer = new XemblyLexer(input);
        final TokenStream tokens = new CommonTokenStream(lexer);
        final XemblyParser parser = new XemblyParser(tokens);
        try {
            return parser.directives();
        } catch (RecognitionException ex) {
            throw new XemblySyntaxException(script, ex);
        } catch (ParsingException ex) {
            throw new XemblySyntaxException(script, ex);
        }
    }

}
