package com.matejdro.bukkit.monsterhunt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Blaze;
import org.bukkit.entity.CaveSpider;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.MagmaCube;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Spider;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import us.corenetwork.core.scoreboard.CoreScoreboardManager;
public class MonsterHuntWorld {
    public String name;
    public boolean manual;
    public int state;
    public boolean waitday;
    public int curday;
    public boolean nextnight;
    public Settings worldSettings;
    
    public List<HuntSpecification> huntList = new ArrayList<HuntSpecification>();
    public HuntSpecification activeHuntSpecification;
    public long lastAnnounceTime;
    
    public HashMap<String, Integer> Score = new HashMap<String, Integer>();
    public List<String> kickedPlayers = new ArrayList<String>();
    public HashMap<String, Integer> lastScore = new HashMap<String, Integer>();
    public ArrayList<Integer> properlyspawned = new ArrayList<Integer>();
    public HashMap<Player, Location> tplocations = new HashMap<Player, Location>();
    
    private static final String OBJECTIVE_NAME = "HuntScore";
    private Scoreboard scoreboard;
    private Objective objective ;
    public MonsterHuntWorld(String w) {
        state = 0;
        waitday = false;
        manual = false;
        curday = 0;
        name = w;
    }
    
    public World getWorld() {
        return MonsterHunt.instance.getServer().getWorld(name);
    }

    public int getSignUpPeriodTime() {
        int time = worldSettings.getInt(Setting.SignUpPeriodTime);
        if (time != 0) {
            time = worldSettings.getInt(Setting.StartTime) - worldSettings.getInt(Setting.SignUpPeriodTime) * 1200;
            if (time < 0) {
                Log.warning("Wrong SignUpPeriodTime Configuration! Sign Up period will be disabled!");
                time = 0;
            }
        }
        return time;
    }
    
    public boolean isKicked(String name)
    {
    	return kickedPlayers.contains(name);
    }
    
    public boolean isBanned(String name)
    {
    	return HuntWorldManager.bannedPlayers.contains(name);
    }
    
    public void signUp(String name, int points)
    {
    	Score.put(name, points);
    }
    
    public void signUp(String name)
    {
    	signUp(name, 0);
    }
    
    public void kick(String name)
    {
    	Score.remove(name);
    	kickedPlayers.add(name);
    	refreshScoreboardPoints();
    	clearScoreboard(name);
    }
    public void unkick(String name) 
    {
		kickedPlayers.remove(name);
	}
    public void start() {
        String message = worldSettings.getString(Setting.StartMessage);
        message = message.replace("<World>", name);
        message = message.replace("<HuntName>", activeHuntSpecification.getDisplayName());
        Util.Broadcast(message);
        state = 2;
        waitday = true;
        removeHostileMobs();
        
        updateLimit();
        generateScoreboard();
        refreshScoreboards(); 
    }
    
    private void updateLimit()
    {
    	int curLimit = Settings.globals.getInt(Setting.HuntLimit); //Limit is global not per type
    	if (curLimit <= 0)
    		return;
    	
    	Settings.globals.setInt(Setting.HuntLimit, curLimit - 1);
    	Settings.globals.save();
    }
    
    private void generateScoreboard() 
    {
    	scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        objective = scoreboard.registerNewObjective(OBJECTIVE_NAME, "");
        objective.setDisplayName(activeHuntSpecification.getDisplayName());
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
	}
	public void refreshScoreboards()
	{
		refreshScoreboardPoints();
		setScoreboards(scoreboard);
	}

	private void clearScoreboard(String playerName)
	{
		Player player = Bukkit.getPlayerExact(playerName);
		
		if (player != null)
		{
			if (MonsterHunt.coreInstalled)
				CoreScoreboardManager.unregisterScoreboard(player, 1);
			else
				player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
		}
	}
    private void clearScoreboards()
    {
    	for(String playerName : Score.keySet())	
		{
			Player player = MonsterHunt.instance.getServer().getPlayerExact(playerName);
			if(player != null)
			{
				if (MonsterHunt.coreInstalled)
					CoreScoreboardManager.unregisterScoreboard(player, 1);
				else
					player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
			}
 		}
    }
	
    private void refreshScoreboardPoints()
    {
    	for(String playerName : Score.keySet())	
		{
			OfflinePlayer offlinePlayer = MonsterHunt.instance.getServer().getOfflinePlayer(playerName);
			if(offlinePlayer != null)
			{
				scoreboard.resetScores(offlinePlayer);
				if (Score.get(playerName) != 0)
				{
					objective.getScore(offlinePlayer).setScore(Score.get(playerName));
				}
			}
 		}
    }
    
