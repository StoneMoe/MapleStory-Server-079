package networking.packet;

import client.MapleCharacter;
import configuration.ServerProperties;
import constants.ServerConstants;
import handling.MaplePacket;
import handling.SendPacketOpcode;
import lombok.extern.slf4j.Slf4j;
import server.MapleCarnivalParty;
import networking.output.MaplePacketLittleEndianWriter;

@Slf4j
public class MonsterCarnivalPacket {
    public static MaplePacket startMonsterCarnival(final MapleCharacter chr, final int enemyavailable, final int enemytotal) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("startMonsterCarnival");
        }
        mplew.writeShort(SendPacketOpcode.MONSTER_CARNIVAL_START.getValue());
        final MapleCarnivalParty friendly = chr.getCarnivalParty();
        mplew.write(friendly.getTeam());
        mplew.writeShort(chr.getAvailableCP());
        mplew.writeShort(chr.getTotalCP());
        mplew.writeShort(friendly.getAvailableCP());
        mplew.writeShort(friendly.getTotalCP());
        mplew.writeShort(enemyavailable);
        mplew.writeShort(enemytotal);
        mplew.writeLong(0L);
        mplew.writeShort(0);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MonsterCarnivalPacket 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket playerDiedMessage(final String name, final int lostCP, final int team) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("playerDiedMessage");
        }
        mplew.writeShort(SendPacketOpcode.MONSTER_CARNIVAL_DIED.getValue());
        mplew.write(team);
        mplew.write(lostCP);
        mplew.writeMapleAsciiString(name);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MonsterCarnivalPacket 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket CPUpdate(final boolean party, final int curCP, final int totalCP, final int team) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("CPUpdate");
        }
        if (!party) {
            mplew.writeShort(SendPacketOpcode.MONSTER_CARNIVAL_OBTAINED_CP.getValue());
        } else {
            mplew.writeShort(SendPacketOpcode.MONSTER_CARNIVAL_PARTY_CP.getValue());
            mplew.write(team);
        }
        mplew.writeShort(curCP);
        mplew.writeShort(totalCP);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MonsterCarnivalPacket 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket playerSummoned(final String name, final int tab, final int number) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("playerSummoned");
        }
        mplew.writeShort(SendPacketOpcode.MONSTER_CARNIVAL_SUMMON.getValue());
        mplew.write(tab);
        mplew.write(number);
        mplew.writeMapleAsciiString(name);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MonsterCarnivalPacket 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }
}
