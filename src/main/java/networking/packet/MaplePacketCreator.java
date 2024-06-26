package networking.packet;

import client.*;
import client.inventory.*;
import configuration.ServerProperties;
import constants.GameConstants;
import constants.ServerConstants;
import handling.ByteArrayMaplePacket;
import handling.MaplePacket;
import handling.SendPacketOpcode;
import handling.channel.MapleGuildRanking;
import handling.channel.handler.DamageParse;
import handling.channel.handler.InventoryHandler;
import handling.world.MapleParty;
import handling.world.MaplePartyCharacter;
import handling.world.PartyOperation;
import handling.world.World;
import handling.world.guild.MapleBBSThread;
import handling.world.guild.MapleGuild;
import handling.world.guild.MapleGuildAlliance;
import handling.world.guild.MapleGuildCharacter;
import lombok.extern.slf4j.Slf4j;
import server.*;
import server.events.MapleSnowball;
import server.life.MapleNPC;
import server.life.PlayerNPC;
import server.life.SummonAttackEntry;
import server.maps.*;
import server.movement.LifeMovementFragment;
import server.shops.HiredMerchant;
import server.shops.MaplePlayerShopItem;
import networking.output.LittleEndianWriter;
import networking.output.MaplePacketLittleEndianWriter;
import utils.datastructures.Pair;
import utils.*;

import java.awt.*;
import java.io.*;
import java.net.InetAddress;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.*;

@Slf4j
public class MaplePacketCreator {
    public static List<Pair<MapleStat, Integer>> EMPTY_STATUPDATE;
    private static final byte[] CHAR_INFO_MAGIC;
    private static final boolean showPacket = false;

