package au.com.thoughtpatterns.core.util;

import java.io.Serializable;
import java.util.Stack;

/**
 * A data structure that represents an in-flight "profiled transaction".
 * It contains a stack of integers that indicates the current position in 
 * the transaction.
 * 
 * <p />
 * This data structure should be propagated across thread boundaries if the 
 * transaction crosses thread boundaries.
 * 
 * <p />
 * This class also provides the notion of the "current ProfiledTransaction", 
 * that is, the ProfiledTransaction associated to the current thread.
 */
public class ProfiledTransaction implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * The stack of integers that represents the current position in 
     * the transaction.
     * 
     * Each ProfilePoint contributes (pushes) an element to the stack when started, 
     * and removes that element when stopped. Each time a ProfilePoint 
     * pushes/pops, the integer that is contributed is incremented. Nested 
     * ProfilePoints will correspond to a growing stack.
     * 
     * 
     */
    private Stack<Integer> stack = new Stack<Integer>();
    
    /**
     * The next value to push onto the stack.
     */
    private int next = 1;

    private static final ThreadLocal<ProfiledTransaction> currentTxn = new ThreadLocal<ProfiledTransaction>();
    
    public void push() {
        stack.push(next);
        next = 1;
    }
    
    public void pop() {
        int last = stack.pop();
        next = last + 1;
    }

    /**
     * Return the current stack (for read-only use)
     */
    public Stack<Integer> getStack() {
        return stack;
    }
    
    // -------------------------------
    // Support for current transaction

    public static ProfiledTransaction getCurrent(boolean create) {
        ProfiledTransaction current = currentTxn.get();
        if (current == null && create) {
            current = new ProfiledTransaction();
            setCurrent(current);
        }
        return current;
    }
    
    public static void setCurrent(ProfiledTransaction txn) {
        currentTxn.set(txn);
    }
    
}
