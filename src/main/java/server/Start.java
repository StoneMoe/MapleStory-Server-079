package server;

import api.APIServer;
import client.MapleCharacter;
import client.SkillFactory;
import constants.GameConstants;
import constants.OtherSettings;
import constants.ServerConstants;
import database.DatabaseConnection;
import handling.MapleServerHandler;
import handling.cashshop.CashShopServer;
import handling.channel.ChannelServer;
import handling.channel.MapleGuildRanking;
import handling.login.LoginInformationProvider;
import handling.login.LoginServer;
import handling.world.World;
import handling.world.family.MapleFamilyBuff;
import handling.world.guild.MapleGuild;
import lombok.extern.slf4j.Slf4j;
import server.events.MapleOxQuizFactory;
import server.life.MapleLifeFactory;
import server.life.MapleMonsterInformationProvider;
import server.life.MobSkillFactory;
import server.maps.MapleMapFactory;
import server.quest.MapleQuest;
import tools.FileoutputUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Slf4j
public class Start {
    public static boolean Check;
    private static APIServer apiServer;
    public static Start instance;

    public static void main(final String[] args) throws InterruptedException {
        String cfgPath = System.getProperty("cfgPath", "./config");
        String scriptsPath = System.getProperty("scriptsPath", "./scripts/scripts");
        String wzPath = System.getProperty("wzPath", "./scripts/wz");
        System.setProperty("server_property_file_path", cfgPath + "/server.properties");
        System.setProperty("server_property_db_path", cfgPath + "/db.properties");
        System.setProperty("server_property_shop_path", cfgPath + "/shop.properties");
        System.setProperty("server_property_fish_path", cfgPath + "/fish.properties");
        System.setProperty("wzPath", wzPath);
        System.setProperty("scripts_path", scriptsPath);
        System.setProperty("server_name", "冒险岛");
        OtherSettings.getInstance();
        Start.instance.run();
    }

    public void run() throws InterruptedException {
        final long start = System.currentTimeMillis();
        if (Boolean.parseBoolean(ServerProperties.getProperty("RoyMS.Admin"))) {
            log.info("[!!! 已开启只能管理员登录模式 !!!]");
        }
        if (Boolean.parseBoolean(ServerProperties.getProperty("RoyMS.AutoRegister"))) {
            log.info("加载 自动注册完成");
        }
        try {
            try (final PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE accounts SET loggedin = 0")) {
                ps.executeUpdate();
            }
            try (final PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE accounts SET lastGainHM = 0")) {
                ps.executeUpdate();
            }
        } catch (SQLException ex) {
            throw new RuntimeException("[数据库异常] 请检查数据库链接。目前无法连接到MySQL数据库.");
        }
        log.info("服务端 开始启动...版本号：079");
        log.info("服务器地址: {}:{}", ServerProperties.getProperty("RoyMS.IP"), LoginServer.PORT);
        log.info("游戏版本: {} v.{}.{}", ServerConstants.MAPLE_TYPE, ServerConstants.MAPLE_VERSION, ServerConstants.MAPLE_PATCH);
        log.info("主服务器: 蓝蜗牛");
        World.init();
        runThread();
        loadData();
        log.info("加载\"登入\"服务...");
        LoginServer.run_startup_configurations();
        log.info("正在加载频道...");
        ChannelServer.startChannel_Main();
        log.info("频道加载完成!");
        log.info("正在加载商城...");
        CashShopServer.run_startup_configurations();
        log.info("刷怪线程");
        World.registerRespawn();
        Timer.CheatTimer.getInstance().register(AutobanManager.getInstance(), 60000L);
        onlineTime(1);
        memoryGC(10);
        MapleServerHandler.registerMBean();
        LoginServer.setOn();
        log.info("经验倍率：{}  物品倍率：{}  金币倍率：{}  BOSS爆率：{}", Integer.parseInt(ServerProperties.getProperty("RoyMS.Exp")), Integer.parseInt(ServerProperties.getProperty("RoyMS.Drop")), Integer.parseInt(ServerProperties.getProperty("RoyMS.Meso")), Integer.parseInt(ServerProperties.getProperty("RoyMS.BDrop")));
        if (Boolean.parseBoolean(ServerProperties.getProperty("RoyMS.检测复制装备", "false"))) {
            checkCopyItemFromSql();
        }
        if (Boolean.parseBoolean(ServerProperties.getProperty("RoyMS.防万能检测", "false"))) {
            log.info("启动防万能检测");
            startCheck();
        }
        final long now = System.currentTimeMillis() - start;
        final long seconds = now / 1000L;
        final long ms = now % 1000L;
        startAPIServer();
        log.info("加载完成, 耗时: {}秒{}毫秒\r\n", seconds, ms);
        AutoSave(Integer.parseInt(ServerProperties.getProperty("Server.AutoSaveMinutes", "5")));
    }

