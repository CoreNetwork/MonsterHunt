package com.matejdro.bukkit.monsterhunt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class HuntWorldManager {
    public static MonsterHuntWorld HuntZoneWorld;
    public static HashMap<String, MonsterHuntWorld> worlds = new HashMap<String, MonsterHuntWorld>();
    public static List<UUID> bannedPlayers = new ArrayList<UUID>();
    
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

    public static void BroadcastToAllParticipants(String message) {
        for(MonsterHuntWorld mhw : HuntWorldManager.getWorlds()) {
            if(mhw.isActive())  {
                for(UUID uuid : mhw.Score.keySet())	{
                    Player player = MonsterHunt.instance.getServer().getPlayer(uuid);
                    if(player != null)
                    {
                        Util.Message(message, player);
                    }
                }
            }
        }
    }


    public static void timer() {
        MonsterHunt.instance.getServer().getScheduler().scheduleSyncRepeatingTask(MonsterHunt.instance, new Runnable() {

            public void run() {
                for (MonsterHuntWorld world : getWorlds()) {
                    if (world == null || world.getBukkitWorld() == null)
                        return;

                    if (world.getState() == HuntState.WAITING_FOR_DAY)
                    {
                        if (!world.isWorldTimeGoodForHunt())
                        {
                            world.setState(HuntState.WAITING_FOR_NIGHT);
                        }
                    }
                    else if (world.getState() == HuntState.WAITING_FOR_NIGHT)
                    {
                        if (!world.isWorldTimeGoodForHunt())
                            return;

                        if (world.getSettings().getInt(Setting.HuntLimit) == 0)
                            return;

                        if (!world.rollStartDie())
                            return;

                        world.startSignups();
                    }
                    else if (world.getState() == HuntState.RUNNING)
                    {
                        if (!world.isWorldTimeGoodForHunt())
                        {
                            world.stop();
                        }
                    }
                }
            }
        }, 200L, 40L);
    }
}
