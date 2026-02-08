/*
 * SPDX-FileCopyrightText: Copyright (c) 2013-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package org.xembly;

import java.util.Deque;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
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

    /**
     * Lock for thread safety.
     */
    private final Lock lock = new ReentrantLock();

    @Override
    public void push(final Directive.Cursor cursor) {
        this.lock.lock();
        try {
            this.cursors.push(cursor);
        } finally {
            this.lock.unlock();
        }
    }

    @Override
    public Directive.Cursor pop() throws ImpossibleModificationException {
        this.lock.lock();
        try {
            return this.cursors.pop();
        } catch (final NoSuchElementException ex) {
            throw new ImpossibleModificationException(
                "Stack is empty, can't POP", ex
            );
        } finally {
            this.lock.unlock();
        }
    }
}
