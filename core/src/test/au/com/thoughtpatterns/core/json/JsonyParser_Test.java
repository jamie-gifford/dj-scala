package au.com.thoughtpatterns.core.json;

import java.io.FileWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;

import au.com.thoughtpatterns.core.json.JsonyParser.JsonParseException;
import au.com.thoughtpatterns.core.json.Lexer.Token;
import au.com.thoughtpatterns.core.json.Lexer.TokenType;
import au.com.thoughtpatterns.core.util.Logger;
import au.com.thoughtpatterns.core.util.Resources;


public class JsonyParser_Test {

    private static final Logger log = Logger.get(JsonyParser_Test.class);
    
    @Test
    public void testParse() {
        String in = "[ 123, 456, { \"a\": \"hello\", \"b\": \"there\" }, [], {}, null, \"\", true, false ]";
        Jsony out = new JsonyParser().parse(in);
        log.debug(out.toJson());
    }
    
    @Test
    public void testParse2() {
        String in = Resources.getResourceAsString(this, "inputs.json");
        
        for (int i = 0; i < 1; i++) {
        // for (int i = 0; i < 1000; i++) {
            Jsony out = new JsonyParser().parse(in);            
        }
    }

    @Test
    public void testWrite() throws Exception {
        String in = Resources.getResourceAsString(this, "inputs.json");
        
        Jsony out = new JsonyParser().parse(in);
        
        String str = out.toJson();
        
        FileWriter w = new FileWriter("/tmp/out.json");
        w.write(str);
        w.close();
    }

    @Test
    public void testDistrib() {
        String in = Resources.getResourceAsString(this, "inputs.json");
        List<Token> tokens = Lexer.lex(in);

        Map<TokenType, Integer> count = new HashMap<>();
        for (Token t : tokens) {
            TokenType type = t.type;
            Integer c = count.get(type);
            if (c == null) {
                c = 1;
            } else {
                c ++;
            }
            count.put(type, c);
        }
        
        for (TokenType t : count.keySet()) {
            log.info(t + " : " + count.get(t));
        }
        
    }
    
    @Test
    public void testErrorReport1() {
        String in = Resources.getResourceAsString(this, "parser_test_1.json");
        int failure = 0;
        try {
            new JsonyParser().parse(in);
        } catch (JsonParseException ex) {
            failure = ex.getLine();
        }
        Assert.assertEquals(21, failure);
    }
    
}
