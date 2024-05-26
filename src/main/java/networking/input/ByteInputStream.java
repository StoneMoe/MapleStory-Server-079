package networking.input;

public interface ByteInputStream
{
    int readByte();
    
    long getBytesRead();
    
    long available();
    
    String toString(final boolean p0);
}
