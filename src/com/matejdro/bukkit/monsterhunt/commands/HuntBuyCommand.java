package com.matejdro.bukkit.monsterhunt.commands;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.matejdro.bukkit.monsterhunt.BougthHuntsStorage;
import com.matejdro.bukkit.monsterhunt.Log;
import com.matejdro.bukkit.monsterhunt.Setting;
import com.matejdro.bukkit.monsterhunt.Settings;
import com.matejdro.bukkit.monsterhunt.Util;

public class HuntBuyCommand extends BaseMHCommand {

	public HuntBuyCommand()
	{
		permission = "buy";
		desc = "Buy hunts for player";
		needPlayer = false;
	}


	public void run(CommandSender sender, String[] args)	
	{
		if (args.length < 1)
        {
			sender.sendMessage("Usage: /hunt buy <player> [<reason>]");
			return;
        }
		
		int amount = 1;
		if (args.length > 1 && Util.isInteger(args[1]))
		{
			amount = Integer.parseInt(args[1]);
		}
		
		String playerName = args[0];
		OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
		UUID playerUUID = offlinePlayer.getUniqueId();
		
		int totalHunts = BougthHuntsStorage.getNumberOfBoughtHunts(playerUUID) + amount;
		BougthHuntsStorage.setNumberOfBoughtHunts(playerUUID, totalHunts);
		
		Player player = offlinePlayer.getPlayer();
		if (player != null)
		{
			String message = Settings.globals.getString(Setting.MessageBoughtHunts);
			message = message.replace("<Amount>", Integer.toString(amount));
			
			if (amount == 1)
				message = message.replace("<PluralS>", "");
			else
				message = message.replace("<PluralS>", "s");
			
			message = message.replace("<TotalHunts>", Integer.toString(totalHunts));
			Util.Message(message, player);
		}
	
		Log.info("Player " + playerName + " bought " + amount + " hunts.");
		
	}
}
