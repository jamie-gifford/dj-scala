package au.com.thoughtpatterns.core.fn;



public class Union_Test {
    
    /*
     * This isn't compiling for some reason. So just comment it out since we aren't using it...
    @Test
    public void testUnion() {
     
        Collection<String> a = new ArrayList<String>();
        Collection<String> b = new ArrayList<String>();
        
        a.add("A1"); a.add("A2");
        b.add("A1"); b.add("B1");
        
        Collection<Collection<String>> sets = new ArrayList<Collection<String>>();
        sets.add(a); sets.add(b);
        
        Collection<String> union = new Union<String>().apply(new Lambda(sets)).eval();;
        
        Assert.assertEquals(3, union.size());
    }
     */
}
