package handling.login.handler;

import client.LoginCrypto;
import configuration.ServerProperties;
import constants.ServerConstants;
import database.DatabaseConnection;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Slf4j
public class AutoRegister {
    private static final int ACCOUNTS_PER_MAC = 100;
    public static boolean autoRegister = ServerProperties.AutoRegister;
    public static boolean success = false;
    public static boolean mac = true;

    public static boolean getAccountExists(final String login) {
        boolean accountExists = false;
        final Connection con = DatabaseConnection.getConnection();
        try {
            final PreparedStatement ps = con.prepareStatement("SELECT name FROM accounts WHERE name = ?");
            ps.setString(1, login);
            final ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                accountExists = true;
            }
            rs.close();
            ps.close();
        } catch (SQLException ex) {
            log.error("getAccountExists SQL err", ex);
        }
        return accountExists;
    }

    public static boolean getAccountExistsByID(final int id) {
        boolean accountExists = false;
        final Connection con = DatabaseConnection.getConnection();
        try {
            final PreparedStatement ps = con.prepareStatement("SELECT name FROM accounts WHERE id = ?");
            ps.setInt(1, id);
            final ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                accountExists = true;
            }
            rs.close();
            ps.close();
        } catch (SQLException ex) {
            log.error("getAccountExists SQL error", ex);
        }
        return accountExists;
    }

    public static void createAccount(final String login, final String pwd, final String eip, final String macs) {
        final String sockAddr = eip;
        Connection con;
        try {
            con = DatabaseConnection.getConnection();
        } catch (Exception ex) {
            log.info("Err during get database connection", ex);
            return;
        }
        try {
            final PreparedStatement ipc = con.prepareStatement("SELECT macs FROM accounts WHERE macs = ?", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ipc.setString(1, macs);
            final ResultSet rs = ipc.executeQuery();
            if (!rs.first() || (rs.last() && rs.getRow() < 100)) {
                final PreparedStatement ps = con.prepareStatement("INSERT INTO accounts (name, password, email, birthday, macs, SessionIP) VALUES (?, ?, ?, ?, ?, ?)");
                ps.setString(1, login);
                ps.setString(2, LoginCrypto.hexSha1(pwd));
                ps.setString(3, "autoregister@mail.com");
                ps.setString(4, "2008-04-07");
                ps.setString(5, macs);
                ps.setString(6, "/" + sockAddr.substring(1, sockAddr.lastIndexOf(58)));
                ps.executeUpdate();
                AutoRegister.success = true;
            }
            AutoRegister.success = true;
            if (rs.getRow() >= 100) {
                AutoRegister.mac = false;
            }
        } catch (SQLException ex2) {
            log.info("Err during account creation", ex2);
        }
    }
}
