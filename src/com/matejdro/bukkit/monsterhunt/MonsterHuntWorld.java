package com.matejdro.bukkit.monsterhunt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.UUID;

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
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import us.corenetwork.core.scoreboard.CoreScoreboardManager;
public class MonsterHuntWorld {
    public String name;
    private HuntState state;
    private Settings worldSettings;
    private boolean skipNextNight;
    private boolean waitUntilMorning;
    private boolean chainedHunt;

    private String currentSponsor;

    private BukkitRunnable stateSwitchTimer;

    public List<HuntSpecification> huntList = new ArrayList<HuntSpecification>();
    public HuntSpecification activeHuntSpecification;
    public long lastAnnounceTime;

    public HashMap<UUID, Integer> Score = new HashMap<UUID, Integer>();
    public List<UUID> kickedPlayers = new ArrayList<UUID>();
    public HashMap<UUID, Integer> lastScore = new HashMap<UUID, Integer>();
    public ArrayList<Integer> properlyspawned = new ArrayList<Integer>();
    public HashMap<Player, Location> tplocations = new HashMap<Player, Location>();
    private List<OfflinePlayer> sponsorQueue;

    private static final String OBJECTIVE_NAME = "HuntScore";
    private Scoreboard scoreboard;
    private Objective objective ;

    public MonsterHuntWorld(String w, Settings settings) {
        name = w;
        chainedHunt = false;

        this.worldSettings = settings;
        sponsorQueue = (List<OfflinePlayer>) this.worldSettings.getList(Setting.SponsorQueue);

        startInBetweenHuntDelay();
    }
    
    public World getBukkitWorld() {
        return MonsterHunt.instance.getServer().getWorld(name);
    }

    public Settings getSettings()
    {
        return worldSettings;
    }

    public boolean isKicked(UUID uuid)
    {
    	return kickedPlayers.contains(uuid);
    }
    
    public boolean isBanned(UUID uuid)
    {
    	return HuntWorldManager.bannedPlayers.contains(uuid);
    }
    
    public void signUp(UUID uuid, int points)
    {
    	Score.put(uuid, points);
    }
    
    public void signUp(UUID uuid)
    {
    	signUp(uuid, 0);
    }
    
    public void kick(UUID uuid)
    {
    	Score.remove(uuid);
    	kickedPlayers.add(uuid);
    	refreshScoreboardPoints();
    	clearScoreboard(uuid);
    }
    public void unkick(UUID uuid) 
    {
		kickedPlayers.remove(uuid);
	}

    public void startSignups()
    {
        if (state == HuntState.SIGNUP)
            return;

        setState(HuntState.SIGNUP);

        OfflinePlayer sponsor = sponsorQueue.get(0);

        String message;
        if (chainedHunt)
        {
            message = worldSettings.getString(Setting.MessageSignUpPeriodChained);
            message = message.replace("<World>", name);
            message = message.replace("<HuntName>", activeHuntSpecification.getDisplayName());
            message = message.replace("<Sponsor>", sponsor.getName());
            message = message.replace("<Time>", TimeUtil.formatTimeTicks(getTimeUntilStart()));

            Util.Broadcast(message);
        }

        signUp(sponsor.getUniqueId());

        chainedHunt = true;
    }

    public void startHunt()
    {
        if (state == HuntState.RUNNING)
            return;

        setState(HuntState.RUNNING);

        String message = worldSettings.getString(Setting.StartMessage);
        message = message.replace("<World>", name);
        message = message.replace("<HuntName>", activeHuntSpecification.getDisplayName());
        Util.Broadcast(message);
        removeHostileMobs();
        
        popSponsorQueue();
        generateScoreboard();
        refreshScoreboards(); 
    }

    public void tryStartSignups()
    {
        if (!sponsorQueue.isEmpty())
            startSignups();
        else
            chainedHunt = false;
    }

    public int getTimeUntilStart()
    {
        int ticksDifference = TimeUtil.getTimeDifference((int) getBukkitWorld().getTime(), worldSettings.getInt(Setting.StartTime));
        if (shouldISkipNextNight())
            ticksDifference += 24000;

        return ticksDifference;
    }

    public int getTimeUntilEnd()
    {
        return TimeUtil.getTimeDifference((int) getBukkitWorld().getTime(), worldSettings.getInt(Setting.EndTime));
    }

    public int getSponsorQueueLength()
    {
        return sponsorQueue.size();
    }

    public void addSponsor(Player player)
    {
        if (isWorldTimeGoodForHunt() || getTimeUntilStart() < worldSettings.getInt(Setting.MinTicksBeforeToStart))
            setShouldSkipNextNight(true);

        sponsorQueue.add(player);
        Settings.globals.setList(Setting.SponsorQueue, sponsorQueue);
        Settings.globals.save();
    }

