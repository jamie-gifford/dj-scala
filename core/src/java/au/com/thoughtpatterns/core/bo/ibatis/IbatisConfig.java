package au.com.thoughtpatterns.core.bo.ibatis;

import java.io.Serializable;
import java.io.StringReader;

import au.com.thoughtpatterns.core.util.Logger;
import au.com.thoughtpatterns.core.util.Parameters;
import au.com.thoughtpatterns.core.util.Resources;

import com.ibatis.sqlmap.client.SqlMapClient;
import com.ibatis.sqlmap.client.SqlMapClientBuilder;

/**
 * Configures Apache Ibatis based on runtime parameters and configuration
 * files.
 * 
 * Apache Ibatis must be configured from XML resources before use. This is 
 * actually a pain - it would be much better if it also provided a programmatic
 * configuration API. 
 * 
 * This class provides Ibatis with XML resources, to define the overall
 * Ibatis configuration and also the per-PersistentObject sqlMap definitions.
 * The main XML definition is loaded from a resource determined by the runtime
 * parameter
 * <code>au.com.thoughtpatterns.core.bo.ibatis.IbatisBox.config</code>
 * 
 * Also, the value of the runtime parameter 
 * <code>au.com.thoughtpatterns.core.bo.ibatis.IbatisBox.maps</code>
 * is taken as a comma-separated list of sqlMap definitions, and spliced
 * into the mail configuration.
 * 
 * <p />
 * This class provides a singleton instance which is the normal
 * instance to use. It is also possible to create new instances 
 * (for unit testing, for instance). 
 */
public class IbatisConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger log = Logger.get(IbatisConfig.class);

    /**
     * Global singleton instance. 
     */
    private static final IbatisConfig INSTANCE = new IbatisConfig();
   
    /**
     * Synthesized config for creating SqlMapClient.
     */
    private String sqlConfig;
    
    /**
     * This is the interface with Ibatis itself, used for executing SQL
     */
    private transient SqlMapClient sqlMapper;

    public static IbatisConfig instance() {
        return INSTANCE;
    }
    
    /**
     * Create an IbatisConfig, configuring Ibatis according to runtime parameters
     * and associated XML resources.
     */
    public IbatisConfig() {
        // It's worth doing some logging here, just in case 
        // the logging infrastructure hasn't been used yet, since Ibatis will want to
        // see an initialised log4j config.
        log.debug("Constructing");
        
        Parameters params = Parameters.instance();

        // Get the name of the main Ibatis config file. 
        String sqlConfigName = params.get("au.com.thoughtpatterns.core.bo.ibatis.IbatisBox.config");
        
        // Load it as a classpath resource.
        sqlConfig = Resources.getResourceAsString(null, sqlConfigName);
        
        // The main config should contain the marker string "$maps", in which case
        // we substitute a bunch of dynamically generated sqlMap references

        String[] sqlMaps = params.getArray("au.com.thoughtpatterns.core.bo.ibatis.IbatisBox.maps");
        StringBuffer additional = new StringBuffer();
        for (String sqlMap : sqlMaps) {
            additional.append("  <sqlMap resource=\"" + sqlMap + "\"/>\n");
            log.debug("Configuring Ibatis with sqlMap resource " + sqlMap);
        }
        
        String maps = additional.toString();
        sqlConfig = sqlConfig.replaceAll("\\$maps", maps);
    }
    
    public SqlMapClient getSqlMapClient() {
        if (sqlMapper == null) {
            // Feed the dynamically generated config into Ibatis and 
            // create the SqlMapClient.
            StringReader reader = new StringReader(sqlConfig);        
            sqlMapper = SqlMapClientBuilder.buildSqlMapClient(reader);
        }
        return sqlMapper;
    }

}
