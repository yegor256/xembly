/*
 * SPDX-FileCopyrightText: Copyright (c) 2013-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package org.xembly;

/**
 * When impossible to understand XML content.
 *
 * @since 0.6
 */
final class XmlContentException extends Exception {

    /**
     * Serialization marker.
     */
    private static final long serialVersionUID = 0x6547f999eaf6efb9L;

    /**
     * Public ctor.
     * @param cause Cause of it
     */
    XmlContentException(final String cause) {
        super(cause);
    }

}
