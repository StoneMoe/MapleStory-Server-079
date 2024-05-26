package utils;

public class BitTools {
    public static byte rollLeft(final byte in, final int count) {
        int tmp = in & 0xFF;
        tmp <<= count % 8;
        return (byte) ((tmp & 0xFF) | tmp >> 8);
    }

    public static byte rollRight(final byte in, final int count) {
        int tmp = in & 0xFF;
        tmp = tmp << 8 >>> count % 8;
        return (byte) ((tmp & 0xFF) | tmp >>> 8);
    }

    public static byte[] multiplyBytes(final byte[] in, final int count, final int mul) {
        final byte[] ret = new byte[count * mul];
        for (int x = 0; x < count * mul; ++x) {
            ret[x] = in[x % count];
        }
        return ret;
    }

    public static int doubleToShortBits(final double d) {
        final long l = Double.doubleToLongBits(d);
        return (int) (l >> 48);
    }
}
