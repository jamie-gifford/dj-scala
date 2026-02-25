package au.com.thoughtpatterns.core;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import au.com.thoughtpatterns.core.bo.BO_Suite;
import au.com.thoughtpatterns.core.bo.ibatis.StaticOneToMany_Test;
import au.com.thoughtpatterns.core.json.Jsony_Suite;
import au.com.thoughtpatterns.core.parcel.Parcel_Suite;
import au.com.thoughtpatterns.core.sql.Sql_Suite;
import au.com.thoughtpatterns.core.unittest.hsqldb.Hsqldb_Suite;
import au.com.thoughtpatterns.core.util.Util_Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
  Util_Suite.class,
  Sql_Suite.class,
  Hsqldb_Suite.class,
  BO_Suite.class,
  Parcel_Suite.class,
  StaticOneToMany_Test.class,
  Jsony_Suite.class
})
public class Core_Suite {

}
