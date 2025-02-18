/*
 * SPDX-FileCopyrightText: Copyright (c) 2013-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
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
            this.original = new Transformers.Formatted(
                new Transformers.Default(),
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
            this.original = new Transformers.Formatted(
                new Transformers.Default(),
                Transformers.Document.defaultProperties()
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
        private static TransformerFactory tfactory = TransformerFactory.newInstance();

        /**
         * Transformer factory.
         */
        private final TransformerFactory factory;

        /**
         * Default ctor.
         * @since 0.30
         */
        Default() {
            this(Transformers.Default.tfactory);
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
