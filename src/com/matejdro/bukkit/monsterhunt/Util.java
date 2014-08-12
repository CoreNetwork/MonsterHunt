package com.matejdro.bukkit.monsterhunt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Util {

	private static Map<String, Long> lastSpamMessage = new HashMap<String, Long>();
	private static final int SPAM_MESSAGE_INTERVAL = 5000;
    public static void Message(String message, CommandSender sender) {
        if (sender instanceof Player) {
            Message(message, (Player) sender);
        } else {
            sender.sendMessage(message);
        }
    }

    private static final Pattern newLinePattern = Pattern.compile("\\s?\\[NEWLINE\\]\\s?");
    public static void Message(String message, Player player) {
    	message = ChatColor.translateAlternateColorCodes('&', message);
        String[] lines = newLinePattern.split(message);
        for(String line : lines) {
            player.sendMessage(line);
        }
    }
    
    public static void SpamMessage(String message, Player player)
    {
    	Long lastMessageTime = lastSpamMessage.get(player.getName());
    	if(lastMessageTime == null || System.currentTimeMillis() - lastMessageTime > SPAM_MESSAGE_INTERVAL)
    	{
    		Message(message, player);
    		lastSpamMessage.put(player.getName(), System.currentTimeMillis());
    	}
    }
    
    /**
     * @deprecated
     */
    public static void OldMessage(String message, Player player) {
        message = message.replaceAll("\\&([0-9abcdef])", "�$1");

        String color = "f";
        final int maxLength = 61; //Max length of chat text message
        final String newLine = "[NEWLINE]";
        ArrayList<String> chat = new ArrayList<String>();
        chat.add(0, "");
        String[] words = message.split(" ");
        int lineNumber = 0;
        for (int i = 0; i < words.length; i++) {
            if (chat.get(lineNumber).length() + words[i].length() < maxLength && !words[i].equals(newLine)) {
                chat.set(lineNumber, chat.get(lineNumber) + (chat.get(lineNumber).length() > 0 ? " " : "�" + color) + words[i]);
                if (words[i].contains("�"))
                    color = Character.toString(words[i].charAt(words[i].indexOf("�") + 1));
            } else {
                lineNumber++;
                if (!words[i].equals(newLine)) {
                    chat.add(lineNumber, "�" + color + words[i]);
                } else {
                    chat.add(lineNumber, "");
                }
            }
        }
        for (int i = 0; i < chat.size(); i++) {
            player.sendMessage(chat.get(i));
        }
    }

    public static void Broadcast(String message) {
        for (Player i : MonsterHunt.instance.getServer().getOnlinePlayers()) {
            Message(message, i);
        }
    }

    public static void BroadcastToParticipants(String message) {
    	for(MonsterHuntWorld mhw : HuntWorldManager.getWorlds()) {
    		if(mhw.state > 0) {
    			for(String playerName : mhw.Score.keySet())	{
    				Player player = MonsterHunt.instance.getServer().getPlayerExact(playerName);
    				if(player != null)
    				{
    					Message(message, MonsterHunt.instance.getServer().getPlayerExact(playerName));
    				}
    			}
    		}
    	}
    }
    
    public static void Debug(String message) {
        if (Settings.globals.config.getBoolean(Setting.Debug.getString(), false)) {
            Log.info("[Debug]" + message);
        }
    }

    public void StartFailed(MonsterHuntWorld world) {
        if (world.worldSettings.getInt(Setting.SkipToIfFailsToStart) >= 0) {
            world.getWorld().setTime(world.worldSettings.getInt(Setting.SkipToIfFailsToStart));
        }
    }
    
	public static Boolean isInteger(String text) {
		try {
			Integer.parseInt(text);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
	
	public static boolean hasPermission(CommandSender player, String permission)
	{
		while (true)
		{
			if (player.hasPermission(permission))
				return true;

			if (permission.length() < 2)
				return false;

			if (permission.endsWith("*"))
				permission = permission.substring(0, permission.length() - 2);

			int lastIndex = permission.lastIndexOf(".");
			if (lastIndex < 0)
				return false;

			permission = permission.substring(0, lastIndex).concat(".*");  
		}
	}

}