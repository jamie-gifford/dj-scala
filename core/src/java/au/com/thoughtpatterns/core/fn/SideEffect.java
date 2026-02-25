package au.com.thoughtpatterns.core.fn;

public abstract class SideEffect<T> implements Ufn<T, Object> {

    private static final long serialVersionUID = 1L;

    public Fn<Object> apply(final T in) {
        return new Fn<Object>() {

            private static final long serialVersionUID = 1L;

            public Object eval() {
                sideEffect(in);
                return null;
            }
            
        };
    }

    public abstract void sideEffect(T t);
    
}