    public static void runThread() {
        log.info("正在加载线程");
        Timer.WorldTimer.getInstance().start();
        Timer.EtcTimer.getInstance().start();
        Timer.MapTimer.getInstance().start();
        Timer.MobTimer.getInstance().start();
        Timer.CloneTimer.getInstance().start();
        Timer.CheatTimer.getInstance().start();
        Timer.EventTimer.getInstance().start();
        Timer.BuffTimer.getInstance().start();
        Timer.TimerManager.getInstance().start();
        Timer.PingTimer.getInstance().start();
        Timer.PGTimer.getInstance().start();
        log.info("完成!");
    }

    public static void loadData() {
        log.info("载入数据(因为数据量大可能比较久而且内存消耗会飙升)");
        log.info("加载等级经验数据");
        GameConstants.LoadExp();
        log.info("加载排名信息数据");
        MapleGuildRanking.getInstance().RankingUpdate();
        log.info("加载公会数据并清理不存在公会");
        MapleGuild.loadAll();
        log.info("加载任务数据");
        MapleQuest.initQuests();
        MapleLifeFactory.loadQuestCounts();
        log.info("加载爆物数据");
        MapleMonsterInformationProvider.getInstance().retrieveGlobal();
        log.info("加载脏话检测系统");
        log.info("Load LoginInformationProvider: {}", LoginInformationProvider.getInstance() != null? "Success": "Failed");
        log.info("加载道具数据");
        log.info("Load ItemMakerFactory: {}", ItemMakerFactory.getInstance() != null? "Success": "Failed");
        log.info("Load MapleItemInformationProvider: {}", MapleItemInformationProvider.getInstance() != null? "Success": "Failed");
        log.info("加载技能数据");
        log.info("Load SkillFactory: {} skills", SkillFactory.Initialize());
        log.info("Load MobSkillFactory: {}", MobSkillFactory.getInstance() != null? "Success": "Failed");
        log.info("Load MapleFamilyBuff: {}", MapleFamilyBuff.getBuffEntry() != null? "Success": "Failed");
        log.info("加载SpeedRunner");
        Runtime.getRuntime().addShutdownHook(new Thread(new Shutdown()));
        try {
            SpeedRunner.getInstance().loadSpeedRuns();
        } catch (SQLException ex) {
            log.info("SpeedRunner错误: {}", ex.getMessage());
        }
        log.info("加载随机奖励系统: {}", RandomRewards.getInstance() != null? "Success": "Failed");
        log.info("加载0X问答系统");
        MapleOxQuizFactory.getInstance().initialize();
        log.info("加载嘉年华数据: {}", MapleCarnivalFactory.getInstance() != null? "Success": "Failed");
        log.info("加载角色类排名数据");
        log.info("加载商城道具数据，数据较为庞大，请耐心等待");
        CashItemFactory.getInstance().initialize();
        MapleMapFactory.loadCustomLife();
    }

    public static void AutoSave(final int time) {
        log.info("服务端启用自动存档.{}分钟自动执行数据存档.", time);
        Timer.WorldTimer.getInstance().register(new Runnable() {
            @Override
            public void run() {
                try {
                    for (final ChannelServer cserv : ChannelServer.getAllInstances()) {
                        for (final MapleCharacter chr : cserv.getPlayerStorage().getAllCharacters()) {
                            if (chr == null) {
                                continue;
                            }
                            chr.saveToDB(false, false);
                        }
                    }
                } catch (Exception ex) {
                    log.error("Failed to auto save", ex);
                }
            }
        }, 60000L * time);
    }

