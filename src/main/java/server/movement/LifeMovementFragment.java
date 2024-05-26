package server.movement;

import java.awt.Point;
import networking.output.LittleEndianWriter;

public interface LifeMovementFragment
{
    void serialize(final LittleEndianWriter p0);
    
    Point getPosition();
}
