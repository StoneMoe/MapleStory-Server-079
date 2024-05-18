package server;

import database.DatabaseConnection;
import handling.cashshop.CashShopServer;
import handling.channel.ChannelServer;
import handling.login.LoginServer;
import handling.world.World;
import lombok.extern.slf4j.Slf4j;
import tools.MaplePacketCreator;

import java.sql.SQLException;
import java.util.Set;

@Slf4j
public class ShutdownServer implements Runnable {
    private static final ShutdownServer instance;
    public static boolean running;
    public int mode;

    public ShutdownServer() {
        this.mode = 0;
    }

    public static ShutdownServer getInstance() {
        return ShutdownServer.instance;
    }

    public void shutdown() {
        this.run();
    }

    @Override
    public void run() {
        Timer.WorldTimer.getInstance().stop();
        Timer.EtcTimer.getInstance().stop();
        Timer.MapTimer.getInstance().stop();
        Timer.MobTimer.getInstance().stop();
        Timer.CloneTimer.getInstance().stop();
        Timer.CheatTimer.getInstance().stop();
        Timer.EventTimer.getInstance().stop();
        Timer.BuffTimer.getInstance().stop();
        Timer.TimerManager.getInstance().stop();
        for (final ChannelServer cs : ChannelServer.getAllInstances()) {
            cs.closeAllMerchant();
        }
        try {
            World.Guild.save();
            World.Alliance.save();
            World.Family.save();
        } catch (Exception ex) {
        }
        World.Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(0, " 游戏服务器将关闭维护，请玩家安全下线..."));
        for (final ChannelServer cs : ChannelServer.getAllInstances()) {
            try {
                cs.setServerMessage("游戏服务器将关闭维护，请玩家安全下线...");
            } catch (Exception ex2) {
            }
        }
        final Set<Integer> channels = ChannelServer.getAllInstance();
        for (final Integer channel : channels) {
            try {
                final ChannelServer cs2 = ChannelServer.getInstance(channel);
                cs2.saveAll();
                cs2.setFinishShutdown();
                cs2.shutdown();
            } catch (Exception e2) {
                log.info("频道" + String.valueOf(channel) + " 关闭错误.");
            }
        }
        log.info("服务端关闭事件 1 已完成.");
        log.info("服务端关闭事件 2 开始...");
        try {
            LoginServer.shutdown();
            log.info("登录伺服器关闭完成...");
        } catch (Exception ex3) {
        }
        try {
            CashShopServer.shutdown();
            log.info("商城伺服器关闭完成...");
        } catch (Exception ex4) {
        }
        try {
            DatabaseConnection.closeAll();
        } catch (SQLException ex5) {
        }
        Timer.PingTimer.getInstance().stop();
        log.info("服务端关闭事件 2 已完成.");
        try {
            Thread.sleep(1000L);
        } catch (InterruptedException e) {
            log.info("关闭服务端错误 - 2" + e);
        }
        System.exit(0);
    }

    static {
        instance = new ShutdownServer();
        ShutdownServer.running = false;
    }
}
