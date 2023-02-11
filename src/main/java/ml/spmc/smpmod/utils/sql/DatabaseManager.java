package ml.spmc.smpmod.utils.sql;

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

    /*public void createTables() {
        for (String query : SMPMod.tableRegistry) {
            try {
                this.connect().createStatement().execute(query);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }*/

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

    // temp
    public boolean playerExists(String uuid) {
        try {
            Connection c = this.connect();
            Statement stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM plrdata WHERE uuid = '" + uuid + "'");
            if (rs.next()) {
                stmt.close();
                c.close();
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void addPlayer(String uuid, String name, String ip) {
        try {
            Connection c = this.connect();
            assert c != null;
            Statement stmt = c.createStatement();
            stmt.executeUpdate("INSERT INTO plrdata (uuid, coins, goldcrate, voidcrate, diamondcrate, playtime, name, ip, level, exp) VALUES ('" + uuid + "', '" + 0 + "', '" + 0 + "', '" + 0 + "', '" + 0 + "', '" + 0 + "', '" + name + "', '" + ip + "', '" + 1 + "', '" + 0 + "')");
            stmt.close();
            c.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void updatePlaytime(String uuid, long playtime) {
        try {
            Connection c = this.connect();
            Statement stmt = c.createStatement();
            stmt.executeUpdate("UPDATE plrdata SET playtime = '" + playtime + "' WHERE uuid = '" + uuid + "'");
            stmt.close();
            c.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateName(String uuid, String name) {
        try {
            Connection c = this.connect();
            Statement stmt = c.createStatement();
            stmt.executeUpdate("UPDATE plrdata SET name = '" + name + "' WHERE uuid = '" + uuid + "'");
            stmt.close();
            c.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateCrates(String uuid, String voidcrate, String diamondcrate, String goldcrate) {
        try {
            Connection c = this.connect();
            Statement stmt = c.createStatement();
            stmt.executeUpdate("UPDATE plrdata SET voidcrate = '" + voidcrate + "' WHERE uuid = '" + uuid + "'");
            stmt.executeUpdate("UPDATE plrdata SET diamondcrate = '" + diamondcrate + "' WHERE uuid = '" + uuid + "'");
            stmt.executeUpdate("UPDATE plrdata SET goldcrate = '" + goldcrate + "' WHERE uuid = '" + uuid + "'");
            stmt.close();
            c.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateExp(String uuid, int exp) {
        try {
            Connection c = this.connect();
            Statement stmt = c.createStatement();
            stmt.executeUpdate("UPDATE plrdata SET exp = '" + exp + "' WHERE uuid = '" + uuid + "'");
            stmt.close();
            c.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateLevel(String uuid, int lvl) {
        try {
            Connection c = this.connect();
            Statement stmt = c.createStatement();
            stmt.executeUpdate("UPDATE plrdata SET level = '" + (lvl + 1) + "' WHERE uuid = '" + uuid + "'");
            stmt.close();
            c.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateIP(String uuid, String ip) {
        try {
            Connection c = this.connect();
            Statement stmt = c.createStatement();
            stmt.executeUpdate("UPDATE plrdata SET ip = '" + ip + "' WHERE uuid = '" + uuid + "'");
            stmt.close();
            c.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateLastEntered(String uuid) {
        try {
            Connection c = this.connect();
            Statement stmt = c.createStatement();
            stmt.executeUpdate("UPDATE plrdata SET lastonline = '" + System.currentTimeMillis() + "' WHERE uuid = '" + uuid + "'");
            stmt.close();
            c.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // get values
    public int getCoins(String uuid) {
        try {
            Connection c = this.connect();
            Statement stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT coins FROM plrdata WHERE uuid = '" + uuid + "'");
            int coins = 0;
            if(rs.next()) coins = rs.getInt("coins");
            stmt.close();
            c.close();
            return coins;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
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


    public int[] getExpLvl(String uuid) {
        try {
            Connection c = this.connect();
            Statement stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT exp, level FROM plrdata WHERE uuid = '" + uuid + "'");
            int[] explvl = {0, 0};
            if (rs.next()) explvl = new int[]{rs.getInt("exp"), rs.getInt("level")};
            stmt.close();
            c.close();
            return explvl;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new int[]{0, 0};
    }

    public long getPlaytime(String uuid) {
        try {
            Connection c = this.connect();
            Statement stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT playtime FROM plrdata WHERE uuid = '" + uuid + "'");
            long playtime = 0;
            if(rs.next()) playtime = rs.getLong("playtime");
            stmt.close();
            c.close();
            return playtime;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int[] getCrates(String uuid) {
        try {
            Connection c = this.connect();
            Statement stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT voidcrate, diamondcrate, goldcrate FROM plrdata WHERE uuid = '" + uuid + "'");
            int[] crates = {0, 0, 0};
            if (rs.next()) crates = new int[]{rs.getInt("voidcrate"), rs.getInt("diamondcrate"), rs.getInt("goldcrate")};
            stmt.close();
            c.close();
            return crates;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new int[]{0, 0, 0};
    }

    public String getName(String uuid) {
        try {
            Connection c = this.connect();
            Statement stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT name FROM plrdata WHERE uuid = '" + uuid + "'");
            String name = "";
            if (rs.next()) name = rs.getString("name");
            stmt.close();
            c.close();
            return name;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }

    public String getIP(String uuid) {
        try {
            Connection c = this.connect();
            Statement stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT ip FROM plrdata WHERE uuid = '" + uuid + "'");
            String ip = "0.0.0.0";
            if (rs.next()) ip = rs.getString("ip");
            stmt.close();
            c.close();
            return ip;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }
}
