package provider;

import java.io.File;
import java.nio.file.Path;

import configuration.EnvProperties;
import provider.WzXML.XMLWZFile;

public class MapleDataProviderFactory
{
    private static final String wzPath = EnvProperties.wzPath;
    
    private static MapleDataProvider getWZ(final File in, final boolean provideImages) {
        return new XMLWZFile(in);
    }
    
    public static MapleDataProvider getDataProvider(final File file) {
        return getWZ(file, false);
    }
    public static MapleDataProvider getDataProvider(final Path path) {
        return getWZ(path.toFile(), false);
    }
    
    public static MapleDataProvider getImageProvidingDataProvider(final File in) {
        return getWZ(in, true);
    }
    
    public static File fileInWZPath(final String filename) {
        return new File(wzPath, filename);
    }
}
