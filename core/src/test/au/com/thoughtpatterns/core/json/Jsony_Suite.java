package au.com.thoughtpatterns.core.json;


import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import au.com.thoughtpatterns.core.json.gen.JsonyGenerator_Test;
import au.com.thoughtpatterns.core.json.schema.Schema_Test;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    AJsonyArray_Test.class,
    AJsonyObject_Test.class,
    JsonUtils_Test.class,
    JsonyParser_Test.class,
    Lexer_Test.class,
    Schema_Test.class,
    JsonyGenerator_Test.class,
    JsonPointer_Test.class
})
public class Jsony_Suite {

}
