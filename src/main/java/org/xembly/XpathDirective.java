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
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import lombok.EqualsAndHashCode;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * XPATH directive.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.1
 */
@Immutable
@EqualsAndHashCode(of = "expr")
@Loggable(Loggable.DEBUG)
final class XPathDirective implements Directive {

    /**
     * XPath to use.
     */
    private final transient String expr;

    /**
     * Public ctor.
     * @param path XPath
     */
    protected XPathDirective(final String path) {
        this.expr = path;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format("XPATH %s", new Arg(this.expr));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Node exec(final Document dom, final Node node) {
        final XPath xpath = XPathFactory.newInstance().newXPath();
        final NodeList nodes;
        try {
            nodes = NodeList.class.cast(
                xpath.evaluate(
                    this.expr,
                    node,
                    XPathConstants.NODESET
                )
            );
        } catch (XPathExpressionException ex) {
            throw new IllegalArgumentException(
                String.format("invalid XPath expression '%s'", this.expr), ex
            );
        }
        if (nodes.getLength() == 0) {
            throw new IllegalStateException(
                String.format("no nodes found by XPath '%s'", this.expr)
            );
        }
        return nodes.item(0);
    }

}
