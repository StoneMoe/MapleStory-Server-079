package tools;

import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Formatter;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

@Slf4j
public class GetInfo {
    public static void main(String[] args) {
        Config();
        getConfig();
        all();
//        System.setProperty("server_property_file_path","E:/game/2020mxd079/079sever/HuaiMS_服务端配置.properties");
//        System.setProperty("server_property_db_path","E:/game/2020mxd079/079sever/HuaiMS_数据库配置.properties");
//        System.setProperty("server_property_shop_path","E:/game/2020mxd079/079sever/HuaiMS_封商城道具.properties");
//        System.setProperty("server_property_fish_path","E:/game/2020mxd079/079sever/HuaiMS_钓鱼设置.properties");


    }

    public static void getIpconfig() {
        Map<String, String> map = System.getenv();
        log.info("Env: {}", map);
        log.info(map.get("USERNAME"));
        log.info(map.get("COMPUTERNAME"));
        log.info(map.get("USERDOMAIN"));
        log.info(map.get("USER"));
    }

    public static void all() {
        Properties 設定檔 = System.getProperties();
        log.info("Java的運行環境版本：" + 設定檔.getProperty("java.version"));
        log.info("Java的運行環境供應商：" + 設定檔.getProperty("java.vendor"));
        log.info("Java供應商的URL：" + 設定檔.getProperty("java.vendor.url"));
        log.info("Java的安裝路徑：" + 設定檔.getProperty("java.home"));
        log.info("Java的虛擬機規範版本：" + 設定檔.getProperty("java.vm.specification.version"));
        log.info("Java的虛擬機規範供應商：" + 設定檔.getProperty("java.vm.specification.vendor"));
        log.info("Java的虛擬機規範名稱：" + 設定檔.getProperty("java.vm.specification.name"));
        log.info("Java的虛擬機實現版本：" + 設定檔.getProperty("java.vm.version"));
        log.info("Java的虛擬機實現供應商：" + 設定檔.getProperty("java.vm.vendor"));
        log.info("Java的虛擬機實現名稱：" + 設定檔.getProperty("java.vm.name"));
        log.info("Java運行時環境規範版本：" + 設定檔.getProperty("java.specification.version"));
        log.info("Java運行時環境規範名稱：" + 設定檔.getProperty("java.specification.name"));
        log.info("Java的類格式版本號：" + 設定檔.getProperty("java.class.version"));
        log.info("Java的類路徑：" + 設定檔.getProperty("java.class.path"));
        log.info("加載庫時搜索的路徑列表：" + 設定檔.getProperty("java.library.path"));
        log.info("默認的臨時文件路徑：" + 設定檔.getProperty("java.io.tmpdir"));
        log.info("一個或多個擴展目錄的路徑：" + 設定檔.getProperty("java.ext.dirs"));
        log.info("操作系統的構架：" + 設定檔.getProperty("os.arch"));
        log.info("操作系統的版本：" + 設定檔.getProperty("os.version"));
        log.info("文件分隔符：" + 設定檔.getProperty("file.separator"));
        log.info("路徑分隔符：" + 設定檔.getProperty("path.separator"));
        log.info("行分隔符：" + 設定檔.getProperty("line.separator"));
        log.info("用戶的賬戶名稱：" + 設定檔.getProperty("user.name"));
        log.info("用戶的主目錄：" + 設定檔.getProperty("user.home"));
        log.info("用戶的當前工作目錄：" + 設定檔.getProperty("user.dir"));
    }

    public static void Config() {
        try {
            InetAddress addr = InetAddress.getLocalHost();
            String ip = addr.getHostAddress();
            String hostName = addr.getHostName();
            log.info("本機IP：" + ip + "\n本機名稱:" + hostName);
            Properties 設定檔 = System.getProperties();
            log.info("操作系統的名稱：" + 設定檔.getProperty("os.name"));
            log.info("操作系統的版本：" + 設定檔.getProperty("os.version"));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public static void getConfig() {
        try {
            InetAddress address = InetAddress.getLocalHost();
            NetworkInterface ni = NetworkInterface.getByInetAddress(address);
            byte[] mac = ni.getHardwareAddress();
            if (mac == null)
                mac = (ni.getInetAddresses().nextElement()).getAddress();
            String sIP = address.getHostAddress();
            String sMAC = "";
            Formatter formatter = new Formatter();
            for (int i = 0; i < mac.length; i++) {
                sMAC = formatter.format(Locale.getDefault(), "%02X%s", new Object[]{Byte.valueOf(mac[i]), (i < mac.length - 1) ? "-" : ""}).toString();
            }
            log.info("IP：" + sIP);
            log.info("MAC：" + sMAC);
        } catch (SocketException | UnknownHostException e) {
            e.printStackTrace();
        }
    }
}
