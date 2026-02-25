package au.com.thoughtpatterns.core.bo;

/**
 * Miscellaneous BO methods
 */
public class BOUtil {

    /**
     * Return the owning entity of the given BusinessObject. 
     * This may be the node itself or an ancestor (parent, grandparent, etc) of the node 
     * @param node
     * @return root node
     */
    public static BusinessObject findRootObject(BusinessObject node) {
        BusinessObject owner = node.getParent();
        if (owner == null) {
            return node;
        }
        return findRootObject(owner);
    }
    
}
