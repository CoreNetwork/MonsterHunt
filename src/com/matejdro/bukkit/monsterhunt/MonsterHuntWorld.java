package com.matejdro.bukkit.monsterhunt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Blaze;
import org.bukkit.entity.CaveSpider;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.MagmaCube;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Spider;
import org.bukkit.entity.Zombie;

public class MonsterHuntWorld {
    public String name;
    public boolean manual;
    public int state;
    public boolean waitday;
    public int curday;
    public boolean nextnight;
    public boolean purgeDone; //true if killHostileMobs already occured when in state = 1
    public Settings settings;

    public HashMap<String, Integer> Score = new HashMap<String, Integer>();
    public HashMap<String, Integer> lastScore = new HashMap<String, Integer>();
    public ArrayList<Integer> properlyspawned = new ArrayList<Integer>();
    public ArrayList<Integer> blacklist = new ArrayList<Integer>();
    public HashMap<Player, Location> tplocations = new HashMap<Player, Location>();

    public MonsterHuntWorld(String w) {
        state = 0;
        waitday = false;
        manual = false;
        curday = 0;
        name = w;
        purgeDone = false;
    }

    public World getWorld() {
        return MonsterHunt.instance.getServer().getWorld(name);
    }

    public int getSignUpPeriodTime() {
        int time = settings.getInt(Setting.SignUpPeriodTime);
        if (time != 0) {
            time = settings.getInt(Setting.StartTime) - settings.getInt(Setting.SignUpPeriodTime) * 1200;
            if (time < 0) {
                Log.warning("Wrong SignUpPeriodTime Configuration! Sign Up period will be disabled!");
                time = 0;
            }
        }
        return time;
    }

    public void start() {
        String message = settings.getString(Setting.StartMessage);
        message = message.replace("<World>", name);
        Util.Broadcast(message);
        state = 2;
        waitday = true;
        
        
    }

    public void stop() {
        if (state < 2) {
            return;
        }
        if (Score.size() < settings.getInt(Setting.MinimumPlayers)) {
            String message = settings.getString(Setting.FinishMessageNotEnoughPlayers);
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
        purgeDone = false;
        for (String i : Score.keySet()) {
            Integer hs = InputOutput.getHighScore(i);
            if (hs == null)
                hs = 0;
            int score = Score.get(i);
            if (score > hs) {
                InputOutput.UpdateHighScore(i, score);
                Player player = MonsterHunt.instance.getServer().getPlayer(i);
                if (player != null) {
                    String message = settings.getString(Setting.HighScoreMessage);
                    message = message.replace("<Points>", String.valueOf(score));
                    Util.Message(message, player);
                }
            }
        }
        lastScore.putAll(Score);
        Score.clear();
        properlyspawned.clear();
    }

    public void skipNight() {
        if (settings.getInt(Setting.SkipToIfFailsToStart) >= 0) {
            getWorld().setTime(settings.getInt(Setting.SkipToIfFailsToStart));
        }
    }

    public boolean canStart() {
        if (curday == 0) {
            curday = settings.getInt(Setting.SkipDays);
            if ((new Random().nextInt(100)) < settings.getInt(Setting.StartChance)) {
                return true;
            }
        } else {
            curday--;
        }
        return false;
    }
    
    public void removeHostileMobs()
    {
    	purgeDone = true;
    	if(settings.getBoolean(Setting.PurgeAllHostileMobsOnStart))
        {
    		Class[] classes = {Creeper.class, Skeleton.class,Zombie.class, Spider.class, Enderman.class, Ghast.class, Slime.class
    							, Blaze.class, CaveSpider.class, MagmaCube.class, PigZombie.class};
        	Collection<Entity> mobs = MonsterHunt.instance.getServer().getWorld(name).getEntitiesByClasses(classes);
        	for(Entity e : mobs)
        		e.remove();
        }	
    }
}