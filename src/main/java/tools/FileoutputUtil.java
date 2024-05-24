package tools;

import client.MapleCharacter;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.util.Calendar;

@Slf4j
public class FileoutputUtil {
    public static String fixdam_mg = "魔法伤害修正";
    public static String fixdam_ph = "物理伤害修正";
    public static String MobVac_log = "吸怪";
    public static String hack_log = "怀疑外挂";
    public static String ban_log = "封号";
    public static String Acc_Stuck = "卡账号";
    public static String Login_Error = "登录错误";
    public static String Movement_Log = "移动出错";
    public static String IP_Log = "账号IP";
    public static String Zakum_Log = "扎昆";
    public static String Horntail_Log = "暗黑龙王";
    public static String Pinkbean_Log = "品克缤";
    public static String ScriptEx_Log = "Script异常";
    public static String PacketEx_Log = "Packet异常";
    public static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static SimpleDateFormat sdf_ = new SimpleDateFormat("yyyy-MM-dd");

    public static void logToFile_chr(final MapleCharacter chr, final String file, final String msg) {
        log.info("{} - 账号:{};名称 {}({});等级:{};地图:{};{}", file, chr.getClient().getAccountName(), chr.getName(), chr.getId(), chr.getLevel(), chr.getMapId(), msg);
    }

    public static void logToFile(final String file, final String msg) {
        log.info("{} - {}", file, msg);
    }

    public static void packetLog(final String file, final String msg) {
        log.info("{} - {}", file, msg);
    }

    public static void log(final String file, final String msg) {
        log.info("{} - {}", file, msg);
    }

    public static void outputFileError(final String file, final Throwable t) {
        log.error(file, t);
    }

    public static void hiredMerchLog(final String file, final String msg) {
        log.info("雇佣商人-{} - {}", file, msg);
    }

    public static String CurrentReadable_Date() {
        return FileoutputUtil.sdf_.format(Calendar.getInstance().getTime());
    }

    public static String CurrentReadable_Time() {
        return FileoutputUtil.sdf.format(Calendar.getInstance().getTime());
    }

}
