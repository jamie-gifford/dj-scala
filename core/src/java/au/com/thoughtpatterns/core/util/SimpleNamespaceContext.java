package au.com.thoughtpatterns.core.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

/**
 * Simple implementation of NamespaceContext.
 */
public class SimpleNamespaceContext implements NamespaceContext {

    private Map<String, String> prefixToNamespace = new HashMap<String, String>();
    
    private Map<String, List<String>> namespaceToPrefix = new HashMap<String, List<String>>();

    public SimpleNamespaceContext() {
        put(XMLConstants.XML_NS_PREFIX, XMLConstants.XML_NS_URI);
        put(XMLConstants.XMLNS_ATTRIBUTE, XMLConstants.XMLNS_ATTRIBUTE_NS_URI);
    }
    
    public String getNamespaceURI(String prefix) {
        String ns = prefixToNamespace.get(prefix);
        if (ns == null) {
            return XMLConstants.NULL_NS_URI;
        }
        return ns;
    }

    public String getPrefix(String namespaceURI) {
        List<String> prefixes = findPrefixes(namespaceURI);
        return ( prefixes.size() == 0 ? null : prefixes.get(0));
    }

    public Iterator getPrefixes(String namespaceURI) {
        List<String> prefixes = findPrefixes(namespaceURI);
        return prefixes.iterator();
    }

    // ----------------------------------
    
    public void put(String prefix, String namespace) {
        if (prefixToNamespace.containsKey(prefix)) {
            return;
        }
        
        prefixToNamespace.put(prefix, namespace);
        
        findPrefixes(namespace).add(prefix);
    }
    
    private List<String> findPrefixes(String namespace) {
        List<String> prefixes = namespaceToPrefix.get(namespace);
        if (prefixes == null) {
            prefixes = new ArrayList<String>();
            namespaceToPrefix.put(namespace, prefixes);
        }
        return prefixes;
    }
}
