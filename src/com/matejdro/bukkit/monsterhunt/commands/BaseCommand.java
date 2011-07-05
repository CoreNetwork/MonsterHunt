package com.matejdro.bukkit.monsterhunt.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.matejdro.bukkit.monsterhunt.Util;

public abstract class BaseCommand {
//Command system concept by oliverw92
	public Boolean needPlayer;
	public String permission;
	
	public abstract Boolean run(CommandSender sender, String[] args);
	
	public Boolean execute(CommandSender sender, String[] args)
	{
		if (!(sender instanceof Player) && needPlayer) return false;
		if (sender instanceof Player && !Util.permission((Player) sender, permission, ((Player) sender).isOp())) return false;
		
		return run(sender, args);
	}

}