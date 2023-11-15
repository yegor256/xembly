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

import java.util.HashMap;
import java.util.Map;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;

/**
 * Output type for Xembler to use.
 * Allows to configure the output.
 * @since 0.30
 */
public interface Output {


    /**
     * Prepare transformer before using it.
     * @param transformer Transformer to prepare.
     * @since 0.30
     */
    void prepare(final Transformer transformer);

    /**
     * Document output.
     * Can accept properties to configure the output.
     * @since 0.30
     */
    final class Document implements Output {

        /**
         * Properties to configure the output.
         */
        private final Map<String, String> properties;

        /**
         * Ctor.
         * Uses default properties.
         * @since 0.30
         */
        public Document() {
            this((Document.defaultProperties()));
        }

        /**
         * Ctor.
         * @param properties Properties to configure the output.
         * @since 0.30
         */
        public Document(final Map<String, String> properties) {
            this.properties = properties;
        }

        @Override
        public void prepare(final Transformer transformer) {
            this.properties.entrySet().stream()
                .forEach((e) -> transformer.setOutputProperty(e.getKey(), e.getValue()));
        }

        /**
         * Default properties prestructor.
         * @return Properties to configure the output.
         */
        private static Map<String, String> defaultProperties() {
            final HashMap<String, String> res = new HashMap<>();
            res.put(OutputKeys.INDENT, "yes");
            res.put(OutputKeys.ENCODING, "UTF-8");
            return res;
        }
    }

    /**
     * Node output.
     * Omits XML declaration: <?xml version="1.0" encoding="UTF-8"?>
     * @since 0.30
     */
    final class Node implements Output {

        @Override
        public void prepare(final Transformer transformer) {
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        }
    }

}
