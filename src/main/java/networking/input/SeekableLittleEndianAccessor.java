package networking.input;

public interface SeekableLittleEndianAccessor extends LittleEndianAccessor
{
    void seek(final long p0);
    
    long getPosition();
}
