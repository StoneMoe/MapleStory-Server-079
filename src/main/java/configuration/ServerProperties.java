package configuration;

import database.DatabaseConnection;
import lombok.extern.slf4j.Slf4j;
import utils.PropertiesUtil;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Paths;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;

@Slf4j
public class ServerProperties {
    private static final Properties props = PropertiesUtil.loadFromFile(Paths.get(EnvProperties.cfgPath, "server.properties"));

    public static int MLevel = Integer.parseInt(props.getProperty("RoyMS.MLevel"));
    public static int QLevel = Integer.parseInt(props.getProperty("RoyMS.QLevel"));
    public static boolean LogPkt = Boolean.parseBoolean(props.getProperty("RoyMS.LogPkt"));
    public static boolean LogPktCall = Boolean.parseBoolean(props.getProperty("RoyMS.LogPktCall"));
    public static boolean LogClientErr = Boolean.parseBoolean(props.getProperty("RoyMS.LogClientErr"));
    public static boolean AutoRegister = Boolean.parseBoolean(props.getProperty("RoyMS.AutoRegister"));
    public static boolean Debug = Boolean.parseBoolean(props.getProperty("RoyMS.Debug"));
    public static short CSPort = Short.parseShort(props.getProperty("RoyMS.CSPort", "5200"));
    public static InetAddress IP;
    public static int Count = Integer.parseInt(props.getProperty("RoyMS.Count", "0"));
    public static int Exp = Integer.parseInt(props.getProperty("RoyMS.Exp"));
    public static int Meso = Integer.parseInt(props.getProperty("RoyMS.Meso"));
    public static int Drop = Integer.parseInt(props.getProperty("RoyMS.Drop"));
    public static int BDrop = Integer.parseInt(props.getProperty("RoyMS.BDrop"));
    public static int Cash = Integer.parseInt(props.getProperty("RoyMS.Cash"));
    public static String ServerMessage = props.getProperty("RoyMS.ServerMessage");
    public static String ServerName = props.getProperty("RoyMS.ServerName");
    public static int WFlags = Integer.parseInt(props.getProperty("RoyMS.WFlags", "0"));
    public static boolean Admin = Boolean.parseBoolean(props.getProperty("RoyMS.Admin"));
    public static String Events = props.getProperty("RoyMS.Events");
    // TODO: PortN
    public static boolean warpcsshop = Boolean.parseBoolean(props.getProperty("RoyMS.warpcsshop"));
    public static boolean warpmts = Boolean.parseBoolean(props.getProperty("RoyMS.warpmts"));
    public static int userLimit = Integer.parseInt(props.getProperty("RoyMS.userLimit"));
    public static String EventMessage = props.getProperty("RoyMS.EventMessage");
    public static Byte Flag = Byte.parseByte(props.getProperty("RoyMS.Flag"));
    public static int LPort = Integer.parseInt(props.getProperty("RoyMS.LPort"));
    public static int MaxCharacters = Integer.parseInt(props.getProperty("RoyMS.MaxCharacters"));
    public static int personPVP = Integer.parseInt(props.getProperty("RoyMS.personPVP"));
    public static int teamPVP = Integer.parseInt(props.getProperty("RoyMS.teamPVP"));
    public static int familyPVP = Integer.parseInt(props.getProperty("RoyMS.familyPVP"));
    public static boolean mxj = Boolean.parseBoolean(props.getProperty("RoyMS.mxj"));
    public static boolean qst = Boolean.parseBoolean(props.getProperty("RoyMS.qst"));
    public static boolean zs = Boolean.parseBoolean(props.getProperty("RoyMS.zs"));
    public static boolean DetectEquipCloning = Boolean.parseBoolean(props.getProperty("RoyMS.检测复制装备"));
    public static boolean 防万能检测 = Boolean.parseBoolean(props.getProperty("RoyMS.防万能检测"));
    public static int AutoSaveMinutes = Integer.parseInt(props.getProperty("RoyMS.AutoSaveMinutes", "5"));
    public static String LotteryMessage = props.getProperty("RoyMS.LotteryMessage");
    public static String cashban = props.getProperty("cashbana", "");
    public static String cashjy = props.getProperty("cashjy", "");
    public static String gysj = props.getProperty("gysj", "");

    static {
        // IP
        try {
            IP = InetAddress.getByName(props.getProperty("RoyMS.IP"));
        } catch (UnknownHostException e) {
            log.info("无效的 IP", e);
        }

        // Channel
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT * FROM auth_server_channel_ip");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                props.put(rs.getString("name") + rs.getInt("channelid"), rs.getString("value"));
            }
            rs.close();
            ps.close();
        } catch (Exception ex) {
            log.error("Channel config loading failed", ex);
            System.exit(0);
        }
    }

    public static String getProperty(final String s) {
        return ServerProperties.props.getProperty(s);
    }

    public static String getProperty(final String s, final String def) {
        return ServerProperties.props.getProperty(s, def);
    }

    public static void setProperty(final String prop, final String newInf) {
        ServerProperties.props.setProperty(prop, newInf);
    }

}
