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
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
 * <p>The class is immutable and thread-safe.
 *
 * @since 0.1
 */
@EqualsAndHashCode(of = "expr")
@SuppressWarnings("aibolit.P13")
final class XpathDirective implements Directive {

    /**
     * XPath factory.
     */
    private static final ThreadLocal<XPathFactory> FACTORY =
        ThreadLocal.withInitial(XPathFactory::newInstance);

    /**
     * Pattern to match root-only XPath queries.
     */
    private static final Pattern ROOT_ONLY =
        Pattern.compile("/([^/\\(\\[\\{:]+)");

    /**
     * XPath to use.
     */
    private final transient Arg expr;

    /**
     * Public ctor.
     * @param path XPath
     * @throws XmlContentException If invalid input
     */
    XpathDirective(final String path) throws XmlContentException {
        this.expr = new Arg(path);
    }

    @Override
    public String toString() {
        return String.format("XPATH %s", this.expr);
    }

    @Override
    public Directive.Cursor exec(final Node dom,
        final Directive.Cursor cursor, final Directive.Stack stack)
        throws ImpossibleModificationException {
        final Collection<Node> targets;
        final String query = this.expr.raw();
        final Matcher matcher = XpathDirective.ROOT_ONLY.matcher(query);
        if (matcher.matches()) {
            targets = XpathDirective.rootOnly(matcher.group(1), dom);
        } else {
            targets = XpathDirective.traditional(query, dom, cursor);
        }
        return new DomCursor(targets);
    }

    /**
     * Fetches only root node.
     * @param root Root node name
     * @param dom Document
     * @return Found nodes
     */
    private static Collection<Node> rootOnly(final String root,
        final Node dom) {
        final Node target;
        if (dom.getOwnerDocument() == null) {
            target = Document.class.cast(dom).getDocumentElement();
        } else {
            target = dom.getOwnerDocument().getDocumentElement();
        }
        final Collection<Node> targets;
        if (root != null && target != null
            && ("*".equals(root) || target.getNodeName().equals(root))) {
            targets = Collections.singletonList(target);
        } else {
            targets = Collections.emptyList();
        }
        return targets;
    }

    /**
     * Fetch them in traditional way.
     * @param query XPath query
     * @param dom Document
     * @param current Nodes we're currently at
     * @return Found nodes
     * @throws ImpossibleModificationException If fails
     */
    private static Collection<Node> traditional(final String query,
        final Node dom, final Collection<Node> current)
        throws ImpossibleModificationException {
        final XPath xpath = XpathDirective.FACTORY.get().newXPath();
        final Collection<Node> targets = new HashSet<>(0);
        for (final Node node : XpathDirective.roots(dom, current)) {
            final NodeList list;
            try {
                list = NodeList.class.cast(
                    xpath.evaluate(query, node, XPathConstants.NODESET)
                );
            } catch (final XPathExpressionException ex) {
                throw new ImpossibleModificationException(
                    String.format("Invalid XPath expression '%s'", query), ex
                );
            }
            XpathDirective.copyTo(list, targets);
        }
        return targets;
    }

    /**
     * Copy nodes from NodeList to a collection.
     * @param list The list
     * @param targets Collection
     */
    private static void copyTo(final NodeList list,
        final Collection<Node> targets) {
        final int len = list.getLength();
        for (int idx = 0; idx < len; ++idx) {
            targets.add(list.item(idx));
        }
    }

    /**
     * Get roots to start searching from.
     * @param dom Document
     * @param nodes Current nodes
     * @return Root nodes to start searching from
     */
    private static Iterable<Node> roots(final Node dom,
        final Collection<Node> nodes) {
        final Collection<Node> roots;
        if (nodes.isEmpty()) {
            if (dom.getOwnerDocument() == null) {
                roots = Collections.singletonList(dom);
            } else {
                roots = Collections.singletonList(
                    dom.getOwnerDocument().getDocumentElement()
                );
            }
        } else {
            roots = nodes;
        }
        return roots;
    }

}
