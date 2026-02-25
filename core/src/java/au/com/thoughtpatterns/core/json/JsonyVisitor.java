package au.com.thoughtpatterns.core.json;

import java.util.Set;

public interface JsonyVisitor {
    
    void visit(Jsony node, Path path);

    /**
     * Depth first visit
     * @param node root object on which to perform the visit
     * @param visitor
     */
    public static void depthFirst(final Jsony node, final JsonyVisitor visitor, final Path path) {

        final JsonyVisitor depthVisitor = new JsonyVisitor() {
            
            @Override public void visit(Jsony node, Path path) {
                visitChildren(node, this, path);
                visitor.visit(node, path);
            }
            
        };
        
        depthVisitor.visit(node, path);
    }
    
    /** Visit immediate children of a Jsony node */
    public static void visitChildren(Jsony node, JsonyVisitor visitor, Path path) {

        if (node instanceof JsonyObject) {
            
            JsonyObject obj = (JsonyObject) node;
            Set<String> props = obj.getPropertyNames();
            for (String prop : props) {
                Jsony subnode = obj.get(prop);
                Path subpath = path != null ? path.create(prop) : null;
                visitor.visit(subnode, subpath);
            }
            
        } else if (node instanceof JsonyArray<?>) {
            
            JsonyArray<?> arr = (JsonyArray<?>) node;
            int size = arr.size();
            for (int i = 0; i < size; i++) {
                Jsony subnode = arr.get(i);
                Path subpath = path != null ? path.create("" + i) : null;
                visitor.visit(subnode, subpath);
            }
        } 
        
    }

    public static class Path {
        
        String component;
        Path parent;
        
        public Path(Path aParent, String aComponent) {
            parent = aParent;
            component = aComponent;
        }
        
        public Path create(String component) {
            Path subpath = new Path(this, component);
            return subpath;
        }
        
        public String toFragment() {
            if (component == null) {
                return null;
            }
            
            String p = (parent != null ? parent.toFragment() : "");
            if (p == null) {
                p = "";
            }
            String fragment = p + "/" + component;
            return fragment;
        }
        
        public int depth() {
            return parent != null ? parent.depth() + 1 : 1;
        }
        
    }

    public static Path ROOT = new Path(null, null);

}
