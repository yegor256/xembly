/*
 * SPDX-FileCopyrightText: Copyright (c) 2013-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package org.xembly;

/**
 * A task that returns a result and may throw an {@link XmlContentException}.
 * @param <V> Type of computed result
 * @since 0.31.0
 */
public interface Callback<V> {
    /**
     * Computes a result.
     * @return The computed result
     * @throws XmlContentException If unable to compute a result
     */
    V call() throws XmlContentException;
}
