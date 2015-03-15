package com.matejdro.bukkit.monsterhunt.commands;

import java.util.UUID;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.matejdro.bukkit.monsterhunt.HuntWorldManager;
import com.matejdro.bukkit.monsterhunt.MonsterHuntWorld;
import com.matejdro.bukkit.monsterhunt.Setting;
import com.matejdro.bukkit.monsterhunt.Settings;
import com.matejdro.bukkit.monsterhunt.Util;

public class HuntLeaveCommand extends BaseMHCommand {

	public HuntLeaveCommand()
	{
		permission = "leave";
		desc = "Leave hunt in progress";
		needPlayer = true;
	}


	public void run(CommandSender sender, String[] args){        
    	String worldName = "";
        MonsterHuntWorld world = null;
        
        if (args.length == 0)
        {
        	if (HuntWorldManager.getWorlds().size() == 1) 
        	{
                for (MonsterHuntWorld w : HuntWorldManager.getWorlds())
                	worldName = w.name;
        	}
        	else
        	{
        		worldName = ((Player) sender).getWorld().getName();
        	}
        }
        else if (args.length == 1)
        {
        	worldName = args[0];
        }
        
        world = HuntWorldManager.getWorld(worldName);
        
        if (world == null || world.getBukkitWorld() == null)
        {
    		Util.Message(Settings.globals.getString(Setting.MessageNoHunt), sender);
            return;
    	}
        
        UUID uuid = ((Player) sender).getUniqueId();
        world.kick(uuid);
        world.unkick(uuid);
        
        Util.Message(world.getSettings().getString(Setting.MessageLeftHunt), sender);
    }

}
