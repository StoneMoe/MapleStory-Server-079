package handling.login;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import configuration.EnvProperties;
import provider.MapleData;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;


public class LoginInformationProvider {
    private static final LoginInformationProvider instance;
    protected List<String> ForbiddenName;

    public static LoginInformationProvider getInstance() {
        return LoginInformationProvider.instance;
    }

    protected LoginInformationProvider() {
        this.ForbiddenName = new ArrayList<String>();
        final MapleData nameData = MapleDataProviderFactory.getDataProvider(Paths.get(EnvProperties.wzPath, "Etc.wz")).getData("ForbiddenName.img");
        for (final MapleData data : nameData.getChildren()) {
            this.ForbiddenName.add(MapleDataTool.getString(data));
        }
    }

    public boolean isForbiddenName(final String in) {
        for (final String name : this.ForbiddenName) {
            if (in.contains(name)) {
                return true;
            }
        }
        return false;
    }

    static {
        instance = new LoginInformationProvider();
    }
}
