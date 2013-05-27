package com.matejdro.bukkit.monsterhunt;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

public class InputOutput {
    private static Connection connection;
    public static synchronized Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = createConnection();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }

    private static Connection createConnection() {
        try {
            if (Settings.globals.getBoolean("Database.UseMySQL", false)) {
                Class.forName("com.mysql.jdbc.Driver");
                Connection ret = DriverManager.getConnection(Settings.globals.getString("Database.MySQLConn", ""), Settings.globals.getString("Database.MySQLUsername", ""), Settings.globals.getString("Database.MySQLPassword", ""));
                ret.setAutoCommit(false);
                return ret;
            } else {
                Class.forName("org.sqlite.JDBC");
                Connection ret = DriverManager.getConnection("jdbc:sqlite:plugins" + File.separator + "MonsterHunt" + File.separator + "MonsterHunt.sqlite");
                ret.setAutoCommit(false);
                return ret;
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Integer getHighScore(String player) {
        Connection conn = getConnection();
        PreparedStatement ps = null;
        ResultSet set = null;
        Integer score = null;

        try {
            ps = conn.prepareStatement("SELECT * FROM monsterhunt_highscores WHERE name = ? LIMIT 1");

            ps.setString(1, player);
            set = ps.executeQuery();

            if (set.next()) {
                score = set.getInt("highscore");
            }
            set.close();
            ps.close();
            conn.close();
        } catch (SQLException e) {
            Log.severe("Error while retreiving high scores! - " + e.getMessage());
            e.printStackTrace();
        }
        return score;
    }

    public static Integer getHighScoreRank(String player) {
        Connection conn = getConnection();
        PreparedStatement ps = null;
        ResultSet set = null;
        boolean exist = false;
        Integer counter = 0;

        try {
            ps = conn.prepareStatement("SELECT * FROM monsterhunt_highscores ORDER BY highscore DESC");
            set = ps.executeQuery();
            while (set.next()) {
                counter++;
                String name = set.getString("name");
                if (name.equals(player)) {
                    exist = true;
                    break;
                }
            }
            set.close();
            ps.close();
            conn.close();
        } catch (SQLException e) {
            Log.severe("Error while retreiving high scores! - " + e.getMessage());
            e.printStackTrace();
        }
        if (exist) {
            return counter;
        } else {
            return null;
        }
    }

    public static LinkedHashMap<String, Integer> getTopScores(int number) {
        Connection conn = getConnection();
        PreparedStatement ps = null;
        ResultSet set = null;
        LinkedHashMap<String, Integer> map = new LinkedHashMap<String, Integer>();

        try {
            ps = conn.prepareStatement("SELECT * FROM monsterhunt_highscores ORDER BY highscore DESC LIMIT ?");
            ps.setInt(1, number);
            set = ps.executeQuery();

            while (set.next()) {
                String name = set.getString("name");
                Integer score = set.getInt("highscore");
                map.put(name, score);
            }
            set.close();
            ps.close();
            conn.close();
        } catch (SQLException e) {
            Log.severe("Error while retreiving high scores! - " + e.getMessage());
            e.printStackTrace();
        }
        return map;
    }

    public static void UpdateHighScore(String playername, int score) {
        try {
            Connection conn = InputOutput.getConnection();
            PreparedStatement ps = conn.prepareStatement("REPLACE INTO monsterhunt_highscores VALUES (?,?)");
            ps.setString(1, playername);
            ps.setInt(2, score);
            ps.executeUpdate();
            conn.commit();
            ps.close();
            conn.close();
        } catch (SQLException e) {
            Log.severe("Error while inserting new high score into DB! - " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void LoadSettings() {
        if (!new File("plugins" + File.separator + "MonsterHunt").exists()) {
            try {
                (new File("plugins" + File.separator + "MonsterHunt")).mkdir();
            } catch (Exception e) {
                Log.severe("Unable to create plugins/MontsterHunt/ directory");
            }
        }
        Settings.globals = new YamlConfiguration();

        //loading default settings, adding if anything is missing
        LoadDefaults();

        //loading world specific settings 
        for (String n : Settings.globals.getString("EnabledWorlds").split(",")) {
            MonsterHuntWorld mw = new MonsterHuntWorld(n);
            mw.settings = LoadWorldSettings(n);
            HuntWorldManager.worlds.put(n, mw);
        }

        String[] temp = Settings.globals.getString("HuntZone.FirstCorner", "0,0,0").split(",");
        HuntZone.corner1 = new Location(null, Double.parseDouble(temp[0]), Double.parseDouble(temp[1]), Double.parseDouble(temp[2]));
        temp = Settings.globals.getString("HuntZone.SecondCorner", "0,0,0").split(",");
        HuntZone.corner2 = new Location(null, Double.parseDouble(temp[0]), Double.parseDouble(temp[1]), Double.parseDouble(temp[2]));
        temp = Settings.globals.getString("HuntZone.TeleportLocation", "0,0,0").split(",");
        World world = MonsterHunt.instance.getServer().getWorld(Settings.globals.getString("HuntZone.World", MonsterHunt.instance.getServer().getWorlds().get(0).getName()));
        HuntZone.teleport = new Location(world, Double.parseDouble(temp[0]), Double.parseDouble(temp[1]), Double.parseDouble(temp[2]));

        //Create zone world
        MonsterHuntWorld mw = new MonsterHuntWorld(world.getName());
        mw.settings = LoadWorldSettings("zone");

        HuntWorldManager.HuntZoneWorld = mw;
    }
    
    //Loads setting from file for given worldName.
    //In effect, if there is not a specified file for this world, returns global settings
    private static Settings LoadWorldSettings(String worldName)
    {
    	return new Settings(new File("plugins" + File.separator + "MonsterHunt" + File.separator, worldName + ".yml"));
    }
    
    public static void LoadDefaults() {
        try {
            Settings.globals.load(new File("plugins" + File.separator + "MonsterHunt" + File.separator, "global.txt"));
        } catch (FileNotFoundException e1) {
            Log.info("Global config file missing. Creating one from scratch.");
        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (InvalidConfigurationException e1) {
            e1.printStackTrace();
        }

        for (String i : new String[] { "Zombie", "Skeleton", "Creeper", "Spider", "Ghast", "Slime", "ZombiePigman", "Giant", 
        		"TamedWolf", "WildWolf", "ElectrifiedCreeper", "Player", "Enderman", "Silverfish", "CaveSpider", "EnderDragon",
        		"MagmaCube", "Blaze", "IronGolem", "Wither", "WitherSkeleton", "Witch" }) {
            if (Settings.globals.get("Value." + i) != null)
                continue;

            Settings.globals.set("Value." + i + ".General", 10);
            Settings.globals.set("Value." + i + ".Wolf", 7);
            Settings.globals.set("Value." + i + ".Arrow", 4);
            Settings.globals.set("Value." + i + ".Snowball", 20);
            Settings.globals.set("Value." + i + ".283", 20);
        }

        for (String i : new String[] { "MushroomCow", "Chicken", "Cow", "Pig", "Sheep", "SnowGolem", "Squid", "Villager" }) {
            if (Settings.globals.get("Value." + i) != null)
                continue;

            Settings.globals.set("Value." + i + ".General", 0);
        }

        if (!new File("plugins" + File.separator + "MonsterHunt" + File.separator, "global.txt").exists()) {
        	
        	Settings.globals.set("Equipment.Leather", 0.5);
        	Settings.globals.set("Equipment.Gold", 1);
        	Settings.globals.set("Equipment.Iron", 1);
        	Settings.globals.set("Equipment.Chain", 1);
        	Settings.globals.set("Equipment.Diamond", 2);
        	Settings.globals.set("Equipment.Shovel", 0.5);
        	Settings.globals.set("Equipment.Sword", 1);
        	Settings.globals.set("Equipment.EnchantedArmor", 0.5);
        	Settings.globals.set("Equipment.EnchantedSword", 0.5);
        	Settings.globals.set("Equipment.EnchantedShovel", 0.5);
        	Settings.globals.set("Equipment.EnchantedBow", 1);
        	
            Settings.globals.set("MinimumPointsPlace1", 1);
            Settings.globals.set("MinimumPointsPlace2", 1);
            Settings.globals.set("MinimumPointsPlace3", 1);
            Settings.globals.set("Rewards.RewardParametersPlace1", "3 3");
            Settings.globals.set("Rewards.RewardParametersPlace2", "3 2");
            Settings.globals.set("Rewards.RewardParametersPlace3", "3 1");
        }

        for (Setting s : Setting.values()) {
            if (s.writeDefault() && Settings.globals.get(s.getString()) == null)
                Settings.globals.set(s.getString(), s.getDefault());
        }

        try {
            Settings.globals.save(new File("plugins" + File.separator + "MonsterHunt" + File.separator, "global.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveZone() {
        Settings.globals.set("HuntZone.FirstCorner", String.valueOf(HuntZone.corner1.getBlockX()) + "," + String.valueOf(HuntZone.corner1.getBlockY()) + "," + String.valueOf(HuntZone.corner1.getBlockZ()));
        Settings.globals.set("HuntZone.SecondCorner", String.valueOf(HuntZone.corner2.getBlockX()) + "," + String.valueOf(HuntZone.corner2.getBlockY()) + "," + String.valueOf(HuntZone.corner2.getBlockZ()));
        Settings.globals.set("HuntZone.TeleportLocation", String.valueOf(HuntZone.teleport.getX()) + "," + String.valueOf(HuntZone.teleport.getY()) + "," + String.valueOf(HuntZone.teleport.getZ()));
        Settings.globals.set("HuntZone.World", HuntZone.teleport.getWorld().getName());

        try {
            Settings.globals.save(new File("plugins" + File.separator + "MonsterHunt" + File.separator, "global.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void ReloadSettings()
    {
    	LoadDefaults();
    	Collection<MonsterHuntWorld> loadedWorlds = HuntWorldManager.getWorlds();
    	String[] arr = Settings.globals.getString("EnabledWorlds").split(",");
    	List<String> newEnabledWorlds = new ArrayList<String>(); 
    	Collections.addAll(newEnabledWorlds, arr); 
    	List<MonsterHuntWorld> toRemoval = new ArrayList<MonsterHuntWorld>();
    	//If the world was enabled before, and is enabled now, reload settings normally
    	//if it was enabled, but is not anymore, stop hunt and schedule it for removal
    	for(MonsterHuntWorld mw : loadedWorlds)
    	{
    		if(newEnabledWorlds.contains(mw.name))
    		{
        		mw.settings = LoadWorldSettings(mw.name);
        		newEnabledWorlds.remove(mw.name);
    		}
    		else
    		{
    			mw.stop();
    			toRemoval.add(mw);
    		}
    	}
    	//Remove disabled worlds
    	for(MonsterHuntWorld mw : toRemoval)
    	{
    		HuntWorldManager.worlds.remove(mw.name);
    	}
    	//Add newly enabled worlds
    	for(String name : newEnabledWorlds)
    	{
    		
    		MonsterHuntWorld mw = new MonsterHuntWorld(name);
            mw.settings = LoadWorldSettings(name);
            HuntWorldManager.worlds.put(name, mw);
    	}
    }
    
    public static void PrepareDB() {
        Connection conn = null;
        Statement st = null;
        try {
            conn = InputOutput.getConnection();
            st = conn.createStatement();
            if (Settings.globals.getBoolean("Database.UseMySQL", false)) {
                st.executeUpdate("CREATE TABLE IF NOT EXISTS `monsterhunt_highscores` ( `name` varchar(250) NOT NULL DEFAULT '', `highscore` integer DEFAULT NULL, PRIMARY KEY (`name`) ) ENGINE=InnoDB DEFAULT CHARSET=utf8;");
            } else {
                st.executeUpdate("CREATE TABLE IF NOT EXISTS \"monsterhunt_highscores\" (\"name\" VARCHAR PRIMARY KEY  NOT NULL , \"highscore\" INTEGER)");
            }
            conn.commit();
        } catch (SQLException e) {
            Log.severe("Error while creating tables! - " + e.getMessage());
        }
    //}

    //public static void initMetrics() {
    //    try {
    //        MetricsLite metrics = new MetricsLite(plugin);
    //        metrics.start();
    //    } catch (IOException e) {
            // Failed to submit the stats :-(
    //    }
    }
}