package org.xembly;

import java.util.HashMap;
import java.util.Map;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;

public interface Output {


    void prepareTransformer(final Transformer transformer);


    final class Document implements Output {

        private final Map<String, String> properties;

        public Document() {
            this((Document.defaultProperties()));
        }

        public Document(final Map<String, String> properties) {
            this.properties = properties;
        }

        @Override
        public void prepareTransformer(final Transformer transformer) {
            this.properties.entrySet().stream()
                .forEach((e) -> transformer.setOutputProperty(e.getKey(), e.getValue()));
        }

        private static Map<String, String> defaultProperties() {
            final HashMap<String, String> res = new HashMap<>();
            res.put(OutputKeys.INDENT, "yes");
            res.put(OutputKeys.ENCODING, "UTF-8");
            return res;
        }
    }

    final class Node implements Output {

        @Override
        public void prepareTransformer(final Transformer transformer) {
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        }
    }

}
