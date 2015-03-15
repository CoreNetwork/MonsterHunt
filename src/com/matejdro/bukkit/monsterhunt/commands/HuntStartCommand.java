package com.matejdro.bukkit.monsterhunt.commands;

import org.bukkit.command.CommandSender;

import com.matejdro.bukkit.monsterhunt.HuntWorldManager;
import com.matejdro.bukkit.monsterhunt.MonsterHuntWorld;
import com.matejdro.bukkit.monsterhunt.Util;

public class HuntStartCommand extends BaseMHCommand {

	public HuntStartCommand()
	{
		permission = "start";
		desc = "Start hunt now";
		needPlayer = false;
	}


	public void run(CommandSender sender, String[] args){
        if (args.length < 1 && HuntWorldManager.getWorlds().size() == 1) {
            String name = "";
            for (MonsterHuntWorld w : HuntWorldManager.getWorlds())
                name = w.name;
            args = new String[] { name };
        } else if (args.length < 1) {
            Util.Message("Usage: /huntstart [World Name]", sender);
            return;
        } else if (HuntWorldManager.getWorld(args[0]) == null) {
            Util.Message("There is no such world!", sender);
            return;
        }
        MonsterHuntWorld world = HuntWorldManager.getWorld(args[0]);
        world.startHunt();
    }
}
