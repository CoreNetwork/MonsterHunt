package com.matejdro.bukkit.monsterhunt.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.matejdro.bukkit.monsterhunt.HuntWorldManager;
import com.matejdro.bukkit.monsterhunt.HuntZone;
import com.matejdro.bukkit.monsterhunt.MonsterHuntWorld;
import com.matejdro.bukkit.monsterhunt.Setting;
import com.matejdro.bukkit.monsterhunt.Settings;
import com.matejdro.bukkit.monsterhunt.Util;

public class HuntTeleCommand extends BaseMHCommand {

	public HuntTeleCommand()
	{
		permission = "tele";
		desc = "Teleport to hunt zone";
		needPlayer = true;
	}


	public void run(CommandSender sender, String[] args){
        Player player = (Player) sender;
        MonsterHuntWorld world = HuntWorldManager.getWorld(player.getWorld().getName());
        if (!Settings.globals.config.getBoolean(Setting.HuntZoneMode.getString(), false) || world == null || world.getBukkitWorld() == null)
            return;

        boolean permission = !sender.hasPermission("monsterhunt.noteleportrestrictions");

        if (world.isActive() && permission) {
            Util.Message(world.getSettings().getString(Setting.MessageHuntTeleNoHunt), player);
            return;
        } else if (world.Score.containsKey(player.getUniqueId()) && permission) {
            Util.Message(world.getSettings().getString(Setting.MessageHuntTeleNotSignedUp), player);
            return;
        }

        world.tplocations.put(player, player.getLocation());
        player.teleport(HuntZone.teleport);
    }

}
