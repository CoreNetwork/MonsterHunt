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

public class HuntUnbanCommand implements CommandExecutor {

	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
	{
		if (args.length == 1)
        {
			OfflinePlayer player = MonsterHunt.instance.getServer().getOfflinePlayer(args[0]);
			if(player != null)
			{
				HuntWorldManager.bannedPlayers.remove(player.getName().toLowerCase());
				InputOutput.unbanPlayer(player.getName().toLowerCase());
				for(MonsterHuntWorld world : HuntWorldManager.getWorlds())
				{
					world.unkick(player.getName());
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
			sender.sendMessage("Usage: /huntunban <player>");
		}
		
		return true;
	}
}
