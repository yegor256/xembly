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
import java.util.Collection;
import java.util.LinkedList;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;

/**
 * Mutable builder of Xembly code.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.1
 */
@EqualsAndHashCode(of = "dirs")
@Loggable(Loggable.DEBUG)
public final class XemblyBuilder {

    /**
     * List of directives.
     */
    private final transient Collection<Directive> dirs =
        new LinkedList<Directive>();

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final StringBuilder text = new StringBuilder();
        for (Directive dir : this.dirs) {
            text.append(dir).append("; ");
        }
        return text.toString();
    }

    /**
     * Get all directives collected so far.
     * @return Directives
     */
    public Collection<Directive> directives() {
        return this.dirs;
    }

    /**
     * Add node.
     * @param name Name of the node to add
     * @return This object
     */
    public XemblyBuilder add(
        @NotNull(message = "name can't be NULL") final String name) {
        this.dirs.add(new AddDirective(name));
        return this;
    }

    /**
     * Set attribute.
     * @param name Name of the attribute
     * @param value Value to set
     * @return This object
     */
    public XemblyBuilder attr(
        @NotNull(message = "attr name can't be NULL") final String name,
        @NotNull(message = "value can't be NULL") final String value) {
        this.dirs.add(new AttrDirective(name, value));
        return this;
    }

    /**
     * Set text content.
     * @param text Text to set
     * @return This object
     */
    public XemblyBuilder set(
        @NotNull(message = "content can't be NULL") final String text) {
        this.dirs.add(new SetDirective(text));
        return this;
    }

    /**
     * Go one node/level up.
     * @return This object
     * @checkstyle MethodName (3 lines)
     */
    @SuppressWarnings("PMD.ShortMethodName")
    public XemblyBuilder up() {
        this.dirs.add(new UpDirective());
        return this;
    }

    /**
     * Go to XPath.
     * @param path Path to go to
     * @return This object
     */
    public XemblyBuilder xpath(
        @NotNull(message = "xpath can't be NULL") final String path) {
        this.dirs.add(new XPathDirective(path));
        return this;
    }

}
