package com.matejdro.bukkit.monsterhunt.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.matejdro.bukkit.monsterhunt.HuntWorldManager;
import com.matejdro.bukkit.monsterhunt.MonsterHuntWorld;
import com.matejdro.bukkit.monsterhunt.Setting;
import com.matejdro.bukkit.monsterhunt.Util;

public class HuntSignupCommand extends BaseMHCommand {

	public HuntSignupCommand()
	{
		permission = "signup";
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
        
        if (world == null || world.getWorld() == null)
        {
    		Util.Message("There is no such world!", sender);
            return;
    	}
        if (world.Score.containsKey(((Player) sender).getName())) {
            Util.Message(world.worldSettings.getString(Setting.MessageAlreadySignedUp), sender);
            return;
        }

        String playerName = ((Player) sender).getName();
        if (world.state < 2) {
        	
        	if (world.isBanned(playerName))
        	{
        		String message = world.worldSettings.getString(Setting.BannedPlayerSignUp);
        		Util.Message(message, sender);
        		return;
        	}
        	if (world.isKicked(playerName))
        	{
        		String message = world.worldSettings.getString(Setting.KickedPlayerSignUp);
        		Util.Message(message, sender);
        		return;
        	}
        	
            world.signUp(playerName);
        	
        	if (world.worldSettings.getBoolean(Setting.AnnounceSignUp)) {
                String message = world.worldSettings.getString(Setting.SignUpAnnouncement);
                message = message.replace("<World>", world.name);
                message = message.replace("<Player>", ((Player) sender).getName());
                Util.Broadcast(message);
            } else {
                String message = world.worldSettings.getString(Setting.SignUpBeforeHuntMessage);
                message = message.replace("<World>", world.name);
                Util.Message(message, sender);
            }

        } else if (world.state == 2 && (world.getSignUpPeriodTime() == 0 || world.worldSettings.getBoolean(Setting.AllowSignUpAfterStart))) {
        	
        	if (world.isKicked(playerName))
        	{
        		String message = world.worldSettings.getString(Setting.KickedPlayerSignUp);
        		Util.Message(message, sender);
        		return;
        	}
        	if (world.isBanned(playerName))
        	{
        		String message = world.worldSettings.getString(Setting.BannedPlayerSignUp);
        		Util.Message(message, sender);
        		return;
        	}
        	
        	 world.signUp(playerName);
        	
        	if (world.worldSettings.getBoolean(Setting.AnnounceSignUp)) {
                String message = world.worldSettings.getString(Setting.SignUpAnnouncement);
                message = message.replace("<World>", world.name);
                message = message.replace("<Player>", ((Player) sender).getName());
                Util.Broadcast(message);
            } else {
                String message = world.worldSettings.getString(Setting.SignUpAfterHuntMessage);
                message = message.replace("<World>", world.name);
                Util.Message(message, sender);
            }            
        } else {
            Util.Message(world.worldSettings.getString(Setting.MessageTooLateSignUp), sender);
        }
    }

}
