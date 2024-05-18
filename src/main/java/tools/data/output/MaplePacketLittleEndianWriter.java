package tools.data.output;

import handling.ByteArrayMaplePacket;
import handling.MaplePacket;
import lombok.extern.slf4j.Slf4j;
import server.ServerProperties;
import tools.HexTool;

import java.io.ByteArrayOutputStream;

@Slf4j
public class MaplePacketLittleEndianWriter extends GenericLittleEndianWriter {
    private static boolean debugMode;
    private ByteArrayOutputStream baos;

    public MaplePacketLittleEndianWriter() {
        this(32);
    }

    public MaplePacketLittleEndianWriter(final int size) {
        this.baos = new ByteArrayOutputStream(size);
        this.setByteOutputStream(new BAOSByteOutputStream(this.baos));
    }

    public MaplePacket getPacket() {
        if (MaplePacketLittleEndianWriter.debugMode) {
            final MaplePacket packet = new ByteArrayMaplePacket(this.baos.toByteArray());
            log.info("Packet to be sent:\n" + packet.toString());
        }
        return new ByteArrayMaplePacket(this.baos.toByteArray());
    }

    @Override
    public String toString() {
        return HexTool.toString(this.baos.toByteArray());
    }

    static {
        MaplePacketLittleEndianWriter.debugMode = Boolean.parseBoolean(ServerProperties.getProperty("RoyMS.Debug", "false"));
    }
}
