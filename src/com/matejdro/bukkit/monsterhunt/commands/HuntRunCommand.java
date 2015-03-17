package com.matejdro.bukkit.monsterhunt.commands;

import com.matejdro.bukkit.monsterhunt.HuntState;
import com.matejdro.bukkit.monsterhunt.TimeUtil;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.matejdro.bukkit.monsterhunt.BougthHuntsStorage;
import com.matejdro.bukkit.monsterhunt.HuntWorldManager;
import com.matejdro.bukkit.monsterhunt.Log;
import com.matejdro.bukkit.monsterhunt.MonsterHuntWorld;
import com.matejdro.bukkit.monsterhunt.Setting;
import com.matejdro.bukkit.monsterhunt.Settings;
import com.matejdro.bukkit.monsterhunt.Util;

public class HuntRunCommand extends BaseMHCommand {

	public HuntRunCommand()
	{
		permission = "run";
		desc = "Run Monster Hunt pass";
		needPlayer = true;
	}


	public void run(CommandSender sender, String[] args)	
	{		
		Player player = (Player) sender;
		String playerName = player.getName();
		
		int huntsLeft = BougthHuntsStorage.getNumberOfBoughtHunts(player.getUniqueId());
		if (huntsLeft <= 0)
		{
			Util.Message(Settings.globals.getString(Setting.MessageCantScheduleHunt), player);
			return;
		}
		
		BougthHuntsStorage.setNumberOfBoughtHunts(player.getUniqueId(), --huntsLeft);
		
		MonsterHuntWorld mainWorld = HuntWorldManager.getWorlds().iterator().next();
        mainWorld.addSponsor(player);

        int numSponsorsBeforeMe = mainWorld.getSponsorQueueLength() - 1;
		int timeUntilMyHuntStarts = mainWorld.getTimeUntilStart() + numSponsorsBeforeMe * 24000;
		
		String timeLeftString = TimeUtil.formatTimeTicks(timeUntilMyHuntStarts);
		
		if (numSponsorsBeforeMe == 0)
		{
			String buyerMessage = mainWorld.getSettings().getString(Setting.MessageHuntScheduled);
			buyerMessage = buyerMessage.replace("<Time>", timeLeftString);
			
			String announcementMessage = mainWorld.getSettings().getString(Setting.MessageHuntScheduledAnnouncement);
			announcementMessage = announcementMessage.replace("<Player>", playerName);
			announcementMessage = announcementMessage.replace("<Time>", timeLeftString);
			
			Util.Message(buyerMessage, player);
			Util.Broadcast(announcementMessage, playerName);
			Log.info(announcementMessage);

		}
		else
		{
			String buyerMessage = mainWorld.getSettings().getString(Setting.MessageHuntScheduledMultiple);
			buyerMessage = buyerMessage.replace("<Time>", timeLeftString);
			buyerMessage = buyerMessage.replace("<QueueLength>", Integer.toString(numSponsorsBeforeMe));

			String announcementMessage = mainWorld.getSettings().getString(Setting.MessageHuntScheduledMultipleAnnouncement);
			announcementMessage = announcementMessage.replace("<Player>", playerName);
			announcementMessage = announcementMessage.replace("<Time>", timeLeftString);
			announcementMessage = announcementMessage.replace("<QueueLength>", Integer.toString(numSponsorsBeforeMe));
			
			Util.Message(buyerMessage, player);
			Util.Broadcast(announcementMessage, playerName);
			Log.info(announcementMessage);
		}

        mainWorld.tryStartSignups();
    }

}
