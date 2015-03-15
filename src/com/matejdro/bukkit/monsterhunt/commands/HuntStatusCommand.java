package com.matejdro.bukkit.monsterhunt.commands;

import com.matejdro.bukkit.monsterhunt.HuntState;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.matejdro.bukkit.monsterhunt.HuntWorldManager;
import com.matejdro.bukkit.monsterhunt.MonsterHuntWorld;
import com.matejdro.bukkit.monsterhunt.Setting;
import com.matejdro.bukkit.monsterhunt.Settings;
import com.matejdro.bukkit.monsterhunt.Util;

public class HuntStatusCommand extends BaseMHCommand {

	public HuntStatusCommand()
	{
		permission = "status";
		desc = "Check hunt status";
		needPlayer = true;
	}


	public void run(CommandSender sender, String[] args){
        Player player = (Player) sender;
        boolean anyactive = false;
        String actives = "";
        for (MonsterHuntWorld world : HuntWorldManager.getWorlds()) {
            if (world.isActive()) {
                anyactive = true;
                actives += world.name + ",";
            }
        }
        if (!anyactive) {
            Util.Message(Settings.globals.config.getString(Setting.MessageHuntStatusNotActive.getString()), player);
        } else {
            Util.Message(Settings.globals.config.getString(Setting.MessageHuntStatusHuntActive.getString()).replace("<Worlds>", actives.substring(0, actives.length() - 1)), player);
        }
        MonsterHuntWorld world = HuntWorldManager.getWorld(player.getWorld().getName());
        if (world == null || world.getBukkitWorld() == null)
            return;

        if (world.getState() == HuntState.SIGNUP) {

            if (world.lastScore.containsKey(player.getUniqueId()))
                Util.Message(world.getSettings().getString(Setting.MessageHuntStatusLastScore).replace("<Points>", String.valueOf(world.lastScore.get(player.getUniqueId()))), player);
            else
                Util.Message(world.getSettings().getString(Setting.MessageHuntStatusNotInvolvedLastHunt), player);
        } else if (world.getState() == HuntState.RUNNING) {
            if (world.Score.containsKey(player.getUniqueId())) {
                if (world.Score.get(player.getUniqueId()) == 0)
                    Util.Message(world.getSettings().getString(Setting.MessageHuntStatusNoKills), player);
                else
                    Util.Message(world.getSettings().getString(Setting.MessageHuntStatusCurrentScore).replace("<Points>", String.valueOf(world.Score.get(player.getUniqueId()))), player);
            }
            if (world.getSettings().getBoolean(Setting.TellTime)) {
                int timediff = world.getSettings().getInt(Setting.EndTime) - world.getSettings().getInt(Setting.StartTime);
                long time = player.getWorld().getTime();
                long curdiff = (time - world.getSettings().getInt(Setting.StartTime)) * 100;
                double calc = curdiff / timediff;
                int curpercent = (int) (100 - Math.round(calc));
                curpercent += 100;
                curpercent /= 1;
                Util.Message(world.getSettings().getString(Setting.MessageHuntStatusTimeReamining).replace("<Timeleft>", String.valueOf(curpercent)), player);
            }
        }
    }
}
