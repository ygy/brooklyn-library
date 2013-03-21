package brooklyn.demo;

import static brooklyn.entity.java.JavaEntityMethods.javaSysProp
import static brooklyn.event.basic.DependentConfiguration.attributeWhenReady
import static brooklyn.event.basic.DependentConfiguration.formatString

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import brooklyn.entity.basic.ApplicationBuilder
import brooklyn.entity.basic.Entities
import brooklyn.entity.basic.StartableApplication
import brooklyn.entity.database.mysql.MySqlNode
import brooklyn.entity.webapp.ControlledDynamicWebAppCluster
import brooklyn.entity.webapp.DynamicWebAppCluster
import brooklyn.launcher.BrooklynLauncherCli
import brooklyn.policy.autoscaling.AutoScalerPolicy
import brooklyn.util.CommandLineUtil

import com.google.common.collect.Lists

/**
 * Launches a 3-tier app with nginx, clustered jboss, and mysql.
 * <p>
 * This variant of {@link WebClusterDatabaseExample} demonstrates <i>Groovy</i> language conveniences.
 **/
public class WebClusterDatabaseExampleGroovy extends ApplicationBuilder {
    
    public static final Logger LOG = LoggerFactory.getLogger(WebClusterDatabaseExampleGroovy.class);
    
    public static final String DEFAULT_LOCATION = "localhost";

    public static final String WAR_PATH = "classpath://hello-world-sql-webapp.war";
    
    public static final String DB_SETUP_SQL_URL = "classpath://visitors-creation-script.sql";
    
    public static final String DB_TABLE = "visitors";
    public static final String DB_USERNAME = "brooklyn";
    public static final String DB_PASSWORD = "br00k11n";
    
    protected void doBuild() {
        MySqlNode mysql = addChild(MySqlNode,
                creationScriptUrl: DB_SETUP_SQL_URL);
        
        ControlledDynamicWebAppCluster web = addChild(ControlledDynamicWebAppCluster,
                war: WAR_PATH,
                httpPort: "8080+",
                (javaSysProp("brooklyn.example.db.url")): 
                    formatString("jdbc:%s%s?user=%s\\&password=%s", 
                            attributeWhenReady(mysql, MySqlNode.MYSQL_URL), 
                            DB_TABLE, DB_USERNAME, DB_PASSWORD));
    
        web.getCluster().addPolicy(AutoScalerPolicy.builder().
                metric(DynamicWebAppCluster.AVERAGE_REQUESTS_PER_SECOND).
                sizeRange(1, 5).
                metricRange(10, 100).
                build());
    }

    public static void main(String[] argv) {
        List<String> args = Lists.newArrayList(argv);
        String port =  CommandLineUtil.getCommandLineOption(args, "--port", "8081+");
        String location = CommandLineUtil.getCommandLineOption(args, "--location", DEFAULT_LOCATION);

        BrooklynLauncherCli launcher = BrooklynLauncherCli.newInstance()
                .application(new WebClusterDatabaseExampleGroovy().appDisplayName("Brooklyn WebApp Cluster with Database example"))
                .webconsolePort(port)
                .location(location)
                .start();
         
        Entities.dumpInfo(launcher.getApplications());
    }
    
}
