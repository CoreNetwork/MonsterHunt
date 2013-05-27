package com.matejdro.bukkit.monsterhunt;

import org.bukkit.Bukkit;

public class Log {
    private static String pre = "[MonsterHunt] ";

    public static void info(String message) {
        Bukkit.getLogger().info(pre + message);
    }

    public static void warning(String message) {
        Bukkit.getLogger().warning(pre + message);
    }

    public static void severe(String message) {
        Bukkit.getLogger().severe(pre + message);
    }

}