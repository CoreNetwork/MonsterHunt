package com.matejdro.bukkit.monsterhunt.commands;

import org.bukkit.command.CommandSender;

import com.matejdro.bukkit.monsterhunt.InputOutput;
import com.matejdro.bukkit.monsterhunt.Util;

public class HuntReloadCommand extends BaseMHCommand {
	
	public HuntReloadCommand()
	{
		permission = "reload";
		desc = "Reload hunt configuration";
		needPlayer = false;
	}


	public void run(CommandSender sender, String[] args) {
		InputOutput.ReloadSettings();
		Util.Message("Settings reloaded.", sender);
	}

}
