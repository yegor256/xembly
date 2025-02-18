/*
 * SPDX-FileCopyrightText: Copyright (c) 2013-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package org.xembly;

import java.util.Deque;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import lombok.EqualsAndHashCode;

/**
 * Stack of DOM cursors.
 *
 * <p>The class is mutable and thread-safe.
 *
 * @since 0.16
 */
@EqualsAndHashCode(of = "cursors")
final class DomStack implements Directive.Stack {

    /**
     * Queue of cursors.
     */
    private final Deque<Directive.Cursor> cursors =
        new LinkedList<>();

    @Override
    public void push(final Directive.Cursor cursor) {
        synchronized (this.cursors) {
            this.cursors.push(cursor);
        }
    }

    @Override
    public Directive.Cursor pop() throws ImpossibleModificationException {
        synchronized (this.cursors) {
            try {
                return this.cursors.pop();
            } catch (final NoSuchElementException ex) {
                throw new ImpossibleModificationException(
                    "Stack is empty, can't POP", ex
                );
            }
        }
    }
}
