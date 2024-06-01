package constants;

import lombok.Getter;
import configuration.ServerProperties;

public class ServerConstants
{
    public static boolean PollEnabled = false;
    public static String Poll_Question = "Are you mudkiz?";
    public static String[] Poll_Answers = new String[] { "test1", "test2", "test3" };
    public static MapleType MAPLE_TYPE = MapleType.CMS;
    public static short MAPLE_VERSION = 79;
    public static String MAPLE_PATCH = "1";
    public static boolean Use_Fixed_IV = false;
    public static int MIN_MTS = 110;
    public static int MTS_BASE = 100;
    public static int MTS_TAX = 10;
    public static int MTS_MESO = 5000;
    public static boolean Super_password = false;
    public static boolean clientAutoDisconnect = true;
    public static String superpw = "";
    public static String PACKET_ERROR = "";
    public static boolean loadop = true;
    
    public static void setPACKET_ERROR(final String ERROR) {
        ServerConstants.PACKET_ERROR = ERROR;
    }
    
    public static String getPACKET_ERROR() {
        return ServerConstants.PACKET_ERROR;
    }

    public static byte Class_Bonus_EXP(final int job) {
        switch (job) {
            case 3000:
            case 3200:
            case 3210:
            case 3211:
            case 3212:
            case 3300:
            case 3310:
            case 3311:
            case 3312:
            case 3500:
            case 3510:
            case 3511:
            case 3512: {
                return 10;
            }
            default: {
                return 0;
            }
        }
    }

    public enum PlayerGMRank
    {
        NORMAL('@', 0), 
        INTERN('!', 1), 
        GM('!', 2), 
        ADMIN('!', 3);
        
        private final char commandPrefix;
        private final int level;
        
        private PlayerGMRank(final char ch, final int level) {
            this.commandPrefix = ch;
            this.level = level;
        }
        
        public char getCommandPrefix() {
            return this.commandPrefix;
        }
        
        public int getLevel() {
            return this.level;
        }
    }

    @Getter
    public enum CommandType
    {
        PlayerCommand('@', 0),
        GMCommand('!', 2);

        private final char prefix;
        private final int gmLevel;

        CommandType(final char prefix, final int gmLevel)
        {
            this.prefix = prefix;
            this.gmLevel = gmLevel;
        }

        public static CommandType getByPrefix(char prefix) {
            for (CommandType type : values()) {
                if (type.getPrefix() == prefix)
                    return type;
            }
            return null;
        }
    }
    
    public enum MapleType
    {
        CMS(4, "GB18030");
        
        final byte type;
        final String ascii;
        
        private MapleType(final int type, final String ascii) {
            this.type = (byte)type;
            this.ascii = ascii;
        }
        
        public String getAscii() {
            return this.ascii;
        }
        
        public byte getType() {
            return this.type;
        }
        
        public static MapleType getByType(final byte type) {
            for (final MapleType l : values()) {
                if (l.getType() == type) {
                    return l;
                }
            }
            return MapleType.CMS;
        }
    }
}
