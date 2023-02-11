package ml.spmc.smpmod.utils.sql;

import ml.spmc.smpmod.SMPMod;

import java.sql.*;

public class DatabaseManager {
    public static String url = "jdbc:mysql://localhost:3306/spacemc?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    public Connection connect() {
        Connection conn = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(url, "tcfplayz", "mcro123123");
        } catch (SQLException | ClassNotFoundException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    public void createTables() {
        for (String query : SMPMod.tableRegistry) {
            try {
                this.connect().createStatement().execute(query);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public long getLastLeft(String uuid) {
        try {
            Connection c = this.connect();
            Statement stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT lastonline FROM plrdata WHERE uuid = '" + uuid + "'");
            long entered = 0;
            if(rs.next()) entered = rs.getLong("lastonline");
            stmt.close();
            c.close();
            return entered;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public double getBalance(String name){
        try {
            Connection c = this.connect();
            Statement stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT coins FROM plrdata WHERE name = '" + name + "'");
            double coins = 0;
            if(rs.next()) coins = rs.getDouble("coins");
            stmt.close();
            c.close();
            return coins;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public boolean setBalance(String name, double money) {
        try {
            Connection c = this.connect();
            Statement stmt = c.createStatement();
            stmt.executeUpdate("UPDATE plrdata SET coins = '" + money + "' WHERE name = '" + name + "'");
            stmt.close();
            c.close();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean changeBalance(String name, double money) {
        if (getBalance(name) + money < 0) return false;
        return setBalance(name, getBalance(name) + money);
    }
}
