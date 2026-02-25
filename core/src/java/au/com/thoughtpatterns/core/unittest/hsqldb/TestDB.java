package au.com.thoughtpatterns.core.unittest.hsqldb;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import au.com.thoughtpatterns.core.sql.AppContextLocalConnectionManager;
import au.com.thoughtpatterns.core.sql.ConnectionManager;
import au.com.thoughtpatterns.core.sql.Connections;
import au.com.thoughtpatterns.core.util.Factory;
import au.com.thoughtpatterns.core.util.Logger;
import au.com.thoughtpatterns.core.util.Parameters;
import au.com.thoughtpatterns.core.util.SystemException;

/**
 * Support for using Hypersonic as a (transient) in-memory database for unit
 * testing. Also used for manipulating the offline hypersonic database during data
 * synch.
 */
public class TestDB {

    private static final Logger log = Logger.get(TestDB.class);

    /**
     * Keep track of DDL for creating each table
     */
    private Map<String,String> tableDDL = new HashMap<String,String>();
    
    private String currentTable;

    /**
     * Match on "create table xxx"
     */
    private static final Pattern CREATE_TABLE_PATTERN = Pattern.compile("create +table +([a-z_]+)", Pattern.CASE_INSENSITIVE);

    /**
     * Match on "drop table"
     */
    private static final Pattern DROP_TABLE_PATTERN = Pattern.compile("drop +table +", Pattern.CASE_INSENSITIVE);

    /**
     * Regular expression group in above RE for table name.
     */
    private static final int TABLE_GROUP = 1;
    
    /**
     * Control whether we do dry runs only (no change to database)
     */
    private boolean dryRun;
    
    /**
     * Configure the persistence layer to use an in-memory hypersonic database.
     * 
     * The database will start off empty. It can be initialised using the
     * executeSql methods.
     */
    public void startHypersonic() {
        Parameters.pushContext();

        Parameters params = Parameters.instance();

        try {
            Class.forName("org.hsqldb.jdbcDriver");
        } catch (ClassNotFoundException ex) {
            throw new SystemException(ex);
        }
        
        // "jdbc:hsqldb:mem:aname", "sa", ""
        params.set("persistence.local", "true");
        params.set("persistence.local.url", "jdbc:hsqldb:mem:aname");
        params.set("persistence.local.user", "sa");
        params.set("persistence.local.passwd", "");

        Factory.configure(ConnectionManager.class,
                AppContextLocalConnectionManager.class);
    }

    /**
     * Turn off the use of the hypersonic database.
     */
    public void stopHypersonic() {
        Parameters.popContext();
    }

    public boolean isDryRun() {
        return dryRun;
    }
    
    public void setDryRun(boolean dryRun) {
        this.dryRun = dryRun;
    }

    public void execute(String sql) throws SQLException {

        // Look for "create table xxx"
        Matcher matcher = CREATE_TABLE_PATTERN.matcher(sql);
        if (matcher.find()) {
            currentTable = matcher.group(TABLE_GROUP);
        } else {
            matcher = DROP_TABLE_PATTERN.matcher(sql);
            if (matcher.find()) {
                currentTable = null;
            }
        }
        
        if (currentTable != null) {
            String ddl = tableDDL.get(currentTable);
            if (ddl == null) {
                ddl = "";
            } else {
                ddl = ddl + "; ";
            }
            ddl = ddl + sql;
            tableDDL.put(currentTable, ddl);
        }

        if (dryRun) {
            return;
        }
        
        Connection c = null;
        Statement s = null;
        try {
            c = Connections.getConnection();
            s = c.createStatement();
            log.debug("Executing " + sql);
            s.execute(sql);
        } catch (SQLException ex) { 
            log.error(sql, ex);
            throw ex;
        } finally {
            tidy(c, s, null);
        } 
    }

    /**
     * Executes a script (ie a series of sql statements separated by semicolons)
     * loaded from a relative resource.
     * 
     * @param relativeResourceName
     */
    public void executeFile(Object root, String relativeResourceName) {
        try {
            executeFileRaw(root, relativeResourceName);
        } catch (SQLException ex) {
            throw new SystemException("SQL exception in "
                    + relativeResourceName, ex);
        }
    }
    
    /**
     * Executes a script (ie a series of sql statements separated by semicolons)
     * loaded from a relative resource.
     * 
     * Throws SQLException if there is a problem
     * 
     * @param relativeResourceName
     */
    public void executeFileRaw(Object root, String relativeResourceName) throws SQLException {

        log.debug("Executing script at " + relativeResourceName);
    
        String contents = loadTextFileContents(root, relativeResourceName);
        if (contents == null) {
            return;
        }
        executeScript(contents);
    }
    

    public void executeScript(String script) throws SQLException {
        
        currentTable = null;
        
        // Normalise whitespace
        script = fixWhitespace(script);

        while (script != null && script.length() > 0) {
            script = script.trim();
            String sql = script;
            int indexOf = script.indexOf(';');
            if (indexOf >= 0) {
                sql = script.substring(0, indexOf);
                if (indexOf < script.length()) {
                    script = script.substring(indexOf + 1);
                } else {
                    script = null;
                }
            } else {
                script = null;
            }
            sql.trim();
            if (sql.length() > 0) {
                execute(sql);
            }
        }
    }

    public String getTableDDL(String tableName) {
        return tableDDL.get(tableName);
    }
    
    protected String loadTextFileContents(Object root, String filename) {
        StringBuffer result = new StringBuffer();

        try {
            byte[] buffer = new byte[10240];
            int length;
            Class clas = ( root instanceof Class ? (Class) root : root.getClass());
            InputStream inputStream = clas.getResourceAsStream(filename);
            if (inputStream == null) {
                URL codebase = clas.getProtectionDomain().getCodeSource()
                        .getLocation();
                String classFile = clas.getName();
                StringBuffer buff = new StringBuffer(classFile);
                int j;
                while ((j = buff.toString().indexOf(".")) > -1) {
                    buff.setCharAt(j, '/');
                }
                classFile = buff.toString();
                URL expectedClassLocation = new URL(codebase, classFile);
                URL expectedLocation = new URL(expectedClassLocation, filename);
                throw new SystemException("Can't find resource " + filename
                        + " relative to " + root.getClass()
                        + "; expected location is " + expectedLocation);
            }

            while ((length = inputStream.read(buffer, 0, buffer.length)) >= 0) {
                result.append(new String(buffer, 0, length));
            }
        } catch (IOException e) {
            return null;
        }

        return result.toString();
    }

    private String fixWhitespace(String in) {
        
        // Remove comments
        
        // Pattern.compile(regex).matcher(this).replaceAll(replacement)
        
        in = Pattern.compile("--.*$", Pattern.MULTILINE).matcher(in).replaceAll("");
        
        StringBuffer buff = new StringBuffer(in);
        for (int i = 0; i < buff.length(); i++) {
            char c = buff.charAt(i);
            if (c == '\r' || c == '\n' || c == '\t') {
                buff.setCharAt(i, ' ');
            }
        }
        return buff.toString();
    }

    private void tidy(Connection c, Statement s, ResultSet r) {
        try {
            try {
                if (r != null) {
                    r.close();
                }
            } finally {
                try {
                    if (s != null) {
                        s.close();
                    }
                } finally {
                    if (c != null) {
                        c.close();
                    }
                }
            }
        } catch (SQLException ex) {
            throw new SystemException("Failed to tidy connection", ex);
        }
    }

}
