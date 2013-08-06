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
import java.util.HashSet;
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
    public Collection<Node> exec(final Document dom,
        final Collection<Node> nodes) throws ImpossibleModificationException {
        final XPath xpath = XPathFactory.newInstance().newXPath();
        final Collection<Node> dests = new HashSet<Node>(0);
        for (Node node : nodes) {
            final NodeList list;
            try {
                list = NodeList.class.cast(
                    xpath.evaluate(
                        this.expr,
                        node,
                        XPathConstants.NODESET
                    )
                );
            } catch (XPathExpressionException ex) {
                throw new ImpossibleModificationException(
                    String.format("invalid XPath expr '%s'", this.expr), ex
                );
            }
            for (int idx = 0; idx < list.getLength(); ++idx) {
                dests.add(list.item(idx));
            }
        }
        return dests;
    }

}