    public static MaplePacket getServerIP(final int port, final int clientId) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("getServerIP");
        }
        mplew.writeShort(SendPacketOpcode.SERVER_IP.getValue());
        mplew.writeShort(0);
        mplew.write(ServerProperties.IP.getAddress());
        mplew.writeShort(port);
        mplew.writeInt(clientId);
        mplew.write(new byte[]{1, 0, 0, 0, 0});
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket getChannelChange(final InetAddress inetAddr, final int port) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("getChannelChange");
        }
        mplew.writeShort(SendPacketOpcode.CHANGE_CHANNEL.getValue());
        mplew.write(1);
        mplew.write(ServerProperties.IP.getAddress());
        mplew.writeShort(port);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket getCharInfo(final MapleCharacter chr) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("getCharInfo");
        }
        mplew.writeShort(SendPacketOpcode.WARP_TO_MAP.getValue());
        mplew.writeInt(chr.getClient().getChannel() - 1);
        mplew.write(0);
        mplew.write(1);
        mplew.write(1);
        mplew.writeShort(0);
        chr.CRand().connectData(mplew);
        PacketHelper.addCharacterInfo(mplew, chr);
        mplew.writeLong(PacketHelper.getTime(System.currentTimeMillis()));
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket enableActions() {
        if (ServerProperties.LogPktCall) {
            log.info("enableActions");
        }
        return updatePlayerStats(MaplePacketCreator.EMPTY_STATUPDATE, true, 0);
    }

    public static MaplePacket updatePlayerStats(final List<Pair<MapleStat, Integer>> stats, final int evan) {
        if (ServerProperties.LogPktCall) {
            log.info("updatePlayerStatsA");
        }
        return updatePlayerStats(stats, false, evan);
    }

    public static MaplePacket updatePlayerStats(final List<Pair<MapleStat, Integer>> stats, final boolean itemReaction, final int evan) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("updatePlayerStats");
        }
        mplew.writeShort(SendPacketOpcode.UPDATE_STATS.getValue());
        mplew.write(itemReaction ? 1 : 0);
        int updateMask = 0;
        for (final Pair<MapleStat, Integer> statupdate : stats) {
            updateMask |= statupdate.getLeft().getValue();
        }
        final List<Pair<MapleStat, Integer>> mystats = stats;
        if (mystats.size() > 1) {
            Collections.sort(mystats, new Comparator<Pair<MapleStat, Integer>>() {
                @Override
                public int compare(final Pair<MapleStat, Integer> o1, final Pair<MapleStat, Integer> o2) {
                    final int val1 = o1.getLeft().getValue();
                    final int val2 = o2.getLeft().getValue();
                    return (val1 < val2) ? -1 : ((val1 == val2) ? 0 : 1);
                }
            });
        }
        mplew.writeInt(updateMask);
        for (final Pair<MapleStat, Integer> statupdate2 : mystats) {
            if (statupdate2.getLeft().getValue() >= 1) {
                if (statupdate2.getLeft().getValue() == 1) {
                    mplew.writeShort(statupdate2.getRight().shortValue());
                } else if (statupdate2.getLeft().getValue() <= 4) {
                    mplew.writeInt(statupdate2.getRight());
                } else if (statupdate2.getLeft().getValue() < 128) {
                    mplew.write(statupdate2.getRight().shortValue());
                } else if (statupdate2.getLeft().getValue() < 262144) {
                    mplew.writeShort(statupdate2.getRight().shortValue());
                } else {
                    mplew.writeInt(statupdate2.getRight());
                }
            }
        }
        mplew.writeShort(0);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket blockedPortal() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("blockedPortal");
        }
        mplew.writeShort(SendPacketOpcode.UPDATE_STATS.getValue());
        mplew.write(1);
        mplew.writeInt(0);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket weirdStatUpdate() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("weirdStatUpdate");
        }
        mplew.writeShort(SendPacketOpcode.UPDATE_STATS.getValue());
        mplew.write(0);
        mplew.write(56);
        mplew.writeShort(0);
        mplew.writeLong(0L);
        mplew.writeLong(0L);
        mplew.writeLong(0L);
        mplew.write(0);
        mplew.write(1);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket updateSp(final MapleCharacter chr, final boolean itemReaction) {
        if (ServerProperties.LogPktCall) {
            log.info("updateSpA");
        }
        return updateSp(chr, itemReaction, false);
    }

    public static MaplePacket updateSp(final MapleCharacter chr, final boolean itemReaction, final boolean overrideJob) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("updateSp");
        }
        mplew.writeShort(SendPacketOpcode.UPDATE_STATS.getValue());
        mplew.write(itemReaction ? 1 : 0);
        mplew.writeInt(131072);
        mplew.writeShort(chr.getRemainingSp());
        mplew.writeShort(0);
        return mplew.getPacket();
    }

    public static MaplePacket getWarpToMap(final MapleMap to, final int spawnPoint, final MapleCharacter chr) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("getWarpToMap");
        }
        mplew.writeShort(SendPacketOpcode.WARP_TO_MAP.getValue());
        mplew.writeInt(chr.getClient().getChannel() - 1);
        mplew.write(0);
        mplew.write(3);
        mplew.writeShort(0);
        mplew.write(0);
        mplew.writeInt(to.getId());
        mplew.write(spawnPoint);
        mplew.writeShort(chr.getStat().getHp());
        mplew.writeLong(PacketHelper.getTime(System.currentTimeMillis()));
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket spawnPortal(final int townId, final int targetId, final int skillId, final Point pos) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("spawnPortal");
        }
        mplew.writeShort(SendPacketOpcode.SPAWN_PORTAL.getValue());
        mplew.writeInt(townId);
        mplew.writeInt(targetId);
        if (pos != null) {
            mplew.writePos(pos);
        }
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket spawnDoor(final int oid, final Point pos, final boolean town) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("spawnDoor");
        }
        mplew.writeShort(SendPacketOpcode.SPAWN_DOOR.getValue());
        mplew.write(town ? 1 : 0);
        mplew.writeInt(oid);
        mplew.writePos(pos);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket removeDoor(final int oid, final boolean town) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("removeDoor");
        }
        if (town) {
            mplew.writeShort(SendPacketOpcode.SPAWN_PORTAL.getValue());
            mplew.writeInt(999999999);
            mplew.writeInt(999999999);
        } else {
            mplew.writeShort(SendPacketOpcode.REMOVE_DOOR.getValue());
            mplew.write(0);
            mplew.writeInt(oid);
        }
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket spawnSummon(final MapleSummon summon, final boolean animated) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("spawnSummon");
        }
        mplew.writeShort(SendPacketOpcode.SPAWN_SUMMON.getValue());
        mplew.writeInt(summon.getOwnerId());
        mplew.writeInt(summon.getObjectId());
        mplew.writeInt(summon.getSkill());
        mplew.write(summon.getOwnerLevel());
        mplew.write(summon.getSkillLevel());
        mplew.writeShort(summon.getPosition().x);
        mplew.writeInt(summon.getPosition().y);
        mplew.write(0);
        mplew.write(summon.getMovementType().getValue());
        mplew.write(summon.getSummonType());
        mplew.write(animated ? 0 : 1);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket removeSummon(final MapleSummon summon, final boolean animated) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("removeSummon");
        }
        mplew.writeShort(SendPacketOpcode.REMOVE_SUMMON.getValue());
        mplew.writeInt(summon.getOwnerId());
        mplew.writeInt(summon.getObjectId());
        mplew.write(animated ? 4 : 1);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket serverMessage(final String message) {
        if (ServerProperties.LogPktCall) {
            log.info("serverMessageA");
        }
        return serverMessage(4, 0, message, false);
    }

    public static MaplePacket serverNotice(final int type, final String message) {
        if (ServerProperties.LogPktCall) {
            log.info("serverNoticeA");
        }
        return serverMessage(type, 0, message, false);
    }

    public static MaplePacket serverNotice(final int type, final int channel, final String message) {
        if (ServerProperties.LogPktCall) {
            log.info("serverNoticeB");
        }
        return serverMessage(type, channel, message, false);
    }

    public static MaplePacket serverNotice(final int type, final int channel, final String message, final boolean smegaEar) {
        if (ServerProperties.LogPktCall) {
            log.info("serverNoticeC");
        }
        return serverMessage(type, channel, message, smegaEar);
    }

    private static MaplePacket serverMessage(final int type, final int channel, final String message, final boolean megaEar) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("serverMessage");
        }
        mplew.writeShort(SendPacketOpcode.SERVERMESSAGE.getValue());
        mplew.write(type);
        if (type == 4) {
            mplew.write(1);
        }
        mplew.writeMapleAsciiString(message);
        switch (type) {
            case 3:
            case 9:
            case 10:
            case 11:
            case 12: {
                mplew.write(channel - 1);
                mplew.write(megaEar ? 1 : 0);
                break;
            }
            case 6:
            case 18: {
                mplew.writeInt((channel >= 1000000 && channel < 6000000) ? channel : 0);
                break;
            }
        }
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket getGachaponMega(final String name, final String message, final IItem item, final byte rareness, final int channel) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("getGachaponMega");
        }
        mplew.writeShort(SendPacketOpcode.SERVERMESSAGE.getValue());
        mplew.write(14);
        mplew.writeMapleAsciiString(name + message);
        mplew.writeInt(channel - 1);
        PacketHelper.addItemInfo(mplew, item, true, true);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket tripleSmega(final List<String> message, final boolean ear, final int channel) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("tripleSmega");
        }
        mplew.writeShort(SendPacketOpcode.SERVERMESSAGE.getValue());
        mplew.write(10);
        if (message.get(0) != null) {
            mplew.writeMapleAsciiString(message.get(0));
        }
        mplew.write(message.size());
        for (int i = 1; i < message.size(); ++i) {
            if (message.get(i) != null) {
                mplew.writeMapleAsciiString(message.get(i));
            }
        }
        mplew.write(channel - 1);
        mplew.write(ear ? 1 : 0);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket getAvatarMega(final MapleCharacter chr, final int channel, final int itemId, final String message, final boolean ear) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("getAvatarMega");
        }
        mplew.writeShort(SendPacketOpcode.AVATAR_MEGA.getValue());
        mplew.writeInt(itemId);
        mplew.writeMapleAsciiString(chr.getName());
        mplew.writeMapleAsciiString(message);
        mplew.writeInt(channel - 1);
        mplew.write(ear ? 1 : 0);
        PacketHelper.addCharLook(mplew, chr, true);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket itemMegaphone(final String msg, final boolean whisper, final int channel, final IItem item) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("itemMegaphone");
        }
        mplew.writeShort(SendPacketOpcode.SERVERMESSAGE.getValue());
        mplew.write(8);
        mplew.writeMapleAsciiString(msg);
        mplew.write(channel - 1);
        mplew.write(whisper ? 1 : 0);
        if (item == null) {
            mplew.write(0);
        } else {
            PacketHelper.addItemInfo(mplew, item, false, false, true);
        }
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket spawnNPC(final MapleNPC life, final boolean show) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("spawnNPC");
        }
        mplew.writeShort(SendPacketOpcode.SPAWN_NPC.getValue());
        mplew.writeInt(life.getObjectId());
        mplew.writeInt(life.getId());
        mplew.writeShort(life.getPosition().x);
        mplew.writeShort(life.getCy());
        mplew.write((life.getF() != 1) ? 1 : 0);
        mplew.writeShort(life.getFh());
        mplew.writeShort(life.getRx0());
        mplew.writeShort(life.getRx1());
        mplew.write(show ? 1 : 0);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket removeNPCController(final int objectid) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.SPAWN_NPC_REQUEST_CONTROLLER.getValue());
        mplew.write(0);
        mplew.writeInt(objectid);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket removeNPC(final int objectid) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("removeNPC");
        }
        mplew.writeShort(SendPacketOpcode.REMOVE_NPC.getValue());
        mplew.writeLong(objectid);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket spawnNPCRequestController(final MapleNPC life, final boolean MiniMap) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("spawnNPCRequestController");
        }
        mplew.writeShort(SendPacketOpcode.SPAWN_NPC_REQUEST_CONTROLLER.getValue());
        mplew.write(1);
        mplew.writeInt(life.getObjectId());
        mplew.writeInt(life.getId());
        mplew.writeShort(life.getPosition().x);
        mplew.writeShort(life.getCy());
        mplew.write((life.getF() != 1) ? 1 : 0);
        mplew.writeShort(life.getFh());
        mplew.writeShort(life.getRx0());
        mplew.writeShort(life.getRx1());
        mplew.write(MiniMap ? 1 : 0);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket spawnPlayerNPC(final PlayerNPC npc) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("spawnPlayerNPC");
        }
        mplew.writeShort(SendPacketOpcode.PLAYER_NPC.getValue());
        mplew.write((npc.getF() != 1) ? 1 : 0);
        mplew.writeInt(npc.getId());
        mplew.writeMapleAsciiString(npc.getName());
        mplew.write(npc.getGender());
        mplew.write(npc.getSkin());
        mplew.writeInt(npc.getFace());
        mplew.write(0);
        mplew.writeInt(npc.getHair());
        final Map<Byte, Integer> equip = npc.getEquips();
        final Map<Byte, Integer> myEquip = new LinkedHashMap<Byte, Integer>();
        final Map<Byte, Integer> maskedEquip = new LinkedHashMap<Byte, Integer>();
        for (final Map.Entry<Byte, Integer> position : equip.entrySet()) {
            byte pos = (byte) (position.getKey() * -1);
            if (pos < 100 && myEquip.get(pos) == null) {
                myEquip.put(pos, position.getValue());
            } else if ((pos > 100 || pos == -128) && pos != 111) {
                pos = (byte) ((pos == -128) ? 28 : (pos - 100));
                if (myEquip.get(pos) != null) {
                    maskedEquip.put(pos, myEquip.get(pos));
                }
                myEquip.put(pos, position.getValue());
            } else {
                if (myEquip.get(pos) == null) {
                    continue;
                }
                maskedEquip.put(pos, position.getValue());
            }
        }
        for (final Map.Entry<Byte, Integer> entry : myEquip.entrySet()) {
            mplew.write(entry.getKey());
            mplew.writeInt(entry.getValue());
        }
        mplew.write(255);
        for (final Map.Entry<Byte, Integer> entry : maskedEquip.entrySet()) {
            mplew.write(entry.getKey());
            mplew.writeInt(entry.getValue());
        }
        mplew.write(255);
        final Integer cWeapon = equip.get(-111);
        if (cWeapon != null) {
            mplew.writeInt(cWeapon);
        } else {
            mplew.writeInt(0);
        }
        for (int i = 0; i < 3; ++i) {
            mplew.writeInt(npc.getPet(i));
        }
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket getChatText(final int cidfrom, final String text, final boolean whiteBG, final int show) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("getChatText");
        }
        mplew.writeShort(SendPacketOpcode.CHATTEXT.getValue());
        mplew.writeInt(cidfrom);
        mplew.write(whiteBG ? 1 : 0);
        mplew.writeMapleAsciiString(text);
        mplew.write(show);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket GameMaster_Func(final int value) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("GameMaster_Func");
        }
        mplew.writeShort(SendPacketOpcode.GM_EFFECT.getValue());
        mplew.write(value);
        mplew.writeZeroBytes(17);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket getPacketFromHexString(final String hex) {
        if (ServerProperties.LogPktCall) {
            log.info("getPacketFromHexString");
        }
        return new ByteArrayMaplePacket(HexTool.getByteArrayFromHexString(hex));
    }

    public static MaplePacket GainEXP_Monster(final int gain, final boolean white, final int 结婚奖励经验值, final int 组队经验值, final int Class_Bonus_EXP, final int 道具佩戴附加经验值, final int 网吧特别经验) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("GainEXP_Monster");
        }
        mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(3);
        mplew.write(white ? 1 : 0);
        mplew.writeInt(gain);
        mplew.write(0);
        mplew.writeInt(0);
        mplew.writeShort(0);
        mplew.writeInt(结婚奖励经验值);
        mplew.write(0);
        mplew.writeInt(组队经验值);
        mplew.writeInt(道具佩戴附加经验值);
        mplew.writeInt(网吧特别经验);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket GainEXP_Others(final int gain, final boolean inChat, final boolean white) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("GainEXP_Others");
        }
        mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(3);
        mplew.write(white ? 1 : 0);
        mplew.writeInt(gain);
        mplew.write(0);
        mplew.writeInt(inChat ? 1 : 0);
        mplew.writeShort(0);
        mplew.writeZeroBytes(4);
        if (inChat) {
            mplew.writeZeroBytes(13);
        } else {
            mplew.writeZeroBytes(13);
        }
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket getShowFameGain(final int gain) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("getShowFameGain");
        }
        mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(4);
        mplew.writeInt(gain);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket showMesoGain(final int gain, final boolean inChat) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("showMesoGain");
        }
        mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        if (!inChat) {
            mplew.write(0);
            mplew.write(1);
            mplew.write(0);
        } else {
            mplew.write(5);
        }
        mplew.writeInt(gain);
        mplew.writeShort(0);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket getShowItemGain(final int itemId, final short quantity) {
        if (ServerProperties.LogPktCall) {
            log.info("getShowItemGainA");
        }
        return getShowItemGain(itemId, quantity, false);
    }

    public static MaplePacket getShowItemGain(final int itemId, final short quantity, final boolean inChat) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("getShowItemGain");
        }
        if (inChat) {
            mplew.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
            mplew.write(3);
            mplew.write(1);
            mplew.writeInt(itemId);
            mplew.writeInt(quantity);
        } else {
            mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
            mplew.writeShort(0);
            mplew.writeInt(itemId);
            mplew.writeInt(quantity);
            mplew.writeInt(0);
            mplew.writeInt(0);
        }
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket showRewardItemAnimation(final int itemId, final String effect) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("showRewardItemAnimationA");
        }
        mplew.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
        mplew.write(11);
        mplew.writeInt(itemId);
        mplew.write((effect != null && effect.length() > 0) ? 1 : 0);
        if (effect != null && effect.length() > 0) {
            mplew.writeMapleAsciiString(effect);
        }
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket showRewardItemAnimation(final int itemId, final String effect, final int from_playerid) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("showRewardItemAnimationB");
        }
        mplew.writeShort(SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
        mplew.writeInt(from_playerid);
        mplew.write(11);
        mplew.writeInt(itemId);
        mplew.write((effect != null && effect.length() > 0) ? 1 : 0);
        if (effect != null && effect.length() > 0) {
            mplew.writeMapleAsciiString(effect);
        }
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket dropItemFromMapObject(final MapleMapItem drop, final Point dropfrom, final Point dropto, final byte mod) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("dropItemFromMapObject");
        }
        mplew.writeShort(SendPacketOpcode.DROP_ITEM_FROM_MAPOBJECT.getValue());
        mplew.write(mod);
        mplew.writeInt(drop.getObjectId());
        mplew.write((drop.getMeso() > 0) ? 1 : 0);
        mplew.writeInt(drop.getItemId());
        mplew.writeInt(drop.getOwner());
        mplew.write(drop.getDropType());
        mplew.writePos(dropto);
        mplew.writeInt(0);
        if (mod != 2) {
            mplew.writePos(dropfrom);
        }
        mplew.write(0);
        if (mod != 2) {
            mplew.write(0);
            mplew.write(1);
        }
        if (drop.getMeso() == 0) {
            PacketHelper.addExpirationTime(mplew, drop.getItem().getExpiration());
        }
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket spawnPlayerMapobject(final MapleCharacter chr) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("spawnPlayerMapobject");
        }
        mplew.writeShort(SendPacketOpcode.SPAWN_PLAYER.getValue());
        mplew.writeInt(chr.getId());
        mplew.write(chr.getLevel());
        mplew.writeMapleAsciiString(chr.getName());
        if (chr.isAriantPQMap()) {
            mplew.writeMapleAsciiString("1st");
            mplew.write(new byte[6]);
        } else if (chr.getGuildId() <= 0) {
            mplew.writeMapleAsciiString("");
            mplew.write(new byte[6]);
        } else {
            final MapleGuild gs = World.Guild.getGuild(chr.getGuildId());
            if (gs != null) {
                mplew.writeMapleAsciiString(gs.getName());
                mplew.writeShort(gs.getLogoBG());
                mplew.write(gs.getLogoBGColor());
                mplew.writeShort(gs.getLogo());
                mplew.write(gs.getLogoColor());
            } else {
                mplew.writeMapleAsciiString("");
                mplew.write(new byte[6]);
            }
        }
        mplew.writeInt(0);
        mplew.write(0);
        mplew.write(224);
        mplew.write(31);
        mplew.write(0);
        if (chr.getBuffedValue(MapleBuffStat.变身) != null) {
            mplew.writeInt(2);
        } else {
            mplew.writeInt(0);
        }
        long buffmask = 0L;
        Integer buffvalue = null;
        if (chr.getBuffedValue(MapleBuffStat.隐身术) != null && !chr.isHidden()) {
            buffmask |= MapleBuffStat.隐身术.getValue();
        }
        if (chr.getBuffedValue(MapleBuffStat.斗气集中) != null) {
            buffmask |= MapleBuffStat.斗气集中.getValue();
            buffvalue = chr.getBuffedValue(MapleBuffStat.斗气集中);
        }
        if (chr.getBuffedValue(MapleBuffStat.影分身) != null) {
            buffmask |= MapleBuffStat.影分身.getValue();
        }
        if (chr.getBuffedValue(MapleBuffStat.无形箭弩) != null) {
            buffmask |= MapleBuffStat.无形箭弩.getValue();
        }
        if (chr.getBuffedValue(MapleBuffStat.变身) != null) {
            buffvalue = chr.getBuffedValue(MapleBuffStat.变身);
        }
        mplew.writeInt((int) (buffmask >> 32 & -1L));
        if (buffvalue != null) {
            if (chr.getBuffedValue(MapleBuffStat.变身) != null) {
                mplew.writeShort(buffvalue);
            } else {
                mplew.write(buffvalue.byteValue());
            }
        }
        final int CHAR_MAGIC_SPAWN = Randomizer.nextInt();
        mplew.writeInt((int) (buffmask & -1L));
        mplew.write(new byte[6]);
        mplew.writeInt(CHAR_MAGIC_SPAWN);
        mplew.writeLong(0L);
        mplew.writeShort(0);
        mplew.write(0);
        mplew.writeInt(CHAR_MAGIC_SPAWN);
        mplew.writeLong(0L);
        mplew.writeShort(0);
        mplew.write(0);
        mplew.writeInt(CHAR_MAGIC_SPAWN);
        mplew.writeShort(0);
        mplew.write(0);
        final IItem mount = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short) (-18));
        if (chr.getBuffedValue(MapleBuffStat.骑兽技能) != null && mount != null) {
            mplew.writeInt(mount.getItemId());
            mplew.writeInt(1004);
            mplew.writeInt(19275520);
            mplew.write(0);
        } else {
            mplew.writeInt(CHAR_MAGIC_SPAWN);
            mplew.writeLong(0L);
            mplew.write(0);
        }
        mplew.writeLong(0L);
        mplew.writeInt(CHAR_MAGIC_SPAWN);
        mplew.write(0);
        mplew.write(1);
        mplew.write(65);
        mplew.write(154);
        mplew.write(112);
        mplew.write(7);
        mplew.writeLong(0L);
        mplew.writeShort(0);
        mplew.writeInt(CHAR_MAGIC_SPAWN);
        mplew.writeLong(0L);
        mplew.writeInt(0);
        mplew.write(0);
        mplew.writeInt(CHAR_MAGIC_SPAWN);
        mplew.writeLong(0L);
        mplew.writeShort(0);
        mplew.write(0);
        mplew.writeInt(CHAR_MAGIC_SPAWN);
        mplew.write(0);
        mplew.writeShort(chr.getJob());
        PacketHelper.addCharLook(mplew, chr, false);
        mplew.writeInt(Math.min(250, chr.getInventory(MapleInventoryType.CASH).countById(5110000)));
        mplew.writeInt(chr.getItemEffect());
        mplew.writeInt(0);
        mplew.writeInt(-1);
        mplew.writeInt((GameConstants.getInventoryType(chr.getChair()) == MapleInventoryType.SETUP) ? chr.getChair() : 0);
        mplew.writePos(chr.getPosition());
        mplew.write(chr.getStance());
        mplew.writeShort(0);
        mplew.write(0);
        mplew.writeInt(chr.getMount().getLevel());
        mplew.writeInt(chr.getMount().getExp());
        mplew.writeInt(chr.getMount().getFatigue());
        PacketHelper.addAnnounceBox(mplew, chr);
        mplew.write((chr.getChalkboard() != null && chr.getChalkboard().length() > 0) ? 1 : 0);
        if (chr.getChalkboard() != null && chr.getChalkboard().length() > 0) {
            mplew.writeMapleAsciiString(chr.getChalkboard());
        }
        final Pair<List<MapleRing>, List<MapleRing>> rings = chr.getRings(false);
        final List<MapleRing> allrings = rings.getLeft();
        allrings.addAll(rings.getRight());
        addRingInfo(mplew, allrings);
        addRingInfo(mplew, allrings);
        addMarriageRingLook(mplew, chr);
        mplew.writeShort(0);
        if (chr.getCarnivalParty() != null) {
            mplew.write(chr.getCoconutTeam());
        } else if (chr.getMapId() == 109080000 || chr.getMapId() == 109080010) {
            mplew.write(chr.getCoconutTeam());
        }
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket removePlayerFromMap(final int cid, final MapleCharacter chr) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("removePlayerFromMap");
        }
        mplew.writeShort(SendPacketOpcode.REMOVE_PLAYER_FROM_MAP.getValue());
        mplew.writeInt(cid);

        if (ServerProperties.LogClientErr) {
            final String note = "时间：" + FileoutputUtil.CurrentReadable_Time() + " || 玩家名字：" + chr.getName() + "|| 玩家地图：" + chr.getMapId() + "\r\n38错误：" + ServerConstants.getPACKET_ERROR() + "\r\n\r\n";
            FileoutputUtil.packetLog("logs/38掉线/" + chr.getName() + ".log", note);
        }
        return mplew.getPacket();
    }

    public static MaplePacket facialExpression(final MapleCharacter from, final int expression) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("facialExpression");
        }
        mplew.writeShort(SendPacketOpcode.FACIAL_EXPRESSION.getValue());
        mplew.writeInt(from.getId());
        mplew.writeInt(expression);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket movePlayer(final int cid, final List<LifeMovementFragment> moves, final Point startPos) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.MOVE_PLAYER.getValue());
        mplew.writeInt(cid);
        mplew.writePos(startPos);
        PacketHelper.serializeMovementList(mplew, moves);
        return mplew.getPacket();
    }

    public static MaplePacket moveSummon(final int cid, final int oid, final Point startPos, final List<LifeMovementFragment> moves) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("moveSummon");
        }
        mplew.writeShort(SendPacketOpcode.MOVE_SUMMON.getValue());
        mplew.writeInt(cid);
        mplew.writeInt(oid);
        mplew.writeShort(startPos.x);
        mplew.writeShort(startPos.y);
        PacketHelper.serializeMovementList(mplew, moves);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket summonAttack(final int cid, final int summonSkillId, final int newStance, final List<SummonAttackEntry> allDamage) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("summonAttack");
        }
        mplew.writeShort(SendPacketOpcode.SUMMON_ATTACK.getValue());
        mplew.writeInt(cid);
        mplew.writeInt(summonSkillId);
        mplew.write(newStance);
        mplew.write(allDamage.size());
        for (final SummonAttackEntry attackEntry : allDamage) {
            mplew.writeInt(attackEntry.getMonster().getObjectId());
            mplew.write(1);
            mplew.write(6);
            mplew.writeInt(attackEntry.getDamage());
        }
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("summonAttack-2158：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket closeRangeAttack(final int cid, final int tbyte, final int skill, final int level, final byte display, final byte animation, final byte speed, final List<DamageParse.AttackPair> damage, final boolean energy, final int lvl, final byte mastery, final byte unk, final int charge) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("closeRangeAttack");
        }
        mplew.writeShort(energy ? SendPacketOpcode.ENERGY_ATTACK.getValue() : SendPacketOpcode.CLOSE_RANGE_ATTACK.getValue());
        mplew.writeInt(cid);
        mplew.write(tbyte);
        mplew.write(lvl);
        if (skill > 0) {
            mplew.write(level);
            mplew.writeInt(skill);
        } else {
            mplew.write(0);
        }
        mplew.write(unk);
        mplew.write(display);
        mplew.write(animation);
        mplew.write(speed);
        mplew.write(mastery);
        mplew.writeInt(0);
        if (skill == 4211006) {
            for (final DamageParse.AttackPair oned : damage) {
                if (oned.attack != null) {
                    mplew.writeInt(oned.objectid);
                    mplew.write(7);
                    mplew.write(oned.attack.size());
                    for (final Pair<Integer, Boolean> eachd : oned.attack) {
                        mplew.writeInt(eachd.left);
                    }
                }
            }
        } else {
            for (final DamageParse.AttackPair oned : damage) {
                if (oned.attack != null) {
                    mplew.writeInt(oned.objectid);
                    mplew.write(7);
                    for (final Pair<Integer, Boolean> eachd : oned.attack) {
                        if (eachd.right) {
                            mplew.writeInt(eachd.left + Integer.MIN_VALUE);
                        } else {
                            mplew.writeInt(eachd.left);
                        }
                    }
                }
            }
        }
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket rangedAttack(final int cid, final byte tbyte, final int skill, final int level, final byte display, final byte animation, final byte speed, final int itemid, final List<DamageParse.AttackPair> damage, final Point pos, final int lvl, final byte mastery, final byte unk) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("rangedAttack");
        }
        mplew.writeShort(SendPacketOpcode.RANGED_ATTACK.getValue());
        mplew.writeInt(cid);
        mplew.write(tbyte);
        mplew.write(lvl);
        if (skill > 0) {
            mplew.write(level);
            mplew.writeInt(skill);
        } else {
            mplew.write(0);
        }
        mplew.write(unk);
        mplew.write(display);
        mplew.write(animation);
        mplew.write(speed);
        mplew.write(mastery);
        mplew.writeInt(itemid);
        for (final DamageParse.AttackPair oned : damage) {
            if (oned.attack != null) {
                mplew.writeInt(oned.objectid);
                mplew.write(7);
                for (final Pair<Integer, Boolean> eachd : oned.attack) {
                    if (eachd.right) {
                        mplew.writeInt(eachd.left + Integer.MIN_VALUE);
                    } else {
                        mplew.writeInt(eachd.left);
                    }
                }
            }
        }
        mplew.writePos(pos);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket magicAttack(final int cid, final int tbyte, final int skill, final int level, final byte display, final byte animation, final byte speed, final List<DamageParse.AttackPair> damage, final int charge, final int lvl, final byte unk) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("magicAttack");
        }
        mplew.writeShort(SendPacketOpcode.MAGIC_ATTACK.getValue());
        mplew.writeInt(cid);
        mplew.write(tbyte);
        mplew.write(lvl);
        mplew.write(level);
        mplew.writeInt(skill);
        mplew.write(unk);
        mplew.write(display);
        mplew.write(animation);
        mplew.write(speed);
        mplew.write(0);
        mplew.writeInt(0);
        for (final DamageParse.AttackPair oned : damage) {
            if (oned.attack != null) {
                mplew.writeInt(oned.objectid);
                mplew.write(-1);
                for (final Pair<Integer, Boolean> eachd : oned.attack) {
                    if (eachd.right) {
                        mplew.writeInt(eachd.left + Integer.MIN_VALUE);
                    } else {
                        mplew.writeInt(eachd.left);
                    }
                }
            }
        }
        if (charge > 0) {
            mplew.writeInt(charge);
        }
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket getNPCShop(final MapleClient c, final int sid, final List<MapleShopItem> items) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        if (ServerProperties.LogPktCall) {
            log.info("getNPCShop");
        }
        mplew.writeShort(SendPacketOpcode.OPEN_NPC_SHOP.getValue());
        mplew.writeInt(sid);
        mplew.writeShort(items.size());
        for (final MapleShopItem item : items) {
            mplew.writeInt(item.getItemId());
            mplew.writeInt(item.getPrice());
            if (!GameConstants.is飞镖道具(item.getItemId()) && !GameConstants.is子弹道具(item.getItemId())) {
                mplew.writeShort(1);
                mplew.writeShort(item.getBuyable());
            } else {
                mplew.writeZeroBytes(6);
                mplew.writeShort(BitTools.doubleToShortBits(ii.getPrice(item.getItemId())));
                mplew.writeShort(ii.getSlotMax(c, item.getItemId()));
            }
        }
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket confirmShopTransaction(final byte code) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("confirmShopTransaction");
        }
        mplew.writeShort(SendPacketOpcode.CONFIRM_SHOP_TRANSACTION.getValue());
        mplew.write(code);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket addInventorySlot(final MapleInventoryType type, final IItem item) {
        if (ServerProperties.LogPktCall) {
            log.info("addInventorySlotA");
        }
        return addInventorySlot(type, item, false);
    }

    public static MaplePacket addInventorySlot(final MapleInventoryType type, final IItem item, final boolean fromDrop) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("addInventorySlot");
        }
        mplew.writeShort(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
        mplew.write(fromDrop ? 1 : 0);
        mplew.writeShort(1);
        mplew.write(type.getType());
        mplew.write(item.getPosition());
        PacketHelper.addItemInfo(mplew, item, true, false);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket pet_updateInventorySlot(final MapleInventoryType type, final IItem item, final boolean fromDrop) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("updateInventorySlot");
        }
        mplew.writeShort(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
        mplew.write(fromDrop ? 1 : 0);
        mplew.write(1);
        mplew.write(1);
        mplew.write(type.getType());
        mplew.writeShort(item.getPosition());
        mplew.writeShort(item.getQuantity());
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket updateInventorySlot(final MapleInventoryType type, final IItem item, final boolean fromDrop) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("updateInventorySlot");
        }
        mplew.writeShort(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
        mplew.write(fromDrop ? 1 : 0);
        mplew.write(1);
        mplew.write(1);
        mplew.write(type.getType());
        mplew.writeShort(item.getPosition());
        mplew.writeShort(item.getQuantity());
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket moveInventoryItem(final MapleInventoryType type, final short src, final short dst) {
        if (ServerProperties.LogPktCall) {
            log.info("moveInventoryItemA");
        }
        return moveInventoryItem(type, src, dst, (short) (-1));
    }

    public static MaplePacket loveEffect() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(72);
        mplew.writeZeroBytes(20);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket moveInventoryItem(final MapleInventoryType type, final short src, final short dst, final short equipIndicator) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("moveInventoryItemB");
        }
        mplew.writeShort(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
        mplew.write(HexTool.getByteArrayFromHexString("01 01 02"));
        mplew.write(type.getType());
        mplew.writeShort(src);
        mplew.writeShort(dst);
        if (equipIndicator != -1) {
            mplew.write(equipIndicator);
        }
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket moveAndMergeInventoryItem(final MapleInventoryType type, final short src, final short dst, final short total) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("moveAndMergeInventoryItem");
        }
        mplew.writeShort(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
        mplew.write(HexTool.getByteArrayFromHexString("01 02 03"));
        mplew.write(type.getType());
        mplew.writeShort(src);
        mplew.write(1);
        mplew.write(type.getType());
        mplew.writeShort(dst);
        mplew.writeShort(total);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket moveAndMergeWithRestInventoryItem(final MapleInventoryType type, final short src, final short dst, final short srcQ, final short dstQ) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("moveAndMergeWithRestInventoryItem");
        }
        mplew.writeShort(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
        mplew.write(HexTool.getByteArrayFromHexString("01 02 01"));
        mplew.write(type.getType());
        mplew.writeShort(src);
        mplew.writeShort(srcQ);
        mplew.write(HexTool.getByteArrayFromHexString("01"));
        mplew.write(type.getType());
        mplew.writeShort(dst);
        mplew.writeShort(dstQ);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket clearInventoryItem(final MapleInventoryType type, final short slot, final boolean fromDrop) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("clearInventoryItem");
        }
        mplew.writeShort(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
        mplew.write(fromDrop ? 1 : 0);
        mplew.write(HexTool.getByteArrayFromHexString("01 03"));
        mplew.write(type.getType());
        mplew.writeShort(slot);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket updateSpecialItemUse(final IItem item, final byte invType) {
        if (ServerProperties.LogPktCall) {
            log.info("updateSpecialItemUseA");
        }
        return updateSpecialItemUse(item, invType, item.getPosition());
    }

    public static MaplePacket updateSpecialItemUse(final IItem item, final byte invType, final short pos) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("updateSpecialItemUseB");
        }
        mplew.writeShort(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
        mplew.write(0);
        mplew.write(2);
        mplew.write(3);
        mplew.write(invType);
        mplew.writeShort(pos);
        mplew.write(0);
        mplew.write(invType);
        if (item.getType() == 1) {
            mplew.writeShort(pos);
        } else {
            mplew.write(pos);
        }
        PacketHelper.addItemInfo(mplew, item, true, true);
        if (item.getPosition() < 0) {
            mplew.write(2);
        }
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket updateSpecialItemUse_(final IItem item, final byte invType) {
        if (ServerProperties.LogPktCall) {
            log.info("updateSpecialItemUse_A");
        }
        return updateSpecialItemUse_(item, invType, item.getPosition());
    }

    public static MaplePacket updateSpecialItemUse_(final IItem item, final byte invType, final short pos) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("updateSpecialItemUse_B");
        }
        mplew.writeShort(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
        mplew.write(0);
        mplew.write(1);
        mplew.write(0);
        mplew.write(invType);
        if (item.getType() == 1) {
            mplew.writeShort(pos);
        } else {
            mplew.write(pos);
        }
        PacketHelper.addItemInfo(mplew, item, true, true);
        if (item.getPosition() < 0) {
            mplew.write(1);
        }
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket scrolledItem(final IItem scroll, final IItem item, final boolean destroyed, final boolean potential) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("scrolledItem");
        }
        mplew.writeShort(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
        mplew.write(1);
        mplew.write(destroyed ? 2 : 3);
        mplew.write((scroll.getQuantity() > 0) ? 1 : 3);
        mplew.write(GameConstants.getInventoryType(scroll.getItemId()).getType());
        mplew.writeShort(scroll.getPosition());
        if (scroll.getQuantity() > 0) {
            mplew.writeShort(scroll.getQuantity());
        }
        mplew.write(3);
        if (!destroyed) {
            mplew.write(MapleInventoryType.EQUIP.getType());
            mplew.writeShort(item.getPosition());
            mplew.write(0);
        }
        mplew.write(MapleInventoryType.EQUIP.getType());
        mplew.writeShort(item.getPosition());
        if (!destroyed) {
            PacketHelper.addItemInfo(mplew, item, true, true);
        }
        if (!potential) {
            mplew.write(1);
        }
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket getScrollEffect(final int chr, final IEquip.ScrollResult scrollSuccess, final boolean legendarySpirit) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("getScrollEffect");
        }
        mplew.writeShort(SendPacketOpcode.SHOW_SCROLL_EFFECT.getValue());
        mplew.writeInt(chr);
        switch (scrollSuccess) {
            case SUCCESS: {
                mplew.writeShort(1);
                mplew.writeShort(legendarySpirit ? 1 : 0);
                break;
            }
            case FAIL: {
                mplew.writeShort(0);
                mplew.writeShort(legendarySpirit ? 1 : 0);
                break;
            }
            case CURSE: {
                mplew.write(0);
                mplew.write(1);
                mplew.writeShort(legendarySpirit ? 1 : 0);
                break;
            }
        }
        mplew.write(0);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket ItemMaker_Success() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("ItemMaker_Success");
        }
        mplew.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
        mplew.write(17);
        mplew.writeZeroBytes(4);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket ItemMaker_Success_3rdParty(final int from_playerid) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("ItemMaker_Success_3rdParty");
        }
        mplew.writeShort(SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
        mplew.writeInt(from_playerid);
        mplew.write(17);
        mplew.writeZeroBytes(4);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket explodeDrop(final int oid) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("explodeDrop");
        }
        mplew.writeShort(SendPacketOpcode.REMOVE_ITEM_FROM_MAP.getValue());
        mplew.write(4);
        mplew.writeInt(oid);
        mplew.writeShort(655);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket removeItemFromMap(final int oid, final int animation, final int cid) {
        if (ServerProperties.LogPktCall) {
            log.info("removeItemFromMapA");
        }
        return removeItemFromMap(oid, animation, cid, 0);
    }

    public static MaplePacket removeItemFromMap(final int oid, final int animation, final int cid, final int slot) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("removeItemFromMapB");
        }
        mplew.writeShort(SendPacketOpcode.REMOVE_ITEM_FROM_MAP.getValue());
        mplew.write(animation);
        mplew.writeInt(oid);
        if (animation >= 2) {
            mplew.writeInt(cid);
            if (animation == 5) {
                mplew.write(slot);
            }
        }
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket updateCharLook(final MapleCharacter chr) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("updateCharLook");
        }
        mplew.writeShort(SendPacketOpcode.UPDATE_CHAR_LOOK.getValue());
        mplew.writeInt(chr.getId());
        mplew.write(1);
        PacketHelper.addCharLook(mplew, chr, false);
        final Pair<List<MapleRing>, List<MapleRing>> rings = chr.getRings(false);
        final List<MapleRing> allrings = rings.getLeft();
        allrings.addAll(rings.getRight());
        addRingInfo(mplew, allrings);
        addRingInfo(mplew, allrings);
        addMarriageRingLook(mplew, chr);
        mplew.writeInt(0);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    private static void addMarriageRingLook(final MaplePacketLittleEndianWriter mplew, final MapleCharacter chr) {
        mplew.write((byte) ((chr.getMarriageRing(false) != null) ? 1 : 0));
        if (chr.getMarriageRing(false) != null) {
            mplew.writeInt(chr.getId());
            mplew.writeInt(chr.getMarriageRing(false).getPartnerChrId());
            mplew.writeInt(chr.getMarriageRing(false).getRingId());
        }
    }

    public static void addRingInfo(final MaplePacketLittleEndianWriter mplew, final List<MapleRing> rings) {
        if (ServerProperties.LogPktCall) {
            log.info("addRingInfo");
        }
        mplew.write((rings.size() > 0) ? 1 : 0);
        mplew.writeInt(rings.size());
        for (final MapleRing ring : rings) {
            mplew.writeLong(ring.getRingId());
            mplew.writeLong(ring.getPartnerRingId());
            mplew.writeInt(ring.getItemId());
        }
    }

    public static MaplePacket dropInventoryItem(final MapleInventoryType type, final short src) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("dropInventoryItem");
        }
        mplew.writeShort(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
        mplew.write(HexTool.getByteArrayFromHexString("01 01 03"));
        mplew.write(type.getType());
        mplew.writeShort(src);
        if (src < 0) {
            mplew.write(1);
        }
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket dropInventoryItemUpdate(final MapleInventoryType type, final IItem item) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("dropInventoryItemUpdate");
        }
        mplew.writeShort(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
        mplew.write(HexTool.getByteArrayFromHexString("01 01 01"));
        mplew.write(type.getType());
        mplew.writeShort(item.getPosition());
        mplew.writeShort(item.getQuantity());
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket damagePlayer(final int skill, final int monsteridfrom, final int cid, final int damage, final int fake, final byte direction, final int reflect, final boolean is_pg, final int oid, final int pos_x, final int pos_y) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("damagePlayer");
        }
        mplew.writeShort(SendPacketOpcode.DAMAGE_PLAYER.getValue());
        mplew.writeInt(cid);
        mplew.write(skill);
        mplew.writeInt(damage);
        mplew.writeInt(monsteridfrom);
        mplew.write(direction);
        if (reflect > 0) {
            mplew.write(reflect);
            mplew.write(is_pg ? 1 : 0);
            mplew.writeInt(oid);
            mplew.write(6);
            mplew.writeShort(pos_x);
            mplew.writeShort(pos_y);
            mplew.write(0);
        } else {
            mplew.writeShort(0);
        }
        mplew.writeInt(damage);
        if (fake > 0) {
            mplew.writeInt(fake);
        }
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket updateQuest(final MapleQuestStatus quest) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("updateQuest");
        }
        mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(1);
        mplew.writeShort(quest.getQuest().getId());
        mplew.write(quest.getStatus());
        switch (quest.getStatus()) {
            case 0: {
                mplew.writeZeroBytes(10);
                break;
            }
            case 1: {
                mplew.writeMapleAsciiString((quest.getCustomData() != null) ? quest.getCustomData() : "");
                break;
            }
            case 2: {
                mplew.writeLong(PacketHelper.getTime(System.currentTimeMillis()));
                break;
            }
        }
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket updateInfoQuest(final int quest, final String data) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("updateInfoQuest");
        }
        mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(10);
        mplew.writeShort(quest);
        mplew.writeMapleAsciiString(data);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket updateQuestInfo(final MapleCharacter c, final int quest, final int npc, final byte progress) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("updateQuestInfo");
        }
        mplew.writeShort(SendPacketOpcode.UPDATE_QUEST_INFO.getValue());
        mplew.write(progress);
        mplew.writeShort(quest);
        mplew.writeInt(npc);
        mplew.writeInt(0);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket updateQuestFinish(final int quest, final int npc, final int nextquest) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("updateQuestFinish");
        }
        mplew.writeShort(SendPacketOpcode.UPDATE_QUEST_INFO.getValue());
        mplew.write(8);
        mplew.writeShort(quest);
        mplew.writeInt(npc);
        mplew.writeInt(nextquest);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket charInfo(final MapleCharacter chr, final boolean isSelf) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("charInfo");
        }
        mplew.writeShort(SendPacketOpcode.CHAR_INFO.getValue());
        mplew.writeInt(chr.getId());
        mplew.write(chr.getLevel());
        mplew.writeShort(chr.getJob());
        mplew.writeShort(chr.getFame());
        mplew.write((chr.getMarriageId() > 0) ? 1 : 0);
        String guildName = "-";
        String allianceName = "-";
        final MapleGuild gs = World.Guild.getGuild(chr.getGuildId());
        if (chr.getGuildId() > 0 && gs != null) {
            guildName = gs.getName();
            if (gs.getAllianceId() > 0) {
                final MapleGuildAlliance allianceNameA = World.Alliance.getAlliance(gs.getAllianceId());
                if (allianceNameA != null) {
                    allianceName = allianceNameA.getName();
                }
            }
        }
        mplew.writeMapleAsciiString(guildName);
        mplew.writeMapleAsciiString(allianceName);
        final IItem inv = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short) (-114));
        final int peteqid = (inv != null) ? inv.getItemId() : 0;
        final IItem inv2 = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short) (-122));
        final int peteqid2 = (inv2 != null) ? inv2.getItemId() : 0;
        final IItem inv3 = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short) (-124));
        final int peteqid3 = (inv3 != null) ? inv3.getItemId() : 0;
        for (final MaplePet pet : chr.getPets()) {
            if (pet.getSummoned()) {
                mplew.write(pet.getUniqueId());
                mplew.writeInt(pet.getPetItemId());
                mplew.writeMapleAsciiString(pet.getName());
                mplew.write(pet.getLevel());
                mplew.writeShort(pet.getCloseness());
                mplew.write(pet.getFullness());
                mplew.writeShort(pet.getFlags());
                mplew.writeInt(peteqid);
            }
        }
        mplew.write(0);
        if (chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short) (-18)) != null) {
            final int itemid = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short) (-18)).getItemId();
            final MapleMount mount = chr.getMount();
            final boolean canwear = MapleItemInformationProvider.getInstance().getReqLevel(itemid) <= chr.getLevel();
            mplew.write(canwear ? 1 : 0);
            mplew.writeInt(mount.getLevel());
            mplew.writeInt(mount.getExp());
            mplew.writeInt(mount.getFatigue());
        } else {
            mplew.write(0);
        }
        mplew.write(0);
        chr.getMonsterBook().addCharInfoPacket(chr.getMonsterBookCover(), mplew);
        final IItem medal = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short) (-49));
        mplew.writeInt((medal == null) ? 0 : medal.getItemId());
        final List<Integer> medalQuests = new ArrayList<Integer>();
        final List<MapleQuestStatus> completed = chr.getCompletedQuests();
        for (final MapleQuestStatus q : completed) {
            if (q.getQuest().getMedalItem() > 0 && GameConstants.getInventoryType(q.getQuest().getMedalItem()) == MapleInventoryType.EQUIP) {
                medalQuests.add(q.getQuest().getId());
            }
        }
        mplew.writeShort(medalQuests.size());
        for (final int x : medalQuests) {
            mplew.writeShort(x);
        }
        final MapleInventory iv = chr.getInventory(MapleInventoryType.SETUP);
        final List<Item> chairItems = new ArrayList<Item>();
        for (final IItem item : iv.list()) {
            if (item.getItemId() >= 3010000 && item.getItemId() <= 3020001) {
                chairItems.add((Item) item);
            }
        }
        mplew.writeInt(chairItems.size());
        for (final IItem item : chairItems) {
            mplew.writeInt(item.getItemId());
        }
        final MapleInventory 勋章列表 = chr.getInventory(MapleInventoryType.EQUIP);
        final List<Item> 勋章列表Items = new ArrayList<Item>();
        for (final IItem item2 : 勋章列表.list()) {
            if (item2.getItemId() >= 1142000 && item2.getItemId() <= 1142999) {
                勋章列表Items.add((Item) item2);
            }
        }
        mplew.writeInt(勋章列表Items.size());
        for (final IItem item2 : 勋章列表Items) {
            mplew.writeInt(item2.getItemId());
        }
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    private static void writeLongMask(final MaplePacketLittleEndianWriter mplew, final List<Pair<MapleBuffStat, Integer>> statups) {
        if (ServerProperties.LogPktCall) {
            log.info("writeLongMask");
        }
        long firstmask = 0L;
        long secondmask = 0L;
        for (final Pair<MapleBuffStat, Integer> statup : statups) {
            if (statup.getLeft().isFirst()) {
                firstmask |= statup.getLeft().getValue();
            } else {
                secondmask |= statup.getLeft().getValue();
            }
        }
        mplew.writeLong(firstmask);
        mplew.writeLong(secondmask);
    }

    private static void writeLongDiseaseMask(final MaplePacketLittleEndianWriter mplew, final List<Pair<MapleDisease, Integer>> statups) {
        if (ServerProperties.LogPktCall) {
            log.info("writeLongDiseaseMask");
        }
        long firstmask = 0L;
        long secondmask = 0L;
        for (final Pair<MapleDisease, Integer> statup : statups) {
            if (statup.getLeft().isFirst()) {
                firstmask |= statup.getLeft().getValue();
            } else {
                secondmask |= statup.getLeft().getValue();
            }
        }
        mplew.writeLong(firstmask);
        mplew.writeLong(secondmask);
    }

    private static void writeLongMaskFromListM(final MaplePacketLittleEndianWriter mplew, final List<MapleBuffStat> statups) {
        if (ServerProperties.LogPktCall) {
            log.info("writeLongMaskFromList");
        }
        long firstmask = 0L;
        long secondmask = 0L;
        mplew.write(0);
        for (final MapleBuffStat statup : statups) {
            if (statup.isFirst()) {
                firstmask |= statup.getValue();
            } else {
                secondmask |= statup.getValue();
            }
        }
        mplew.writeLong(firstmask);
        mplew.writeInt(0);
        mplew.writeZeroBytes(3);
    }

    private static void writeLongMaskFromList(final MaplePacketLittleEndianWriter mplew, final List<MapleBuffStat> statups) {
        if (ServerProperties.LogPktCall) {
            log.info("writeLongMaskFromList");
        }
        long firstmask = 0L;
        long secondmask = 0L;
        for (final MapleBuffStat statup : statups) {
            if (statup.isFirst()) {
                firstmask |= statup.getValue();
            } else {
                secondmask |= statup.getValue();
            }
        }
        mplew.writeLong(firstmask);
        mplew.writeLong(secondmask);
    }

    public static MaplePacket giveMount(MapleCharacter c, int buffid, int skillid, List<Pair<MapleBuffStat, Integer>> statups) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) log.info("giveMount");
        mplew.writeShort(SendPacketOpcode.GIVE_BUFF.getValue());
        mplew.write(0);
        writeLongMask(mplew, statups);
        for (Pair<MapleBuffStat, Integer> statup : statups) {
            if (statup.getRight().shortValue() >= 1000 && statup.getRight().shortValue() != 1002) {
                mplew.writeShort(statup.getRight().shortValue() + c.getGender() * 100);
            } else {
                mplew.write(0);
            }
            mplew.writeInt(buffid);
            mplew.writeInt(skillid);
        }
        mplew.writeInt(0);
        mplew.writeShort(0);
        mplew.write(0);
        mplew.write(2);
        int a = giveBuff(c, buffid);
        if (a > 0) mplew.write(a);
        if (ServerProperties.LogClientErr) {
            ServerConstants ERROR = new ServerConstants();
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static int giveBuff(final MapleCharacter c, final int buffid) {
        int a = 0;
        switch (buffid) {
            case 1002:
            case 8000:
            case 1121000:
            case 1221000:
            case 1321000:
            case 2121000:
            case 2221000:
            case 2321000:
            case 3121000:
            case 3221000:
            case 4101004:
            case 4121000:
            case 4201003:
            case 4221000:
            case 4341000:
            case 5101007:
            case 5121000:
            case 5221000:
            case 5321005:
            case 9001001:
            case 10001002:
            case 10008000:
            case 14101003:
            case 20001002:
            case 20008000:
            case 20018000:
            case 21121000:
            case 22171000:
            case 23121005:
            case 30008000:
            case 31121004:
            case 32121007:
            case 33121007:
            case 35121007: {
                a = 5;
                break;
            }
            case 32101003: {
                a = 29;
                break;
            }
            case -2022458:
            case 33121006: {
                a = 6;
                break;
            }
            case 5111005:
            case 5121003:
            case 13111005:
            case 15111002: {
                a = 7;
                break;
            }
            case 5301003: {
                a = 3;
                break;
            }
        }
        return a;
    }

    public static MaplePacket givePirate(final List<Pair<MapleBuffStat, Integer>> statups, final int duration, final int skillid) {
        final boolean infusion = skillid == 5121009 || skillid == 15111005;
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("givePirate");
        }
        mplew.writeShort(SendPacketOpcode.GIVE_BUFF.getValue());
        mplew.writeLong(0L);
        mplew.writeLong(MapleBuffStat.变身.getValue());
        mplew.writeShort(0);
        mplew.writeInt(skillid);
        mplew.writeZeroBytes(1);
        mplew.writeInt(duration);
        mplew.writeZeroBytes(6);
        mplew.writeShort(0);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket giveForeignPirate(final List<Pair<MapleBuffStat, Integer>> statups, final int duration, final int cid, final int skillid) {
        final boolean infusion = skillid == 5121009 || skillid == 15111005;
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("giveForeignPirate");
        }
        mplew.writeShort(SendPacketOpcode.GIVE_FOREIGN_BUFF.getValue());
        mplew.writeInt(cid);
        writeLongMask(mplew, statups);
        mplew.writeShort(0);
        for (final Pair<MapleBuffStat, Integer> stat : statups) {
            mplew.writeInt(stat.getRight());
            mplew.writeLong(skillid);
            mplew.writeZeroBytes(infusion ? 7 : 1);
            mplew.writeShort(duration);
        }
        mplew.writeShort(infusion ? 600 : 0);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket giveHoming(final int skillid, final int mobid, final int x) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("giveHoming");
        }
        mplew.writeShort(SendPacketOpcode.GIVE_BUFF.getValue());
        mplew.writeLong(MapleBuffStat.导航辅助.getValue());
        mplew.writeLong(0L);
        mplew.writeShort(0);
        mplew.writeInt(x);
        mplew.writeLong(skillid);
        mplew.write(0);
        mplew.writeInt(mobid);
        mplew.writeShort(0);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket giveEnergyChargeTest(final int bar, final int bufflength) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("giveEnergyChargeTestA");
        }
        mplew.writeShort(SendPacketOpcode.GIVE_BUFF.getValue());
        mplew.writeLong(MapleBuffStat.能量获得.getValue());
        mplew.writeLong(0L);
        mplew.writeShort(0);
        mplew.writeInt(Math.min(bar, 10000));
        mplew.writeLong(0L);
        mplew.write(0);
        mplew.writeInt((bar >= 10000) ? bufflength : 0);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket givePirateBuff(int buffid, int bufflength, List<Pair<MapleBuffStat, Integer>> statups) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.GIVE_BUFF.getValue());
        mplew.writeLong(MapleBuffStat.能量获得.getValue());
        mplew.writeLong(0L);
        mplew.writeShort(0);
        for (Pair<MapleBuffStat, Integer> statup : statups) {
            mplew.writeShort(statup.getRight().shortValue());
            mplew.writeShort(0);
            mplew.writeInt(buffid);
            mplew.writeInt(0);
            mplew.write(0);
            mplew.writeShort(bufflength);
        }
        mplew.writeShort(0);
        mplew.write(0);
        if (ServerProperties.LogClientErr) {
            ServerConstants ERROR = new ServerConstants();
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket 能量条(List<Pair<MapleBuffStat, Integer>> statups, int duration) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.GIVE_BUFF.getValue());
        mplew.write(0);
        mplew.writeLong(MapleBuffStat.能量获得.getValue());
        mplew.writeLong(0L);
        mplew.write(0);
        for (Pair<MapleBuffStat, Integer> stat : statups) {
            mplew.writeInt(stat.getRight().shortValue());
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.write(0);
            mplew.writeShort(duration);
        }
        mplew.writeShort(0);
        mplew.write(2);
        return mplew.getPacket();
    }


    public static MaplePacket 能量条2(List<Pair<MapleBuffStat, Integer>> statups, int duration) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.GIVE_BUFF.getValue());
        mplew.write(0);
        mplew.writeLong(MapleBuffStat.能量获得.getValue());
        mplew.writeLong(0L);
        mplew.write(0);
        for (Pair<MapleBuffStat, Integer> stat : statups) {
            mplew.writeInt(stat.getRight().shortValue());
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.write(0);
            mplew.writeShort(duration);
        }
        mplew.writeShort(0);
        mplew.write(2);
        if (ServerProperties.LogClientErr) {
            ServerConstants ERROR = new ServerConstants();
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket giveEnergyChargeTest(final int cid, final int bar, final int bufflength) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("giveEnergyChargeTestB");
        }
        mplew.writeShort(SendPacketOpcode.GIVE_FOREIGN_BUFF.getValue());
        mplew.writeInt(cid);
        mplew.writeLong(0L);
        mplew.writeLong(MapleBuffStat.能量获得.getValue());
        mplew.writeShort(0);
        mplew.writeInt(Math.min(bar, 10000));
        mplew.writeLong(0L);
        mplew.writeInt((bar >= 10000) ? bufflength : 0);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket giveBuff(final int buffid, final int bufflength, final List<Pair<MapleBuffStat, Integer>> statups, final MapleStatEffect effect) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("giveBuff");
        }
        mplew.writeShort(SendPacketOpcode.GIVE_BUFF.getValue());
        writeLongMask(mplew, statups);
        for (final Pair<MapleBuffStat, Integer> statup : statups) {
            mplew.writeShort(statup.getRight().shortValue());
            mplew.writeInt(buffid);
            mplew.writeInt(bufflength);
        }
        mplew.writeShort(0);
        mplew.writeShort(0);
        if (effect == null || (!effect.is斗气集中() && !effect.isFinalAttack())) {
            mplew.write(0);
        }
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket giveDebuff(final List<Pair<MapleDisease, Integer>> statups, final int skillid, final int level, final int duration) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("giveDebuff");
        }
        mplew.writeShort(SendPacketOpcode.GIVE_BUFF.getValue());
        writeLongDiseaseMask(mplew, statups);
        for (final Pair<MapleDisease, Integer> statup : statups) {
            mplew.writeShort(statup.getRight().shortValue());
            mplew.writeShort(skillid);
            mplew.writeShort(level);
            mplew.writeInt(duration);
        }
        mplew.writeShort(0);
        mplew.writeShort(900);
        mplew.write(2);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket giveForeignDebuff(final int cid, final List<Pair<MapleDisease, Integer>> statups, final int skillid, final int level) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("giveForeignDebuff");
        }
        mplew.writeShort(SendPacketOpcode.GIVE_FOREIGN_BUFF.getValue());
        mplew.writeInt(cid);
        writeLongDiseaseMask(mplew, statups);
        mplew.writeShort(skillid);
        mplew.writeShort(level);
        mplew.writeShort(0);
        mplew.writeShort(900);
        mplew.write(3);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket cancelForeignDebuff(final int cid, final long mask, final boolean first) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("cancelForeignDebuff");
        }
        mplew.writeShort(SendPacketOpcode.CANCEL_FOREIGN_BUFF.getValue());
        mplew.writeInt(cid);
        mplew.writeLong(first ? mask : 0L);
        mplew.writeLong(first ? 0L : mask);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket showMonsterRiding(final int cid, final List<Pair<MapleBuffStat, Integer>> statups, final int itemId, final int skillId) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("showMonsterRiding");
        }
        mplew.writeShort(SendPacketOpcode.GIVE_FOREIGN_BUFF.getValue());
        mplew.writeInt(cid);
        mplew.write(0);
        writeLongMask(mplew, statups);
        mplew.write(0);
        mplew.writeInt(itemId);
        mplew.writeInt(skillId);
        mplew.writeInt(0);
        mplew.writeShort(0);
        mplew.write(0);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket giveForeignBuff(MapleCharacter c, int cid, List<Pair<MapleBuffStat, Integer>> statups, MapleStatEffect effect) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) log.info("giveForeignBuff");
        mplew.writeShort(SendPacketOpcode.GIVE_FOREIGN_BUFF.getValue());
        mplew.writeInt(cid);
        writeLongMask(mplew, statups);
        for (Pair<MapleBuffStat, Integer> statup : statups) {
            if (effect.isMorph() && statup.getRight().intValue() <= 255) {
                mplew.write(statup.getRight().byteValue());
                continue;
            }
            if (effect.isPirateMorph()) {
                mplew.writeShort(statup.getRight().shortValue() + c.getGender() * 100);
                continue;
            }
            mplew.writeShort(statup.getRight().shortValue());
        }
        mplew.writeShort(0);
        if (effect.isMorph() && !effect.isPirateMorph()) mplew.writeShort(0);
        mplew.write(0);
        mplew.write(0);
        if (ServerProperties.LogClientErr) {
            ServerConstants ERROR = new ServerConstants();
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket cancelForeignBuff(final int cid, final List<MapleBuffStat> statups) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("cancelForeignBuff");
        }
        mplew.writeShort(SendPacketOpcode.CANCEL_FOREIGN_BUFF.getValue());
        mplew.writeInt(cid);
        writeLongMaskFromList(mplew, statups);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket cancelForeignBuffMONSTER(final int cid, final List<MapleBuffStat> statups) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("cancelForeignBuff");
        }
        mplew.writeShort(SendPacketOpcode.CANCEL_FOREIGN_BUFF.getValue());
        mplew.writeInt(cid);
        writeLongMaskFromListM(mplew, statups);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket cancelBuffMONSTER(final List<MapleBuffStat> statups) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("cancelBuff");
        }
        mplew.writeShort(SendPacketOpcode.CANCEL_BUFF.getValue());
        writeLongMaskFromListM(mplew, statups);
        mplew.write(3);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket cancelBuff(final List<MapleBuffStat> statups) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("cancelBuff");
        }
        mplew.writeShort(SendPacketOpcode.CANCEL_BUFF.getValue());
        writeLongMaskFromList(mplew, statups);
        mplew.write(3);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket cancelHoming() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("cancelHoming");
        }
        mplew.writeShort(SendPacketOpcode.CANCEL_BUFF.getValue());
        mplew.writeLong(MapleBuffStat.导航辅助.getValue());
        mplew.writeLong(0L);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket cancelDebuff(final long mask, final boolean first) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("cancelDebuff");
        }
        mplew.writeShort(SendPacketOpcode.CANCEL_BUFF.getValue());
        mplew.writeLong(first ? mask : 0L);
        mplew.writeLong(first ? 0L : mask);
        mplew.write(0);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket updateMount(final MapleCharacter chr, final boolean levelup) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("updateMount");
        }
        mplew.writeShort(SendPacketOpcode.UPDATE_MOUNT.getValue());
        mplew.writeInt(chr.getId());
        mplew.writeInt(chr.getMount().getLevel());
        mplew.writeInt(chr.getMount().getExp());
        mplew.writeInt(chr.getMount().getFatigue());
        mplew.write(levelup ? 1 : 0);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket mountInfo(final MapleCharacter chr) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("mountInfo");
        }
        mplew.writeShort(SendPacketOpcode.UPDATE_MOUNT.getValue());
        mplew.writeInt(chr.getId());
        mplew.write(1);
        mplew.writeInt(chr.getMount().getLevel());
        mplew.writeInt(chr.getMount().getExp());
        mplew.writeInt(chr.getMount().getFatigue());
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket getPlayerShopNewVisitor(final MapleCharacter c, final int slot) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("getPlayerShopNewVisitor");
        }
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(HexTool.getByteArrayFromHexString("04 0" + slot));
        PacketHelper.addCharLook(mplew, c, false);
        mplew.writeMapleAsciiString(c.getName());
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket getPlayerShopRemoveVisitor(final int slot) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("getPlayerShopRemoveVisitor");
        }
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(HexTool.getByteArrayFromHexString("0A 0" + slot));
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket getTradePartnerAdd(final MapleCharacter c) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("getTradePartnerAdd");
        }
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(4);
        mplew.write(1);
        PacketHelper.addCharLook(mplew, c, false);
        mplew.writeMapleAsciiString(c.getName());
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket getTradeInvite(final MapleCharacter c, final boolean 现金交易) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("getTradeInvite");
        }
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(2);
        mplew.write(现金交易 ? 6 : 3);
        mplew.writeMapleAsciiString(c.getName());
        mplew.writeInt(0);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket getTradeMesoSet(final byte number, final int meso) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("getTradeMesoSet");
        }
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(15);
        mplew.write(number);
        mplew.writeInt(meso);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket getTradeItemAdd(final byte number, final IItem item) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("getTradeItemAdd");
        }
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(14);
        mplew.write(number);
        PacketHelper.addItemInfo(mplew, item, false, false, true);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket getTradeStart(final MapleClient c, final MapleTrade trade, final byte number, final boolean 现金交易) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("getTradeStart");
        }
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(5);
        mplew.write(现金交易 ? 6 : 3);
        mplew.write(2);
        mplew.write(number);
        if (number == 1) {
            mplew.write(0);
            PacketHelper.addCharLook(mplew, trade.getPartner().getChr(), false);
            mplew.writeMapleAsciiString(trade.getPartner().getChr().getName());
        }
        mplew.write(number);
        PacketHelper.addCharLook(mplew, c.getPlayer(), false);
        mplew.writeMapleAsciiString(c.getPlayer().getName());
        mplew.write(255);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket getTradeConfirmation() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("getTradeConfirmation");
        }
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(16);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket TradeMessage(final byte UserSlot, final byte message) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("TradeMessage");
        }
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(10);
        mplew.write(UserSlot);
        mplew.write(message);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket getTradeCancel(final byte UserSlot, final int unsuccessful) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("getTradeCancel");
        }
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(10);
        mplew.write(UserSlot);
        mplew.write((unsuccessful == 0) ? 2 : ((unsuccessful == 1) ? 9 : 10));
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket getNPCTalk(final int npc, final byte msgType, final String talk, final String endBytes, final byte type) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("getNPCTalk");
        }
        mplew.writeShort(SendPacketOpcode.NPC_TALK.getValue());
        mplew.write(4);
        mplew.writeInt(npc);
        mplew.write(msgType);
        mplew.write(type);
        mplew.writeMapleAsciiString(talk);
        mplew.write(HexTool.getByteArrayFromHexString(endBytes));
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket getMapSelection(final int npcid, final String sel) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("getMapSelection");
        }
        mplew.writeShort(SendPacketOpcode.NPC_TALK.getValue());
        mplew.write(4);
        mplew.writeInt(npcid);
        mplew.writeShort(13);
        mplew.writeInt(0);
        mplew.writeInt(5);
        mplew.writeMapleAsciiString(sel);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket getNPCTalkStyle(final int npc, final String talk, final int card, final int[] args) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("getNPCTalkStyle");
        }
        mplew.writeShort(SendPacketOpcode.NPC_TALK.getValue());
        mplew.write(4);
        mplew.writeInt(npc);
        mplew.writeShort(7);
        mplew.writeMapleAsciiString(talk);
        mplew.write(args.length);
        for (int i = 0; i < args.length; ++i) {
            mplew.writeInt(args[i]);
        }
        mplew.writeInt(card);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket getNPCTalkNum(final int npc, final String talk, final int def, final int min, final int max) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("getNPCTalkNum");
        }
        mplew.writeShort(SendPacketOpcode.NPC_TALK.getValue());
        mplew.write(4);
        mplew.writeInt(npc);
        mplew.writeShort(3);
        mplew.writeMapleAsciiString(talk);
        mplew.writeInt(def);
        mplew.writeInt(min);
        mplew.writeInt(max);
        mplew.writeInt(0);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket getNPCTalkText(final int npc, final String talk) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("getNPCTalkText");
        }
        mplew.writeShort(SendPacketOpcode.NPC_TALK.getValue());
        mplew.write(4);
        mplew.writeInt(npc);
        mplew.writeShort(2);
        mplew.writeMapleAsciiString(talk);
        mplew.writeInt(0);
        mplew.writeInt(0);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket showForeignEffect(final int cid, final int effect) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("showForeignEffect");
        }
        mplew.writeShort(SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
        mplew.writeInt(cid);
        mplew.write(effect);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket showBuffeffect(final int cid, final int skillid, final int effectid) {
        if (ServerProperties.LogPktCall) {
            log.info("showBuffeffect");
        }
        return showBuffeffect(cid, skillid, effectid, (byte) 3);
    }

    public static MaplePacket showBuffeffect(final int cid, final int skillid, final int effectid, final byte direction) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("showBuffeffectA");
        }
        mplew.writeShort(SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
        mplew.writeInt(cid);
        mplew.write(effectid);
        mplew.writeInt(skillid);
        mplew.write(2);
        mplew.write(1);
        if (direction != 3) {
            mplew.write(direction);
        }
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket showOwnBuffEffect(final int skillid, final int effectid) {
        if (ServerProperties.LogPktCall) {
            log.info("showOwnBuffEffectA");
        }
        return showOwnBuffEffect(skillid, effectid, (byte) 3);
    }

    public static MaplePacket showOwnBuffEffect(final int skillid, final int effectid, final byte direction) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("showOwnBuffEffectB");
        }
        mplew.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
        mplew.write(effectid);
        mplew.writeInt(skillid);
        mplew.write(169);
        mplew.write(1);
        if (direction != 3) {
            mplew.write(direction);
        }
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket showItemLevelupEffect() {
        if (ServerProperties.LogPktCall) {
            log.info("showItemLevelupEffect");
        }
        return showSpecialEffect(17);
    }

    public static MaplePacket showMonsterBookPickup() {
        return showSpecialEffect(14);
    }

    public static MaplePacket showEquipmentLevelUp() {
        return showSpecialEffect(17);
    }

    public static MaplePacket showItemLevelup() {
        return showSpecialEffect(17);
    }

    public static MaplePacket showForeignItemLevelupEffect(final int cid) {
        if (ServerProperties.LogPktCall) {
            log.info("showForeignItemLevelupEffect");
        }
        return showSpecialEffect(cid, 17);
    }

    public static MaplePacket showSpecialEffect(final int effect) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("showSpecialEffectA");
        }
        mplew.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
        mplew.write(effect);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket showSpecialEffect(final int cid, final int effect) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("showSpecialEffectB");
        }
        mplew.writeShort(SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
        mplew.writeInt(cid);
        mplew.write(effect);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket updateSkill(final int skillid, final int level, final int masterlevel, final long expiration) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("updateSkill");
        }
        mplew.writeShort(SendPacketOpcode.UPDATE_SKILLS.getValue());
        mplew.write(1);
        mplew.writeShort(1);
        mplew.writeInt(skillid);
        mplew.writeInt(level);
        mplew.writeInt(masterlevel);
        mplew.write(1);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket updateQuestMobKills(final MapleQuestStatus status) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("updateQuestMobKills");
        }
        mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(1);
        mplew.writeShort(status.getQuest().getId());
        mplew.write(1);
        final StringBuilder sb = new StringBuilder();
        for (final int kills : status.getMobKills().values()) {
            sb.append(StringUtil.getLeftPaddedStr(String.valueOf(kills), '0', 3));
        }
        mplew.writeMapleAsciiString(sb.toString());
        mplew.writeZeroBytes(8);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket 游戏屏幕中间黄色字体(final String status) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("updateQuestMobKills");
        }
        mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(1);
        mplew.writeShort(4761);
        mplew.write(1);
        mplew.writeMapleAsciiString(status);
        mplew.writeZeroBytes(8);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket 游戏屏幕中间黄色字体(final String status, final int id) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("updateQuestMobKills");
        }
        mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(1);
        mplew.writeShort(id);
        mplew.write(1);
        mplew.writeMapleAsciiString(status);
        mplew.writeZeroBytes(8);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket getShowQuestCompletion(final int id) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("getShowQuestCompletion");
        }
        mplew.writeShort(SendPacketOpcode.SHOW_QUEST_COMPLETION.getValue());
        mplew.writeShort(id);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket getKeymap(final MapleKeyLayout layout) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("getKeymap");
        }
        mplew.writeShort(SendPacketOpcode.KEYMAP.getValue());
        mplew.write(0);
        layout.writeData(mplew);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket getWhisper(final String sender, final int channel, final String text) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("getWhisper");
        }
        mplew.writeShort(SendPacketOpcode.WHISPER.getValue());
        mplew.write(18);
        mplew.writeMapleAsciiString(sender);
        mplew.writeShort(channel - 1);
        mplew.writeMapleAsciiString(text);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket getWhisperReply(final String target, final byte reply) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("getWhisperReply");
        }
        mplew.writeShort(SendPacketOpcode.WHISPER.getValue());
        mplew.write(10);
        mplew.writeMapleAsciiString(target);
        mplew.write(reply);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket getFindReplyWithMap(final String target, final int mapid, final boolean buddy) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("getFindReplyWithMap");
        }
        mplew.writeShort(SendPacketOpcode.WHISPER.getValue());
        mplew.write(buddy ? 72 : 9);
        mplew.writeMapleAsciiString(target);
        mplew.write(1);
        mplew.writeInt(mapid);
        mplew.writeZeroBytes(8);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket getFindReply(final String target, final int channel, final boolean buddy) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("getFindReply");
        }
        mplew.writeShort(SendPacketOpcode.WHISPER.getValue());
        mplew.write(buddy ? 72 : 9);
        mplew.writeMapleAsciiString(target);
        mplew.write(3);
        mplew.writeInt(channel - 1);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket getInventoryFull() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("getInventoryFull");
        }
        mplew.writeShort(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
        mplew.write(1);
        mplew.write(0);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket getShowInventoryFull() {
        if (ServerProperties.LogPktCall) {
            log.info("getShowInventoryFull");
        }
        return getShowInventoryStatus(255);
    }

    public static MaplePacket showItemUnavailable() {
        if (ServerProperties.LogPktCall) {
            log.info("showItemUnavailable");
        }
        return getShowInventoryStatus(254);
    }

    public static MaplePacket getShowInventoryStatus(final int mode) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("getShowInventoryStatus");
        }
        mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(0);
        mplew.write(mode);
        mplew.writeInt(0);
        mplew.writeInt(0);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket getStorage(final int npcId, final byte slots, final Collection<IItem> items, final int meso) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("getStorage");
        }
        mplew.writeShort(SendPacketOpcode.OPEN_STORAGE.getValue());
        mplew.write(22);
        mplew.writeInt(npcId);
        mplew.write(slots);
        mplew.writeShort(126);
        mplew.writeShort(0);
        mplew.writeInt(0);
        mplew.writeInt(meso);
        mplew.write((byte) items.size());
        for (final IItem item : items) {
            if (GameConstants.is豆豆装备(item.getItemId())) {
                PacketHelper.addDDItemInfo(mplew, item, true, true, false);
            } else {
                PacketHelper.addItemInfo(mplew, item, true, true);
            }
        }
        mplew.writeShort(0);
        mplew.writeShort(0);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket getStorageFull() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("getStorageFull");
        }
        mplew.writeShort(SendPacketOpcode.OPEN_STORAGE.getValue());
        mplew.write(17);
        mplew.write(0);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket mesoStorage(final byte slots, final int meso) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("mesoStorage");
        }
        mplew.writeShort(SendPacketOpcode.OPEN_STORAGE.getValue());
        mplew.write(19);
        mplew.write(slots);
        mplew.writeShort(2);
        mplew.writeShort(0);
        mplew.writeInt(0);
        mplew.writeInt(meso);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket storeStorage(final byte slots, final MapleInventoryType type, final Collection<IItem> items) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("storeStorage");
        }
        mplew.writeShort(SendPacketOpcode.OPEN_STORAGE.getValue());
        mplew.write(13);
        mplew.write(slots);
        mplew.writeShort(type.getBitfieldEncoding());
        mplew.writeShort(0);
        mplew.writeInt(0);
        mplew.write(items.size());
        for (final IItem item : items) {
            PacketHelper.addItemInfo(mplew, item, true, true);
        }
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket takeOutStorage(final byte slots, final MapleInventoryType type, final Collection<IItem> items) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("takeOutStorage");
        }
        mplew.writeShort(SendPacketOpcode.OPEN_STORAGE.getValue());
        mplew.write(9);
        mplew.write(slots);
        mplew.writeShort(type.getBitfieldEncoding());
        mplew.writeShort(0);
        mplew.writeInt(0);
        mplew.write(items.size());
        for (final IItem item : items) {
            PacketHelper.addItemInfo(mplew, item, true, true);
        }
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket fairyPendantMessage(final int type, final int percent) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("fairyPendantMessage");
        }
        mplew.writeShort(SendPacketOpcode.FAIRY_PEND_MSG.getValue());
        mplew.writeShort(21);
        mplew.writeInt(0);
        mplew.writeShort(0);
        mplew.writeShort(percent);
        mplew.writeShort(0);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket giveFameResponse(final int mode, final String charname, final int newfame) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("giveFameResponse");
        }
        mplew.writeShort(SendPacketOpcode.FAME_RESPONSE.getValue());
        mplew.write(0);
        mplew.writeMapleAsciiString(charname);
        mplew.write(mode);
        mplew.writeShort(newfame);
        mplew.writeShort(0);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket giveFameErrorResponse(final int status) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("giveFameErrorResponse");
        }
        mplew.writeShort(SendPacketOpcode.FAME_RESPONSE.getValue());
        mplew.write(status);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket receiveFame(final int mode, final String charnameFrom) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("receiveFame");
        }
        mplew.writeShort(SendPacketOpcode.FAME_RESPONSE.getValue());
        mplew.write(5);
        mplew.writeMapleAsciiString(charnameFrom);
        mplew.write(mode);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket partyCreated(final int partyid) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("partyCreated");
        }
        mplew.writeShort(SendPacketOpcode.PARTY_OPERATION.getValue());
        mplew.write(8);
        mplew.writeInt(partyid);
        mplew.write(MaplePacketCreator.CHAR_INFO_MAGIC);
        mplew.write(MaplePacketCreator.CHAR_INFO_MAGIC);
        mplew.writeInt(0);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket partyInvite(final MapleCharacter from) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("partyInvite");
        }
        mplew.writeShort(SendPacketOpcode.PARTY_OPERATION.getValue());
        mplew.write(4);
        mplew.writeInt(from.getParty().getId());
        mplew.writeMapleAsciiString(from.getName());
        mplew.write(0);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket partyStatusMessage(final int message) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("partyStatusMessageA");
        }
        mplew.writeShort(SendPacketOpcode.PARTY_OPERATION.getValue());
        mplew.write(message);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket partyStatusMessage(final int message, final String charname) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("partyStatusMessageB");
        }
        mplew.writeShort(SendPacketOpcode.PARTY_OPERATION.getValue());
        mplew.write(message);
        mplew.writeMapleAsciiString(charname);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    private static void addPartyStatus(final int forchannel, final MapleParty party, final LittleEndianWriter lew, final boolean leaving) {
        if (ServerProperties.LogPktCall) {
            log.info("addPartyStatus");
        }
        final List<MaplePartyCharacter> partymembers = new ArrayList<MaplePartyCharacter>(party.getMembers());
        while (partymembers.size() < 6) {
            partymembers.add(new MaplePartyCharacter());
        }
        for (final MaplePartyCharacter partychar : partymembers) {
            lew.writeInt(partychar.getId());
        }
        for (final MaplePartyCharacter partychar : partymembers) {
            lew.writeAsciiString(partychar.getName(), 13);
        }
        for (final MaplePartyCharacter partychar : partymembers) {
            lew.writeInt(partychar.getJobId());
        }
        for (final MaplePartyCharacter partychar : partymembers) {
            lew.writeInt(partychar.getLevel());
        }
        for (final MaplePartyCharacter partychar : partymembers) {
            if (partychar.isOnline()) {
                lew.writeInt(partychar.getChannel() - 1);
            } else {
                lew.writeInt(-2);
            }
        }
        lew.writeInt(party.getLeader().getId());
        for (final MaplePartyCharacter partychar : partymembers) {
            if (partychar.getChannel() == forchannel) {
                lew.writeInt(partychar.getMapid());
            } else {
                lew.writeInt(0);
            }
        }
        for (final MaplePartyCharacter partychar : partymembers) {
            if (partychar.getChannel() == forchannel && !leaving) {
                lew.writeInt(partychar.getDoorTown());
                lew.writeInt(partychar.getDoorTarget());
                lew.writeInt(partychar.getDoorPosition().x);
                lew.writeInt(partychar.getDoorPosition().y);
            } else {
                lew.writeInt(0);
                lew.writeInt(0);
                lew.writeInt(0);
                lew.writeInt(0);
            }
        }
    }

    public static MaplePacket updateParty(final int forChannel, final MapleParty party, final PartyOperation op, final MaplePartyCharacter target) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("updateParty");
        }
        mplew.writeShort(SendPacketOpcode.PARTY_OPERATION.getValue());
        switch (op) {
            case DISBAND:
            case EXPEL:
            case LEAVE: {
                mplew.write(12);
                mplew.writeInt(party.getId());
                mplew.writeInt(target.getId());
                mplew.write((op != PartyOperation.DISBAND) ? 1 : 0);
                if (op == PartyOperation.DISBAND) {
                    mplew.writeInt(target.getId());
                    break;
                }
                mplew.write((op == PartyOperation.EXPEL) ? 1 : 0);
                mplew.writeMapleAsciiString(target.getName());
                addPartyStatus(forChannel, party, mplew, false);
                break;
            }
            case JOIN: {
                mplew.write(15);
                mplew.writeInt(party.getId());
                mplew.writeMapleAsciiString(target.getName());
                addPartyStatus(forChannel, party, mplew, false);
                break;
            }
            case SILENT_UPDATE:
            case LOG_ONOFF: {
                mplew.write(7);
                mplew.writeInt(party.getId());
                addPartyStatus(forChannel, party, mplew, false);
                break;
            }
            case CHANGE_LEADER: {
                mplew.write(26);
                mplew.writeInt(target.getId());
                mplew.write(0);
                break;
            }
        }
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket partyPortal(final int townId, final int targetId, final int skillId, final Point position) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("partyPortal");
        }
        mplew.writeShort(SendPacketOpcode.PARTY_OPERATION.getValue());
        mplew.writeShort(35);
        mplew.writeInt(townId);
        mplew.writeInt(targetId);
        mplew.writePos(position);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket updatePartyMemberHP(final int cid, final int curhp, final int maxhp) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("updatePartyMemberHP");
        }
        mplew.writeShort(SendPacketOpcode.UPDATE_PARTYMEMBER_HP.getValue());
        mplew.writeInt(cid);
        mplew.writeInt(curhp);
        mplew.writeInt(maxhp);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket multiChat(final String name, final String chattext, final int mode) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("multiChat");
        }
        mplew.writeShort(SendPacketOpcode.MULTICHAT.getValue());
        mplew.write(mode);
        mplew.writeMapleAsciiString(name);
        mplew.writeMapleAsciiString(chattext);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket getClock(final int time) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("getClock");
        }
        mplew.writeShort(SendPacketOpcode.CLOCK.getValue());
        mplew.write(2);
        mplew.writeInt(time);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket getClockTime(final int hour, final int min, final int sec) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("getClockTime");
        }
        mplew.writeShort(SendPacketOpcode.CLOCK.getValue());
        mplew.write(1);
        mplew.write(hour);
        mplew.write(min);
        mplew.write(sec);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket spawnMist(final MapleMist mist) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("spawnMist");
        }
        mplew.writeShort(SendPacketOpcode.SPAWN_MIST.getValue());
        mplew.writeInt(mist.getObjectId());
        mplew.writeInt(mist.isMobMist() ? 0 : ((mist.isPoisonMist() != 0) ? 1 : 2));
        mplew.writeInt(mist.getOwnerId());
        if (mist.getMobSkill() == null) {
            mplew.writeInt(mist.getSourceSkill().getId());
        } else {
            mplew.writeInt(mist.getMobSkill().getSkillId());
        }
        mplew.write(mist.getSkillLevel());
        mplew.writeShort(mist.getSkillDelay());
        mplew.writeInt(mist.getBox().x);
        mplew.writeInt(mist.getBox().y);
        mplew.writeInt(mist.getBox().x + mist.getBox().width);
        mplew.writeInt(mist.getBox().y + mist.getBox().height);
        mplew.writeInt(0);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket removeMist(final int oid, final boolean eruption) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("removeMist");
        }
        mplew.writeShort(SendPacketOpcode.REMOVE_MIST.getValue());
        mplew.writeInt(oid);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket damageSummon(final int cid, final int summonSkillId, final int damage, final int unkByte, final int monsterIdFrom) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("damageSummon");
        }
        mplew.writeShort(SendPacketOpcode.DAMAGE_SUMMON.getValue());
        mplew.writeInt(cid);
        mplew.writeInt(summonSkillId);
        mplew.write(unkByte);
        mplew.writeInt(damage);
        mplew.writeInt(monsterIdFrom);
        mplew.write(0);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket buddylistMessage(final byte message) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("buddylistMessage");
        }
        mplew.writeShort(SendPacketOpcode.BUDDYLIST.getValue());
        mplew.write(message);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket updateBuddylist(final Collection<BuddyEntry> buddylist) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("updateBuddylist");
        }
        mplew.writeShort(SendPacketOpcode.BUDDYLIST.getValue());
        mplew.write(7);
        mplew.write(buddylist.size());
        for (final BuddyEntry buddy : buddylist) {
            if (buddy.isVisible()) {
                mplew.writeInt(buddy.getCharacterId());
                mplew.writeAsciiString(buddy.getName(), 13);
                mplew.write(0);
                mplew.writeInt((buddy.getChannel() == -1) ? -1 : (buddy.getChannel() - 1));
                mplew.writeAsciiString(buddy.getGroup(), 17);
            }
        }
        for (int x = 0; x < buddylist.size(); ++x) {
            mplew.writeInt(0);
        }
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("updateBuddylist MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket requestBuddylistAdd(final int cidFrom, final String nameFrom, final int levelFrom, final int jobFrom) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("requestBuddylistAdd");
        }
        mplew.writeShort(SendPacketOpcode.BUDDYLIST.getValue());
        mplew.write(9);
        mplew.writeInt(cidFrom);
        mplew.writeMapleAsciiString(nameFrom);
        mplew.writeInt(cidFrom);
        mplew.writeAsciiString(nameFrom, 13);
        mplew.write(1);
        mplew.write(5);
        mplew.write(0);
        mplew.writeShort(0);
        mplew.writeAsciiString("群未定", 17);
        mplew.write(0);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket updateBuddyChannel(final int characterid, final int channel) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("updateBuddyChannel");
        }
        mplew.writeShort(SendPacketOpcode.BUDDYLIST.getValue());
        mplew.write(20);
        mplew.writeInt(characterid);
        mplew.write(0);
        mplew.writeInt(channel);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket itemEffect(final int characterid, final int itemid) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("itemEffect");
        }
        mplew.writeShort(SendPacketOpcode.SHOW_ITEM_EFFECT.getValue());
        mplew.writeInt(characterid);
        mplew.writeInt(itemid);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket itemEffects(final int characterid, final int itemid) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("itemEffect");
        }
        mplew.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
        mplew.writeInt(characterid);
        mplew.writeInt(itemid);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket updateBuddyCapacity(final int capacity) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("updateBuddyCapacity");
        }
        mplew.writeShort(SendPacketOpcode.BUDDYLIST.getValue());
        mplew.write(21);
        mplew.write(capacity);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket showChair(final int characterid, final int itemid) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("showChair");
        }
        mplew.writeShort(SendPacketOpcode.SHOW_CHAIR.getValue());
        mplew.writeInt(characterid);
        mplew.writeInt(itemid);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket cancelChair(final int id) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("cancelChair");
        }
        mplew.writeShort(SendPacketOpcode.CANCEL_CHAIR.getValue());
        if (id == -1) {
            mplew.write(0);
        } else {
            mplew.write(1);
            mplew.writeShort(id);
        }
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket spawnReactor(final MapleReactor reactor) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("spawnReactor");
        }
        mplew.writeShort(SendPacketOpcode.REACTOR_SPAWN.getValue());
        mplew.writeInt(reactor.getObjectId());
        mplew.writeInt(reactor.getReactorId());
        mplew.write(reactor.getState());
        mplew.writePos(reactor.getPosition());
        mplew.write(reactor.getFacingDirection());
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket triggerReactor(final MapleReactor reactor, final int stance) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("triggerReactor");
        }
        mplew.writeShort(SendPacketOpcode.REACTOR_HIT.getValue());
        mplew.writeInt(reactor.getObjectId());
        mplew.write(reactor.getState());
        mplew.writePos(reactor.getPosition());
        mplew.writeInt(stance);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket destroyReactor(final MapleReactor reactor) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("destroyReactor");
        }
        mplew.writeShort(SendPacketOpcode.REACTOR_DESTROY.getValue());
        mplew.writeInt(reactor.getObjectId());
        mplew.write(reactor.getState());
        mplew.writePos(reactor.getPosition());
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket musicChange(final String song) {
        if (ServerProperties.LogPktCall) {
            log.info("musicChange");
        }
        return environmentChange(song, 6);
    }

    public static MaplePacket showEffect(final String effect) {
        if (ServerProperties.LogPktCall) {
            log.info("showEffect");
        }
        return environmentChange(effect, 3);
    }

    public static MaplePacket playSound(final String sound) {
        if (ServerProperties.LogPktCall) {
            log.info("playSound");
        }
        return environmentChange(sound, 4);
    }

    public static MaplePacket environmentChange(final String env, final int mode) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("environmentChange");
        }
        mplew.writeShort(SendPacketOpcode.BOSS_ENV.getValue());
        mplew.write(mode);
        mplew.writeMapleAsciiString(env);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket startMapEffect(final String msg, final int itemid, final boolean active) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("startMapEffect");
        }
        mplew.writeShort(SendPacketOpcode.MAP_EFFECT.getValue());
        mplew.write(active ? 0 : 1);
        mplew.writeInt(itemid);
        if (active) {
            mplew.writeMapleAsciiString(msg);
        }
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket removeMapEffect() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("removeMapEffect");
        }
        mplew.writeShort(SendPacketOpcode.MAP_EFFECT.getValue());
        mplew.write(0);
        mplew.writeInt(0);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket fuckGuildInfo(final MapleCharacter c) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("fuckGuildInfo");
        }
        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(26);
        String Prefix = "";
        if (c.getPrefix() == 1) {
            Prefix = "[技术团队成员]";
        }
        if (c.getPrefix() == 2) {
            Prefix = "[游戏管理成员]";
        }
        if (c.getPrefix() == 3) {
            Prefix = "[活动办理成员]";
        }
        mplew.write(1);
        mplew.writeInt(0);
        mplew.writeMapleAsciiString(Prefix);
        mplew.write(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeShort(0);
        mplew.write(0);
        mplew.writeShort(0);
        mplew.write(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket showGuildInfo(final MapleCharacter c) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("showGuildInfo");
        }
        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(26);
        if (c == null || c.getMGC() == null) {
            mplew.write(0);
            return mplew.getPacket();
        }
        final MapleGuild g = World.Guild.getGuild(c.getGuildId());
        if (g == null) {
            mplew.write(0);
            return mplew.getPacket();
        }
        final MapleGuildCharacter mgc = g.getMGC(c.getId());
        c.setGuildRank(mgc.getGuildRank());
        mplew.write(1);
        getGuildInfo(mplew, g);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    private static void getGuildInfo(final MaplePacketLittleEndianWriter mplew, final MapleGuild guild) {
        if (ServerProperties.LogPktCall) {
            log.info("getGuildInfo");
        }
        mplew.writeInt(guild.getId());
        mplew.writeMapleAsciiString(guild.getName());
        for (int i = 1; i <= 5; ++i) {
            mplew.writeMapleAsciiString(guild.getRankTitle(i));
        }
        guild.addMemberData(mplew);
        mplew.writeInt(guild.getCapacity());
        mplew.writeShort(guild.getLogoBG());
        mplew.write(guild.getLogoBGColor());
        mplew.writeShort(guild.getLogo());
        mplew.write(guild.getLogoColor());
        mplew.writeMapleAsciiString(guild.getNotice());
        mplew.writeInt(guild.getGP());
        mplew.writeInt((guild.getAllianceId() > 0) ? guild.getAllianceId() : 0);
    }

    private static void getGuildInfo2(final MaplePacketLittleEndianWriter mplew, final MapleGuild guild, final MapleCharacter chr) {
        if (ServerProperties.LogPktCall) {
            log.info("getGuildInfo2");
        }
        mplew.writeInt(guild.getId());
        mplew.writeMapleAsciiString(guild.getName());
        for (int i = 1; i <= 5; ++i) {
            mplew.writeMapleAsciiString(guild.getRankTitle(i));
        }
        guild.addMemberData(mplew);
        mplew.writeInt(guild.getCapacity());
        mplew.writeShort(guild.getLogoBG());
        mplew.write(guild.getLogoBGColor());
        mplew.writeShort(guild.getLogo());
        mplew.write(guild.getLogoColor());
        mplew.writeMapleAsciiString(guild.getNotice());
        mplew.writeInt(guild.getGP());
        mplew.writeInt((guild.getAllianceId() > 0) ? guild.getAllianceId() : 0);
    }

    public static MaplePacket guildMemberOnline(final int gid, final int cid, final boolean bOnline) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("guildMemberOnline");
        }
        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(61);
        mplew.writeInt(gid);
        mplew.writeInt(cid);
        mplew.write(bOnline ? 1 : 0);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket guildInvite(final int gid, final String charName, final int levelFrom, final int jobFrom) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("guildInvite");
        }
        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(5);
        mplew.writeInt(gid);
        mplew.writeMapleAsciiString(charName);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket denyGuildInvitation(final String charname) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("denyGuildInvitation");
        }
        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(55);
        mplew.writeMapleAsciiString(charname);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket genericGuildMessage(final byte code) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("genericGuildMessage");
        }
        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(code);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket newGuildMember(final MapleGuildCharacter mgc) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("newGuildMember");
        }
        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(39);
        mplew.writeInt(mgc.getGuildId());
        mplew.writeInt(mgc.getId());
        mplew.writeAsciiString(mgc.getName(), 13);
        mplew.writeInt(mgc.getJobId());
        mplew.writeInt(mgc.getLevel());
        mplew.writeInt(mgc.getGuildRank());
        mplew.writeInt(mgc.isOnline() ? 1 : 0);
        mplew.writeInt(1);
        mplew.writeInt(mgc.getAllianceRank());
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket memberLeft(final MapleGuildCharacter mgc, final boolean bExpelled) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("memberLeft");
        }
        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(bExpelled ? 47 : 44);
        mplew.writeInt(mgc.getGuildId());
        mplew.writeInt(mgc.getId());
        mplew.writeMapleAsciiString(mgc.getName());
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket changeRank(final MapleGuildCharacter mgc) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("changeRank");
        }
        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(64);
        mplew.writeInt(mgc.getGuildId());
        mplew.writeInt(mgc.getId());
        mplew.write(mgc.getGuildRank());
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket guildNotice(final int gid, final String notice) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("guildNotice");
        }
        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(68);
        mplew.writeInt(gid);
        mplew.writeMapleAsciiString(notice);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket guildMemberLevelJobUpdate(final MapleGuildCharacter mgc) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("guildMemberLevelJobUpdate");
        }
        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(60);
        mplew.writeInt(mgc.getGuildId());
        mplew.writeInt(mgc.getId());
        mplew.writeInt(mgc.getLevel());
        mplew.writeInt(mgc.getJobId());
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket rankTitleChange(final int gid, final String[] ranks) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("rankTitleChange");
        }
        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(62);
        mplew.writeInt(gid);
        for (final String r : ranks) {
            mplew.writeMapleAsciiString(r);
        }
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket guildDisband(final int gid) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("guildDisband");
        }
        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(50);
        mplew.writeInt(gid);
        mplew.write(1);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket guildEmblemChange(final int gid, final short bg, final byte bgcolor, final short logo, final byte logocolor) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("guildEmblemChange");
        }
        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(66);
        mplew.writeInt(gid);
        mplew.writeShort(bg);
        mplew.write(bgcolor);
        mplew.writeShort(logo);
        mplew.write(logocolor);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket guildCapacityChange(final int gid, final int capacity) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("guildCapacityChange");
        }
        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(58);
        mplew.writeInt(gid);
        mplew.write(capacity);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket removeGuildFromAlliance(final MapleGuildAlliance alliance, final MapleGuild expelledGuild, final boolean expelled) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("removeGuildFromAlliance");
        }
        mplew.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(16);
        addAllianceInfo(mplew, alliance);
        getGuildInfo(mplew, expelledGuild);
        mplew.write(expelled ? 1 : 0);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket changeAlliance(final MapleGuildAlliance alliance, final boolean in) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("changeAlliance");
        }
        mplew.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(1);
        mplew.write(in ? 1 : 0);
        mplew.writeInt(in ? alliance.getId() : 0);
        final int noGuilds = alliance.getNoGuilds();
        final MapleGuild[] g = new MapleGuild[noGuilds];
        for (int i = 0; i < noGuilds; ++i) {
            g[i] = World.Guild.getGuild(alliance.getGuildId(i));
            if (g[i] == null) {
                return enableActions();
            }
        }
        mplew.write(noGuilds);
        for (int i = 0; i < noGuilds; ++i) {
            mplew.writeInt(g[i].getId());
            final Collection<MapleGuildCharacter> members = g[i].getMembers();
            mplew.writeInt(members.size());
            for (final MapleGuildCharacter mgc : members) {
                mplew.writeInt(mgc.getId());
                mplew.write(in ? mgc.getAllianceRank() : 0);
            }
        }
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket changeAllianceLeader(final int allianceid, final int newLeader, final int oldLeader) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("changeAllianceLeaderA");
        }
        mplew.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(2);
        mplew.writeInt(allianceid);
        mplew.writeInt(oldLeader);
        mplew.writeInt(newLeader);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket updateAllianceLeader(final int allianceid, final int newLeader, final int oldLeader) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("updateAllianceLeaderB");
        }
        mplew.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(25);
        mplew.writeInt(allianceid);
        mplew.writeInt(oldLeader);
        mplew.writeInt(newLeader);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket sendAllianceInvite(final String allianceName, final MapleCharacter inviter) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("sendAllianceInvite");
        }
        mplew.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(3);
        mplew.writeInt(inviter.getGuildId());
        mplew.writeMapleAsciiString(inviter.getName());
        mplew.writeMapleAsciiString(allianceName);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket changeGuildInAlliance(final MapleGuildAlliance alliance, final MapleGuild guild, final boolean add) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("changeGuildInAlliance");
        }
        mplew.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(4);
        mplew.writeInt(add ? alliance.getId() : 0);
        mplew.writeInt(guild.getId());
        final Collection<MapleGuildCharacter> members = guild.getMembers();
        mplew.writeInt(members.size());
        for (final MapleGuildCharacter mgc : members) {
            mplew.writeInt(mgc.getId());
            mplew.write(add ? mgc.getAllianceRank() : 0);
        }
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket changeAllianceRank(final int allianceid, final MapleGuildCharacter player) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("changeAllianceRank");
        }
        mplew.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(5);
        mplew.writeInt(allianceid);
        mplew.writeInt(player.getId());
        mplew.writeInt(player.getAllianceRank());
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket createGuildAlliance(final MapleGuildAlliance alliance) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("createGuildAlliance");
        }
        mplew.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(15);
        addAllianceInfo(mplew, alliance);
        final int noGuilds = alliance.getNoGuilds();
        final MapleGuild[] g = new MapleGuild[noGuilds];
        for (int i = 0; i < alliance.getNoGuilds(); ++i) {
            g[i] = World.Guild.getGuild(alliance.getGuildId(i));
            if (g[i] == null) {
                return enableActions();
            }
        }
        for (final MapleGuild gg : g) {
            getGuildInfo(mplew, gg);
        }
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket getAllianceInfo(final MapleGuildAlliance alliance) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("getAllianceInfo");
        }
        mplew.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(12);
        mplew.write((alliance != null) ? 1 : 0);
        if (alliance != null) {
            addAllianceInfo(mplew, alliance);
        }
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket getAllianceUpdate(final MapleGuildAlliance alliance) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("getAllianceUpdate");
        }
        mplew.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(23);
        addAllianceInfo(mplew, alliance);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket getGuildAlliance(final MapleGuildAlliance alliance) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("getGuildAlliance");
        }
        mplew.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(13);
        if (alliance == null) {
            mplew.writeInt(0);
            return mplew.getPacket();
        }
        final int noGuilds = alliance.getNoGuilds();
        final MapleGuild[] g = new MapleGuild[noGuilds];
        for (int i = 0; i < alliance.getNoGuilds(); ++i) {
            g[i] = World.Guild.getGuild(alliance.getGuildId(i));
            if (g[i] == null) {
                return enableActions();
            }
        }
        mplew.writeInt(noGuilds);
        for (final MapleGuild gg : g) {
            getGuildInfo(mplew, gg);
        }
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket addGuildToAlliance(final MapleGuildAlliance alliance, final MapleGuild newGuild) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("addGuildToAlliance");
        }
        mplew.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(18);
        addAllianceInfo(mplew, alliance);
        mplew.writeInt(newGuild.getId());
        getGuildInfo(mplew, newGuild);
        mplew.write(0);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    private static void addAllianceInfo(final MaplePacketLittleEndianWriter mplew, final MapleGuildAlliance alliance) {
        if (ServerProperties.LogPktCall) {
            log.info("addAllianceInfo");
        }
        mplew.writeInt(alliance.getId());
        mplew.writeMapleAsciiString(alliance.getName());
        for (int i = 1; i <= 5; ++i) {
            mplew.writeMapleAsciiString(alliance.getRank(i));
        }
        mplew.write(alliance.getNoGuilds());
        for (int i = 0; i < alliance.getNoGuilds(); ++i) {
            mplew.writeInt(alliance.getGuildId(i));
        }
        mplew.writeInt(alliance.getCapacity());
        mplew.writeMapleAsciiString(alliance.getNotice());
    }

    public static MaplePacket allianceMemberOnline(final int alliance, final int gid, final int id, final boolean online) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("allianceMemberOnline");
        }
        mplew.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(14);
        mplew.writeInt(alliance);
        mplew.writeInt(gid);
        mplew.writeInt(id);
        mplew.write(online ? 1 : 0);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket updateAlliance(final MapleGuildCharacter mgc, final int allianceid) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("updateAlliance");
        }
        mplew.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(24);
        mplew.writeInt(allianceid);
        mplew.writeInt(mgc.getGuildId());
        mplew.writeInt(mgc.getId());
        mplew.writeInt(mgc.getLevel());
        mplew.writeInt(mgc.getJobId());
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket updateAllianceRank(final int allianceid, final MapleGuildCharacter mgc) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("updateAllianceRank");
        }
        mplew.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(27);
        mplew.writeInt(allianceid);
        mplew.writeInt(mgc.getId());
        mplew.writeInt(mgc.getAllianceRank());
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket disbandAlliance(final int alliance) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("disbandAlliance");
        }
        mplew.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(29);
        mplew.writeInt(alliance);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket BBSThreadList(final List<MapleBBSThread> bbs, int start) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("BBSThreadList");
        }
        mplew.writeShort(SendPacketOpcode.BBS_OPERATION.getValue());
        mplew.write(6);
        if (bbs == null) {
            mplew.write(0);
            mplew.writeLong(0L);
            return mplew.getPacket();
        }
        int threadCount = bbs.size();
        MapleBBSThread notice = null;
        for (final MapleBBSThread b : bbs) {
            if (b.isNotice()) {
                notice = b;
                break;
            }
        }
        final int ret = (notice != null) ? 1 : 0;
        mplew.write(ret);
        if (notice != null) {
            addThread(mplew, notice);
            --threadCount;
        }
        if (threadCount < start) {
            start = 0;
        }
        mplew.writeInt(threadCount);
        final int pages = Math.min(10, threadCount - start);
        mplew.writeInt(pages);
        for (int i = 0; i < pages; ++i) {
            addThread(mplew, bbs.get(start + i + ret));
        }
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    private static void addThread(final MaplePacketLittleEndianWriter mplew, final MapleBBSThread rs) {
        if (ServerProperties.LogPktCall) {
            log.info("addThread");
        }
        mplew.writeInt(rs.localthreadID);
        mplew.writeInt(rs.ownerID);
        mplew.writeMapleAsciiString(rs.name);
        mplew.writeLong(PacketHelper.getKoreanTimestamp(rs.timestamp));
        mplew.writeInt(rs.icon);
        mplew.writeInt(rs.getReplyCount());
    }

    public static MaplePacket showThread(final MapleBBSThread thread) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("showThread");
        }
        mplew.writeShort(SendPacketOpcode.BBS_OPERATION.getValue());
        mplew.write(7);
        mplew.writeInt(thread.localthreadID);
        mplew.writeInt(thread.ownerID);
        mplew.writeLong(PacketHelper.getKoreanTimestamp(thread.timestamp));
        mplew.writeMapleAsciiString(thread.name);
        mplew.writeMapleAsciiString(thread.text);
        mplew.writeInt(thread.icon);
        mplew.writeInt(thread.getReplyCount());
        for (final MapleBBSThread.MapleBBSReply reply : thread.replies.values()) {
            mplew.writeInt(reply.replyid);
            mplew.writeInt(reply.ownerID);
            mplew.writeLong(PacketHelper.getKoreanTimestamp(reply.timestamp));
            mplew.writeMapleAsciiString(reply.content);
        }
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket showGuildRanks(final int npcid, final List<MapleGuildRanking.GuildRankingInfo> all) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("showGuildRanks");
        }
        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(73);
        mplew.writeInt(npcid);
        mplew.writeInt(all.size());
        for (final MapleGuildRanking.GuildRankingInfo info : all) {
            mplew.writeMapleAsciiString(info.getName());
            mplew.writeInt(info.getGP());
            mplew.writeInt(info.getLogo());
            mplew.writeInt(info.getLogoColor());
            mplew.writeInt(info.getLogoBg());
            mplew.writeInt(info.getLogoBgColor());
        }
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket showmesoRanks(int npcid, List<MapleGuildRanking.mesoRankingInfo> all) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(73);
        mplew.writeInt(npcid);
        mplew.writeInt(all.size());
        for (MapleGuildRanking.mesoRankingInfo info : all) {
            mplew.writeMapleAsciiString(info.getName());
            mplew.writeInt(Long.valueOf(info.getMeso()).intValue());
            mplew.writeInt(info.getStr());
            mplew.writeInt(info.getDex());
            mplew.writeInt(info.getInt());
            mplew.writeInt(info.getLuk());
        }
        if (ServerProperties.LogClientErr) {
            ServerConstants ERROR = new ServerConstants();
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket showlevelRanks(final int npcid, final List<MapleGuildRanking.levelRankingInfo> all) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(73);
        mplew.writeInt(npcid);
        mplew.writeInt(all.size());
        for (final MapleGuildRanking.levelRankingInfo info : all) {
            mplew.writeMapleAsciiString(info.getName());
            mplew.writeInt(info.getLevel());
            mplew.writeInt(info.getStr());
            mplew.writeInt(info.getDex());
            mplew.writeInt(info.getInt());
            mplew.writeInt(info.getLuk());
        }
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket showGuildRanks(final int npcid, final ResultSet rs) throws SQLException {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(73);
        mplew.writeInt(npcid);
        if (!rs.last()) {
            mplew.writeInt(0);
            return mplew.getPacket();
        }
        mplew.writeInt(rs.getRow());
        rs.beforeFirst();
        while (rs.next()) {
            mplew.writeMapleAsciiString(rs.getString("name"));
            mplew.writeInt(rs.getInt("GP"));
            mplew.writeInt(rs.getInt("logo"));
            mplew.writeInt(rs.getInt("logoColor"));
            mplew.writeInt(rs.getInt("logoBG"));
            mplew.writeInt(rs.getInt("logoBGColor"));
        }
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket showLevelRanks(final int npcid, final ResultSet rs) throws SQLException {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(73);
        mplew.writeInt(npcid);
        if (!rs.last()) {
            mplew.writeInt(0);
            return mplew.getPacket();
        }
        mplew.writeInt(rs.getRow());
        rs.beforeFirst();
        while (rs.next()) {
            mplew.writeMapleAsciiString(rs.getString("name"));
            mplew.writeInt(rs.getInt("level"));
            mplew.writeInt(rs.getInt("vip"));
            mplew.writeInt(rs.getInt("meso"));
            mplew.writeInt(0);
            mplew.writeInt(0);
        }
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket showMesoRanks(final int npcid, final ResultSet rs) throws SQLException {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(73);
        mplew.writeInt(npcid);
        if (!rs.last()) {
            mplew.writeInt(0);
            return mplew.getPacket();
        }
        mplew.writeInt(rs.getRow());
        rs.beforeFirst();
        while (rs.next()) {
            mplew.writeMapleAsciiString(rs.getString("name"));
            mplew.writeInt(rs.getInt("meso"));
            mplew.writeInt(rs.getInt("vip"));
            mplew.writeInt(rs.getInt("level"));
            mplew.writeInt(0);
            mplew.writeInt(0);
        }
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket MapleMSpvpdeaths(final int npcid, final ResultSet rs) throws SQLException {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(73);
        mplew.writeInt(npcid);
        if (!rs.last()) {
            mplew.writeInt(0);
            return mplew.getPacket();
        }
        mplew.writeInt(rs.getRow());
        rs.beforeFirst();
        while (rs.next()) {
            mplew.writeMapleAsciiString(rs.getString("name"));
            mplew.writeInt(rs.getInt("pvpdeaths"));
            mplew.writeInt(rs.getInt("str"));
            mplew.writeInt(rs.getInt("dex"));
            mplew.writeInt(rs.getInt("int"));
            mplew.writeInt(rs.getInt("luk"));
        }
        return mplew.getPacket();
    }

    public static MaplePacket showCustomRanks(final int npcid, final ResultSet rs) throws SQLException {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(73);
        mplew.writeInt(npcid);
        if (!rs.last()) {
            mplew.writeInt(0);
            return mplew.getPacket();
        }
        mplew.writeInt(rs.getRow());
        rs.beforeFirst();
        while (rs.next()) {
            mplew.writeMapleAsciiString(rs.getString("name"));
            mplew.writeInt(rs.getInt("data"));
            mplew.writeInt(rs.getInt("level"));
            mplew.writeInt(rs.getInt("meso"));
            mplew.writeInt(0);
            mplew.writeInt(0);
        }
        return mplew.getPacket();
    }

    public static MaplePacket MapleMSpvpkills(final int npcid, final ResultSet rs) throws SQLException {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(73);
        mplew.writeInt(npcid);
        if (!rs.last()) {
            mplew.writeInt(0);
            return mplew.getPacket();
        }
        mplew.writeInt(rs.getRow());
        rs.beforeFirst();
        while (rs.next()) {
            mplew.writeMapleAsciiString(rs.getString("name"));
            mplew.writeInt(rs.getInt("pvpkills"));
            mplew.writeInt(rs.getInt("str"));
            mplew.writeInt(rs.getInt("dex"));
            mplew.writeInt(rs.getInt("int"));
            mplew.writeInt(rs.getInt("luk"));
        }
        return mplew.getPacket();
    }

    public static MaplePacket showRQRanks(final int npcid, final ResultSet rs) throws SQLException {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(73);
        mplew.writeInt(npcid);
        if (!rs.last()) {
            mplew.writeInt(0);
            return mplew.getPacket();
        }
        mplew.writeInt(rs.getRow());
        rs.beforeFirst();
        while (rs.next()) {
            mplew.writeMapleAsciiString(rs.getString("name"));
            mplew.writeInt(rs.getInt("fame"));
            mplew.writeInt(rs.getInt("level"));
            mplew.writeInt(rs.getInt("meso"));
            mplew.writeInt(0);
            mplew.writeInt(0);
        }
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket showVipRanks(final int npcid, final ResultSet rs) throws SQLException {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(73);
        mplew.writeInt(npcid);
        if (!rs.last()) {
            mplew.writeInt(0);
            return mplew.getPacket();
        }
        mplew.writeInt(rs.getRow());
        rs.beforeFirst();
        while (rs.next()) {
            mplew.writeMapleAsciiString(rs.getString("name"));
            mplew.writeInt(rs.getInt("vip"));
            mplew.writeInt(rs.getInt("level"));
            mplew.writeInt(rs.getInt("meso"));
            mplew.writeInt(0);
            mplew.writeInt(0);
        }
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket updateGP(final int gid, final int GP) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("updateGP");
        }
        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(72);
        mplew.writeInt(gid);
        mplew.writeInt(GP);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket skillEffect(final MapleCharacter from, final int skillId, final byte level, final byte flags, final byte speed, final byte unk) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("skillEffect");
        }
        mplew.writeShort(SendPacketOpcode.SKILL_EFFECT.getValue());
        mplew.writeInt(from.getId());
        mplew.writeInt(skillId);
        mplew.write(level);
        mplew.write(flags);
        mplew.write(speed);
        mplew.write(unk);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket skillCancel(final MapleCharacter from, final int skillId) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("skillCancel");
        }
        mplew.writeShort(SendPacketOpcode.CANCEL_SKILL_EFFECT.getValue());
        mplew.writeInt(from.getId());
        mplew.writeInt(skillId);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket showMagnet(final int mobid, final byte success) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("showMagnet");
        }
        mplew.writeShort(SendPacketOpcode.SHOW_MAGNET.getValue());
        mplew.writeInt(mobid);
        mplew.write(success);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket sendHint(final String hint, int width, int height) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("sendHint");
        }
        if (width < 1) {
            width = hint.length() * 10;
            if (width < 40) {
                width = 40;
            }
        }
        if (height < 5) {
            height = 5;
        }
        mplew.writeShort(SendPacketOpcode.PLAYER_HINT.getValue());
        mplew.writeMapleAsciiString(hint);
        mplew.writeShort(width);
        mplew.writeShort(height);
        mplew.write(1);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket messengerInvite(final String from, final int messengerid) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("messengerInvite");
        }
        mplew.writeShort(SendPacketOpcode.MESSENGER.getValue());
        mplew.write(3);
        mplew.writeMapleAsciiString(from);
        mplew.write(5);
        mplew.writeInt(messengerid);
        mplew.write(0);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket addMessengerPlayer(final String from, final MapleCharacter chr, final int position, final int channel) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("addMessengerPlayer");
        }
        mplew.writeShort(SendPacketOpcode.MESSENGER.getValue());
        mplew.write(0);
        mplew.write(position);
        PacketHelper.addCharLook(mplew, chr, true);
        mplew.writeMapleAsciiString(from);
        mplew.writeShort(channel);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket removeMessengerPlayer(final int position) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("removeMessengerPlayer");
        }
        mplew.writeShort(SendPacketOpcode.MESSENGER.getValue());
        mplew.write(2);
        mplew.write(position);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket updateMessengerPlayer(final String from, final MapleCharacter chr, final int position, final int channel) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("updateMessengerPlayer");
        }
        mplew.writeShort(SendPacketOpcode.MESSENGER.getValue());
        mplew.write(7);
        mplew.write(position);
        PacketHelper.addCharLook(mplew, chr, true);
        mplew.writeMapleAsciiString(from);
        mplew.writeShort(channel);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket joinMessenger(final int position) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("joinMessenger");
        }
        mplew.writeShort(SendPacketOpcode.MESSENGER.getValue());
        mplew.write(1);
        mplew.write(position);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket messengerChat(final String text) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("messengerChat");
        }
        mplew.writeShort(SendPacketOpcode.MESSENGER.getValue());
        mplew.write(6);
        mplew.writeMapleAsciiString(text);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket messengerNote(final String text, final int mode, final int mode2) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("messengerNote");
        }
        mplew.writeShort(SendPacketOpcode.MESSENGER.getValue());
        mplew.write(mode);
        mplew.writeMapleAsciiString(text);
        mplew.write(mode2);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket getFindReplyWithCS(final String target, final boolean buddy) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("getFindReplyWithCS");
        }
        mplew.writeShort(SendPacketOpcode.WHISPER.getValue());
        mplew.write(buddy ? 72 : 9);
        mplew.writeMapleAsciiString(target);
        mplew.write(2);
        mplew.writeInt(-1);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket getFindReplyWithMTS(final String target, final boolean buddy) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("getFindReplyWithMTS");
        }
        mplew.writeShort(SendPacketOpcode.WHISPER.getValue());
        mplew.write(buddy ? 72 : 9);
        mplew.writeMapleAsciiString(target);
        mplew.write(0);
        mplew.writeInt(-1);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket showEquipEffect() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("showEquipEffectA");
        }
        mplew.writeShort(SendPacketOpcode.SHOW_EQUIP_EFFECT.getValue());
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket showEquipEffect(final int team) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("showEquipEffectB");
        }
        mplew.writeShort(SendPacketOpcode.SHOW_EQUIP_EFFECT.getValue());
        mplew.writeShort(team);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket summonSkill(final int cid, final int summonSkillId, final int newStance) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("summonSkill");
        }
        mplew.writeShort(SendPacketOpcode.SUMMON_SKILL.getValue());
        mplew.writeInt(cid);
        mplew.writeInt(summonSkillId);
        mplew.write(newStance);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket skillCooldown(final int sid, final int time) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("skillCooldown");
        }
        mplew.writeShort(SendPacketOpcode.COOLDOWN.getValue());
        mplew.writeInt(sid);
        mplew.writeShort(time);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket useSkillBook(final MapleCharacter chr, final int skillid, final int maxlevel, final boolean canuse, final boolean success) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("useSkillBook");
        }
        mplew.writeShort(SendPacketOpcode.USE_SKILL_BOOK.getValue());
        mplew.writeInt(chr.getId());
        mplew.write(1);
        mplew.writeInt(skillid);
        mplew.writeInt(maxlevel);
        mplew.write(canuse ? 1 : 0);
        mplew.write(success ? 1 : 0);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket getMacros(final SkillMacro[] macros) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("getMacros");
        }
        mplew.writeShort(SendPacketOpcode.SKILL_MACRO.getValue());
        int count = 0;
        for (int i = 0; i < 5; ++i) {
            if (macros[i] != null) {
                ++count;
            }
        }
        mplew.write(count);
        for (int i = 0; i < 5; ++i) {
            final SkillMacro macro = macros[i];
            if (macro != null) {
                mplew.writeMapleAsciiString(macro.getName());
                mplew.write(macro.getShout());
                mplew.writeInt(macro.getSkill1());
                mplew.writeInt(macro.getSkill2());
                mplew.writeInt(macro.getSkill3());
            }
        }
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket updateAriantPQRanking(final String name, final int score, final boolean empty) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("updateAriantPQRanking");
        }
        mplew.writeShort(SendPacketOpcode.ARIANT_SCORE_UPDATE.getValue());
        mplew.write(empty ? 0 : 1);
        if (!empty) {
            mplew.writeMapleAsciiString(name);
            mplew.writeInt(score);
        }
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket catchMonster(final int mobid, final int itemid, final byte success) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("catchMonster");
        }
        if (itemid == 2270002) {
        }
        mplew.writeShort(153);
        mplew.writeInt(mobid);
        mplew.writeInt(itemid);
        mplew.write(success);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket showAriantScoreBoard() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("showAriantScoreBoard");
        }
        mplew.writeShort(SendPacketOpcode.ARIANT_SCOREBOARD.getValue());
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.write(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket boatPacket(final boolean type) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("boatPacket1");
        }
        mplew.writeShort(SendPacketOpcode.BOAT_PACKET.getValue());
        mplew.writeShort(type ? 1 : 2);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket boatPacket(final int effect) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("boatPacket2");
        }
        mplew.writeShort(SendPacketOpcode.BOAT_PACKET.getValue());
        mplew.writeShort(effect);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket boatEffect(final int effect) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("boatEffect");
        }
        mplew.writeShort(SendPacketOpcode.BOAT_EFFECT.getValue());
        mplew.writeShort(effect);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket removeItemFromDuey(final boolean remove, final int Package) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("removeItemFromDuey");
        }
        mplew.writeShort(SendPacketOpcode.DUEY.getValue());
        mplew.write(23);
        mplew.writeInt(Package);
        mplew.write(remove ? 3 : 4);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket sendDuey(final byte operation, final List<MapleDueyActions> packages) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("sendDuey");
        }
        mplew.writeShort(SendPacketOpcode.DUEY.getValue());
        mplew.write(operation);
        switch (operation) {
            case 8: {
                mplew.write(1);
                break;
            }
            case 9: {
                mplew.write(0);
                mplew.write(packages.size());
                for (final MapleDueyActions dp : packages) {
                    mplew.writeInt(dp.getPackageId());
                    mplew.writeAsciiString(dp.getSender(), 15);
                    mplew.writeInt(dp.getMesos());
                    mplew.writeLong(KoreanDateUtil.getFileTimestamp(dp.getSentTime(), false));
                    mplew.writeZeroBytes(205);
                    if (dp.getItem() != null) {
                        mplew.write(1);
                        PacketHelper.addItemInfo(mplew, dp.getItem(), true, true);
                    } else {
                        mplew.write(0);
                    }
                }
                mplew.write(0);
                break;
            }
        }
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket Mulung_DojoUp2() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("Mulung_DojoUp2");
        }
        mplew.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
        mplew.write(7);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket dojoWarpUp() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.DOJO_WARP_UP.getValue());
        mplew.write(0);
        mplew.write(6);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket showQuestMsg(final String msg) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("showQuestMsg");
        }
        mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(9);
        mplew.writeMapleAsciiString(msg);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket HSText(final String m) {
        if (ServerProperties.LogPktCall) {
            log.info("Mulung_Pts");
        }
        return showQuestMsg(m);
    }

    public static MaplePacket Mulung_Pts(final int recv, final int total) {
        if (ServerProperties.LogPktCall) {
            log.info("Mulung_Pts");
        }
        return showQuestMsg("你获得 " + recv + " 修炼点数, 目前累计了 " + total + " 点修炼点数");
    }

    public static MaplePacket showOXQuiz(final int questionSet, final int questionId, final boolean askQuestion) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("showOXQuiz");
        }
        mplew.writeShort(SendPacketOpcode.OX_QUIZ.getValue());
        mplew.write(askQuestion ? 1 : 0);
        mplew.write(questionSet);
        mplew.writeShort(questionId);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket leftKnockBack() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("leftKnockBack");
        }
        mplew.writeShort(SendPacketOpcode.LEFT_KNOCK_BACK.getValue());
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket rollSnowball(final int type, final MapleSnowball.MapleSnowballs ball1, final MapleSnowball.MapleSnowballs ball2) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("rollSnowball");
        }
        mplew.writeShort(SendPacketOpcode.ROLL_SNOWBALL.getValue());
        mplew.write(type);
        mplew.writeInt((ball1 == null) ? 0 : (ball1.getSnowmanHP() / 75));
        mplew.writeInt((ball2 == null) ? 0 : (ball2.getSnowmanHP() / 75));
        mplew.writeShort((ball1 == null) ? 0 : ball1.getPosition());
        mplew.write(0);
        mplew.writeShort((ball2 == null) ? 0 : ball2.getPosition());
        mplew.writeZeroBytes(11);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket enterSnowBall() {
        if (ServerProperties.LogPktCall) {
            log.info("enterSnowBall");
        }
        return rollSnowball(0, null, null);
    }

    public static MaplePacket hitSnowBall(final int team, final int damage, final int distance, final int delay) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("hitSnowBall");
        }
        mplew.writeShort(SendPacketOpcode.HIT_SNOWBALL.getValue());
        mplew.write(team);
        mplew.writeShort(damage);
        mplew.write(distance);
        mplew.write(delay);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket snowballMessage(final int team, final int message) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("snowballMessage");
        }
        mplew.writeShort(SendPacketOpcode.SNOWBALL_MESSAGE.getValue());
        mplew.write(team);
        mplew.writeInt(message);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket finishedSort(final int type) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("finishedSort");
        }
        mplew.writeShort(SendPacketOpcode.FINISH_SORT.getValue());
        mplew.write(1);
        mplew.write(type);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket coconutScore(final int[] coconutscore) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("coconutScore");
        }
        mplew.writeShort(SendPacketOpcode.COCONUT_SCORE.getValue());
        mplew.writeShort(coconutscore[0]);
        mplew.writeShort(coconutscore[1]);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket hitCoconut(final boolean spawn, final int id, final int type) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("hitCoconut");
        }
        mplew.writeShort(SendPacketOpcode.HIT_COCONUT.getValue());
        if (spawn) {
            mplew.write(0);
            mplew.writeInt(128);
        } else {
            mplew.writeInt(id);
            mplew.write(type);
        }
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket finishedGather(final int type) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("finishedGather");
        }
        mplew.writeShort(SendPacketOpcode.FINISH_GATHER.getValue());
        mplew.write(1);
        mplew.write(type);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket yellowChat(final String msg) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("yellowChat");
        }
        mplew.writeShort(SendPacketOpcode.YELLOW_CHAT.getValue());
        mplew.write(-1);
        mplew.writeMapleAsciiString(msg);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket sendLevelup(final boolean family, final int level, final String name) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("sendLevelup");
        }
        mplew.writeShort(SendPacketOpcode.LEVEL_UPDATE.getValue());
        mplew.write(family ? 1 : 2);
        mplew.writeInt(level);
        mplew.writeMapleAsciiString(name);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket sendMarriage(final boolean family, final String name) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("sendMarriage");
        }
        mplew.writeShort(SendPacketOpcode.MARRIAGE_UPDATE.getValue());
        mplew.write(family ? 1 : 0);
        mplew.writeMapleAsciiString(name);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket sendJobup(final boolean family, final int jobid, final String name) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("sendJobup");
        }
        mplew.writeShort(SendPacketOpcode.JOB_UPDATE.getValue());
        mplew.write(family ? 1 : 0);
        mplew.writeInt(jobid);
        mplew.writeMapleAsciiString(name);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket showZakumShrine(final boolean spawned, final int time) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("showZakumShrine");
        }
        mplew.writeShort(SendPacketOpcode.ZAKUM_SHRINE.getValue());
        mplew.write(spawned ? 1 : 0);
        mplew.writeInt(time);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket showHorntailShrine(final boolean spawned, final int time) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("showHorntailShrine");
        }
        mplew.writeShort(SendPacketOpcode.HORNTAIL_SHRINE.getValue());
        mplew.write(spawned ? 1 : 0);
        mplew.writeInt(time);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket showChaosZakumShrine(final boolean spawned, final int time) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("showChaosZakumShrine");
        }
        mplew.writeShort(SendPacketOpcode.CHAOS_ZAKUM_SHRINE.getValue());
        mplew.write(spawned ? 1 : 0);
        mplew.writeInt(time);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket showChaosHorntailShrine(final boolean spawned, final int time) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("showChaosHorntailShrine");
        }
        mplew.writeShort(SendPacketOpcode.CHAOS_HORNTAIL_SHRINE.getValue());
        mplew.write(spawned ? 1 : 0);
        mplew.writeInt(time);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket stopClock() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("stopClock");
        }
        mplew.writeShort(SendPacketOpcode.STOP_CLOCK.getValue());
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket addTutorialStats() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(0);
        if (ServerProperties.LogPktCall) {
            log.info("addTutorialStats");
        }
        mplew.writeShort(SendPacketOpcode.TEMP_STATS.getValue());
        mplew.writeInt(3871);
        mplew.writeShort(999);
        mplew.writeShort(999);
        mplew.writeShort(999);
        mplew.writeShort(999);
        mplew.writeShort(255);
        mplew.writeShort(999);
        mplew.writeShort(999);
        mplew.write(120);
        mplew.write(140);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket temporaryStats_Aran() {
        if (ServerProperties.LogPktCall) {
            log.info("temporaryStats_Aran");
        }
        final List<Pair<MapleStat.Temp, Integer>> stats = new ArrayList<Pair<MapleStat.Temp, Integer>>();
        stats.add(new Pair<MapleStat.Temp, Integer>(MapleStat.Temp.STR, 999));
        stats.add(new Pair<MapleStat.Temp, Integer>(MapleStat.Temp.DEX, 999));
        stats.add(new Pair<MapleStat.Temp, Integer>(MapleStat.Temp.INT, 999));
        stats.add(new Pair<MapleStat.Temp, Integer>(MapleStat.Temp.LUK, 999));
        stats.add(new Pair<MapleStat.Temp, Integer>(MapleStat.Temp.WATK, 255));
        stats.add(new Pair<MapleStat.Temp, Integer>(MapleStat.Temp.ACC, 999));
        stats.add(new Pair<MapleStat.Temp, Integer>(MapleStat.Temp.AVOID, 999));
        stats.add(new Pair<MapleStat.Temp, Integer>(MapleStat.Temp.SPEED, 140));
        stats.add(new Pair<MapleStat.Temp, Integer>(MapleStat.Temp.JUMP, 120));
        return temporaryStats(stats);
    }

    public static MaplePacket temporaryStats_Balrog(final MapleCharacter chr) {
        if (ServerProperties.LogPktCall) {
            log.info("temporaryStats_Balrog");
        }
        final List<Pair<MapleStat.Temp, Integer>> stats = new ArrayList<Pair<MapleStat.Temp, Integer>>();
        final int offset = 1 + (chr.getLevel() - 90) / 20;
        stats.add(new Pair<MapleStat.Temp, Integer>(MapleStat.Temp.STR, chr.getStat().getTotalStr() / offset));
        stats.add(new Pair<MapleStat.Temp, Integer>(MapleStat.Temp.DEX, chr.getStat().getTotalDex() / offset));
        stats.add(new Pair<MapleStat.Temp, Integer>(MapleStat.Temp.INT, chr.getStat().getTotalInt() / offset));
        stats.add(new Pair<MapleStat.Temp, Integer>(MapleStat.Temp.LUK, chr.getStat().getTotalLuk() / offset));
        stats.add(new Pair<MapleStat.Temp, Integer>(MapleStat.Temp.WATK, chr.getStat().getTotalWatk() / offset));
        stats.add(new Pair<MapleStat.Temp, Integer>(MapleStat.Temp.MATK, chr.getStat().getTotalMagic() / offset));
        return temporaryStats(stats);
    }

    public static MaplePacket temporaryStats(final List<Pair<MapleStat.Temp, Integer>> stats) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("temporaryStats");
        }
        mplew.writeShort(SendPacketOpcode.TEMP_STATS.getValue());
        int updateMask = 0;
        for (final Pair<MapleStat.Temp, Integer> statupdate : stats) {
            updateMask |= statupdate.getLeft().getValue();
        }
        final List<Pair<MapleStat.Temp, Integer>> mystats = stats;
        if (mystats.size() > 1) {
            Collections.sort(mystats, new Comparator<Pair<MapleStat.Temp, Integer>>() {
                @Override
                public int compare(final Pair<MapleStat.Temp, Integer> o1, final Pair<MapleStat.Temp, Integer> o2) {
                    final int val1 = o1.getLeft().getValue();
                    final int val2 = o2.getLeft().getValue();
                    return (val1 < val2) ? -1 : ((val1 == val2) ? 0 : 1);
                }
            });
        }
        mplew.writeInt(updateMask);
        for (final Pair<MapleStat.Temp, Integer> statupdate2 : mystats) {
            final Integer value = statupdate2.getLeft().getValue();
            if (value >= 1) {
                if (value <= 512) {
                    mplew.writeShort(statupdate2.getRight().shortValue());
                } else {
                    mplew.write(statupdate2.getRight().byteValue());
                }
            }
        }
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket temporaryStats_Reset() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("temporaryStats_Reset");
        }
        mplew.writeShort(SendPacketOpcode.TEMP_STATS_RESET.getValue());
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket showHpHealed(final int cid, final int amount) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("showHpHealed");
        }
        mplew.writeShort(SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
        mplew.writeInt(cid);
        mplew.write(6);
        mplew.writeInt(amount);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket showOwnHpHealed(final int amount) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("showOwnHpHealed");
        }
        mplew.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
        mplew.write(6);
        mplew.writeInt(amount);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket sendPyramidUpdate(final int amount) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("sendPyramidUpdate");
        }
        mplew.writeShort(SendPacketOpcode.PYRAMID_UPDATE.getValue());
        mplew.writeInt(amount);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket sendPyramidResult(final byte rank, final int amount) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("sendPyramidResult");
        }
        mplew.writeShort(SendPacketOpcode.PYRAMID_RESULT.getValue());
        mplew.write(rank);
        mplew.writeInt(amount);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket sendMarrageEffect() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("sendMarrageEffect");
        }
        mplew.writeShort(71);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket sendPyramidEnergy(final String type, final String amount) {
        if (ServerProperties.LogPktCall) {
            log.info("sendPyramidEnergy");
        }
        return sendString(1, type, amount);
    }

    public static MaplePacket sendString(int type, String object, String amount) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) log.info("sendString");
        switch (type) {
            case 1:
                mplew.writeShort(SendPacketOpcode.ENERGY.getValue());
                break;
            case 2:
                mplew.writeShort(SendPacketOpcode.GHOST_POINT.getValue());
                break;
            case 3:
                mplew.writeShort(SendPacketOpcode.GHOST_STATUS.getValue());
                break;
        }
        mplew.writeMapleAsciiString(object);
        mplew.writeMapleAsciiString(amount);
        if (ServerProperties.LogClientErr) {
            ServerConstants ERROR = new ServerConstants();
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket sendGhostPoint(final String type, final String amount) {
        if (ServerProperties.LogPktCall) {
            log.info("sendGhostPoint");
        }
        return sendString(2, type, amount);
    }

    public static MaplePacket sendGhostStatus(final String type, final String amount) {
        if (ServerProperties.LogPktCall) {
            log.info("sendGhostStatus");
        }
        return sendString(3, type, amount);
    }

    public static MaplePacket MulungEnergy(final int energy) {
        if (ServerProperties.LogPktCall) {
            log.info("MulungEnergy");
        }
        return sendPyramidEnergy("energy", String.valueOf(energy));
    }

    public static MaplePacket getEvanTutorial(final String data) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("getEvanTutorial");
        }
        mplew.writeShort(SendPacketOpcode.NPC_TALK.getValue());
        mplew.writeInt(8);
        mplew.write(0);
        mplew.write(1);
        mplew.write(1);
        mplew.write(1);
        mplew.writeMapleAsciiString(data);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket showEventInstructions() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("showEventInstructions");
        }
        mplew.writeShort(SendPacketOpcode.GMEVENT_INSTRUCTIONS.getValue());
        mplew.write(0);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket getOwlOpen() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("getOwlOpen");
        }
        mplew.writeShort(SendPacketOpcode.OWL_OF_MINERVA.getValue());
        mplew.write(7);
        mplew.write(GameConstants.owlItems.length);
        for (final int i : GameConstants.owlItems) {
            mplew.writeInt(i);
        }
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket getOwlSearched(final int itemSearch, final List<HiredMerchant> hms) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("getOwlSearched");
        }
        mplew.writeShort(SendPacketOpcode.OWL_OF_MINERVA.getValue());
        mplew.write(6);
        mplew.writeInt(0);
        mplew.writeInt(itemSearch);
        int size = 0;
        for (final HiredMerchant hm : hms) {
            size += hm.searchItem(itemSearch).size();
        }
        mplew.writeInt(size);
        for (final HiredMerchant hm : hms) {
            final List<MaplePlayerShopItem> items = hm.searchItem(itemSearch);
            for (final MaplePlayerShopItem item : items) {
                mplew.writeMapleAsciiString(hm.getOwnerName());
                mplew.writeInt(hm.getMap().getId());
                mplew.writeMapleAsciiString(hm.getDescription());
                mplew.writeInt(item.item.getQuantity());
                mplew.writeInt(item.bundles);
                mplew.writeInt(item.price);
                switch (InventoryHandler.OWL_ID) {
                    case 0: {
                        mplew.writeInt(hm.getOwnerId());
                        break;
                    }
                    case 1: {
                        mplew.writeInt(hm.getStoreId());
                        break;
                    }
                    default: {
                        mplew.writeInt(hm.getObjectId());
                        break;
                    }
                }
                mplew.write((hm.getFreeSlot() == -1) ? 1 : 0);
                mplew.write(GameConstants.getInventoryType(itemSearch).getType());
                if (GameConstants.getInventoryType(itemSearch) == MapleInventoryType.EQUIP) {
                    PacketHelper.addItemInfo(mplew, item.item, true, true);
                }
            }
        }
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket getRPSMode(final byte mode, final int mesos, final int selection, final int answer) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("getRPSMode");
        }
        mplew.writeShort(SendPacketOpcode.RPS_GAME.getValue());
        mplew.write(mode);
        switch (mode) {
            case 6: {
                if (mesos != -1) {
                    mplew.writeInt(mesos);
                    break;
                }
                break;
            }
            case 8: {
                mplew.writeInt(9000019);
                break;
            }
            case 11: {
                mplew.write(selection);
                mplew.write(answer);
                break;
            }
        }
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket getSlotUpdate(final byte invType, final byte newSlots) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("getSlotUpdate");
        }
        mplew.writeShort(SendPacketOpcode.UPDATE_INVENTORY_SLOT.getValue());
        mplew.write(invType);
        mplew.write(newSlots);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket getMovingPlatforms(final MapleMap map) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("getMovingPlatforms");
        }
        mplew.writeShort(SendPacketOpcode.MOVE_PLATFORM.getValue());
        mplew.writeInt(map.getPlatforms().size());
        for (final MapleNodes.MaplePlatform mp : map.getPlatforms()) {
            mplew.writeMapleAsciiString(mp.name);
            mplew.writeInt(mp.start);
            mplew.writeInt(mp.SN.size());
            for (int x = 0; x < mp.SN.size(); ++x) {
                mplew.writeInt(mp.SN.get(x));
            }
            mplew.writeInt(mp.speed);
            mplew.writeInt(mp.x1);
            mplew.writeInt(mp.x2);
            mplew.writeInt(mp.y1);
            mplew.writeInt(mp.y2);
            mplew.writeInt(mp.x1);
            mplew.writeInt(mp.y1);
            mplew.writeShort(mp.r);
        }
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket getUpdateEnvironment(final MapleMap map) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("getUpdateEnvironment");
        }
        mplew.writeShort(SendPacketOpcode.UPDATE_ENV.getValue());
        mplew.writeInt(map.getEnvironment().size());
        for (final Map.Entry<String, Integer> mp : map.getEnvironment().entrySet()) {
            mplew.writeMapleAsciiString(mp.getKey());
            mplew.writeInt(mp.getValue());
        }
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket sendEngagementRequest(final String name, final int cid) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("sendEngagementRequest");
        }
        mplew.writeShort(SendPacketOpcode.ENGAGE_REQUEST.getValue());
        mplew.write(0);
        mplew.writeMapleAsciiString(name);
        mplew.writeInt(cid);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket trembleEffect(final int type, final int delay) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("trembleEffect");
        }
        mplew.writeShort(SendPacketOpcode.BOSS_ENV.getValue());
        mplew.write(1);
        mplew.write(type);
        mplew.writeInt(delay);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket sendEngagement(final byte msg, final int item, final MapleCharacter male, final MapleCharacter female) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("sendEngagement");
        }
        mplew.writeShort(SendPacketOpcode.ENGAGE_RESULT.getValue());
        mplew.write(msg);
        switch (msg) {
            case 11: {
                mplew.writeInt(0);
                mplew.writeInt(male.getId());
                mplew.writeInt(female.getId());
                mplew.writeShort(1);
                mplew.writeInt(item);
                mplew.writeInt(item);
                mplew.writeAsciiString(male.getName(), 15);
                mplew.writeAsciiString(female.getName(), 15);
                break;
            }
        }
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket englishQuizMsg(final String msg) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("englishQuizMsg");
        }
        mplew.writeShort(SendPacketOpcode.ENGLISH_QUIZ.getValue());
        mplew.writeInt(20);
        mplew.writeMapleAsciiString(msg);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket openBeans(final int beansCount, final int type) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("openBeans");
        }
        mplew.writeShort(SendPacketOpcode.BEANS_GAME1.getValue());
        mplew.writeInt(beansCount);
        mplew.write(type);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket updateBeans(final int cid, final int beansCount) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("updateBeans");
        }
        mplew.writeShort(SendPacketOpcode.UPDATE_BEANS.getValue());
        mplew.writeInt(cid);
        mplew.writeInt(beansCount);
        mplew.writeInt(0);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket showBeans(final int 力度, final int size, final int Pos, final int Type) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("showBeans");
        }
        mplew.writeShort(SendPacketOpcode.BEANS_GAME2.getValue());
        mplew.writeShort(力度);
        mplew.write(size);
        mplew.writeShort(Pos);
        mplew.writeInt(Type);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket showCharCash(final MapleCharacter chr) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("showCharCash");
        }
        mplew.writeShort(SendPacketOpcode.CHAR_CASH.getValue());
        mplew.writeInt(chr.getId());
        mplew.writeInt(chr.getCSPoints(2));
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket spawnLove(final int oid, final int itemid, final String name, final String msg, final Point pos, final int ft) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("spawnLove");
        }
        mplew.writeShort(SendPacketOpcode.SPAWN_LOVE.getValue());
        mplew.writeInt(oid);
        mplew.writeInt(itemid);
        mplew.writeMapleAsciiString(msg);
        mplew.writeMapleAsciiString(name);
        mplew.writeShort(pos.x);
        mplew.writeShort(ft);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket removeLove(final int oid) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("removeLove");
        }
        mplew.writeShort(SendPacketOpcode.REMOVE_LOVE.getValue());
        mplew.writeInt(oid);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket licenseRequest() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("licenseRequest");
        }
        mplew.writeShort(SendPacketOpcode.LOGIN_STATUS.getValue());
        mplew.write(22);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket licenseResult() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("licenseResult");
        }
        mplew.writeShort(SendPacketOpcode.LICENSE_RESULT.getValue());
        mplew.write(1);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket showForcedEquip() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("showForcedEquip");
        }
        mplew.writeShort(SendPacketOpcode.FORCED_MAP_EQUIP.getValue());
        mplew.writeInt(0);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket removeTutorialStats() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("removeTutorialStats");
        }
        mplew.writeShort(SendPacketOpcode.TEMP_STATS_RESET.getValue());
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket spawnTutorialSummon(final int type) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("spawnTutorialSummon");
        }
        mplew.writeShort(SendPacketOpcode.TUTORIAL_SUMMON.getValue());
        mplew.write(type);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket requestBuddylistAdd(final int cidFrom, final String nameFrom) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.BUDDYLIST.getValue());
        mplew.write(9);
        mplew.writeInt(cidFrom);
        mplew.writeMapleAsciiString(nameFrom);
        mplew.writeInt(cidFrom);
        mplew.writeAsciiString(nameFrom, 13);
        mplew.write(1);
        mplew.write(5);
        mplew.write(0);
        mplew.writeShort(0);
        mplew.writeAsciiString("群未定", 17);
        mplew.write(0);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket sendAutoHpPot(final int itemId) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.AUTO_HP_POT.getValue());
        mplew.writeInt(itemId);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket sendAutoMpPot(final int itemId) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.AUTO_MP_POT.getValue());
        mplew.writeInt(itemId);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket testPacket(final byte[] testmsg) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.write(testmsg);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket updateAriantScore(final List<MapleCharacter> players) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.ARIANT_SCORE_UPDATE.getValue());
        mplew.write(players.isEmpty() ? 0 : 1);
        if (!players.isEmpty()) {
            for (final MapleCharacter i : players) {
                mplew.writeMapleAsciiString(i.getName());
                mplew.writeInt(i.getAriantScore());
            }
        }
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket updateAriantScore(final String name, final int score, final boolean empty) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.ARIANT_SCORE_UPDATE.getValue());
        mplew.write(empty ? 0 : 1);
        if (!empty) {
            mplew.writeMapleAsciiString(name);
            mplew.writeInt(score);
        }
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket modifyInventory(final boolean updateTick, final ModifyInventory mod) {
        return modifyInventory(updateTick, Collections.singletonList(mod));
    }

    public static MaplePacket modifyInventory(final boolean updateTick, final List<ModifyInventory> mods) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
        mplew.write(updateTick ? 1 : 0);
        mplew.write(mods.size());
        int addMovement = -1;
        for (final ModifyInventory mod : mods) {
            mplew.write(mod.getMode());
            mplew.write(mod.getInventoryType());
            mplew.writeShort((mod.getMode() == 2) ? mod.getOldPosition() : mod.getPosition());
            switch (mod.getMode()) {
                case 0: {
                    PacketHelper.addItemInfo(mplew, mod.getItem(), true, false);
                    break;
                }
                case 1: {
                    mplew.writeShort(mod.getQuantity());
                    break;
                }
                case 2: {
                    mplew.writeShort(mod.getPosition());
                    if (mod.getPosition() < 0 || mod.getOldPosition() < 0) {
                        addMovement = ((mod.getOldPosition() < 0) ? 1 : 2);
                        break;
                    }
                    break;
                }
                case 3: {
                    if (mod.getPosition() < 0) {
                        addMovement = 2;
                        break;
                    }
                    break;
                }
            }
            mod.clear();
        }
        if (addMovement > -1) {
            mplew.write(addMovement);
        }
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket petAutoHP(final int itemId) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.AUTO_HP_POT.getValue());
        mplew.writeInt(itemId);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket petAutoMP(final int itemId) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.AUTO_MP_POT.getValue());
        mplew.writeInt(itemId);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket catchMob(final int mobid, final int itemid, final byte success) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(80);
        mplew.write(success);
        mplew.writeInt(itemid);
        mplew.writeInt(mobid);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket serverMessagePopUp(final String message) {
        return serverMessage(1, 0, message, false);
    }

    public static MaplePacket updateEquipSlot(final IItem item) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("updateEquipSlot");
        }
        mplew.writeShort(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
        mplew.write(0);
        mplew.write(HexTool.getByteArrayFromHexString("02 03 01"));
        mplew.writeShort(item.getPosition());
        mplew.write(0);
        mplew.write(item.getType());
        mplew.writeShort(item.getPosition());
        PacketHelper.addItemInfo(mplew, item, true, true);
        mplew.writeMapleAsciiString("wat");
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket cancelBuffMONSTERS(final List<MapleBuffStat> statups) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("cancelBuffMONSTERS");
        }
        mplew.writeShort(SendPacketOpcode.CANCEL_BUFF.getValue());
        mplew.write(HexTool.getByteArrayFromHexString("00 00 00 00 00 00 01 00"));
        mplew.write(HexTool.getByteArrayFromHexString("00 00 00 00 00 00 00 00"));
        mplew.write(3);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket cancelForeignBuffMONSTERS(final int cid, final List<MapleBuffStat> statups) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("cancelForeignBuffA");
        }
        mplew.writeShort(SendPacketOpcode.CANCEL_FOREIGN_BUFF.getValue());
        mplew.writeInt(cid);
        mplew.write(HexTool.getByteArrayFromHexString("00 00 00 00 00 00 01 00"));
        mplew.write(HexTool.getByteArrayFromHexString("00 00 00 00 00 00 00 00"));
        mplew.write(3);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket displayGuide(final int guide) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.SUMMON_HINT_MSG.getValue());
        mplew.write(1);
        mplew.writeInt(guide);
        mplew.writeInt(12000);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static String returnSerialNumber() {
        final String cpu = getCPUSerial();
        final String disk = getHardDiskSerialNumber("C");
        final int newdisk = Integer.parseInt(disk);
        final String s = cpu + newdisk;
        final String newStr = s.substring(8);
        return newStr;
    }

    public static String getCPUSerial() {
        String result = "";
        try {
            final File file = File.createTempFile("tmp", ".vbs");
            file.deleteOnExit();
            final FileWriter fw = new FileWriter(file);
            final String vbs = "Set objWMIService = GetObject(\"winmgmts://./root/cimv2\")\nSet colItems = objWMIService.ExecQuery _ \n   (\"Select * from Win32_Processor\") \nFor Each objItem in colItems \n    Wscript.Echo objItem.ProcessorId \n    exit for  ' do the first cpu only! \nNext \n";
            fw.write(vbs);
            fw.close();
            final Process p = Runtime.getRuntime().exec("cscript //NoLogo " + file.getPath());
            final BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = input.readLine()) != null) {
                result += line;
            }
            input.close();
            file.delete();
        } catch (IOException e) {
            e.fillInStackTrace();
        }
        if (result.trim().length() < 1 || result == null) {
            result = "无CPU_ID被读取";
        }
        return result.trim();
    }

    public static String getHardDiskSerialNumber(final String drive) {
        String result = "";
        try {
            final File file = File.createTempFile("realhowto", ".vbs");
            file.deleteOnExit();
            final FileWriter fw = new FileWriter(file);
            final String vbs = "Set objFSO = CreateObject(\"Scripting.FileSystemObject\")\nSet colDrives = objFSO.Drives\nSet objDrive = colDrives.item(\"" + drive + "\")\nWscript.Echo objDrive.SerialNumber";
            fw.write(vbs);
            fw.close();
            final Process p = Runtime.getRuntime().exec("cscript //NoLogo " + file.getPath());
            final BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = input.readLine()) != null) {
                result += line;
            }
            input.close();
        } catch (IOException ex) {
        }
        return result.trim();
    }

    public static boolean isshowPacket() {
        return false;
    }

    public static MaplePacket openWeb(final String web) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("openWeb");
        }
        mplew.writeShort(SendPacketOpcode.OPEN_WEB.getValue());
        mplew.writeMapleAsciiString(web);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket LieDetectorResponse(final byte msg) {
        return LieDetectorResponse(msg, (byte) 0);
    }

    public static MaplePacket LieDetectorResponse(final byte msg, final byte msg2) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.LIE_DETECTOR.getValue());
        mplew.write(msg);
        mplew.write(msg2);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket sendLieDetector(final byte[] image, final int attempt) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.LIE_DETECTOR.getValue());
        mplew.write(9);
        mplew.write(1);
        mplew.write(1);
        mplew.write(attempt - 1);
        if (image == null) {
            mplew.writeInt(0);
            return mplew.getPacket();
        }
        mplew.writeInt(image.length);
        mplew.write(image);
        log.info("调用: " + new Throwable().getStackTrace()[0] + " 测谎仪图片大小: " + image.length + " 换图次数: " + (attempt - 1));
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket shenlong(final int i) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(i);
        mplew.write(HexTool.getByteArrayFromHexString("DC 05 00 00 90 5F 01 00 DC 05 00 00 9B 00 00 00"));
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket shenlong2(final int i) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(i);
        mplew.write(HexTool.getByteArrayFromHexString("02 CB 06 00 00 FB 44 00 00"));
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket DragonBall1(final int i, final boolean Zhaohuan) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeInt(0);
        mplew.write(1);
        if (!Zhaohuan) {
            mplew.writeShort(0);
            mplew.writeShort(i);
            mplew.writeShort(0);
        } else {
            mplew.writeLong(512L);
        }
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket 杀怪排行榜(final int npcid, final ResultSet rs) throws SQLException {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(73);
        mplew.writeInt(npcid);
        if (!rs.last()) {
            mplew.writeInt(0);
            return mplew.getPacket();
        }
        mplew.writeInt(rs.getRow());
        rs.beforeFirst();
        while (rs.next()) {
            mplew.writeMapleAsciiString(rs.getString("name"));
            mplew.writeInt(rs.getInt("shaguai"));
            mplew.writeInt(rs.getInt("str"));
            mplew.writeInt(rs.getInt("dex"));
            mplew.writeInt(rs.getInt("int"));
            mplew.writeInt(rs.getInt("luk"));
        }
        return mplew.getPacket();
    }

    public static MaplePacket getCY1(final int npc, final String talk, final byte type) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.NPC_TALK.getValue());
        mplew.write(4);
        mplew.writeInt(npc);
        mplew.write(13);
        mplew.write(0);
        mplew.writeInt(0);
        mplew.write(type);
        mplew.writeShort(0);
        mplew.write(0);
        mplew.writeMapleAsciiString(talk);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket getCY2(final int npc, final String talk, final byte type) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.NPC_TALK.getValue());
        mplew.write(4);
        mplew.writeInt(npc);
        mplew.write(16);
        mplew.write(0);
        mplew.writeInt(0);
        mplew.write(type);
        mplew.writeShort(0);
        mplew.write(0);
        mplew.writeMapleAsciiString(talk);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("MaplePacketCreator 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket PVPdamagePlayer(final int chrId, final int type, final int monsteridfrom, final int damage) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.DAMAGE_PLAYER.getValue());
        mplew.writeInt(chrId);
        mplew.write(type);
        mplew.writeInt(damage);
        mplew.writeInt(monsteridfrom);
        mplew.write(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(damage);
        return mplew.getPacket();
    }

    public static MaplePacket testCombo(final int value) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("testCombo");
        }
        mplew.writeShort(SendPacketOpcode.ARAN_COMBO.getValue());
        mplew.writeInt(value);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("testCombo-864：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket getInventoryStatus() {
        return modifyInventory(false, Collections.EMPTY_LIST);
    }

    static {
        MaplePacketCreator.EMPTY_STATUPDATE = Collections.emptyList();
        CHAR_INFO_MAGIC = new byte[]{-1, -55, -102, 59};
    }
}
