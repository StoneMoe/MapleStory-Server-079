package networking.packet;

import configuration.ServerProperties;
import constants.ServerConstants;
import handling.MaplePacket;
import handling.SendPacketOpcode;
import lombok.extern.slf4j.Slf4j;
import networking.output.MaplePacketLittleEndianWriter;

@Slf4j
public class MonsterBookPacket {
    public static MaplePacket addCard(final boolean full, final int cardid, final int level) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("addCard");
        }
        mplew.writeShort(SendPacketOpcode.MONSTERBOOK_ADD.getValue());
        if (!full) {
            mplew.write(1);
            mplew.writeInt(cardid);
            mplew.writeInt(level);
        } else {
            mplew.write(0);
        }
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MonsterBookPacket 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket showGainCard(final int itemid) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("showGainCard");
        }
        mplew.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
        mplew.write(15);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MonsterBookPacket 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket showForeginCardEffect(final int id) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("showForeginCardEffect");
        }
        mplew.writeShort(SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
        mplew.writeInt(id);
        mplew.write(13);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MonsterBookPacket 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket changeCover(final int cardid) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("changeCover");
        }
        mplew.writeShort(SendPacketOpcode.MONSTERBOOK_CHANGE_COVER.getValue());
        mplew.writeInt(cardid);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MonsterBookPacket 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }
}
