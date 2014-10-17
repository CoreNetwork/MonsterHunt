package com.matejdro.bukkit.monsterhunt.commands;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.matejdro.bukkit.monsterhunt.HuntWorldManager;
import com.matejdro.bukkit.monsterhunt.InputOutput;
import com.matejdro.bukkit.monsterhunt.MonsterHunt;
import com.matejdro.bukkit.monsterhunt.MonsterHuntWorld;
import com.matejdro.bukkit.monsterhunt.Util;

public class HuntUnbanCommand extends BaseMHCommand {

	public HuntUnbanCommand()
	{
		permission = "unban";
		desc = "Unban player from the hunt";
		needPlayer = false;
	}


	public void run(CommandSender sender, String[] args)
	{
		if (args.length == 1)
        {
			OfflinePlayer player = MonsterHunt.instance.getServer().getOfflinePlayer(args[0]);
			if(player != null)
			{
				HuntWorldManager.bannedPlayers.remove(player.getName().toLowerCase());
				InputOutput.unbanPlayer(player.getUniqueId());
				for(MonsterHuntWorld world : HuntWorldManager.getWorlds())
				{
					world.unkick(player.getUniqueId());
				}
				
				Util.Message("Player " + args[0] + " unbanned.", sender);
			}
			else
			{
				Util.Message("There is no player called " + args[0], sender);
			}
        }
		else
		{
			sender.sendMessage("Usage: /hunt unban <player>");
		}
	}
}
