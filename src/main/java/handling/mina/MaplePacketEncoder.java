package handling.mina;

import client.MapleClient;
import constants.ServerConstants;
import handling.MaplePacket;
import handling.SendPacketOpcode;
import lombok.extern.slf4j.Slf4j;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import utils.FileoutputUtil;
import utils.HexTool;
import networking.MapleAESOFB;
import networking.MapleCustomEncryption;
import networking.input.ByteArrayByteStream;
import networking.input.GenericLittleEndianAccessor;

import java.util.concurrent.locks.Lock;

@Slf4j
public class MaplePacketEncoder implements ProtocolEncoder {

    public void encode(final IoSession session, final Object message, final ProtocolEncoderOutput out) throws Exception {
        final MapleClient client = (MapleClient) session.getAttribute(MapleClient.CLIENT_KEY);
        if (client != null) {
            final MapleAESOFB send_crypto = client.getSendCrypto();
            final byte[] inputInitialPacket = ((MaplePacket) message).getBytes();
            if (ServerConstants.封包显示) {
                final int packetLen = inputInitialPacket.length;
                final int pHeader = this.readFirstShort(inputInitialPacket);
                final String pHeaderStr = Integer.toHexString(pHeader).toUpperCase();
                final String op = this.lookupRecv(pHeader);
                boolean show = true;
                final String s = op;
                switch (s) {
                    case "WARP_TO_MAP":
                    case "PING":
                    case "NPC_ACTION":
                    case "UPDATE_STATS":
                    case "MOVE_PLAYER":
                    case "SPAWN_NPC":
                    case "SPAWN_NPC_REQUEST_CONTROLLER":
                    case "REMOVE_NPC":
                    case "MOVE_LIFE":
                    case "MOVE_MONSTER":
                    case "MOVE_MONSTER_RESPONSE":
                    case "SPAWN_MONSTER":
                    case "SPAWN_MONSTER_CONTROL":
                    case "ANDROID_MOVE": {
                        show = false;
                        break;
                    }
                }
                final String Recv = "服务端发送 " + op + " [" + pHeaderStr + "] (" + packetLen + ")\r\n";
                if (packetLen <= 50000) {
                    final String RecvTo = Recv + HexTool.toString(inputInitialPacket) + "\r\n" + HexTool.toStringFromAscii(inputInitialPacket);
                    if (show) {
                        FileoutputUtil.packetLog("logs/服务端封包.log", RecvTo);
                        log.info(RecvTo);
                    }
                } else {
                    MaplePacketEncoder.log.info(HexTool.toString(new byte[]{inputInitialPacket[0], inputInitialPacket[1]}) + " ...");
                }
            }
            final byte[] unencrypted = new byte[inputInitialPacket.length];
            System.arraycopy(inputInitialPacket, 0, unencrypted, 0, inputInitialPacket.length);
            final byte[] ret = new byte[unencrypted.length + 4];
            final Lock mutex = client.getLock();
            mutex.lock();
            try {
                final byte[] header = send_crypto.getPacketHeader(unencrypted.length);
                MapleCustomEncryption.encryptData(unencrypted);
                send_crypto.crypt(unencrypted);
                System.arraycopy(header, 0, ret, 0, 4);
            } finally {
                mutex.unlock();
            }
            System.arraycopy(unencrypted, 0, ret, 4, unencrypted.length);
            out.write(IoBuffer.wrap(ret));
        } else {
            out.write(IoBuffer.wrap(((MaplePacket) message).getBytes()));
        }
    }

    public void dispose(final IoSession session) throws Exception {
    }

    private String lookupRecv(final int val) {
        for (final SendPacketOpcode op : SendPacketOpcode.values()) {
            if (op.getValue() == val) {
                return op.name();
            }
        }
        return "UNKNOWN";
    }

    private int readFirstShort(final byte[] arr) {
        return new GenericLittleEndianAccessor(new ByteArrayByteStream(arr)).readShort();
    }

}
