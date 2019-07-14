/**
 * Copyright (c) 2013-2019, xembly.org
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

import java.util.ArrayList;
import java.util.Collection;
import lombok.EqualsAndHashCode;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * ADDIF directive.
 *
 * <p>The class is immutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.1
 */
@EqualsAndHashCode(of = "name")
final class AddIfDirective implements Directive {

    /**
     * Name of node to add.
     */
    private final transient Arg name;

    /**
     * Public ctor.
     * @param node Name of node to add
     * @throws XmlContentException If invalid input
     */
    AddIfDirective(final String node) throws XmlContentException {
        this.name = new Arg(node);
    }

    @Override
    public String toString() {
        return String.format("ADDIF %s", this.name);
    }

    @Override
    public Directive.Cursor exec(final Node dom,
        final Directive.Cursor cursor, final Directive.Stack stack) {
        final Collection<Node> targets = new ArrayList<Node>(cursor.size());
        final String label = this.name.raw();
        for (final Node node : cursor) {
            final NodeList kids = node.getChildNodes();
            Node target = null;
            final int len = kids.getLength();
            for (int idx = 0; idx < len; ++idx) {
                if (kids.item(idx).getNodeName()
                    .compareToIgnoreCase(label) == 0) {
                    target = kids.item(idx);
                    break;
                }
            }
            if (target == null) {
                final Document doc;
                if (dom.getOwnerDocument() == null) {
                    doc = Document.class.cast(dom);
                } else {
                    doc = dom.getOwnerDocument();
                }
                target = doc.createElement(this.name.raw());
                node.appendChild(target);
            }
            targets.add(target);
        }
        return new DomCursor(targets);
    }

}
