package au.com.thoughtpatterns.core.bo;



import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import au.com.thoughtpatterns.core.bo.ibatis.DateBO_Test;
import au.com.thoughtpatterns.core.bo.ibatis.DependentObject_Test;
import au.com.thoughtpatterns.core.bo.ibatis.IbatisBox_Test;
import au.com.thoughtpatterns.core.bo.ibatis.OptimisticLock_Test;
import au.com.thoughtpatterns.core.bo.ibatis.RemoteBox_Test;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    BO_Test.class,
    Box_Test.class,
    IbatisBox_Test.class,
    OptimisticLock_Test.class,
    DependentObject_Test.class,
    RemoteBox_Test.class,
    DateBO_Test.class
})

public class BO_Suite {

}
