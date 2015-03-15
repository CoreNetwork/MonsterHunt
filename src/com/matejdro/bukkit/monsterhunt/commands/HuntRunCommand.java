package com.matejdro.bukkit.monsterhunt.commands;

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



        int timeLeftUntilNextNight = TimeUtil.getTimeDifference((int) mainWorld.getBukkitWorld().getTime(), mainWorld.getSettings().getInt(Setting.StartTime));
        if (mainWorld.isWorldTimeGoodForHunt() || timeLeftUntilNextNight < mainWorld.getSettings().getInt(Setting.MinTicksBeforeToStart))
        {
            mainWorld.setShouldSkipNextNight(true);
        }

        Util.Debug("Time left until night: " + timeLeftUntilNextNight);

        int queueLength = Settings.globals.getInt(Setting.HuntLimit);
		Settings.globals.setInt(Setting.HuntLimit, ++queueLength);
		Settings.globals.save();
		
		World world = mainWorld.getBukkitWorld();
		
		int timeLeftOneNight = (int) (mainWorld.getSettings().getInt(Setting.StartTime) - world.getTime());
		if (timeLeftOneNight < 0)
			timeLeftOneNight += 24000;
		
		int timeLeft = (queueLength - 1) * 24000 + timeLeftOneNight;
		int timeLeftSeconds = timeLeft / 20;
		String timeLeftString = formatTimeSeconds(timeLeftSeconds);
		
		if (queueLength == 1)
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
			buyerMessage = buyerMessage.replace("<QueueLength>", Integer.toString(queueLength));

			String announcementMessage = mainWorld.getSettings().getString(Setting.MessageHuntScheduledMultipleAnnouncement);
			announcementMessage = announcementMessage.replace("<Player>", playerName);
			announcementMessage = announcementMessage.replace("<Time>", timeLeftString);
			announcementMessage = announcementMessage.replace("<QueueLength>", Integer.toString(queueLength));
			
			Util.Message(buyerMessage, player);
			Util.Broadcast(announcementMessage, playerName);
			Log.info(announcementMessage);
		}
	}
	
	private static String formatTimeSeconds(int seconds)
	{
		if (seconds > 60)
			return formatTimeMinutes((int) Math.round(seconds / 60.0));
		
		String out = Integer.toString(seconds).concat(" second");
		if (seconds != 1)
			out = out.concat("s");
		return out;
	}
	
	private static String formatTimeMinutes(int minutes)
	{
		if (minutes > 60)
			return formatTimeHours((int) Math.round(minutes / 60.0));
	
		String out = Integer.toString(minutes).concat(" minute");
		if (minutes != 1)
			out = out.concat("s");
		return out;

	}
	
	
	private static String formatTimeHours(int hours)
	{
		String out = Integer.toString(hours).concat(" hour");
		if (hours != 1)
			out = out.concat("s");
		return out;

	}
}
