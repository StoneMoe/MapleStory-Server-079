package networking.output;

import java.awt.Point;
import java.nio.charset.Charset;
import java.util.Arrays;

public class GenericLittleEndianWriter implements LittleEndianWriter
{
    private static Charset Encoding;
    private ByteOutputStream bos;

    static {
        GenericLittleEndianWriter.Encoding = Charset.forName("GBK");
    }

    protected GenericLittleEndianWriter() {
    }
    
    public GenericLittleEndianWriter(final ByteOutputStream bos) {
        this.bos = bos;
    }
    
    protected void setByteOutputStream(final ByteOutputStream bos) {
        this.bos = bos;
    }
    
    @Override
    public void writeZeroBytes(final int i) {
        for (int x = 0; x < i; ++x) {
            this.bos.writeByte((byte)0);
        }
    }
    
    @Override
    public void write(final byte[] b) {
        for (int x = 0; x < b.length; ++x) {
            this.bos.writeByte(b[x]);
        }
    }
    
    @Override
    public void write(final byte b) {
        this.bos.writeByte(b);
    }
    
    @Override
    public void write(final int b) {
        this.bos.writeByte((byte)b);
    }
    
    @Override
    public void writeShort(final short i) {
        this.bos.writeByte((byte)(i & 0xFF));
        this.bos.writeByte((byte)(i >>> 8 & 0xFF));
    }
    
    @Override
    public void writeShort(final int i) {
        this.bos.writeByte((byte)(i & 0xFF));
        this.bos.writeByte((byte)(i >>> 8 & 0xFF));
    }
    
    @Override
    public void writeInt(final int i) {
        this.bos.writeByte((byte)(i & 0xFF));
        this.bos.writeByte((byte)(i >>> 8 & 0xFF));
        this.bos.writeByte((byte)(i >>> 16 & 0xFF));
        this.bos.writeByte((byte)(i >>> 24 & 0xFF));
    }
    
    @Override
    public void writeAsciiString(final String s) {
        this.write(s.getBytes(GenericLittleEndianWriter.Encoding));
    }

    @Override
    public void writeAsciiString(String s, final int max) {
        byte[] bytes = s.getBytes(GenericLittleEndianWriter.Encoding);

        if (GenericLittleEndianWriter.Encoding == Charset.forName("GBK")) {
            bytes = trimGBKString(bytes, max);
        }

        if (bytes.length > max) {
            this.write(Arrays.copyOf(bytes, max));
        } else {
            this.write(bytes);
            for (int i = bytes.length; i < max; i++) {
                this.write(0);
            }
        }
    }
    
    @Override
    public void writeMapleAsciiString(final String s) {
        this.writeShort((short)s.getBytes(GenericLittleEndianWriter.Encoding).length);
        this.writeAsciiString(s);
    }
    
    @Override
    public void writePos(final Point s) {
        this.writeShort(s.x);
        this.writeShort(s.y);
    }
    
    @Override
    public void writeLong(final long l) {
        this.bos.writeByte((byte)(l & 0xFFL));
        this.bos.writeByte((byte)(l >>> 8 & 0xFFL));
        this.bos.writeByte((byte)(l >>> 16 & 0xFFL));
        this.bos.writeByte((byte)(l >>> 24 & 0xFFL));
        this.bos.writeByte((byte)(l >>> 32 & 0xFFL));
        this.bos.writeByte((byte)(l >>> 40 & 0xFFL));
        this.bos.writeByte((byte)(l >>> 48 & 0xFFL));
        this.bos.writeByte((byte)(l >>> 56 & 0xFFL));
    }

    public static byte[] trimGBKString(byte[] gbkBytes, int maxBytes) {
        if (gbkBytes.length <= maxBytes) {
            return gbkBytes;
        }

        int validLength = 0;
        for (int i = 0; i < gbkBytes.length; i++) {
            int want = isGBKCharFirstByte(gbkBytes[i]) ? 2 : 1;
            if (validLength + want > maxBytes) {
                break;
            }
            validLength += want;

            if (isGBKCharFirstByte(gbkBytes[i])) {
                i++;
            }
        }

        return Arrays.copyOf(gbkBytes, validLength);
    }

    private static boolean isGBKCharFirstByte(byte b) {
        return (b & 0xFF) >= 0x81 && (b & 0xFF) <= 0xFE;
    }
}
