/*
 * SPDX-FileCopyrightText: Copyright (c) 2013-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package org.xembly;

/**
 * When parsing of directives is impossible.
 *
 * @since 0.6
 */
final class ParsingException extends Exception {

    /**
     * Serialization marker.
     */
    private static final long serialVersionUID = 0x6547f999eaf6efb9L;

    /**
     * Public ctor.
     * @param cause Cause of it
     */
    ParsingException(final String cause) {
        super(cause);
    }
}
