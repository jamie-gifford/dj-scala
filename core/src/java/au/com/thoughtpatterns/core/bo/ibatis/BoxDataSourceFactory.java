package au.com.thoughtpatterns.core.bo.ibatis;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Map;
import java.util.logging.Logger;

import javax.sql.DataSource;

import au.com.thoughtpatterns.core.sql.Connections;

import com.ibatis.sqlmap.engine.datasource.DataSourceFactory;

/**
 * Provides database connections to Apache Ibatis.
 * We don't want Apache Ibatis to do its own connection management.
 * Instead, we provide this DataSourceFactory in the Ibatis config.
 */
public class BoxDataSourceFactory implements DataSourceFactory {

    public DataSource getDataSource() {
        return new BoxDataSource();
    }

    public void initialize(Map map) {
        // NOP
    }

    static class BoxDataSource implements DataSource {

        public Connection getConnection() throws SQLException {
            return Connections.getConnection();
        }

        public Connection getConnection(String username, String password) throws SQLException {
            return getConnection();
        }

        public int getLoginTimeout() throws SQLException {
            return 0;
        }

        public PrintWriter getLogWriter() throws SQLException {
            return null;
        }

        public void setLoginTimeout(int seconds) throws SQLException {
            // NOP
        }

        public void setLogWriter(PrintWriter out) throws SQLException {
            // NOP
        }

        // ---------------------------
        // Java 1.6 
        
        public boolean isWrapperFor(Class<?> iface) throws SQLException {
            return false;
        }

        public <T> T unwrap(Class<T> iface) throws SQLException {
            return null;
        }

        // ---------------------------
        // Java 1.7

		public Logger getParentLogger() throws SQLFeatureNotSupportedException {
			throw new SQLFeatureNotSupportedException();
		}

    }
    
}
