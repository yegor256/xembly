/*
 * Copyright (c) 2013-2023, xembly.org
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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;

/**
 * Transformer factory.
 * @since 0.30
 */
public interface Transformers {

    /**
     * Create transformer.
     * @return Transformer.
     */
    Transformer create();

    /**
     * Transformer factory that omits XML declaration.
     * @since 0.30
     */
    final class Node implements Transformers {

        /**
         * Original transformer factory.
         */
        private final Transformers original;

        /**
         * Default ctor.
         * @since 0.30
         */
        public Node() {
            this.original = new Formatted(
                new Default(),
                Collections.singletonMap(OutputKeys.OMIT_XML_DECLARATION, "yes")
            );
        }

        @Override
        public Transformer create() {
            return this.original.create();
        }
    }

    /**
     * Transformer factory that produces document transformers.
     * All transformers produced by this factory will be configured to produce
     * XML documents with XML declaration and indentation.
     * @since 0.30
     */
    final class Document implements Transformers {

        /**
         * Original transformer factory.
         */
        private final Transformers original;

        /**
         * Default ctor.
         * @since 0.30
         */
        public Document() {
            this.original = new Formatted(
                new Default(),
                Document.defaultProperties()
            );
        }

        @Override
        public Transformer create() {
            return this.original.create();
        }

        /**
         * Default properties prestructor.
         * @return Properties to configure the output.
         */
        private static Map<String, String> defaultProperties() {
            final Map<String, String> res = new HashMap<>();
            res.put(OutputKeys.INDENT, "yes");
            res.put(OutputKeys.ENCODING, "UTF-8");
            return res;
        }
    }

    /**
     * Default transformer factory.
     * @since 0.30
     */
    final class Default implements Transformers {
        /**
         * Default transformer factory.
         */
        private static TransformerFactory TFACTORY = TransformerFactory.newInstance();

        /**
         * Transformer factory.
         */
        private final TransformerFactory factory;

        /**
         * Default ctor.
         * @since 0.30
         */
        Default() {
            this(Default.TFACTORY);
        }

        /**
         * Ctor.
         * @param factory Transformer factory.
         * @since 0.30
         */
        private Default(final TransformerFactory factory) {
            this.factory = factory;
        }

        @Override
        public Transformer create() {
            try {
                return this.factory.newTransformer();
            } catch (final TransformerConfigurationException ex) {
                throw new IllegalStateException(
                    String.format(
                        "Failed to create new Transformer at %s",
                        this.factory.getClass().getCanonicalName()
                    ),
                    ex
                );
            }
        }
    }

    /**
     * Transformer factory that produces formatted transformers.
     * @since 0.30
     */
    final class Formatted implements Transformers {

        /**
         * Original transformer factory.
         */
        private final Transformers original;

        /**
         * Properties to configure the output.
         */
        private final Map<String, String> properties;

        /**
         * Ctor.
         * @param original Original transformer factory.
         * @param properties Properties to configure the output.
         */
        Formatted(
            final Transformers original,
            final Map<String, String> properties
        ) {
            this.original = original;
            this.properties = properties;
        }

        @Override
        public Transformer create() {
            final Transformer transformer = this.original.create();
            this.properties.forEach(transformer::setOutputProperty);
            return transformer;
        }
    }
}
