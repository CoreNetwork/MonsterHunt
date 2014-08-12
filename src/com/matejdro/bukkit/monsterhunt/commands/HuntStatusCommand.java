package com.matejdro.bukkit.monsterhunt.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
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
            if (world.state > 0) {
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
        if (world == null || world.getWorld() == null)
            return;

        if (world.state == 0) {

            if (world.lastScore.containsKey(player.getName()))
                Util.Message(world.worldSettings.getString(Setting.MessageHuntStatusLastScore).replace("<Points>", String.valueOf(world.lastScore.get(player.getName()))), player);
            else
                Util.Message(world.worldSettings.getString(Setting.MessageHuntStatusNotInvolvedLastHunt), player);
        } else if (world.state == 2) {
            if (world.Score.containsKey(player.getName())) {
                if (world.Score.get(player.getName()) == 0)
                    Util.Message(world.worldSettings.getString(Setting.MessageHuntStatusNoKills), player);
                else
                    Util.Message(world.worldSettings.getString(Setting.MessageHuntStatusCurrentScore).replace("<Points>", String.valueOf(world.Score.get(player.getName()))), player);
            }
            if (world.worldSettings.getBoolean(Setting.TellTime) && !world.manual) {
                int timediff = world.worldSettings.getInt(Setting.EndTime) - world.worldSettings.getInt(Setting.StartTime);
                long time = player.getWorld().getTime();
                long curdiff = (time - world.worldSettings.getInt(Setting.StartTime)) * 100;
                double calc = curdiff / timediff;
                int curpercent = (int) (100 - Math.round(calc));
                curpercent += 100;
                curpercent /= 1;
                Util.Message(world.worldSettings.getString(Setting.MessageHuntStatusTimeReamining).replace("<Timeleft>", String.valueOf(curpercent)), player);
            }
        }
    }
}
