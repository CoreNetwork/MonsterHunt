package com.matejdro.bukkit.monsterhunt.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.matejdro.bukkit.monsterhunt.HuntWorldManager;
import com.matejdro.bukkit.monsterhunt.MonsterHuntWorld;
import com.matejdro.bukkit.monsterhunt.Setting;
import com.matejdro.bukkit.monsterhunt.Settings;
import com.matejdro.bukkit.monsterhunt.Util;

public class HuntStopCommand extends BaseMHCommand {

	public HuntStopCommand()
	{
		permission = "stop";
		desc = "Stop hunt now";
		needPlayer = false;
	}


	public void run(CommandSender sender, String[] args){
        if (args.length < 1 && Settings.globals.config.getBoolean(Setting.HuntZoneMode.getString(), false)) {
            args = new String[] { "something" };
        } else if (args.length < 1) {
            Util.Message("Usage: /hunt stop [World Name]", sender);
            return;
        } else if (HuntWorldManager.getWorld(args[0]) == null) {
            Util.Message("There is no such world!", sender);
            return;
        }
        MonsterHuntWorld world = HuntWorldManager.getWorld(args[0]);
        world.stop();
    }

}
