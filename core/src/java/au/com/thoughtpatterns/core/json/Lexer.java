package au.com.thoughtpatterns.core.json;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import au.com.thoughtpatterns.core.util.Logger;

/**
 * Lexer for JSON.
 * For speed, the lexer is written "by hand" rather than using a more conventional approach.
 */
class Lexer {
    
    // private static final Logger log = Logger.get(Lexer.class);

    public static enum TokenType {

        STRING("\"([^\"\\\\\u0001-\u001f]|\\\\([\"\\\\/bnrt]|u[0-9A-Fa-f]{4}))*\"", "\"A string\""),
        COLON(":", ":"),
        COMMA(",", ","),
        NULL("null", "null"),
        OBJECTSTART("\\{", "{"), 
        OBJECTEND("\\}", "}"), 
        NUMBER("-?([0-9]+|0)(\\.[0-9]+)?([eE][+-]?[0-9]+)?", "123"),
        BOOLEAN("true|false", "true"),
        ARRAYSTART("\\[", "["),
        ARRAYEND("\\]", "]"),
        WHITESPACE("[ \t\f]+", " "),
        NEWLINE("(\r\n|\n)", "\\n");

        public final String pattern;
        
        public final String example;
        
        public final Pattern re;

        private TokenType(String pattern, String example) {
            this.pattern = pattern;
            this.example = example;
            this.re = Pattern.compile(pattern);
        }
    }
    
    static class Token {

        public final TokenType type;

        public final String data;

        public final int pos;
        
        public final int line;
        
        public final int col;
        
        public Token(TokenType type, String data, int pos, int line, int col) {
            this.type = type;
            this.data = data;
            this.pos = pos;
            this.line = line;
            this.col = col;
        }

        @Override public String toString() {
            return String.format("(%s %s)", type.name(), data);
        }
    }

    static ArrayList<Token> lex(String input) throws LexerException {
        Lexer lexer = new Lexer(input);
        lexer.doLex();
        return lexer.tokens;
    }
    
    private String input;
    
    private int cursor;
    
    private int length;
    
    private int line;
    
    private int lastLine;

    private ArrayList<Token> tokens = new ArrayList<Token>();

    Lexer(String aInput) {
        input = aInput;
        cursor = 0;
        lastLine = 0;
        length = input.length();
        line = 1;
    }
    
    private void doLex() {
        
        // Begin matching tokens
        // Matcher matcher = TOKENIZER.matcher(input);
        
        while (cursor < length) {
            
            char peek = input.charAt(cursor);
            
            if (peek == '"') {
                expectString();
            } else if (peek == ':') {
                tokens.add(new Token(TokenType.COLON, ":", cursor, line, col()));
                cursor ++;
            } else if (peek == ',') {
                tokens.add(new Token(TokenType.COMMA, ",", cursor, line, col()));
                cursor ++;
            } else if (peek == 'n') {
                expectLiteral(TokenType.NULL);
            } else if (peek == '{') {
                expectLiteral(TokenType.OBJECTSTART);
            } else if (peek == '}') {
                expectLiteral(TokenType.OBJECTEND);
            } else if (peek == '-' || (peek >= '0' && peek <= '9')) {
                expectRe(TokenType.NUMBER);
            } else if (peek == 't' || peek == 'f') {
                expectRe(TokenType.BOOLEAN);
            } else if (peek == '[') {
                expectLiteral(TokenType.ARRAYSTART);
            } else if (peek == ']') {
                expectLiteral(TokenType.ARRAYEND);
            } else if (peek == ' ' || peek == '\t' || peek == '\f') {
                skipRe(TokenType.WHITESPACE);
            } else if (peek == '\r' || peek == '\n') {
                line ++;
                skipRe(TokenType.NEWLINE);
                lastLine = cursor;
            } else {
                throwException("Unexpected char " + peek + " at line " + line + ", col " + col());
            }
        
        }
    }

    private int col() {
        return cursor - lastLine + 1;
    }
    
    private void expectLiteral(TokenType type) {
        String literal = type.example;
        int l = literal.length();
        if (cursor + l > length) {
            throwException("Expected " + literal + " at pos " + cursor);
        }
        String sub = input.substring(cursor, cursor + l);
        if (! sub.equals(literal)) {
            throwException("Expected " + literal + ", got " + sub + " at pos " + cursor);
        }
        tokens.add(new Token(type, sub, cursor, line, (cursor - lastLine + 1)));
        cursor += l;
    }

    private void expectRe(TokenType type) {
        Matcher m = type.re.matcher(input);
        if (! m.find(cursor)) {
            throwException("Expected " + type + " at pos " + cursor);
        }
        String sub = m.group();
        tokens.add(new Token(type, sub, cursor, line, (cursor - lastLine + 1)));
        cursor += sub.length();
    }

    private void skipRe(TokenType type) {
        Matcher m = type.re.matcher(input);
        if (! m.find(cursor)) {
            throwException("Expected " + type + " at pos " + cursor);
        }
        String sub = m.group();
        cursor += sub.length();
    }

    private char nextChar() {
        cursor ++;
        if (cursor >= length) {
            throwException("Unexpected end of input");
        }
        return input.charAt(cursor);
    }
    
    private void expectString() {
        StringBuffer buff = new StringBuffer();
        buff.append('"');
        int start = cursor;
        cursor ++;
        while (cursor < length) {
            char peek = input.charAt(cursor);
            if (peek == '"') {
                // Finish
                buff.append('"');
                cursor ++;
                tokens.add(new Token(TokenType.STRING, buff.toString(), start, line, col()));
                return;
            }
            if (peek == '\\') {
                peek = nextChar();
                if (peek == '"') {
                    buff.append("\"");
                    cursor ++;
                } else if (peek == '\\') {
                    buff.append("\\");
                    cursor ++;
                } else if (peek == '/') {
                    buff.append("/");
                    cursor ++;
                } else if (peek == 'b') {
                    buff.append("\u0008");
                    cursor ++;
                } else if (peek == 'f') {
                    buff.append("\u000c");
                    cursor ++;
                } else if (peek == 'n') {
                    buff.append("\n");
                    cursor ++;
                } else if (peek == 'r') {
                    buff.append("\r");
                    cursor ++;
                } else if (peek == 't') {
                    buff.append("\t");
                    cursor ++;
                } else if (peek == 'u') {
                    // Expect 4 hex digits
                    cursor ++;
                    if (cursor + 4 > length) {
                        throwException("Expected 4 hex digits at " + cursor);
                    }
                    String hex = input.substring(cursor, cursor + 4);
                    char c = (char)Integer.parseInt(hex, 16);
                    buff.append(c);
                    cursor += 4;
                } else {
                    throwException("Unexpected char " + peek + " at " + cursor);
                }
            } else {
                if (peek == '\\' || (peek <= '\u001f')) {
                    throwException("Unexpected char " + peek + " at " + cursor);
                }
                buff.append(peek);
                cursor ++;
            }
        }
    }

    private void throwException(String msg) {
        throw new LexerException(msg, line);
    }
    
    public static class LexerException extends RuntimeException {
        public LexerException(String message, int line) {
            super("Line " + line + ": " + message);
        }
    }

}
