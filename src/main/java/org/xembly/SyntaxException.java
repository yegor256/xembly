/*
 * SPDX-FileCopyrightText: Copyright (c) 2013-2025 Yegor Bugayenko <yegor256@gmail.com>
 * SPDX-License-Identifier: MIT
 */
package org.xembly;

/**
 * When syntax is broken.
 *
 * @since 0.3
 */
public final class SyntaxException extends RuntimeException {

    /**
     * Serialization marker.
     */
    private static final long serialVersionUID = 0x6547f9e98af6efb9L;

    /**
     * Public ctor.
     * @param cause Cause of it
     * @param thr Original throwable
     */
    SyntaxException(final String cause, final Throwable thr) {
        super(cause, thr);
    }

}
