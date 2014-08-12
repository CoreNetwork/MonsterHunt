package com.matejdro.bukkit.monsterhunt.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.matejdro.bukkit.monsterhunt.HuntWorldManager;
import com.matejdro.bukkit.monsterhunt.InputOutput;
import com.matejdro.bukkit.monsterhunt.MonsterHunt;
import com.matejdro.bukkit.monsterhunt.MonsterHuntWorld;
import com.matejdro.bukkit.monsterhunt.Setting;
import com.matejdro.bukkit.monsterhunt.Settings;
import com.matejdro.bukkit.monsterhunt.Util;

public class HuntBanCommand extends BaseMHCommand {

	public HuntBanCommand()
	{
		permission = "ban";
		desc = "Ban player from the hunt";
		needPlayer = false;
	}


	public void run(CommandSender sender, String[] args)	
	{
		if (args.length == 1 || args.length == 2)
        {
			String reason = "";
			if(args.length == 2)
				reason = args[1];
				
			Player player = MonsterHunt.instance.getServer().getPlayerExact(args[0]);
			if(player != null)
			{
				for(MonsterHuntWorld world : HuntWorldManager.getWorlds())
				{
					world.kick(player.getName());
				}
				HuntWorldManager.bannedPlayers.add(player.getName().toLowerCase());
				InputOutput.banPlayer(player.getName().toLowerCase(), reason);
				
				String message = Settings.globals.config.getString(Setting.AnnounceBan.getString());
				message = message.replace("<PlayerName>", player.getName());
				message = message.replace("<Reason>", reason);
	        	Util.Broadcast(message);
			}
			else
			{
				Util.Message("There is no player called " + args[0], sender);
			}
        }
		else
		{
			sender.sendMessage("Usage: /huntban <player> [<reason>]");
		}
	}
}
