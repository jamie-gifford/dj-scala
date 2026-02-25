package au.com.thoughtpatterns.core.json;

import java.io.IOException;
import java.io.Reader;
import java.util.List;

import au.com.thoughtpatterns.core.json.Lexer.Token;
import au.com.thoughtpatterns.core.json.Lexer.TokenType;
import au.com.thoughtpatterns.core.util.Resources;

/**
 * A custom parser for JSON. 
 * The parser allows for a factory to instantiate Jsony objects according to a JSON schema.
 */
public class JsonyParser {

    // JSON is simple enough that we can hand-code a lexer/parser.

    public Jsony parse(Reader reader) throws IOException {
        String in = Resources.readString(reader);
        return parse(in);
    }
    
    public Jsony parse(String in) {

        List<Token> tokens = Lexer.lex(in);

        CursorList cl = new CursorList();
        cl.tokens = tokens;
        cl.cursor = 0;

        Jsony out = parse(cl);

        return out;
    }

    private Jsony parse(CursorList cl) throws JsonParseException {
        Jsony val = parse0(cl);
        val = postparse(val);
        return val;
    }
    
    protected Jsony postparse(Jsony val) {
        return val;
    }
    
    private Jsony parse0(CursorList cl) throws JsonParseException {

        Token next = cl.next();

        if (next == null) {
            throw parseException("Unexpected end-of-input", null);
        }

        TokenType type = next.type;
        switch (type) {
        case NULL:
            return null;
        case BOOLEAN:
            return next.data.equals("true") ? Jsony.of(true) : Jsony.of(false);
        case NUMBER:
            if (next.data.indexOf('.') > -1) {
                return Jsony.of(Double.parseDouble(next.data));
            } else {
                return Jsony.of(Long.parseLong(next.data));
            }
        case STRING:
            String str = next.data.substring(1, next.data.length() - 1);
            return Jsony.of(str);
        case OBJECTSTART:
            Jsony obj = parseObject(cl);
            return obj;
        case ARRAYSTART:
            Jsony arr = parseArray(cl);
            return arr;
        default:
            throw parseException("Unexpected token type", next);
        }

    }

    private JsonyObject parseObject(CursorList cl) {

        AJsonyObject out = new AJsonyObject();

        while (!cl.peekNext(TokenType.OBJECTEND)) {

            Token prop = cl.next();
            if (prop == null || prop.type != TokenType.STRING) {
                throw parseException("Unexpected non-string as property name", prop);
            }

            cl.checkNext(TokenType.COLON);

            Jsony val = parse(cl);

            String key = prop.data.substring(1, prop.data.length() - 1);
            out.set(key, val);

            if (cl.peekNext(TokenType.COMMA)) {
                cl.next();
            } else {
                break;
            }
        }

        cl.checkNext(TokenType.OBJECTEND);

        return out;
    }

    private JsonyArray<Jsony> parseArray(CursorList cl) {

        AJsonyArray<Jsony> out = new AJsonyArray<Jsony>();

        while (!cl.peekNext(TokenType.ARRAYEND)) {

            Jsony val = parse(cl);

            out.add(val);

            if (cl.peekNext(TokenType.COMMA)) {
                cl.next();
            } else {
                break;
            }
        }

        cl.checkNext(TokenType.ARRAYEND);

        return out;
    }

    static class CursorList {

        List<Token> tokens;

        int cursor;

        Token peek() {
            return cursor < tokens.size() ? tokens.get(cursor) : null;
        }

        Token next() {
            Token next = peek();
            cursor++;
            return next;
        }

        void checkNext(TokenType type) {
            Token next = next();
            if (next == null || next.type != type) {
                throw parseException("Wrong token type, expected " + type, next);
            }
        }

        boolean peekNext(TokenType type) {
            Token peek = peek();
            return (peek != null && peek.type == type);
        }

    }

    private static JsonParseException parseException(String err, Token token) {
        String location;
        if (token != null) {
            location = token + " at line " + token.line + ", col " + token.col;
        } else {
            location = "end-of-input";
        }
        String msg = err + ", received " + location;
        return new JsonParseException(msg, token);
    }
    
    public static class JsonParseException extends RuntimeException {

        private Token token;
        
        public JsonParseException(String msg, Token token) {
            super(msg);
            this.token = token;
        }
        
        public Integer getLine() {
            return token != null ? token.line : null;
        }
        public Integer getColumn() {
            return token != null ? token.col : null;
        }

    }

}
