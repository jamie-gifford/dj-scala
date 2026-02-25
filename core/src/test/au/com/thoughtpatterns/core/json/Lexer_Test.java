package au.com.thoughtpatterns.core.json;

import java.util.ArrayList;

import org.junit.Test;

import au.com.thoughtpatterns.core.json.Lexer.Token;


public class Lexer_Test {

    @Test
    public void testLexer() {
        
        String str = "[ 123 , \"hello\", { \"fred\": 123, \"frank\": true} ]";
        
        ArrayList<Token> tokens = Lexer.lex(str);
        
    }
    
}
