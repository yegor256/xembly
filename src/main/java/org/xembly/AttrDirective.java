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

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import lombok.EqualsAndHashCode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * ATTR directive.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.1
 */
@Immutable
@EqualsAndHashCode(of = { "name", "value" })
@Loggable(Loggable.DEBUG)
final class AttrDirective implements Directive {

    /**
     * Attribute name.
     */
    private final transient Arg name;

    /**
     * Text value to set.
     */
    private final transient Arg value;

    /**
     * Public ctor.
     * @param attr Attribute name
     * @param val Text value to set
     * @throws XmlContentException If invalid input
     */
    protected AttrDirective(final String attr, final String val)
        throws XmlContentException {
        this.name = new Arg(attr.toLowerCase(Locale.ENGLISH));
        this.value = new Arg(val);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format("ATTR %s, %s", this.name, this.value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Node> exec(final Document dom,
        final Collection<Node> nodes) {
        for (Node node : nodes) {
            Element.class.cast(node).setAttribute(
                this.name.raw(), this.value.raw()
            );
        }
        return Collections.unmodifiableCollection(nodes);
    }

}
