package com.matejdro.bukkit.monsterhunt.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.matejdro.bukkit.monsterhunt.BougthHuntsStorage;
import com.matejdro.bukkit.monsterhunt.Setting;
import com.matejdro.bukkit.monsterhunt.Settings;
import com.matejdro.bukkit.monsterhunt.Util;

public class HuntCheckCommand extends BaseMHCommand {

	public HuntCheckCommand()
	{
		permission = "check";
		desc = "Check amount of hunts you have left to use";
		needPlayer = true;
	}


	public void run(CommandSender sender, String[] args)	
	{		
		Player player = (Player) sender;
		
		int totalHunts = BougthHuntsStorage.getNumberOfBoughtHunts(player.getUniqueId());
		String message = Settings.globals.getString(Setting.MessageCheckHunts);
		
		if (totalHunts == 1)
			message = message.replace("<PluralS>", "");
		else
			message = message.replace("<PluralS>", "s");
		
		message = message.replace("<TotalHunts>", Integer.toString(totalHunts));
		Util.Message(message, player);
		
	}
}
