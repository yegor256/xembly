/*
 * SPDX-FileCopyrightText: 2013-2025 Yegor Bugayenko <yegor256@gmail.com>
 * SPDX-License-Identifier: MIT
 */
package org.xembly;

import java.util.Collection;
import org.w3c.dom.Node;

/**
 * Directive.
 *
 * <p>Use {@link Directives} to create a collection of them. You don't
 * need to use this interface directly and make instances of it. Everything
 * is done through {@link Directives} and {@link Xembler}.
 *
 * @since 0.1
 */
public interface Directive {

    /**
     * Execute it in the given document with current position at the given node.
     * @param dom Document
     * @param cursor Nodes we're currently at
     * @param stack Execution stack
     * @return New current nodes
     * @throws ImpossibleModificationException If can't do it
     */
    Directive.Cursor exec(Node dom, Directive.Cursor cursor,
        Directive.Stack stack) throws ImpossibleModificationException;

    /**
     * Cursor.
     * @since 0.16
     */
    interface Cursor extends Collection<Node> {
    }

    /**
     * Stack.
     * @since 0.16
     */
    interface Stack {
        /**
         * Push cursor (runtime exception if stack is full).
         * @param cursor Cursor to push
         */
        void push(Directive.Cursor cursor);

        /**
         * Pop cursor (runtime exception if stack is empty).
         * @return Cursor recently added
         * @throws ImpossibleModificationException If fails
         */
        Directive.Cursor pop()
            throws ImpossibleModificationException;
    }

}
