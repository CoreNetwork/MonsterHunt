package com.matejdro.bukkit.monsterhunt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;

public class HuntWorldManager {
    public static MonsterHuntWorld HuntZoneWorld;
    public static HashMap<String, MonsterHuntWorld> worlds = new HashMap<String, MonsterHuntWorld>();
    public static List<String> bannedPlayers = new ArrayList<String>();
    
    public static MonsterHuntWorld getWorld(String name) {
        if (Settings.globals.config.getBoolean(Setting.HuntZoneMode.getString(), false)) {
            return HuntZoneWorld;
        } else {
            return worlds.get(name);
        }
    }

    public static Collection<MonsterHuntWorld> getWorlds() {
        if (Settings.globals.config.getBoolean(Setting.HuntZoneMode.getString(), false)) {
            ArrayList<MonsterHuntWorld> list = new ArrayList<MonsterHuntWorld>();
            list.add(HuntZoneWorld);
            return list;
        } else {
            return worlds.values();
        }
    }

    public static void timer() {
        MonsterHunt.instance.getServer().getScheduler().scheduleSyncRepeatingTask(MonsterHunt.instance, new Runnable() {

            public void run() {
                for (MonsterHuntWorld world : getWorlds()) {
                    if (world == null || world.getWorld() == null)
                        return;
                    long time = world.getWorld().getTime();
                    
                    if (world.state == 0 && time < world.worldSettings.getInt(Setting.StartTime) && time > world.getSignUpPeriodTime() && world.getSignUpPeriodTime() > 0 && !world.manual && !world.waitday) {
                        if (world.canStart()) {
                            world.state = 1;
                            String message = world.worldSettings.getString(Setting.MessageSignUpPeriod);
                            message = message.replace("<World>", world.name);
                            message = message.replace("<HuntName>", world.activeHuntSpecification.getDisplayName());
                            Util.Broadcast(message);
                        }
                        world.waitday = true;

                    } else if (world.state < 2 && time > world.worldSettings.getInt(Setting.StartTime) && time < world.worldSettings.getInt(Setting.EndTime) && !world.manual) {
                        if (world.state == 1) {
                            if (world.Score.size() < world.worldSettings.getInt(Setting.MinimumPlayers) && world.worldSettings.getBoolean(Setting.EnableSignup)) {
                                Util.Broadcast(world.worldSettings.getString(Setting.MessageStartNotEnoughPlayers));
                                world.state = 0;
                                world.Score.clear();
                                world.waitday = true;
                                world.skipNight();
                            } else {
	                             world.start();
                            }
                        } else if (!world.waitday && world.worldSettings.getInt(Setting.SignUpPeriodTime) == 0) {
                            world.waitday = true;
                            if (world.canStart())
                                world.start();
                        }
                    } 
                    else if (world.state == 2 && (time > world.worldSettings.getInt(Setting.EndTime) || time < world.worldSettings.getInt(Setting.StartTime)) && !world.manual) {
                        Util.Debug("[DEBUG - NEVEREND]Stop Time");
                        world.stop();
                    } else if (world.waitday && (time > world.worldSettings.getInt(Setting.EndTime) || time < world.worldSettings.getInt(Setting.StartTime) - world.getSignUpPeriodTime())) {
                        world.waitday = false;
                    }
                }
            }
        }, 200L, 40L);
    }
}
