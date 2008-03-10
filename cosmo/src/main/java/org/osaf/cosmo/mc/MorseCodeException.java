/*
 * Copyright 2006-2007 Open Source Applications Foundation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.osaf.cosmo.mc;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * An unclassified Morse Code exception.
 */
public class MorseCodeException extends RuntimeException
    implements MorseCodeConstants {
    
    private int code;
    private McNamespaceContext nsc;

    public MorseCodeException(String message) {
        this(message, null);
    }

    public MorseCodeException(int code,
                              String message) {
        this(code, message, null);
    }

    public MorseCodeException(String message,
                              Throwable cause) {
        this(500, message, cause);
    }

    public MorseCodeException(int code,
                              Throwable cause) {
        this(code, cause.getMessage(), cause);
    }

    public MorseCodeException(Throwable cause) {
        this(cause.getMessage(), cause);
    }

    public MorseCodeException(int code,
                              String message,
                              Throwable cause) {
        super(message, cause);
        this.code = code;
        nsc = new McNamespaceContext();
    }

    public int getCode() {
        return code;
    }

    public boolean hasContent() {
        return true;
    }

    public McNamespaceContext getNamespaceContext() {
        return nsc;
    }

    public void writeTo(XMLStreamWriter writer)
        throws XMLStreamException {
        writer.setNamespaceContext(nsc);
        writer.writeStartElement(NS_MC, "error");
        for (String uri : nsc.getNamespaceURIs())
            writer.writeNamespace(nsc.getPrefix(uri), uri);
        writeContent(writer);
        writer.writeEndElement();
    }

    protected void writeContent(XMLStreamWriter writer)
        throws XMLStreamException {
        writer.writeStartElement(NS_MC, "internal-server-error");
        if (getMessage() != null)
            writer.writeCharacters(getMessage());
        writer.writeEndElement();
    }

    public static class McNamespaceContext implements NamespaceContext {
        private HashMap<String,String> uris;
        private HashMap<String,HashSet<String>> prefixes;

        public McNamespaceContext() {
            uris = new HashMap<String,String>();
            prefixes = new HashMap<String,HashSet<String>>();

            addNamespace(PRE_MC, NS_MC);
        }

        // NamespaceContext methods

        public String getNamespaceURI(String prefix) {
            return uris.get(prefix);
        }

        public String getPrefix(String namespaceURI) {
            if (prefixes.get(namespaceURI) == null)
                return null;
            return prefixes.get(namespaceURI).iterator().next();
        }

        public Iterator getPrefixes(String namespaceURI) {
            if (prefixes.get(namespaceURI) == null)
                return null;
            return prefixes.get(namespaceURI).iterator();
        }

        // our methods

        public Set<String> getNamespaceURIs() {
            return prefixes.keySet();
        }

        public void addNamespace(String prefix,
                                 String namespaceURI) {
            uris.put(prefix, namespaceURI);

            HashSet<String> ns = new HashSet<String>(1);
            ns.add(prefix);
            prefixes.put(namespaceURI, ns);
        }
    }
}
