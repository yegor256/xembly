/*
 * SPDX-FileCopyrightText: Copyright (c) 2013-2026 Yegor Bugayenko
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
     * @param error Original throwable
     */
    SyntaxException(final String cause, final Throwable error) {
        super(cause, error);
    }

}
