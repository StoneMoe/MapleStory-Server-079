package networking.packet;

import client.MapleCharacter;
import client.MapleStat;
import client.inventory.IItem;
import client.inventory.MaplePet;
import configuration.ServerProperties;
import constants.ServerConstants;
import handling.MaplePacket;
import handling.SendPacketOpcode;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import server.movement.LifeMovementFragment;
import networking.output.MaplePacketLittleEndianWriter;

@Slf4j
public class PetPacket {
    private static final byte[] ITEM_MAGIC;

    public static MaplePacket updatePet(final MaplePet pet, final IItem item, final boolean active) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("updatePet");
        }
        mplew.writeShort(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
        mplew.write(pet.getInventoryPosition());
        mplew.write(2);
        mplew.write(3);
        mplew.write(5);
        mplew.writeShort(pet.getInventoryPosition());
        mplew.write(0);
        mplew.write(5);
        mplew.writeShort(pet.getInventoryPosition());
        mplew.write(3);
        mplew.writeInt(pet.getPetItemId());
        mplew.write(1);
        mplew.writeLong(pet.getUniqueId());
        PacketHelper.addPetItemInfo(mplew, item, pet, active);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("PetPacket 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket removePet(final MapleCharacter chr, final int slot) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("removePet");
        }
        mplew.writeShort(SendPacketOpcode.SPAWN_PET.getValue());
        mplew.writeInt(chr.getId());
        mplew.writeShort(slot);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("PetPacket 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket showPet(final MapleCharacter chr, final MaplePet pet, final boolean remove, final boolean hunger) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("showPet");
        }
        mplew.writeShort(SendPacketOpcode.SPAWN_PET.getValue());
        mplew.writeInt(chr.getId());
        mplew.write(chr.getPetIndex(pet));
        if (remove) {
            mplew.write(0);
            mplew.write(hunger ? 1 : 0);
        } else {
            mplew.write(1);
            mplew.write(0);
            mplew.writeInt(pet.getPetItemId());
            mplew.writeMapleAsciiString(pet.getName());
            mplew.writeLong(pet.getUniqueId());
            mplew.writeShort(pet.getPos().x);
            mplew.writeShort(pet.getPos().y);
            mplew.write(pet.getStance());
            mplew.writeLong(pet.getFh());
        }
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("PetPacket 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static void addPetInfo(final MaplePacketLittleEndianWriter mplew, final MapleCharacter chr, final MaplePet pet, final boolean showpet) {
        if (showpet) {
            mplew.write(1);
            mplew.write(chr.getPetIndex(pet));
        }
        mplew.writeInt(pet.getPetItemId());
        mplew.writeMapleAsciiString(pet.getName());
        mplew.writeLong(pet.getUniqueId());
        mplew.writeShort(pet.getPos().x);
        mplew.writeShort(pet.getPos().y);
        mplew.write(pet.getStance());
        mplew.writeLong(pet.getFh());
    }

    public static MaplePacket removePet(final int cid, final int index) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("removePet");
        }
        mplew.writeShort(SendPacketOpcode.SPAWN_PET.getValue());
        mplew.writeInt(cid);
        mplew.write(index);
        mplew.writeShort(0);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("PetPacket 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket movePet(final int cid, final int pid, final byte slot, final List<LifeMovementFragment> moves) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("movePet");
        }
        mplew.writeShort(SendPacketOpcode.MOVE_PET.getValue());
        mplew.writeInt(cid);
        mplew.write(slot);
        mplew.writeInt(pid);
        PacketHelper.serializeMovementList(mplew, moves);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("PetPacket 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket petChat(final int cid, final int un, final String text, final byte slot) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("petChat");
        }
        mplew.writeShort(SendPacketOpcode.PET_CHAT.getValue());
        mplew.writeInt(cid);
        mplew.write(slot);
        mplew.writeShort(un);
        mplew.writeMapleAsciiString(text);
        mplew.write(0);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("PetPacket 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket commandResponse(final int cid, final byte command, final byte slot, final boolean success, final boolean food) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("commandResponse");
        }
        mplew.writeShort(SendPacketOpcode.PET_COMMAND.getValue());
        mplew.writeInt(cid);
        mplew.write(slot);
        mplew.write((command == 1) ? 1 : 0);
        mplew.write(command);
        if (command == 1) {
            mplew.write(0);
        } else {
            mplew.writeShort(success ? 1 : 0);
        }
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("PetPacket 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket showOwnPetLevelUp(final byte index) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("showOwnPetLevelUp");
        }
        mplew.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
        mplew.write(4);
        mplew.write(0);
        mplew.write(index);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("PetPacket 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket showPetLevelUp(final MapleCharacter chr, final byte index) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("showPetLevelUp");
        }
        mplew.writeShort(SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
        mplew.writeInt(chr.getId());
        mplew.write(4);
        mplew.write(0);
        mplew.write(index);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("PetPacket 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket emptyStatUpdate() {
        if (ServerProperties.LogPktCall) {
            log.info("emptyStatUpdate");
        }
        return MaplePacketCreator.enableActions();
    }

    public static MaplePacket petStatUpdate_Empty() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("petStatUpdate_Empty");
        }
        mplew.writeShort(SendPacketOpcode.UPDATE_STATS.getValue());
        mplew.write(0);
        mplew.writeInt(MapleStat.PET.getValue());
        mplew.writeZeroBytes(25);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("PetPacket 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    public static MaplePacket petStatUpdate(final MapleCharacter chr) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (ServerProperties.LogPktCall) {
            log.info("petStatUpdate");
        }
        mplew.writeShort(SendPacketOpcode.UPDATE_STATS.getValue());
        mplew.write(0);
        mplew.writeInt(MapleStat.PET.getValue());
        byte count = 0;
        for (final MaplePet pet : chr.getPets()) {
            if (pet.getSummoned()) {
                mplew.writeLong(pet.getUniqueId());
                ++count;
            }
        }
        while (count < 3) {
            mplew.writeZeroBytes(8);
            ++count;
        }
        mplew.write(0);
        if (ServerProperties.LogClientErr) {
            ServerConstants.setPACKET_ERROR("PetPacket 暂未定义 ：\r\n" + mplew.getPacket() + "\r\n\r\n");
        }
        return mplew.getPacket();
    }

    static {
        ITEM_MAGIC = new byte[]{-128, 5};
    }
}
