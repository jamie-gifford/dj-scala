package au.com.thoughtpatterns.core.fn;

public class Lambda<T> implements Fn<T> {

    private static final long serialVersionUID = 1L;
    private T t;

    public Lambda(T aT) {
        t = aT;
    }
    
    public T eval() {
        return t;
    }
    
}
