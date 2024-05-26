package networking.input;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class GenericSeekableLittleEndianAccessor extends GenericLittleEndianAccessor implements SeekableLittleEndianAccessor {
    private final SeekableInputStreamBytestream bs;

    public GenericSeekableLittleEndianAccessor(final SeekableInputStreamBytestream bs) {
        super(bs);
        this.bs = bs;
    }

    @Override
    public void seek(final long offset) {
        try {
            this.bs.seek(offset);
        } catch (IOException e) {
            log.error("Seek failed" + e);
        }
    }

    @Override
    public long getPosition() {
        try {
            return this.bs.getPosition();
        } catch (IOException e) {
            log.error("getPosition failed" + e);
            return -1L;
        }
    }

    @Override
    public void skip(final int num) {
        this.seek(this.getPosition() + num);
    }
}
