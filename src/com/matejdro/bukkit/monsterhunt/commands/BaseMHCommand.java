package com.matejdro.bukkit.monsterhunt.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.matejdro.bukkit.monsterhunt.Setting;
import com.matejdro.bukkit.monsterhunt.Settings;
import com.matejdro.bukkit.monsterhunt.Util;


public abstract class BaseMHCommand {
	public Boolean needPlayer;
	public String desc;
	public String permission;

	public abstract void run(CommandSender sender, String[] args);

	public Boolean execute(CommandSender sender, String[] args)
	{
		if (args.length > 0 && !Util.isInteger(args[0]))
		{
			String[] newargs = new String[args.length - 1];
			for (int i = 1; i < args.length; i++)
			{
				newargs[i - 1] = args[i];
			}
			args = newargs;			
		}

		if (!(sender instanceof Player) && needPlayer) 
		{
			Util.Message("Sorry, but you need to execute this command as player.", sender);
			return true;
		}
		if (sender instanceof Player && !Util.hasPermission(sender,"monsterhunt.command." + permission)) 
		{
			Util.Message(Settings.globals.getString(Setting.MessageNoPermission), sender);
			return true;
		}

		run(sender, args);
		return true;
	}

}