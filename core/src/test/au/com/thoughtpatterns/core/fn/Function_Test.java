package au.com.thoughtpatterns.core.fn;

import junit.framework.Assert;

import org.junit.Test;


public class Function_Test {

    private Factorial f = new Factorial();
    
    @Test
    public void testFactorial() {
        Assert.assertTrue(1 == f.apply(1).eval());
        Assert.assertTrue(2 == f.apply(2).eval());
        Assert.assertTrue(6 == f.apply(3).eval());
        Assert.assertTrue(24 == f.apply(4).eval());
    }
    
    private static class Factorial implements Ufn<Integer, Integer> {
        private static final long serialVersionUID = 1L;

        public Fn<Integer> apply(final Integer in) {

            if (in == null || in <= 2) {
                return new Lambda<Integer>(in);
            }
            return new Fn<Integer>() {
                private static final long serialVersionUID = 1L;

                public Integer eval() {
                    return in * new Factorial().apply(in - 1).eval();
                }
            };  
        }
    }
    
}
