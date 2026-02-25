package au.com.thoughtpatterns.core.bo.ibatis;

import au.com.thoughtpatterns.core.bo.Box;
import au.com.thoughtpatterns.core.bo.BoxFactory;


public class IbatisBoxFactory implements BoxFactory {

    private static final long serialVersionUID = 1L;

    public Box createBox() {
        return new IbatisBox();
    }
    
}
