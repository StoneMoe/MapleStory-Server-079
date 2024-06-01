package configuration;

import utils.PropertiesUtil;

import java.nio.file.Paths;
import java.util.Properties;

public class DBProperties {
    private static final Properties props = PropertiesUtil.loadFromFile(Paths.get(EnvProperties.cfgPath, "db.properties"));

    public static String driverClassName = props.getProperty("driverClassName");
    public static String url = props.getProperty("url");
    public static String username = props.getProperty("username");
    public static String password = props.getProperty("password");
    public static long timeout = Long.parseLong(props.getProperty("timeout"));

}
