package handling.mina;

import client.MapleClient;
import configuration.ServerProperties;
import handling.RecvPacketOpcode;
import lombok.extern.slf4j.Slf4j;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import utils.FileoutputUtil;
import utils.HexTool;
import networking.MapleAESOFB;
import networking.MapleCustomEncryption;
import networking.input.ByteArrayByteStream;
import networking.input.GenericLittleEndianAccessor;

@Slf4j
public class MaplePacketDecoder extends CumulativeProtocolDecoder {
    public static String DECODER_STATE_KEY;

    protected boolean doDecode(final IoSession session, final IoBuffer in, final ProtocolDecoderOutput out) throws Exception {
        DecoderState decoderState = (DecoderState) session.getAttribute(MaplePacketDecoder.DECODER_STATE_KEY);
        if (decoderState == null) {
            decoderState = new DecoderState();
            session.setAttribute(MaplePacketDecoder.DECODER_STATE_KEY, decoderState);
        }
        final MapleClient client = (MapleClient) session.getAttribute(MapleClient.CLIENT_KEY);
        if (decoderState.packetlength == -1) {
            if (in.remaining() >= 4) {
                final int packetHeader = in.getInt();
                if (!client.getReceiveCrypto().checkPacket(packetHeader)) {
                    session.closeNow();
                    return false;
                }
                decoderState.packetlength = MapleAESOFB.getPacketLength(packetHeader);
            } else if (in.remaining() < 4 && decoderState.packetlength == -1) {
                MaplePacketDecoder.log.trace("解码…没有足够的数据/就是所谓的包不完整");
                return false;
            }
        }
        if (in.remaining() >= decoderState.packetlength) {
            final byte[] decryptedPacket = new byte[decoderState.packetlength];
            in.get(decryptedPacket, 0, decoderState.packetlength);
            decoderState.packetlength = -1;
            client.getReceiveCrypto().crypt(decryptedPacket);
            MapleCustomEncryption.decryptData(decryptedPacket);
            out.write(decryptedPacket);
            if (ServerProperties.LogPkt) {
                final int packetLen = decryptedPacket.length;
                final int pHeader = this.readFirstShort(decryptedPacket);
                final String pHeaderStr = Integer.toHexString(pHeader).toUpperCase();
                final String op = this.lookupSend(pHeader);
                if (packetLen <= 3000) {
                    log.info("客户端发送 {} [{}] ({})\r\n{}\r\n{}", op, pHeaderStr, packetLen, HexTool.toString(decryptedPacket), HexTool.toStringFromAscii(decryptedPacket));
                } else {
                    log.info("{}...", HexTool.toString(new byte[]{decryptedPacket[0], decryptedPacket[1]}));
                }
            }
            return true;
        }
        return false;
    }

    private String lookupSend(final int val) {
        for (final RecvPacketOpcode op : RecvPacketOpcode.values()) {
            if (op.getValue() == val) {
                return op.name();
            }
        }
        return "UNKNOWN";
    }

    private int readFirstShort(final byte[] arr) {
        return new GenericLittleEndianAccessor(new ByteArrayByteStream(arr)).readShort();
    }

    static {
        MaplePacketDecoder.DECODER_STATE_KEY = MaplePacketDecoder.class.getName() + ".STATE";
    }

    public static class DecoderState {
        public int packetlength;

        public DecoderState() {
            this.packetlength = -1;
        }
    }
}