    private void setScoreboards(Scoreboard scoreboard)
    {
    	for(String playerName : Score.keySet())	
		{
			Player player = MonsterHunt.instance.getServer().getPlayerExact(playerName);
			if(player != null)
			{
				if (MonsterHunt.coreInstalled)
					CoreScoreboardManager.registerScoreboard(player, 1, scoreboard);
				else
					player.setScoreboard(scoreboard);
			}
 		}
    }
    
    private void setScoreboard(String playerName, Scoreboard scoreboard)
    {
    	Player player = MonsterHunt.instance.getServer().getPlayerExact(playerName);
		if(player != null)
		{
			if (MonsterHunt.coreInstalled)
				CoreScoreboardManager.registerScoreboard(player, 1, scoreboard);
			else
				player.setScoreboard(scoreboard);
		}
    }

    public void stop() {
        if (state < 2) {
            return;
        }
        if (Score.size() < worldSettings.getInt(Setting.MinimumPlayers)) {
            String message = worldSettings.getString(Setting.FinishMessageNotEnoughPlayers);
            message = message.replace("<HuntName>", activeHuntSpecification.getDisplayName());
            message = message.replace("<World>", name);
            Util.Broadcast(message);
        } else {
            RewardManager.RewardWinners(this);
        }
        for (Entry<Player, Location> e : tplocations.entrySet()) {
            Player player = e.getKey();
            if (player == null || !player.isOnline()) {
                continue;
            }
            player.teleport(e.getValue());
        }
        state = 0;
        for (String i : Score.keySet()) {
            Integer hs = InputOutput.getHighScore(i);
            if (hs == null)
                hs = 0;
            int score = Score.get(i);
            if (score > hs) {
                InputOutput.UpdateHighScore(i, score);
                Player player = MonsterHunt.instance.getServer().getPlayer(i);
                if (player != null) {
                    String message = worldSettings.getString(Setting.HighScoreMessage);
                    message = message.replace("<Points>", String.valueOf(score));
                    Util.Message(message, player);
                }
            }
        }
        lastScore.putAll(Score);
        clearScoreboards();
        Score.clear();
        kickedPlayers.clear();
        properlyspawned.clear();
        randomizeHunt();
    }
    
	public void randomizeHunt()
	{
		int weightSum = 0;
		for(HuntSpecification hs : huntList)
			weightSum += hs.getChance();
		
		Random r = new Random();
		int roll = r.nextInt(weightSum);
		
		int sum = 0;
    	boolean found = false;
    	int i = 0;
    	while(!found && i < huntList.size())
    	{
    		HuntSpecification hs = huntList.get(i);
    		sum += hs.getChance();
    		if (roll < sum)
    		{
    			found = true;
    			worldSettings = hs.getSettings();
    			activeHuntSpecification = hs;
    		}
    		i++;
    	}
	}

    public void skipNight() {
        if (worldSettings.getInt(Setting.SkipToIfFailsToStart) >= 0) {
            getWorld().setTime(worldSettings.getInt(Setting.SkipToIfFailsToStart));
        }
    }
    
    public boolean canStart() {
        if (curday == 0) {
            curday = worldSettings.getInt(Setting.SkipDays);
            if ((new Random().nextInt(100)) < worldSettings.getInt(Setting.StartChance)) {
                return Settings.globals.getInt(Setting.HuntLimit) != 0; //Only start if limit is not 0 (can be -1 for no limit)
              
            }
        } else {
            curday--; 
        }
        return false;
    }
    
    public void removeHostileMobs()
    {
    	if(worldSettings.getBoolean(Setting.PurgeAllHostileMobsOnStart))
        {
    		@SuppressWarnings("rawtypes")
			Class[] classes = {Creeper.class, Skeleton.class,Zombie.class, Spider.class, Enderman.class, Ghast.class, Slime.class
    							, Blaze.class, CaveSpider.class, MagmaCube.class, PigZombie.class};
        	Collection<Entity> mobs = MonsterHunt.instance.getServer().getWorld(name).getEntitiesByClasses(classes);
        	for(Entity e : mobs)
        	{
        		LivingEntity le = (LivingEntity)e;
        		EntityEquipment eq = le.getEquipment();
        		if(le.getCustomName() == null && eq.getBootsDropChance() < 1 && eq.getChestplateDropChance() < 1 && eq.getLeggingsDropChance() < 1 && eq.getHelmetDropChance() < 1 && eq.getItemInHandDropChance() < 1)
        			e.remove();
        		
        	}
        }	
    }


	
}