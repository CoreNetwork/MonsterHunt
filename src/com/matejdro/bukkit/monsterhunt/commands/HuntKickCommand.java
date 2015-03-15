package com.matejdro.bukkit.monsterhunt.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.matejdro.bukkit.monsterhunt.HuntWorldManager;
import com.matejdro.bukkit.monsterhunt.MonsterHunt;
import com.matejdro.bukkit.monsterhunt.MonsterHuntWorld;
import com.matejdro.bukkit.monsterhunt.Setting;
import com.matejdro.bukkit.monsterhunt.Util;

public class HuntKickCommand extends BaseMHCommand 
{
	public HuntKickCommand()
	{
		permission = "kick";
		desc = "Kick player from the hunt";
		needPlayer = false;
	}


	public void run(CommandSender sender, String[] args)
	{
		if (args.length == 1)
        {
			Player player = MonsterHunt.instance.getServer().getPlayerExact(args[0]);
			if(player == null)
			{
				Util.Message("There is no player called " + args[0], sender);
				return;
			}
			for(MonsterHuntWorld world : HuntWorldManager.getWorlds())
			{
				world.kick(player.getUniqueId());
				String message = world.getSettings().getString(Setting.AnnounceKick).replace("<PlayerName>",  player.getName());
	        	Util.Broadcast(message);
			}
        }
		else if (args.length == 2)
		{
			Player player = MonsterHunt.instance.getServer().getPlayerExact(args[0]);
			if(player == null)
			{
				Util.Message("There is no player called " + args[0], sender);
				return;
			}
			
			MonsterHuntWorld world = HuntWorldManager.getWorld(args[1]);
	        if (world == null || world.getBukkitWorld() == null)
	        {
	    		Util.Message("There is no such world!", sender);
	            return;
	    	}
	        else
	        {
	        	world.kick(player.getUniqueId());
				String message = world.getSettings().getString(Setting.AnnounceKick).replace("<PlayerName>", player.getName());
	        	Util.Broadcast(message);
	        }
		}
		else
		{
			sender.sendMessage("Usage: /hunt kick <player> [<world>]");
		}
	}
	

}
