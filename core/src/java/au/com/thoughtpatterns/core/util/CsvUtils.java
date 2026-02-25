package au.com.thoughtpatterns.core.util;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Utilities for dealing with "csv" files
 */
public class CsvUtils {

    private StringBuffer out = new StringBuffer();

    /**
     * Controls whether output file uses CRLF or just plain LF
     */
    private boolean crlf = false;
    
    // ------------------------------------------------------

    public boolean getCrlf() {
        return crlf;
    }
    
    public void setCrlf(boolean crlf) {
        this.crlf = crlf;
    }
    
    /**
     * From an array of arrays of Strings, produce a String that represents the
     * data as a "csv" file.
     */
    public void toCsv(String[][] data) {

        for (String[] line : data) {
            toCsv(line);
        }

    }

    public void toCsv(List<String[]> data) {

        for (String[] line : data) {
            toCsv(line);
        }

    }

    public String getFormattedString() {
        return out.toString();
    }

    private void toCsv(String[] line) {

        String EOL = crlf ? "\r\n" : "\n";
        
        String[] replace = new String[line.length];

        // "Quote" doublequotes by repeating
        for (int i = 0; i < line.length; i++) {
            String elt = line[i];
            if (elt == null) {
                elt = "";
            }
            elt = elt.replaceAll("\"", "\"\"");

            boolean needsQuoting = elt.indexOf(',') > -1 || elt.indexOf('"') > 0
                    || elt.indexOf('\r') > -1 || elt.indexOf('\n') > -1;

            if (needsQuoting) {
                elt = "\"" + elt + "\"";
            }
            replace[i] = elt;
        }

        String cline = Util.join(",", replace);
        out.append(cline).append(EOL);
    }

    // ------------------------------------------------------

    static enum TokenType {
        LETTER, FIELD_SEPARATOR, QUOTE, NEWLINE, WHITESPACE;
    }

    static enum ParserState {
        NONE, EXPECTING_SEPARATOR, IN_FIELD;
    }

    private ArrayList<String[]> records = new ArrayList<String[]>();

    private List<String> fields = new ArrayList<String>();

    private StringBuffer field = new StringBuffer();

    private ParserState state;

    private String terminator = null;

    /**
     * From a CSV file represented as a String, return a list of records, each
     * one of which is an array of Strings
     */
    public List<String[]> fromCsv(String input) {
        try {
            StringReader reader = new StringReader(input);
            return fromCsv(reader);
        } catch (IOException unexpected) {
            throw new SystemException(unexpected);
        }
    }

    /**
     * From a CSV file represented as a Reader, return a list of records, each
     * one of which is an array of Strings.
     */
    public List<String[]> fromCsv(Reader input) throws IOException {

        records.clear();
        fields.clear();
        state = ParserState.NONE;

        LineNumberReader lines = new LineNumberReader(input);

        while (true) {

            String nextLine = lines.readLine();
            if (nextLine == null) {
                // EOF
                break;
            }

            // Fields are comma separated, but fields may be quoted in ".
            // Also quotes may appear doubled "" to represent a quote character.
            // Newlines may be embedded.
            // Space before and after delimiter commas to be trimmed.

            parseLine(nextLine);

        }

        return records;
    }

    private void parseLine(String line) {
        int length = line.length();

        final char SEPARATOR_CHAR = ',';

        for (int i = 0; i < length; i++) {

            char ch = line.charAt(i);
            char next = (i < length - 1 ? line.charAt(i + 1) : 0);

            TokenType type = TokenType.LETTER;
            switch (ch) {
            case SEPARATOR_CHAR:
                type = TokenType.FIELD_SEPARATOR;
                break;
            case '"':
                type = TokenType.QUOTE;
                break;
            case '\n':
                type = TokenType.NEWLINE;
                break;
            case ' ':
                type = TokenType.WHITESPACE;
                break;
            }

            // Special treatment of "" if we are inside a field.
            // It's treated as a single ".
            if (state == ParserState.IN_FIELD && ch == '"' && next == '"') {
                next = 0;
                type = TokenType.LETTER;
                // skip second quote
                i++;
            }

            // Special treatment of characters if we are inside a quoted field.
            // All characters except the quote character are letters
            if (terminator != null && type != TokenType.QUOTE) {
                type = TokenType.LETTER;
            }

            boolean parsing = true;
            while (parsing) {
                parsing = false;

                if (state == ParserState.NONE) {

                    // Expect either a letter or a field separator or whitespace
                    if (type == TokenType.LETTER) {
                        // Accummulate
                        state = ParserState.IN_FIELD;
                        field.append(ch);
                    } else if (type == TokenType.FIELD_SEPARATOR) {
                        // Empty field
                        fields.add(null);
                    } else if (type == TokenType.QUOTE) {
                        terminator = "\"";
                        state = ParserState.IN_FIELD;
                    }

                } else if (state == ParserState.IN_FIELD) {

                    if (type == TokenType.QUOTE) {
                        // End of field
                        addField();
                        state = ParserState.EXPECTING_SEPARATOR;
                        terminator = null;
                    } else if (type == TokenType.FIELD_SEPARATOR) {
                        addField();
                        state = ParserState.NONE;
                    } else {
                        field.append(ch);
                    }

                } else if (state == ParserState.EXPECTING_SEPARATOR) {
                    if (type == TokenType.FIELD_SEPARATOR) {
                        state = ParserState.NONE;
                    }
                }
            }
        }

        // At the end of the line, if we have no pending terminator,
        // end the field
        // SD change Also need to account for last char on line being separator
        // (ie last field is null)
        if (terminator == null) {
            if ((state == ParserState.IN_FIELD)
                    || (line.length() > 0 && (line.charAt(line.length() - 1) == SEPARATOR_CHAR))) {
                addField();
                state = ParserState.NONE;
            }
            addRecord();
            state = ParserState.NONE;
        } else {
            // Add the line terminator to the field
            // Strictly speaking this is not right - we need to take into
            // account CR as well as LF
            field.append('\n');
        }
    }

    private void addField() {
        fields.add(field.toString());
        field.setLength(0);
    }

    private void addRecord() {
        String[] arr = new String[fields.size()];
        fields.toArray(arr);
        records.add(arr);
        fields.clear();
    }
}
