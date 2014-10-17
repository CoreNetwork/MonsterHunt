package com.matejdro.bukkit.monsterhunt.commands;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.matejdro.bukkit.monsterhunt.InputOutput;
import com.matejdro.bukkit.monsterhunt.RewardManager;
import com.matejdro.bukkit.monsterhunt.Setting;
import com.matejdro.bukkit.monsterhunt.Settings;
import com.matejdro.bukkit.monsterhunt.Util;

public class HuntRewardCommand extends BaseMHCommand {

    public HuntRewardCommand()
	{
		permission = "reward";
		desc = "Claim your hunt rewards";
		needPlayer = true;
	}


	public void run(CommandSender sender, String[] args)
	{
        boolean anythingClaimed = false;
        Player player = (Player) sender;
        
        try
        {
        	PreparedStatement statement = InputOutput.getConnection().prepareStatement("SELECT * FROM monsterhunt_RewardsToClaim WHERE PlayerName = ?");
        	statement.setString(1, player.getUniqueId().toString());
        	
        	ResultSet set = statement.executeQuery();
        	while (set.next())
        	{
        		anythingClaimed = true;
        		
        		String huntName = set.getString("HuntName");
        		String rewardType = set.getString("RewardType");
        		int score = set.getInt("Score");
        		
        		Settings huntSettings = new Settings(new File("plugins" + File.separator + "MonsterHunt" + File.separator, huntName + ".yml"), Settings.globals);
        		RewardManager.giveItems(player, rewardType, huntSettings, score);
        	}
        }
        catch (SQLException e)
        {
        	e.printStackTrace();
        }
        
        if (!anythingClaimed)
        {
        	Util.Message(Settings.globals.getString(Setting.MessageNoRewardToClaim), sender);
        	return;
        }
    
        try
        {
        	PreparedStatement statement = InputOutput.getConnection().prepareStatement("DELETE FROM monsterhunt_RewardsToClaim WHERE PlayerName = ?");
        	statement.setString(1, player.getUniqueId().toString());

        	statement.executeUpdate();
        	statement.close();
        	InputOutput.getConnection().commit();
        }
        catch (SQLException e)
        {
        	e.printStackTrace();
        }
        
        return;
    }

}
