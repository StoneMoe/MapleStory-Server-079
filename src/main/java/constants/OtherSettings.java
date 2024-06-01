package constants;

import configuration.ServerProperties;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
public class OtherSettings {
    private static OtherSettings instance = null;
    private static boolean CANLOG;
    private String[] itempb_id;
    private String[] itemjy_id;
    private String[] itemgy_id;
    private String[] mappb_id;

    public static OtherSettings getInstance() {
        if (OtherSettings.instance == null) {
            OtherSettings.instance = new OtherSettings();
        }
        return OtherSettings.instance;
    }

    public OtherSettings() {
        this.itempb_id = ServerProperties.cashban.split(",");
        this.itemjy_id = ServerProperties.cashjy.split(",");
        this.itemgy_id = ServerProperties.gysj.split(",");
    }

    public boolean isCANLOG() {
        return OtherSettings.CANLOG;
    }

    public void setCANLOG(final boolean CANLOG) {
        OtherSettings.CANLOG = CANLOG;
    }

}
