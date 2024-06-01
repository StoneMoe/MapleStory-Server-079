package networking.packet;

import client.MapleCharacter;
import client.MapleClient;
import configuration.ServerProperties;
import constants.GameConstants;
import constants.ServerConstants;
import handling.MaplePacket;
import handling.SendPacketOpcode;
import handling.login.LoginServer;
import handling.login.handler.Balloon;

import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import utils.HexTool;
import networking.output.MaplePacketLittleEndianWriter;

@Slf4j
public class LoginPacket {
    public static MaplePacket getHello(final short mapleVersion, final byte[] sendIv, final byte[] recvIv) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(16);
        if (ServerProperties.LogPktCall) {
            log.info("getHello");
        }
        mplew.writeShort(13);
        mplew.writeShort(mapleVersion);
        mplew.write(new byte[]{0, 0});
        mplew.write(recvIv);
        mplew.write(sendIv);
        mplew.write(4);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("LoginPacket 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket getPing() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(16);
        if (ServerProperties.LogPktCall) {
            log.info("getPing");
        }
        mplew.writeShort(SendPacketOpcode.PING.getValue());
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("LoginPacket 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket StrangeDATA() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(16);
        if (ServerProperties.LogPktCall) {
            log.info("StrangeDATA");
        }
        mplew.writeShort(18);
        mplew.writeMapleAsciiString("30819F300D06092A864886F70D010101050003818D0030818902818100994F4E66B003A7843C944E67BE4375203DAA203C676908E59839C9BADE95F53E848AAFE61DB9C09E80F48675CA2696F4E897B7F18CCB6398D221C4EC5823D11CA1FB9764A78F84711B8B6FCA9F01B171A51EC66C02CDA9308887CEE8E59C4FF0B146BF71F697EB11EDCEBFCE02FB0101A7076A3FEB64F6F6022C8417EB6B87270203010001");
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("LoginPacket 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket genderNeeded(final MapleClient c) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
        if (ServerProperties.LogPktCall) {
            log.info("genderNeeded");
        }
        mplew.writeShort(SendPacketOpcode.CHOOSE_GENDER.getValue());
        mplew.writeMapleAsciiString(c.getAccountName());
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("LoginPacket 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket getLoginFailed(final int reason) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(16);
        if (ServerProperties.LogPktCall) {
            log.info("getLoginFailed");
        }
        mplew.writeShort(SendPacketOpcode.LOGIN_STATUS.getValue());
        mplew.writeInt(reason);
        mplew.writeShort(0);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("LoginPacket 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket getPermBan(final byte reason) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(16);
        if (ServerProperties.LogPktCall) {
            log.info("getPermBan");
        }
        mplew.writeShort(SendPacketOpcode.LOGIN_STATUS.getValue());
        mplew.writeShort(2);
        mplew.write(0);
        mplew.write(reason);
        mplew.write(HexTool.getByteArrayFromHexString("01 01 01 01 00"));
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("LoginPacket 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket getTempBan(final long timestampTill, final byte reason) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(17);
        if (ServerProperties.LogPktCall) {
            log.info("getTempBan");
        }
        mplew.writeShort(SendPacketOpcode.LOGIN_STATUS.getValue());
        mplew.write(2);
        mplew.write(HexTool.getByteArrayFromHexString("00 00 00 00 00"));
        mplew.write(reason);
        mplew.writeLong(timestampTill);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("LoginPacket 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket getGenderChanged(final MapleClient client) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("getGenderChanged");
        }
        mplew.writeShort(SendPacketOpcode.GENDER_SET.getValue());
        mplew.write(client.getGender());
        mplew.writeMapleAsciiString(client.getAccountName());
        mplew.writeMapleAsciiString(String.valueOf(client.getAccID()));
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("LoginPacket 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket getGenderNeeded(final MapleClient client) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("getGenderNeeded");
        }
        mplew.writeShort(SendPacketOpcode.CHOOSE_GENDER.getValue());
        mplew.writeMapleAsciiString(client.getAccountName());
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("LoginPacket 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket getAuthSuccessRequest(final MapleClient client) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("getAuthSuccessRequest");
        }
        mplew.writeShort(SendPacketOpcode.LOGIN_STATUS.getValue());
        mplew.write(0);
        mplew.writeInt(client.getAccID());
        mplew.write(client.getGender());
        mplew.writeShort(client.isGm() ? 1 : 0);
        mplew.writeMapleAsciiString(client.getAccountName());
        mplew.write(HexTool.getByteArrayFromHexString("00 00 00 03 01 00 00 00 E2 ED A3 7A FA C9 01"));
        mplew.writeInt(0);
        mplew.writeLong(0L);
        mplew.writeMapleAsciiString(String.valueOf(client.getAccID()));
        mplew.writeMapleAsciiString(client.getAccountName());
        mplew.write(1);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("LoginPacket 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket getServerList(final int serverId, final String serverName, final Map<Integer, Integer> channelLoad) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("getServerList");
        }
        mplew.writeShort(SendPacketOpcode.SERVERLIST.getValue());
        mplew.write(serverId);
        mplew.writeMapleAsciiString(serverName);
        mplew.write(LoginServer.getFlag());
        mplew.writeMapleAsciiString(LoginServer.getEventMessage());
        mplew.writeShort(100);
        mplew.writeShort(100);
        int lastChannel = 1;
        final Set<Integer> channels = channelLoad.keySet();
        for (int i = 30; i > 0; --i) {
            if (channels.contains(i)) {
                lastChannel = i;
                break;
            }
        }
        mplew.write(lastChannel);
        mplew.writeInt(500);
        for (int j = 1; j <= lastChannel; ++j) {
            int load;
            if (channels.contains(j)) {
                load = channelLoad.get(j);
            } else {
                load = 1200;
            }
            mplew.writeMapleAsciiString(serverName + "-" + j);
            mplew.writeInt(load);
            mplew.write(serverId);
            mplew.writeShort(j - 1);
        }
        mplew.writeShort(GameConstants.getBalloons().size());
        for (final Balloon balloon : GameConstants.getBalloons()) {
            mplew.writeShort(balloon.nX);
            mplew.writeShort(balloon.nY);
            mplew.writeMapleAsciiString(balloon.sMessage);
        }
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("LoginPacket 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket getEndOfServerList() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("getEndOfServerList");
        }
        mplew.writeShort(SendPacketOpcode.SERVERLIST.getValue());
        mplew.write(255);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("LoginPacket 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket getServerStatus(final int status) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("getServerStatus");
        }
        mplew.writeShort(SendPacketOpcode.SERVERSTATUS.getValue());
        mplew.writeShort(status);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("LoginPacket 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket getCharList(final boolean secondpw, final List<MapleCharacter> chars, final int charslots) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("getCharList");
        }
        mplew.writeShort(SendPacketOpcode.CHARLIST.getValue());
        mplew.write(0);
        mplew.writeInt(0);
        mplew.write(chars.size());
        for (final MapleCharacter chr : chars) {
            addCharEntry(mplew, chr, !chr.isGM() && chr.getLevel() >= 10, false);
        }
        mplew.writeShort(3);
        mplew.writeInt(charslots);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("LoginPacket 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket addNewCharEntry(final MapleCharacter chr, final boolean worked) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("addNewCharEntry");
        }
        mplew.writeShort(SendPacketOpcode.ADD_NEW_CHAR_ENTRY.getValue());
        mplew.write(worked ? 0 : 1);
        addCharEntry(mplew, chr, false, false);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("LoginPacket 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket charNameResponse(final String charname, final boolean nameUsed) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("charNameResponse");
        }
        mplew.writeShort(SendPacketOpcode.CHAR_NAME_RESPONSE.getValue());
        mplew.writeMapleAsciiString(charname);
        mplew.write(nameUsed ? 1 : 0);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("LoginPacket 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    private static void addCharEntry(final MaplePacketLittleEndianWriter mplew, final MapleCharacter chr, final boolean ranking, final boolean viewAll) {
        if (ServerProperties.LogPktCall) {
            log.info("addCharEntry");
        }
        PacketHelper.addCharStats(mplew, chr);
        PacketHelper.addCharLook(mplew, chr, true, viewAll);
        mplew.write(0);
        if (chr.getJob() == 900) {
            mplew.write(2);
        }
    }
}
