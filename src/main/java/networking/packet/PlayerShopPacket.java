package networking.packet;

import client.MapleCharacter;
import client.MapleClient;
import client.inventory.IItem;
import configuration.ServerProperties;
import constants.ServerConstants;
import handling.MaplePacket;
import handling.SendPacketOpcode;
import lombok.extern.slf4j.Slf4j;
import server.MerchItemPackage;
import server.shops.*;
import utils.datastructures.Pair;
import networking.output.MaplePacketLittleEndianWriter;

import java.util.List;

@Slf4j
public class PlayerShopPacket {
    public static MaplePacket addCharBox(final MapleCharacter c, final int type) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("addCharBox");
        }
        mplew.writeShort(SendPacketOpcode.UPDATE_CHAR_BOX.getValue());
        mplew.writeInt(c.getId());
        PacketHelper.addAnnounceBox(mplew, c);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("PlayerShopPacket 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket removeCharBox(final MapleCharacter c) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("removeCharBox");
        }
        mplew.writeShort(SendPacketOpcode.UPDATE_CHAR_BOX.getValue());
        mplew.writeInt(c.getId());
        mplew.write(0);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("PlayerShopPacket 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket sendTitleBox() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("sendTitleBox");
        }
        mplew.writeShort(SendPacketOpcode.SEND_TITLE_BOX.getValue());
        mplew.write(7);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("PlayerShopPacket 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket sendPlayerShopBox(final MapleCharacter c) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("sendPlayerShopBox");
        }
        mplew.writeShort(SendPacketOpcode.UPDATE_CHAR_BOX.getValue());
        mplew.writeInt(c.getId());
        PacketHelper.addAnnounceBox(mplew, c);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("PlayerShopPacket 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket getHiredMerch(final MapleCharacter chr, final HiredMerchant merch, final boolean firstTime) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("getHiredMerch");
        }
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(5);
        mplew.write(5);
        mplew.write(4);
        mplew.writeShort(merch.getVisitorSlot(chr));
        mplew.writeInt(merch.getItemId());
        mplew.writeMapleAsciiString("雇佣商人");
        for (final Pair<Byte, MapleCharacter> storechr : merch.getVisitors()) {
            mplew.write(storechr.left);
            PacketHelper.addCharLook(mplew, storechr.right, false);
            mplew.writeMapleAsciiString(storechr.right.getName());
        }
        mplew.write(-1);
        mplew.writeShort(0);
        mplew.writeMapleAsciiString(merch.getOwnerName());
        if (merch.isOwner(chr)) {
            mplew.writeInt(merch.getTimeLeft());
            mplew.write(firstTime ? 1 : 0);
            mplew.write(merch.getBoughtItems().size());
            for (final AbstractPlayerStore.BoughtItem SoldItem : merch.getBoughtItems()) {
                mplew.writeInt(SoldItem.id);
                mplew.writeShort(SoldItem.quantity);
                mplew.writeInt(SoldItem.totalPrice);
                mplew.writeMapleAsciiString(SoldItem.buyer);
            }
            mplew.writeInt(merch.getMeso());
        }
        mplew.writeMapleAsciiString(merch.getDescription());
        mplew.write(10);
        mplew.writeInt(merch.getMeso());
        mplew.write(merch.getItems().size());
        for (final MaplePlayerShopItem item : merch.getItems()) {
            mplew.writeShort(item.bundles);
            mplew.writeShort(item.item.getQuantity());
            mplew.writeInt(item.price);
            PacketHelper.addItemInfo(mplew, item.item, true, true);
        }
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("PlayerShopPacket 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket getPlayerStore(final MapleCharacter chr, final boolean firstTime) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("getPlayerStore");
        }
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        final IMaplePlayerShop ips = chr.getPlayerShop();
        switch (ips.getShopType()) {
            case 2: {
                mplew.write(5);
                mplew.write(4);
                mplew.write(4);
                break;
            }
            case 3: {
                mplew.write(5);
                mplew.write(2);
                mplew.write(2);
                break;
            }
            case 4: {
                mplew.write(5);
                mplew.write(1);
                mplew.write(2);
                break;
            }
        }
        mplew.writeShort(ips.getVisitorSlot(chr));
        PacketHelper.addCharLook(mplew, ((MaplePlayerShop) ips).getMCOwner(), false);
        mplew.writeMapleAsciiString(ips.getOwnerName());
        for (final Pair<Byte, MapleCharacter> storechr : ips.getVisitors()) {
            mplew.write(storechr.left);
            PacketHelper.addCharLook(mplew, storechr.right, false);
            mplew.writeMapleAsciiString(storechr.right.getName());
        }
        mplew.write(255);
        mplew.writeMapleAsciiString(ips.getDescription());
        mplew.write(10);
        mplew.write(ips.getItems().size());
        for (final MaplePlayerShopItem item : ips.getItems()) {
            mplew.writeShort(item.bundles);
            mplew.writeShort(item.item.getQuantity());
            mplew.writeInt(item.price);
            PacketHelper.addItemInfo(mplew, item.item, true, true);
        }
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("PlayerShopPacket 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket shopChat(final String message, final int slot) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("shopChat");
        }
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(6);
        mplew.write(8);
        mplew.write(slot);
        mplew.writeMapleAsciiString(message);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("PlayerShopPacket 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket shopErrorMessage(final int error, final int type) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("shopErrorMessage");
        }
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(10);
        mplew.write(type);
        mplew.write(error);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("PlayerShopPacket 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket spawnHiredMerchant(final HiredMerchant hm) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("spawnHiredMerchant");
        }
        mplew.writeShort(SendPacketOpcode.SPAWN_HIRED_MERCHANT.getValue());
        mplew.writeInt(hm.getOwnerId());
        mplew.writeInt(hm.getItemId());
        mplew.writePos(hm.getPosition());
        mplew.writeShort(0);
        mplew.writeMapleAsciiString(hm.getOwnerName());
        PacketHelper.addInteraction(mplew, hm);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("PlayerShopPacket 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket destroyHiredMerchant(final int id) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("destroyHiredMerchant");
        }
        mplew.writeShort(SendPacketOpcode.DESTROY_HIRED_MERCHANT.getValue());
        mplew.writeInt(id);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("PlayerShopPacket 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket shopItemUpdate(final IMaplePlayerShop shop) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("shopItemUpdate");
        }
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(23);
        if (shop.getShopType() == 1) {
            mplew.writeInt(0);
        }
        mplew.write(shop.getItems().size());
        for (final MaplePlayerShopItem item : shop.getItems()) {
            mplew.writeShort(item.bundles);
            mplew.writeShort(item.item.getQuantity());
            mplew.writeInt(item.price);
            PacketHelper.addItemInfo(mplew, item.item, true, true);
        }
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("PlayerShopPacket 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket shopVisitorAdd(final MapleCharacter chr, final int slot) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("shopVisitorAdd");
        }
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(4);
        mplew.write(slot);
        PacketHelper.addCharLook(mplew, chr, false);
        mplew.writeMapleAsciiString(chr.getName());
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("PlayerShopPacket 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket shopVisitorLeave(final byte slot) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("shopVisitorLeave");
        }
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(10);
        if (slot > 0) {
            mplew.write(slot);
            mplew.write(slot);
            mplew.write(slot);
        }
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("PlayerShopPacket 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket Merchant_Buy_Error(final byte message) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("Merchant_Buy_Error");
        }
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(23);
        mplew.write(message);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("PlayerShopPacket 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket updateHiredMerchant(final HiredMerchant shop) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("updateHiredMerchant");
        }
        mplew.writeShort(SendPacketOpcode.UPDATE_HIRED_MERCHANT.getValue());
        mplew.writeInt(shop.getOwnerId());
        PacketHelper.addInteraction(mplew, shop);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("PlayerShopPacket 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket merchItem_Message(final byte op) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("merchItem_Message");
        }
        mplew.writeShort(SendPacketOpcode.MERCH_ITEM_MSG.getValue());
        mplew.write(op);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("PlayerShopPacket 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket merchItemStore(final byte op) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("merchItemStore");
        }
        mplew.writeShort(SendPacketOpcode.MERCH_ITEM_STORE.getValue());
        mplew.write(op);
        switch (op) {
            case 36: {
                mplew.writeZeroBytes(8);
                break;
            }
            default: {
                mplew.write(0);
                break;
            }
        }
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("PlayerShopPacket 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket merchItemStore_ItemData(final MerchItemPackage pack) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("merchItemStore_ItemData");
        }
        mplew.writeShort(SendPacketOpcode.MERCH_ITEM_STORE.getValue());
        mplew.write(35);
        mplew.writeInt(9030000);
        mplew.writeInt(32272);
        mplew.writeZeroBytes(5);
        mplew.writeInt(pack.getMesos());
        mplew.write(0);
        mplew.write(pack.getItems().size());
        for (final IItem item : pack.getItems()) {
            PacketHelper.addItemInfo(mplew, item, true, true);
        }
        mplew.writeZeroBytes(3);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("PlayerShopPacket 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket getMiniGame(final MapleClient c, final MapleMiniGame minigame) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("getMiniGame");
        }
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(5);
        mplew.write(minigame.getGameType());
        mplew.write(minigame.getMaxSize());
        mplew.writeShort(minigame.getVisitorSlot(c.getPlayer()));
        PacketHelper.addCharLook(mplew, minigame.getMCOwner(), false);
        mplew.writeMapleAsciiString(minigame.getOwnerName());
        for (final Pair<Byte, MapleCharacter> visitorz : minigame.getVisitors()) {
            mplew.write(visitorz.getLeft());
            PacketHelper.addCharLook(mplew, visitorz.getRight(), false);
            mplew.writeMapleAsciiString(visitorz.getRight().getName());
        }
        mplew.write(-1);
        mplew.write(0);
        addGameInfo(mplew, minigame.getMCOwner(), minigame);
        for (final Pair<Byte, MapleCharacter> visitorz : minigame.getVisitors()) {
            mplew.write(visitorz.getLeft());
            addGameInfo(mplew, visitorz.getRight(), minigame);
        }
        mplew.write(-1);
        mplew.writeMapleAsciiString(minigame.getDescription());
        mplew.writeShort(minigame.getPieceType());
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("PlayerShopPacket 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket getMiniGameReady(final boolean ready) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("getMiniGameReady");
        }
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(ready ? 57 : 58);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("PlayerShopPacket 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket getMiniGameExitAfter(final boolean ready) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("getMiniGameExitAfter");
        }
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(ready ? 55 : 56);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("PlayerShopPacket 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket getMiniGameStart(final int loser) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("getMiniGameStart");
        }
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(60);
        mplew.write((loser != 1) ? 1 : 0);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("PlayerShopPacket 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket getMiniGameSkip(final int slot) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("getMiniGameSkip");
        }
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(62);
        mplew.write(slot);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("PlayerShopPacket 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket getMiniGameSkip1(final int slot) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("getMiniGameSkip1");
        }
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(62);
        mplew.write((slot != 1) ? 1 : 0);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("PlayerShopPacket 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket getMiniGameRequestTie() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("getMiniGameRequestTie");
        }
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(48);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("PlayerShopPacket 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket getMiniGameRequestREDO() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("getMiniGameRequestREDO");
        }
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(53);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("PlayerShopPacket 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket getMiniGameDenyTie() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("getMiniGameDenyTie");
        }
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(49);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("PlayerShopPacket 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket getMiniGameDenyREDO() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("getMiniGameDenyREDO");
        }
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(48);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("PlayerShopPacket 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket getMiniGameFull() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("getMiniGameFull");
        }
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.writeShort(5);
        mplew.write(2);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("PlayerShopPacket 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket getMiniGameMoveOmok(final int move1, final int move2, final int move3) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("getMiniGameMoveOmok");
        }
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(63);
        mplew.writeInt(move1);
        mplew.writeInt(move2);
        mplew.write(move3);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("PlayerShopPacket 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket getMiniGameNewVisitor(final MapleCharacter c, final int slot, final MapleMiniGame game) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("getMiniGameNewVisitor");
        }
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(4);
        mplew.write(slot);
        PacketHelper.addCharLook(mplew, c, false);
        mplew.writeMapleAsciiString(c.getName());
        addGameInfo(mplew, c, game);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("PlayerShopPacket 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static void addGameInfo(final MaplePacketLittleEndianWriter mplew, final MapleCharacter chr, final MapleMiniGame game) {
        mplew.writeInt(game.getGameType());
        mplew.writeInt(game.getWins(chr));
        mplew.writeInt(game.getTies(chr));
        mplew.writeInt(game.getLosses(chr));
        mplew.writeInt(game.getScore(chr));
    }

    public static MaplePacket getMiniGameClose(final byte number) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("getMiniGameClose");
        }
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(10);
        mplew.write(1);
        mplew.write(number);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("PlayerShopPacket 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket getMatchCardStart(final MapleMiniGame game, final int loser) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("getMatchCardStart");
        }
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(60);
        mplew.write((loser != 1) ? 1 : 0);
        final int times = (game.getPieceType() == 1) ? 20 : ((game.getPieceType() == 2) ? 30 : 12);
        mplew.write(times);
        for (int i = 1; i <= times; ++i) {
            mplew.writeInt(game.getCardId(i));
        }
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("PlayerShopPacket 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket getMatchCardSelect(final int turn, final int slot, final int firstslot, final int type) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("getMatchCardSelect");
        }
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(67);
        mplew.write(turn);
        mplew.write(slot);
        if (turn == 0) {
            mplew.write(firstslot);
            mplew.write(type);
        }
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("PlayerShopPacket 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket getMiniGameResult(final MapleMiniGame game, final int type, final int x) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("getMiniGameResult");
        }
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(61);
        mplew.write(type);
        game.setPoints(x, type);
        if (type != 0) {
            game.setPoints((x != 1) ? 1 : 0, (type != 2) ? 1 : 0);
        }
        if (type != 1) {
            if (type == 0) {
                mplew.write((x != 1) ? 1 : 0);
            } else {
                mplew.write(x);
            }
        }
        addGameInfo(mplew, game.getMCOwner(), game);
        for (final Pair<Byte, MapleCharacter> visitorz : game.getVisitors()) {
            addGameInfo(mplew, visitorz.right, game);
        }
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("PlayerShopPacket 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket MerchantVisitorView(final List<String> visitor) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("MerchantVisitorView");
        }
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(44);
        mplew.writeShort(visitor.size());
        for (final String visit : visitor) {
            mplew.writeMapleAsciiString(visit);
            mplew.writeInt(1);
        }
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("PlayerShopPacket 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket MerchantBlackListView(final List<String> blackList) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("MerchantBlackListView");
        }
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(45);
        mplew.writeShort(blackList.size());
        for (int i = 0; i < blackList.size(); ++i) {
            if (blackList.get(i) != null) {
                mplew.writeMapleAsciiString(blackList.get(i));
            }
        }
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("PlayerShopPacket 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket sendHiredMerchantMessage(final byte type) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("sendHiredMerchantMessage");
        }
        mplew.writeShort(SendPacketOpcode.MERCH_ITEM_MSG.getValue());
        mplew.write(type);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("PlayerShopPacket 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket shopMessage(final int type) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("shopMessage");
        }
        mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(type);
        mplew.write(0);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("PlayerShopPacket 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }
}