    //在线时间
    public static void onlineTime(final int time) {
        log.info("服务端启用在线时间统计.{}分钟记录一次在线时间.", time);
        Timer.WorldTimer.getInstance().register(new Runnable() {
            @Override
            public void run() {
                try {
                    for (final ChannelServer chan : ChannelServer.getAllInstances()) {
                        for (final MapleCharacter chr : chan.getPlayerStorage().getAllCharacters()) {
                            if (chr == null) {
                                continue;
                            }
                            chr.gainGamePoints(1);
                            if (chr.getGamePoints() >= 5) {
                                continue;
                            }
                            chr.resetFBRW();
                            chr.resetFBRWA();
                            chr.resetSBOSSRW();
                            chr.resetSBOSSRWA();
                            chr.resetSGRW();
                            chr.resetSGRWA();
                            chr.resetSJRW();
                            chr.resetlb();
                            chr.setmrsjrw(0);
                            chr.setmrfbrw(0);
                            chr.setmrsgrw(0);
                            chr.setmrsbossrw(0);
                            chr.setmrfbrwa(0);
                            chr.setmrsgrwa(0);
                            chr.setmrsbossrwa(0);
                            chr.setmrfbrwas(0);
                            chr.setmrsgrwas(0);
                            chr.setmrsbossrwas(0);
                            chr.setmrfbrws(0);
                            chr.setmrsgrws(0);
                            chr.setmrsbossrws(0);
                            chr.resetGamePointsPS();
                            chr.resetGamePointsPD();
                        }
                    }
                } catch (Exception ex) {
                    log.error("Online Time exception: {}", ex.getMessage());
                }
            }
        }, 60000L * time);
    }

    protected static void checkCopyItemFromSql() {
        log.info("服务端启用 防复制系统，发现复制装备.进行删除处理功能");
        final List<Integer> equipOnlyIds = new ArrayList<>();
        final Map<Integer, Integer> checkItems = new HashMap<>();
        try {
            final Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM inventoryitems WHERE equipOnlyId > 0");
            final ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                final int itemId = rs.getInt("itemId");
                final int equipOnlyId = rs.getInt("equipOnlyId");
                if (equipOnlyId > 0) {
                    if (checkItems.containsKey(equipOnlyId)) {
                        if (checkItems.get(equipOnlyId) != itemId) {
                            continue;
                        }
                        equipOnlyIds.add(equipOnlyId);
                    } else {
                        checkItems.put(equipOnlyId, itemId);
                    }
                }
            }
            rs.close();
            ps.close();
            Collections.sort(equipOnlyIds);
            for (final int i : equipOnlyIds) {
                ps = con.prepareStatement("DELETE FROM inventoryitems WHERE equipOnlyId = ?");
                ps.setInt(1, i);
                ps.executeUpdate();
                ps.close();
                log.info("发现复制装备 该装备的唯一ID: {} 已进行删除处理..", i);
                FileoutputUtil.log("装备复制.txt", "发现复制装备 该装备的唯一ID: " + i + " 已进行删除处理..");
            }
        } catch (SQLException ex) {
            log.error("[EXCEPTION] 清理复制装备出现错误.", ex);
        }
    }

    public static void startCheck() {
        log.info("服务端启用检测.30秒检测一次角色是否与登录器断开连接.");
        Timer.WorldTimer.getInstance().register(new Runnable() {
            @Override
            public void run() {
                for (final ChannelServer channel : ChannelServer.getAllInstances()) {
                    for (final MapleCharacter chr : channel.getPlayerStorage().getAllCharacters()) {
                        if (chr != null) {
                            chr.startCheck();
                        }
                    }
                }
            }
        }, 30000L);
    }

    //内存回收
    public static void memoryGC(final int time) {
        Timer.WorldTimer.getInstance().register(new Runnable() {
            @Override
            public void run() {
                System.gc();
            }
        }, 60000L * time);
    }
    
    static {
        Start.Check = true;
        Start.instance = new Start();
    }
    
    public static class Shutdown implements Runnable
    {
        @Override
        public void run() {
            new Thread(ShutdownServer.getInstance()).start();
        }
    }

    private static void startAPIServer() {
        if (Start.apiServer != null)
        {
            Start.apiServer.stop();
        }

        Start.apiServer = new APIServer(
                ServerProperties.getProperty("APIServer.ListenIP", "0.0.0.0"),
                Integer.parseInt(ServerProperties.getProperty("APIServer.ListenPort", "8000"))
        );
        try {
            Start.apiServer.start();
        }
        catch (Exception e) {
            log.error("API Server start failed", e);
        }
    }
}
