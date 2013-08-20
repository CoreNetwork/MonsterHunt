package com.matejdro.bukkit.monsterhunt.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.matejdro.bukkit.monsterhunt.HuntWorldManager;
import com.matejdro.bukkit.monsterhunt.MonsterHunt;
import com.matejdro.bukkit.monsterhunt.MonsterHuntWorld;
import com.matejdro.bukkit.monsterhunt.Setting;
import com.matejdro.bukkit.monsterhunt.Util;

public class HuntKickCommand implements CommandExecutor 
{
	// /huntkick <player> [<world>]
	
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
	{
		if (args.length == 1)
        {
			Player player = MonsterHunt.instance.getServer().getPlayerExact(args[0]);
			if(player == null)
			{
				Util.Message("There is no player called " + args[0], sender);
				return true;
			}
			for(MonsterHuntWorld world : HuntWorldManager.getWorlds())
			{
				world.kick(player.getName());
				String message = world.settings.getString(Setting.AnnounceKick).replace("<PlayerName>",  player.getName());
	        	Util.Broadcast(message);
			}
        }
		else if (args.length == 2)
		{
			Player player = MonsterHunt.instance.getServer().getPlayerExact(args[0]);
			if(player == null)
			{
				Util.Message("There is no player called " + args[0], sender);
				return true;
			}
			
			MonsterHuntWorld world = HuntWorldManager.getWorld(args[1]);
	        if (world == null || world.getWorld() == null)
	        {
	    		Util.Message("There is no such world!", sender);
	            return true;
	    	}
	        else
	        {
	        	world.kick(player.getName());
				String message = world.settings.getString(Setting.AnnounceKick).replace("<PlayerName>", player.getName());
	        	Util.Broadcast(message);
	        }
		}
		else
		{
			sender.sendMessage("Usage: /huntkick <player> [<world>]");
		}
		
		return true;
	}
	

}
