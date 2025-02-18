/*
 * SPDX-FileCopyrightText: Copyright (c) 2013-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package org.xembly;

/**
 * When further modification is impossible.
 *
 * @since 0.3
 */
public final class ImpossibleModificationException extends Exception {

    /**
     * Serialization marker.
     */
    private static final long serialVersionUID = 0x654739998af6efb9L;

    /**
     * Public ctor.
     * @param cause Cause of it
     */
    ImpossibleModificationException(final String cause) {
        super(cause);
    }

    /**
     * Public ctor.
     * @param cause Cause of it
     * @param thr Original throwable
     */
    ImpossibleModificationException(final String cause,
        final Throwable thr) {
        super(cause, thr);
    }

}
