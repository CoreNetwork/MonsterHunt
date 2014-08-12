package com.matejdro.bukkit.monsterhunt.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.matejdro.bukkit.monsterhunt.HuntZoneCreation;

public class HuntZoneCommand extends BaseMHCommand {

	public HuntZoneCommand()
	{
		permission = "zone";
		desc = "Setup hunt zone";
		needPlayer = true;
	}


	public void run(CommandSender sender, String[] args){
        HuntZoneCreation.selectstart((Player) sender);
    }

}
