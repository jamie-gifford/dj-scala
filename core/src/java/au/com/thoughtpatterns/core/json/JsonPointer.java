package au.com.thoughtpatterns.core.json;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a JsonPointer resolved against a given Json document.
 */
public class JsonPointer {

    private Jsony root;
    private List<String> path;
    
    public JsonPointer(Jsony aRoot, String aPath) throws JsonPointerException {
        root = aRoot;
        path = parse(aPath);
    }

    private JsonPointer(Jsony aRoot, List<String> aPath) {
        root = aRoot;
        path = aPath;
    }
    
    /**
     * Get JsonPointer to parent, or null if the current pointer refers to the root of the document
     * @return parent pointer or null
     */
    public JsonPointer parent() {
        if (path.size() == 0) {
            return null;
        }
        List<String> copy = new ArrayList<>();
        for (int i = 0; i < path.size() - 1; i++) {
            copy.add(path.get(i));
        }
        return new JsonPointer(root, copy);
    }
    
    /**
     * Get the Jsony object referred to by the pointer.
     * @param aPath
     * @return Jsony 
     * @throws JsonPointerException if path fails to resolve
     */
    public Jsony resolve() {
        Jsony current = root;
        int size = path.size();
        for (int i = 0; i < size; i++) {
            String p = path.get(i);
            if (current instanceof JsonyObject) {
                current = ((JsonyObject)current).get(p);
                if (current == null) {
                    throw new JsonPointerException("Path component " + (i+1) + " " + p + " does not exist");
                }
            } else if (current instanceof JsonyArray<?>) {
                
                int x = -1;
                try {
                    x = Integer.parseInt(p);
                } catch (NumberFormatException ex) {
                    throw new JsonPointerException("Path component " + (i+1) + " " + p + " not integer");
                }
                
                current = (Jsony) ((JsonyArray<?>)current).get(x);
            }
        }
        return current;
    }
    
    /**
     * Update the target with the given value. 
     * This works with pointers that end with "-" for appending to array.
     * It doesn't work for setting the entire document.
     */
    public void set(Jsony val) {
        JsonPointer parent = parent();
        if (parent == null) {
            throw new JsonPointerException("Can't change root node with JsonPointer.set");
        }
        Jsony node = parent.resolve();
        
        // Last component
        String p = path.get(path.size() - 1);
        
        if (node instanceof JsonyObject) {
            ((JsonyObject) node).set(p, val);
        } else if (node instanceof JsonyArray<?>) {
            
            @SuppressWarnings("unchecked") 
            JsonyArray<Jsony> arr = (JsonyArray<Jsony>) node;
            
            if ("-".equals(p)) {
                arr.add(val);
            } else {

                int x = -1;
                try {
                    x = Integer.parseInt(p);
                } catch (NumberFormatException ex) {
                    throw new JsonPointerException("Final path component " + p + " not integer");
                }

                arr.set(x, val);
            }
            
        }
    }

    /**
     * Remove the target of the pointer
     * @param aPath
     * @return the deleted element
     * @throws JsonPointerException
     */
    public Jsony remove() throws JsonPointerException {

        Jsony removed;
        
        JsonPointer parent = parent();
        if (parent == null) {
            throw new JsonPointerException("Can't change root node with JsonPointer.set");
        }
        Jsony node = parent.resolve();
        
        // Last component
        String p = path.get(path.size() - 1);
        
        if (node instanceof JsonyObject) {
            
            JsonyObject obj = (JsonyObject) node;
            removed = obj.get(p);
            obj.delete(p);
            
        } else if (node instanceof JsonyArray<?>) {
            
            @SuppressWarnings("unchecked") 
            JsonyArray<Jsony> arr = (JsonyArray<Jsony>) node;
            
            int x = -1;
            try {
                x = Integer.parseInt(p);
            } catch (NumberFormatException ex) {
                throw new JsonPointerException("Final path component " + p + " not integer");
            }

            removed = arr.get(x);
            
            arr.remove(x);
        } else {
            throw new JsonPointerException("Can't remove element from Jsony node of type " + node.getClass());
        }

        return removed;
    }
    
    /**
     * Test whether the target node exists
     * @return true if so, false otherwise
     */
    public boolean exists() {
        int cursor = 0;
        Jsony current = root;
        int size = path.size();
        for (int i = 0; i < size; i++) {
            String p = path.get(i);
            if (current instanceof JsonyObject) {
                current = ((JsonyObject)current).get(p);
                if (current == null) {
                    return false;
                }
            } else if (current instanceof JsonyArray<?>) {
                
                int x = -1;
                try {
                    x = Integer.parseInt(p);
                } catch (NumberFormatException ex) {
                    return false;
                }
                
                JsonyArray<?> arr = (JsonyArray<?>)current;
                if (x < 0 || x >= arr.size()) {
                    return false;
                }
                
                current = (Jsony) arr.get(x);
            }
        }
        return current != null;
        
    }
    
    /**
     * Test whether this pointer is a "create" pointer (ie, the last component is "-")
     * @return true if the pointer is a "create" pointer.
     */
    public boolean isCreate() {
        if (path.size() == 0) {
            return false;
        }
        return "-".equals(path.get(path.size() - 1));
    }
    
    private List<String> parse(String aPath) throws JsonPointerException {
        
        // Tokenise
        List<String> tokens = new ArrayList<>();
        int cursor = 0;
        
        int length = aPath.length();
        while (cursor < length) {
            if (aPath.charAt(cursor) != '/') {
                throw new JsonPointerException("Parse error at position " + cursor + " in " + aPath + "; expected /");
            }
            int end = aPath.indexOf('/', cursor + 1);
            if (end == -1) {
                end = length;
            }
            String token = aPath.substring(cursor + 1, end);
            
            token = token.replaceAll("\\~1", "/");
            token = token.replaceAll("\\~0", "~");
            
            tokens.add(token);
            
            cursor = end;
        }
        
        return tokens;
    }
    
    public String toString() {
        StringBuffer out = new StringBuffer();
        for (String p : path) {
            out.append("/").append(p);
        }
        return out.toString();
    }
    
    static class JsonPointerException extends RuntimeException {

        public JsonPointerException(String message) {
            super(message);
        }
        
    }
    
}
