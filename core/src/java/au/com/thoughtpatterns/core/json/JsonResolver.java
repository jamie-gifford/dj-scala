package au.com.thoughtpatterns.core.json;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import au.com.thoughtpatterns.core.util.Resources;
import au.com.thoughtpatterns.core.util.Util;

public class JsonResolver {

    // Cached state:
    // URI -> schema mappings

    private Map<URI, Jsony> cache = new HashMap<>();

    public void register(URI base, Jsony value) {
        cache.put(base, value);
    }

    /**
     * Follow JSON ref and JSON pointer
     */
    public Jsony getRef(URI uri) {
        if (uri == null) {
            return null;
        }
        uri = uri.normalize();
        URI base = null;
        try {
            base = new URI(uri.getScheme(), uri.getSchemeSpecificPart(), null);
        } catch (URISyntaxException impossible) {
            throw new JsonyException("Impossible problem with " + uri, impossible);
        }

        Jsony root = load(base);
        
        Jsony out;
        
        String pointer = uri.getFragment();
        out = pointer(root, pointer);
        
        return out;
    }
    
    public Jsony pointer(Jsony root, String pointer) {
        
        if (Util.empty(pointer)) {
            return root;
        }
        
        String[] bits = pointer.substring(1).split("/");
        
        int cursor = 0;
        Jsony value = root;
        while (cursor < bits.length) {
            
            String bit = bits[cursor++];
            
            if (value instanceof JsonyArray) {
                JsonyArray arr = (JsonyArray) value;
                int index = Integer.parseInt(bit);
                value = arr.get(index);
            } else if (value instanceof JsonyObject) {
                JsonyObject obj = (JsonyObject) value;
                value = obj.get(bit);
            }
            
        }
        
        return value;
    }

    private Jsony load(URI uri) {
        Jsony value = cache.get(uri);
        if (value == null) {

            try {
                URL url = uri.toURL();
                try (InputStream in = url.openStream();
                        InputStreamReader reader = new InputStreamReader(in)) {

                    String str = Resources.readString(reader);
                    value = new JsonyParser().parse(str);
                }
            } catch (IOException ex) {
                throw new JsonyException("Failed to load " + uri, ex);
            }

            cache.put(uri, value);
        }
        return value;
    }

}
