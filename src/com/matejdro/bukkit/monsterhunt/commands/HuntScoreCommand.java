package com.matejdro.bukkit.monsterhunt.commands;

import java.util.LinkedHashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.matejdro.bukkit.monsterhunt.InputOutput;
import com.matejdro.bukkit.monsterhunt.Util;

public class HuntScoreCommand extends BaseMHCommand {
	
	public HuntScoreCommand()
	{
		permission = "score";
		desc = "Check your highscore";
		needPlayer = true;
	}


	public void run(CommandSender sender, String[] args)
	{
        if (args.length > 0 && args[0].equals("rank")) {
            Integer rank = InputOutput.getHighScoreRank(((Player) sender).getUniqueId());
            if (rank != null)
                Util.Message("Your current high score rank is " + String.valueOf(rank), sender);
            else
                Util.Message("You do not have your high score yet.", sender);
        } else if (args.length > 0 && args[0].equals("top")) {
            Integer number = 5;
            if (args.length > 1)
                number = Integer.parseInt(args[1]);

            LinkedHashMap<UUID, Integer> tops = InputOutput.getTopScores(number);
            Util.Message("Top high scores:", sender);
            int counter = 0;
            for (UUID player : tops.keySet()) {
                counter++;
                String rank = String.valueOf(counter);
                String score = String.valueOf(tops.get(player));
                String playerName = Bukkit.getOfflinePlayer(player).getName();
                
                Util.Message(rank + ". &6" + playerName + "&f - &a" + score + "&f points", sender);
            }
        } else if (args.length > 0) {
        	UUID uuid = Bukkit.getOfflinePlayer(args[0]).getUniqueId();
            Integer score = InputOutput.getHighScore(uuid);
            if (score != null)
                Util.Message("High score of player &6" + args[0] + "&f is &6" + String.valueOf(score) + "&f points.", sender);
            else
                Util.Message("Player &6" + args[0] + "&f do not have high score yet.", sender);
        } else {
            Integer score = InputOutput.getHighScore(((Player) sender).getUniqueId());
            if (score != null)
                Util.Message("Your high score is &6" + String.valueOf(score) + "&f points.", sender);
            else
                Util.Message("You do not have your high score yet.", sender);
        }
    }

}
