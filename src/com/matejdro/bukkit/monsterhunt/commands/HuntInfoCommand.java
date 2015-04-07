package com.matejdro.bukkit.monsterhunt.commands;

import com.matejdro.bukkit.monsterhunt.HuntState;
import com.matejdro.bukkit.monsterhunt.TimeUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.matejdro.bukkit.monsterhunt.HuntWorldManager;
import com.matejdro.bukkit.monsterhunt.MonsterHuntWorld;
import com.matejdro.bukkit.monsterhunt.Setting;
import com.matejdro.bukkit.monsterhunt.Settings;
import com.matejdro.bukkit.monsterhunt.Util;

public class HuntInfoCommand extends BaseMHCommand {

	public HuntInfoCommand()
	{
		permission = "info";
		desc = "Check hunt info";
		needPlayer = false;
	}


	public void run(CommandSender sender, String[] args){

        MonsterHuntWorld mainWorld = HuntWorldManager.getWorlds().iterator().next();

        if (!mainWorld.isActive())
        {
            Util.Message(mainWorld.getSettings().getString(Setting.MessageHuntInfoNotActive), sender);
        }
        else if (mainWorld.getState() == HuntState.SIGNUP)
        {
            String message;

            if(!(sender instanceof Player))
                message = mainWorld.getSettings().getString(Setting.MessageHuntInfoSignupsConsole);
            else if (mainWorld.Score.containsKey(((Player) sender).getUniqueId()))
                message = mainWorld.getSettings().getString(Setting.MessageHuntInfoSignupsYouSignedUp);
            else
                message = mainWorld.getSettings().getString(Setting.MessageHuntInfoSignupsYouNotSignedUp);

            message = message.replace("<Time>", TimeUtil.formatTimeTicks(mainWorld.getTimeUntilStart()));
            Util.Message(message, sender);
        }
        else if (mainWorld.getState() == HuntState.RUNNING)
        {
            String message;

            if(!(sender instanceof Player))
                message = mainWorld.getSettings().getString(Setting.MessageHuntInfoSignupsConsole);
            else if (mainWorld.Score.containsKey(((Player) sender).getUniqueId()))
                message = mainWorld.getSettings().getString(Setting.MessageHuntInfoRunningYouSignedUp);
            else
                message = mainWorld.getSettings().getString(Setting.MessageHuntInfoRunningYouNotSignedUp);

            message = message.replace("<Time>", TimeUtil.formatTimeTicks(mainWorld.getTimeUntilEnd()));
            Util.Message(message, sender);
        }
    }
}
