package com.matejdro.bukkit.monsterhunt.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.matejdro.bukkit.monsterhunt.HuntWorldManager;
import com.matejdro.bukkit.monsterhunt.MonsterHuntWorld;
import com.matejdro.bukkit.monsterhunt.Setting;
import com.matejdro.bukkit.monsterhunt.Util;

public class HuntCommand implements CommandExecutor {

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
        if (!(sender instanceof Player)) {
            sender.sendMessage("Sorry, but you need to execute this command as player.");
            return true;
        }
        MonsterHuntWorld world = HuntWorldManager.getWorld(((Player) sender).getWorld().getName());
        if (world == null || world.getWorld() == null)
            return true;
        if (world.Score.containsKey(((Player) sender).getName())) {
            Util.Message(world.settings.getString(Setting.MessageAlreadySignedUp), sender);
            return true;
        }

        if (world.state < 2) {
            if (world.settings.getBoolean(Setting.AnnounceSignUp)) {
                String message = world.settings.getString(Setting.SignUpAnnouncement);
                message = message.replace("<World>", world.name);
                message = message.replace("<Player>", ((Player) sender).getName());
                Util.Broadcast(message);
            } else {
                String message = world.settings.getString(Setting.SignUpBeforeHuntMessage);
                message = message.replace("<World>", world.name);
                Util.Message(message, sender);
            }

            world.Score.put(((Player) sender).getName(), 0);

        } else if (world.state == 2 && (world.getSignUpPeriodTime() == 0 || world.settings.getBoolean(Setting.AllowSignUpAfterStart))) {
            if (world.settings.getBoolean(Setting.AnnounceSignUp)) {
                String message = world.settings.getString(Setting.SignUpAnnouncement);
                message = message.replace("<World>", world.name);
                message = message.replace("<Player>", ((Player) sender).getName());
                Util.Broadcast(message);
            } else {
                String message = world.settings.getString(Setting.SignUpAfterHuntMessage);
                message = message.replace("<World>", world.name);
                Util.Message(message, sender);
            }

            world.Score.put(((Player) sender).getName(), 0);
        } else {
            Util.Message(world.settings.getString(Setting.MessageTooLateSignUp), sender);
        }
        return true;
    }

}
