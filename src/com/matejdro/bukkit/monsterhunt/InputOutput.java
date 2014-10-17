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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.InvalidConfigurationException;

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
            if (Settings.globals.config.getBoolean("Database.UseMySQL", false)) {
                Class.forName("com.mysql.jdbc.Driver");
                Connection ret = DriverManager.getConnection(Settings.globals.config.getString("Database.MySQLConn", ""), Settings.globals.config.getString("Database.MySQLUsername", ""), Settings.globals.config.getString("Database.MySQLPassword", ""));
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

    public static Integer getHighScore(UUID player) {
        Connection conn = getConnection();
        PreparedStatement ps = null;
        ResultSet set = null;
        Integer score = null;

        try {
            ps = conn.prepareStatement("SELECT * FROM monsterhunt_highscores WHERE name = ? LIMIT 1");

            ps.setString(1, player.toString());
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

    public static Integer getHighScoreRank(UUID player) {
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

    public static LinkedHashMap<UUID, Integer> getTopScores(int number) {
        Connection conn = getConnection();
        PreparedStatement ps = null;
        ResultSet set = null;
        LinkedHashMap<UUID, Integer> map = new LinkedHashMap<UUID, Integer>();

        try {
            ps = conn.prepareStatement("SELECT * FROM monsterhunt_highscores ORDER BY highscore DESC LIMIT ?");
            ps.setInt(1, number);
            set = ps.executeQuery();

            while (set.next()) {
                String uuid = set.getString("name");
                Integer score = set.getInt("highscore");
                map.put(UUID.fromString(uuid), score);
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

    public static void UpdateHighScore(UUID player, int score) {
        try {
            Connection conn = InputOutput.getConnection();
            PreparedStatement ps = conn.prepareStatement("REPLACE INTO monsterhunt_highscores VALUES (?,?)");
            ps.setString(1, player.toString());
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
        
        LoadDefaults();

        //loading world specific settings 
        List<HuntSpecification> globalHunts = Settings.globals.getListOfHunts();
        for (String n : Settings.globals.config.getString("EnabledWorlds").split(",")) 
        {
            MonsterHuntWorld mw = LoadWorld(n, globalHunts);
            HuntWorldManager.worlds.put(n, mw);
        }

        //loading the zone
        String[] temp = Settings.globals.config.getString("HuntZone.FirstCorner", "0,0,0").split(",");
        HuntZone.corner1 = new Location(null, Double.parseDouble(temp[0]), Double.parseDouble(temp[1]), Double.parseDouble(temp[2]));
        temp = Settings.globals.config.getString("HuntZone.SecondCorner", "0,0,0").split(",");
        HuntZone.corner2 = new Location(null, Double.parseDouble(temp[0]), Double.parseDouble(temp[1]), Double.parseDouble(temp[2]));
        temp = Settings.globals.config.getString("HuntZone.TeleportLocation", "0,0,0").split(",");
        World world = MonsterHunt.instance.getServer().getWorld(Settings.globals.config.getString("HuntZone.World", MonsterHunt.instance.getServer().getWorlds().get(0).getName()));
        HuntZone.teleport = new Location(world, Double.parseDouble(temp[0]), Double.parseDouble(temp[1]), Double.parseDouble(temp[2]));

        //Create zone world
        MonsterHuntWorld mw = LoadWorld(world.getName(), globalHunts);
        HuntWorldManager.HuntZoneWorld = mw;
    }
    
    private static MonsterHuntWorld LoadWorld(String worldName, List<HuntSpecification> globalHunts)
    {
    	MonsterHuntWorld world = new MonsterHuntWorld(worldName);
    	
    	world.worldSettings = LoadWorldSettings(worldName);
        List<HuntSpecification> worldHunts = world.worldSettings.getListOfHunts();
        world.huntList.addAll(globalHunts);
        world.huntList.removeAll(worldHunts);
        world.huntList.addAll(worldHunts);
        
        if(world.huntList.size() == 0)
        {
        	world.huntList.add(new HuntSpecification("default", "The Hunt", 100, world.worldSettings));
        }
        
        world.randomizeHunt();
        
        Util.Debug("-------   " + worldName + "  --------");
        for(HuntSpecification hs : world.huntList)
        	Util.Debug(hs.toString());
        
        return world;
    }
    
    private static Settings LoadWorldSettings(String worldName)
    {
    	return new Settings(new File("plugins" + File.separator + "MonsterHunt" + File.separator, worldName + ".yml"), Settings.globals);
    }
    public static void LoadBans()
    {
    	Connection conn = getConnection();
        PreparedStatement ps = null;
        ResultSet set = null;
        
        try {
            ps = conn.prepareStatement("SELECT name FROM monsterhunt_bans");
            set = ps.executeQuery();

            while (set.next()) {
                String uuid = set.getString("name");
                HuntWorldManager.bannedPlayers.add(UUID.fromString(uuid));
            }
            set.close();
            ps.close();
            conn.close();
        } catch (SQLException e) {
            Log.severe("Error while retreiving banned players! - " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static void banPlayer(UUID player, String reason) 
    {
    	try {
            Connection conn = InputOutput.getConnection();
            PreparedStatement ps = conn.prepareStatement("REPLACE INTO monsterhunt_bans VALUES (?,?)");
            ps.setString(1, player.toString());
            ps.setString(2, reason);
            ps.executeUpdate();
            conn.commit();
            ps.close();
            conn.close();
        } catch (SQLException e) {
            Log.severe("Error while inserting new ban into DB! - " + e.getMessage());
            e.printStackTrace();
        }
	}
    public static void unbanPlayer(UUID player) {
    	try {
            Connection conn = InputOutput.getConnection();
            PreparedStatement ps = conn.prepareStatement("DELETE FROM monsterhunt_bans WHERE NAME = ?");
            ps.setString(1, player.toString());
            ps.executeUpdate();
            conn.commit();
            ps.close();
            conn.close();
        } catch (SQLException e) {
            Log.severe("Error while ban from DB! - " + e.getMessage());
            e.printStackTrace();
        }
	}
    
    
    public static void LoadDefaults() {
        try {
        	Settings.loadGlobals(new File("plugins" + File.separator + "MonsterHunt" + File.separator, "global.yml"));
            return;
        } catch (FileNotFoundException e1) {
            Log.info("Global config file missing. Creating one from scratch.");
        } catch (IOException e1) {
            e1.printStackTrace();
            return;
        } catch (InvalidConfigurationException e1) {
            e1.printStackTrace();
            return;
        }

        String mobSettingString = "Points.Mobs.";
        
        for (String i : new String[] { "Zombie", "Skeleton", "Creeper", "Spider", "Ghast", "Slime", "ZombiePigman", "Giant", 
        		"TamedWolf", "WildWolf", "ElectrifiedCreeper", "Player", "Enderman", "Silverfish", "CaveSpider", "EnderDragon",
        		"MagmaCube", "Blaze", "IronGolem", "Wither", "WitherSkeleton", "Witch" }) {
            if (Settings.globals.config.contains(mobSettingString + i) == false)
            {
            	  Settings.globals.config.set(mobSettingString + i + ".General", 10);
                  Settings.globals.config.set(mobSettingString + i + ".Wolf", 7);
                  Settings.globals.config.set(mobSettingString + i + ".Arrow", 4);
                  Settings.globals.config.set(mobSettingString + i + ".Snowball", 20);
                  Settings.globals.config.set(mobSettingString + i + ".283", 20);
            }
        }
        
        for (String i : new String[] { "MushroomCow", "Chicken", "Cow", "Pig", "Sheep", "SnowGolem", "Squid", "Villager" }) {
            if (Settings.globals.config.contains(mobSettingString + i) == false)
            {
                Settings.globals.config.set(mobSettingString + i + ".General", 0);
            }
        }
      
        for (Setting s : Setting.values()) {
            if (s.writeDefault() && Settings.globals.config.get(s.getString()) == null)
                Settings.globals.config.set(s.getString(), s.getDefault());
        }
        
        if (!new File("plugins" + File.separator + "MonsterHunt" + File.separator, "global.yml").exists()) 
        {
	        Settings.globals.config.set("Rewards.MinimumPointsPlace1", 1);
	        Settings.globals.config.set("Rewards.RewardParametersPlace1", "3 3");
	        Settings.globals.config.set("Rewards.MinimumPointsPlace2", 1);
	        Settings.globals.config.set("Rewards.RewardParametersPlace2", "3 2");
	        Settings.globals.config.set("Rewards.MinimumPointsPlace3", 1);
	        Settings.globals.config.set("Rewards.RewardParametersPlace3", "3 1");
	        
	        Settings.globals.config.set("Messages.FinishMessageWinners.WinnerPlace1", "1st place: <Names> (<Points> points) [NEWLINE]");
	        Settings.globals.config.set("Messages.FinishMessageWinners.WinnerPlace2", "2nd place: <Names> (<Points> points) [NEWLINE]");
	        Settings.globals.config.set("Messages.FinishMessageWinners.WinnerPlace3", "3rd place: <Names> (<Points> points)");
	        
	        
	        Settings.globals.config.set("Points.EffectPenalty.increase_damage_1", "0%");
	        Settings.globals.config.set("Points.EffectPenalty.increase_damage_1", "0%");
	        Settings.globals.config.set("Points.EffectPenalty.increase_damage_2", "0%");
	        Settings.globals.config.set("Points.EffectPenalty.speed_1", 0);
	        Settings.globals.config.set("Points.EffectPenalty.speed_2", 0);
	        Settings.globals.config.set("Points.EffectPenalty.jump_1", "0%");
	        Settings.globals.config.set("Points.EffectPenalty.jump_2", "0%");
	        Settings.globals.config.set("Points.EffectPenalty.regeneration_1", 0);
	        Settings.globals.config.set("Points.EffectPenalty.damage_resistance_1", 0);
	        Settings.globals.config.set("Points.EffectPenalty.damage_resistance_2", "0%");
	        Settings.globals.config.set("Points.EffectPenalty.fire_resistance_1", "0%");

	        List<HashMap<String, Object>> listOfHunts = new ArrayList<HashMap<String, Object>>();
	        HashMap<String, Object> defaultHunt = new HashMap<String, Object>();
	        defaultHunt.put("Name", "default");
	        defaultHunt.put("DisplayName", "The Hunt");
	        defaultHunt.put("Chance", 77);

	        listOfHunts.add(defaultHunt);
	        Settings.globals.config.set("Hunts", listOfHunts);
	    }
        
        try {
            Settings.globals.config.save(new File("plugins" + File.separator + "MonsterHunt" + File.separator, "global.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveZone() {
    	Settings.globals.config.set("HuntZone.FirstCorner", String.valueOf(HuntZone.corner1.getBlockX()) + "," + String.valueOf(HuntZone.corner1.getBlockY()) + "," + String.valueOf(HuntZone.corner1.getBlockZ()));
        Settings.globals.config.set("HuntZone.SecondCorner", String.valueOf(HuntZone.corner2.getBlockX()) + "," + String.valueOf(HuntZone.corner2.getBlockY()) + "," + String.valueOf(HuntZone.corner2.getBlockZ()));
        Settings.globals.config.set("HuntZone.TeleportLocation", String.valueOf(HuntZone.teleport.getX()) + "," + String.valueOf(HuntZone.teleport.getY()) + "," + String.valueOf(HuntZone.teleport.getZ()));
        Settings.globals.config.set("HuntZone.World", HuntZone.teleport.getWorld().getName());

        try {
            Settings.globals.config.save(new File("plugins" + File.separator + "MonsterHunt" + File.separator, "global.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void ReloadSettings()
    {
    	for(MonsterHuntWorld mw : HuntWorldManager.getWorlds())
    	{
			mw.stop();
    	}
    	HuntWorldManager.worlds.clear();
    	LoadSettings();
    }
    
    public static void PrepareDB() {
        Connection conn = null;
        Statement st = null;
        try {
            conn = InputOutput.getConnection();
            st = conn.createStatement();
            if (Settings.globals.config.getBoolean("Database.UseMySQL", false)) {
                st.executeUpdate("CREATE TABLE IF NOT EXISTS `monsterhunt_highscores` ( `name` varchar(250) NOT NULL DEFAULT '', `highscore` integer DEFAULT NULL, PRIMARY KEY (`name`) ) ENGINE=InnoDB DEFAULT CHARSET=utf8;");
                st.executeUpdate("CREATE TABLE IF NOT EXISTS `monsterhunt_bans` ( `name` varchar(250) NOT NULL DEFAULT '',`reason` varchar(250), PRIMARY KEY (`name`) ) ENGINE=InnoDB DEFAULT CHARSET=utf8;");
                st.executeUpdate("CREATE TABLE IF NOT EXISTS monsterhunt_RewardsToClaim (PlayerUUID VARCHAR(250) NOT NULL, HuntName VARCHAR(250), RewardType VARCHAR(250), Score INTEGER)");
                st.executeUpdate("CREATE TABLE IF NOT EXISTS monsterhunt_BoughtHunts (UUID VARCHAR(250) PRIMARY KEY NOT NULL, Amount INTEGER)");
            } else {
                st.executeUpdate("CREATE TABLE IF NOT EXISTS \"monsterhunt_highscores\" (\"name\" VARCHAR PRIMARY KEY  NOT NULL , \"highscore\" INTEGER)");
                st.executeUpdate("CREATE TABLE IF NOT EXISTS \"monsterhunt_bans\" (\"name\" VARCHAR PRIMARY KEY  NOT NULL, \"reason\" VARCHAR)");
                st.executeUpdate("CREATE TABLE IF NOT EXISTS monsterhunt_RewardsToClaim (PlayerName VARCHAR NOT NULL, HuntName VARCHAR, RewardType VARCHAR, Score INTEGER)");
                st.executeUpdate("CREATE TABLE IF NOT EXISTS monsterhunt_BoughtHunts (UUID VARCHAR PRIMARY KEY NOT NULL, Amount INTEGER)");
            }
            conn.commit();
        } catch (SQLException e) {
            Log.severe("Error while creating tables! - " + e.getMessage());
        }
        
        upgradeDB();
    }

    public static void upgradeDB()
    {
    	migrateToUUID("monsterhunt_bans", "name");
    	migrateToUUID("monsterhunt_highscores", "name");
    	migrateToUUID("monsterhunt_RewardsToClaim", "PlayerName");
    }
    
    private static void migrateToUUID(String table, String field)
    {
    	if (!arePlayersStoredAsNames(table, field))
    		return;
    	
    	Log.info("Migrating table " + table + " to UUID...");
    	
    	try {
			PreparedStatement updateStatement = getConnection().prepareStatement("UPDATE " + table + " SET " + field + " = ? WHERE " + field + " = ?");
			PreparedStatement selectStatement = getConnection().prepareStatement("SELECT " + field + " FROM " + table);
			ResultSet set = selectStatement.executeQuery();
			
			while (set.next())
			{
				String name = set.getString(1);
				UUID uuid = Bukkit.getOfflinePlayer(name).getUniqueId();
				
				updateStatement.setString(1, uuid.toString());
				updateStatement.setString(2, name);
				updateStatement.addBatch();

			}
			
			selectStatement.close();
			updateStatement.executeBatch();
			updateStatement.close();
			getConnection().commit();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
        
    private static boolean arePlayersStoredAsNames(String table, String field)
    {
    	boolean playersStoredAsNames = false;
    	
    	try
    	{
    		PreparedStatement statement = getConnection().prepareStatement("SELECT " + field + " FROM " + table + " LIMIT 1");
    		ResultSet set = statement.executeQuery();
    		
    		if (set.next())
    		{
    			String name = set.getString(1);
    			playersStoredAsNames = !name.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
    		}
    		
    		statement.close();
    	}
    	catch (SQLException e)
    	{
    		e.printStackTrace();
    	}
    	
    	return playersStoredAsNames;
    }
	

	
}