    public String getCurrentSponsor()
    {
        return currentSponsor;
    }

    public void startInBetweenHuntDelay()
    {
        state = HuntState.SLEEPING;
        scheduleStateChange(HuntState.WAITING_FOR_SPONSOR, worldSettings.getInt(Setting.AfterHuntDelay));
    }

    public HuntState getState()
    {
        return state;
    }

    private void setState(HuntState state)
    {
        this.state = state;

        if (stateSwitchTimer != null)
        {
            stateSwitchTimer.cancel();
            stateSwitchTimer = null;
        }

        if (state == HuntState.WAITING_FOR_SPONSOR)
        {
            tryStartSignups();
        }
    }

    public void scheduleStateChange(HuntState state, int ticksLater)
    {
        if (stateSwitchTimer != null)
            stateSwitchTimer.cancel();

        stateSwitchTimer = new StateChangerRunnable(state);
        stateSwitchTimer.runTaskLater(MonsterHunt.instance, ticksLater);
    }

    public boolean isWorldTimeGoodForHunt()
    {
        int time = (int) getBukkitWorld().getTime();
        return time >= worldSettings.getInt(Setting.StartTime) && time <= worldSettings.getInt(Setting.EndTime);
    }

    public boolean shouldISkipNextNight()
    {
        return skipNextNight;
    }

    public void setShouldSkipNextNight(boolean skipNextNight)
    {
        this.skipNextNight = skipNextNight;
    }

    public boolean shouldIWaitUntilMorning()
    {
        return waitUntilMorning;
    }

    public void setShouldWaitUntilMorning(boolean waitUntilMorning)
    {
        this.waitUntilMorning = waitUntilMorning;
    }

    public boolean isActive()
    {
        return state == HuntState.SIGNUP || state == HuntState.RUNNING;
    }

    private void popSponsorQueue()
    {
    	if (sponsorQueue.size() == 0)
        {
            Log.severe("Starting hunt without sponsor! Error, bug matejdro!");
            return;
        }

        currentSponsor = sponsorQueue.get(0).getName();
        sponsorQueue.remove(0);

    	Settings.globals.setList(Setting.SponsorQueue, sponsorQueue);
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

	private void clearScoreboard(UUID uuid)
	{
		Player player = Bukkit.getPlayer(uuid);
		
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
    	for(UUID uuid : Score.keySet())	
		{
			Player player = MonsterHunt.instance.getServer().getPlayer(uuid);
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
    	for(UUID uuid : Score.keySet())	
		{
			OfflinePlayer offlinePlayer = MonsterHunt.instance.getServer().getOfflinePlayer(uuid);
			if(offlinePlayer != null)
			{
				scoreboard.resetScores(offlinePlayer);
				if (Score.get(uuid) != 0)
				{
					objective.getScore(offlinePlayer).setScore(Score.get(uuid));
				}
			}
 		}
    }
    
    private void setScoreboards(Scoreboard scoreboard)
    {
    	for(UUID uuid : Score.keySet())	
		{
			Player player = MonsterHunt.instance.getServer().getPlayer(uuid);
			if(player != null)
			{
				if (MonsterHunt.coreInstalled)
					CoreScoreboardManager.registerScoreboard(player, 1, scoreboard);
				else
					player.setScoreboard(scoreboard);
			}
 		}
    }
    
    public void stop() {
        if (!isActive())
            return;

        startInBetweenHuntDelay();

        if (Score.size() < worldSettings.getInt(Setting.MinimumPlayers)) {
            String message = worldSettings.getString(Setting.FinishMessageNotEnoughPlayers);
            message = message.replace("<HuntName>", activeHuntSpecification.getDisplayName());
            message = message.replace("<World>", name);
            Util.Broadcast(message);
        }
        else
        {
            RewardManager.RewardWinners(this);
        }

        for (Entry<Player, Location> e : tplocations.entrySet()) {
            Player player = e.getKey();
            if (player == null || !player.isOnline()) {
                continue;
            }
            player.teleport(e.getValue());
        }

        for (UUID uuid : Score.keySet()) {
            Integer hs = InputOutput.getHighScore(uuid);
            if (hs == null)
                hs = 0;
            int score = Score.get(uuid);
            if (score > hs) {
                InputOutput.UpdateHighScore(uuid, score);
                Player player = MonsterHunt.instance.getServer().getPlayer(uuid);
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

    private class StateChangerRunnable extends BukkitRunnable
    {
        private HuntState state;

        public StateChangerRunnable(HuntState state)
        {
            this.state = state;
        }

        public void run()
        {
            setState(state);
        }
    }


	
}