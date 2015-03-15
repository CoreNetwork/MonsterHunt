package com.matejdro.bukkit.monsterhunt.commands;

import com.matejdro.bukkit.monsterhunt.HuntState;
import java.util.UUID;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.matejdro.bukkit.monsterhunt.HuntWorldManager;
import com.matejdro.bukkit.monsterhunt.MonsterHuntWorld;
import com.matejdro.bukkit.monsterhunt.Setting;
import com.matejdro.bukkit.monsterhunt.Util;

public class HuntJoinCommand extends BaseMHCommand {

	public HuntJoinCommand()
	{
		permission = "join";
		desc = "Signup for hunt (you can also just use /hunt)";
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
    		Util.Message("There is no such world!", sender);
            return;
    	}
        if (world.Score.containsKey(((Player) sender).getName())) {
            Util.Message(world.getSettings().getString(Setting.MessageAlreadySignedUp), sender);
            return;
        }

        UUID playerUUID = ((Player) sender).getUniqueId();
        if (world.getState() == HuntState.SIGNUP)
        {
        	if (world.isBanned(playerUUID))
        	{
        		String message = world.getSettings().getString(Setting.BannedPlayerSignUp);
        		Util.Message(message, sender);
        		return;
        	}

        	if (world.isKicked(playerUUID))
        	{
        		String message = world.getSettings().getString(Setting.KickedPlayerSignUp);
        		Util.Message(message, sender);
        		return;
        	}
        	
            world.signUp(playerUUID);
        	
        	if (world.getSettings().getBoolean(Setting.AnnounceSignUp))
            {
                String message = world.getSettings().getString(Setting.SignUpAnnouncement);
                message = message.replace("<World>", world.name);
                message = message.replace("<Player>", sender.getName());
                Util.Broadcast(message);
            }
            else
            {
                String message = world.getSettings().getString(Setting.SignUpBeforeHuntMessage);
                message = message.replace("<World>", world.name);
                Util.Message(message, sender);
            }

        }
        else
        {
            Util.Message(world.getSettings().getString(Setting.MessageTooLateSignUp), sender);
        }
    }

}
