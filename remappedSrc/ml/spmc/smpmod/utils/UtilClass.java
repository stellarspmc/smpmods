package ml.spmc.smpmod.utils;

import ml.spmc.smpmod.utils.sql.DatabaseManager;

public class UtilClass {

    public static boolean lockdown = false;
    public static DatabaseManager getDatabaseManager() {
        return new DatabaseManager();
    }
}
