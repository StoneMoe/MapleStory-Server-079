package networking.packet;

import configuration.ServerProperties;
import constants.ServerConstants;
import handling.MaplePacket;
import handling.SendPacketOpcode;
import lombok.extern.slf4j.Slf4j;
import networking.output.MaplePacketLittleEndianWriter;

@Slf4j
public class UIPacket {
    public static MaplePacket getSPMsg(final byte sp, final short job) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("getSPMsg");
        }
        mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(3);
        mplew.writeShort(job);
        mplew.write(sp);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("UIPacket 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket getGPMsg(final int itemid) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("getGPMsg");
        }
        mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(6);
        mplew.writeInt(itemid);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("UIPacket 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket getTopMsg(final String msg) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("getTopMsg");
        }
        mplew.writeShort(SendPacketOpcode.TOP_MSG.getValue());
        mplew.writeMapleAsciiString(msg);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("UIPacket 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket getStatusMsg(final int itemid) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("getStatusMsg");
        }
        mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(7);
        mplew.writeInt(itemid);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("UIPacket 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket MapEff(final String path) {
        if (ServerProperties.LogPktCall) {
            log.info("MapEff");
        }
        return MaplePacketCreator.environmentChange(path, 3);
    }

    public static MaplePacket MapNameDisplay(final int mapid) {
        if (ServerProperties.LogPktCall) {
            log.info("MapNameDisplay");
        }
        return MaplePacketCreator.environmentChange("maplemap/enter/" + mapid, 3);
    }

    public static MaplePacket Aran_Start() {
        if (ServerProperties.LogPktCall) {
            log.info("Aran_Start");
        }
        return MaplePacketCreator.environmentChange("Aran/balloon", 4);
    }

    public static MaplePacket AranTutInstructionalBalloon(final String data) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("AranTutInstructionalBalloon");
        }
        mplew.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
        mplew.write(35);
        mplew.writeMapleAsciiString(data);
        mplew.writeInt(1);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("UIPacket 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket ShowWZEffect(final String data, final int info) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("ShowWZEffect");
        }
        mplew.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
        if (info == -1) {
            mplew.write(18);
        } else {
            mplew.write(23);
        }
        mplew.writeMapleAsciiString(data);
        if (info > -1) {
            mplew.writeInt(info);
        }
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("UIPacket 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket ShowWZEffectS(final String data, final int info) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
        mplew.write(20);
        mplew.writeMapleAsciiString(data);
        if (info > -1) {
            mplew.writeInt(info);
        }
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("UIPacket 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket summonHelper(final boolean summon) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("summonHelper");
        }
        mplew.writeShort(SendPacketOpcode.SUMMON_HINT.getValue());
        mplew.write(summon ? 1 : 0);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("UIPacket 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket summonMessage(final int type) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("summonMessageA");
        }
        mplew.writeShort(SendPacketOpcode.SUMMON_HINT_MSG.getValue());
        mplew.write(1);
        mplew.writeInt(type);
        mplew.writeInt(7000);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("UIPacket 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket summonMessage(final String message) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("summonMessageB");
        }
        mplew.writeShort(SendPacketOpcode.SUMMON_HINT_MSG.getValue());
        mplew.write(0);
        mplew.writeMapleAsciiString(message);
        mplew.writeInt(200);
        mplew.writeShort(0);
        mplew.writeInt(10000);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("UIPacket 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket IntroLock(final boolean enable) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("IntroLock");
        }
        mplew.writeShort(SendPacketOpcode.CYGNUS_INTRO_LOCK.getValue());
        mplew.write(enable ? 1 : 0);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("UIPacket 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket IntroDisableUI(final boolean enable) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("IntroDisableUI");
        }
        mplew.writeShort(SendPacketOpcode.CYGNUS_INTRO_DISABLE_UI.getValue());
        mplew.write(enable ? 1 : 0);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("UIPacket 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket fishingUpdate(final byte type, final int id) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("fishingUpdate");
        }
        mplew.writeShort(SendPacketOpcode.FISHING_BOARD_UPDATE.getValue());
        mplew.write(type);
        mplew.writeInt(id);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("UIPacket 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket fishingCaught(final int chrid) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("fishingCaught");
        }
        mplew.writeShort(SendPacketOpcode.FISHING_CAUGHT.getValue());
        mplew.writeInt(chrid);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("UIPacket 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }
}
