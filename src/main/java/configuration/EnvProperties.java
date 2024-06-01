package configuration;

public class EnvProperties {
    public static String cfgPath = System.getProperty("cfgPath", "./config");
    public static String scriptsPath = System.getProperty("scriptsPath", "./data/scripts");
    public static String wzPath = System.getProperty("wzPath", "./data/wz");
}
