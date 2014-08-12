package com.matejdro.bukkit.monsterhunt;

import java.util.HashMap;
import java.util.logging.Logger;

import javax.swing.Timer;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import com.matejdro.bukkit.monsterhunt.commands.BaseMHCommand;
import com.matejdro.bukkit.monsterhunt.commands.HuntBanCommand;
import com.matejdro.bukkit.monsterhunt.commands.HuntBuyCommand;
import com.matejdro.bukkit.monsterhunt.commands.HuntCheckCommand;
import com.matejdro.bukkit.monsterhunt.commands.HuntClaimCommand;
import com.matejdro.bukkit.monsterhunt.commands.HuntHelpCommand;
import com.matejdro.bukkit.monsterhunt.commands.HuntKickCommand;
import com.matejdro.bukkit.monsterhunt.commands.HuntReloadCommand;
import com.matejdro.bukkit.monsterhunt.commands.HuntRunCommand;
import com.matejdro.bukkit.monsterhunt.commands.HuntScoreCommand;
import com.matejdro.bukkit.monsterhunt.commands.HuntSignupCommand;
import com.matejdro.bukkit.monsterhunt.commands.HuntStartCommand;
import com.matejdro.bukkit.monsterhunt.commands.HuntStatusCommand;
import com.matejdro.bukkit.monsterhunt.commands.HuntStopCommand;
import com.matejdro.bukkit.monsterhunt.commands.HuntTeleCommand;
import com.matejdro.bukkit.monsterhunt.commands.HuntUnbanCommand;
import com.matejdro.bukkit.monsterhunt.commands.HuntZoneCommand;
import com.matejdro.bukkit.monsterhunt.listeners.MonsterHuntListener;

public class MonsterHunt extends JavaPlugin implements CommandExecutor {
    public static Logger log = Logger.getLogger("Minecraft");
    private MonsterHuntListener entityListener;
    Timer timer;
    
    public static boolean coreInstalled = false;

    //public static HashMap<String,Integer> highscore = new HashMap<String,Integer>();
    public static HashMap<String, BaseMHCommand> commands = new HashMap<String, BaseMHCommand>();
    
    public static MonsterHunt instance;

    @Override
    public void onDisable() {
        for (MonsterHuntWorld world : HuntWorldManager.getWorlds())
            world.stop();
    }

    @Override
    public void onEnable() {
        PluginDescriptionFile pdfFile = getDescription();
        String version = pdfFile.getVersion();
        this.getLogger().info("v" + version + "Loaded!");
        initialize();

        InputOutput.LoadSettings();
        InputOutput.PrepareDB();
        InputOutput.LoadBans();
        
        getServer().getPluginManager().registerEvents(entityListener, this);

        commands.put("ban", new HuntBanCommand());
        commands.put("claim", new HuntClaimCommand());
        commands.put("help", new HuntHelpCommand());
        commands.put("kick", new HuntKickCommand());
        commands.put("reload", new HuntReloadCommand());
        commands.put("score", new HuntScoreCommand());
        commands.put("signup", new HuntSignupCommand());
        commands.put("status", new HuntStatusCommand());
        commands.put("stop", new HuntStopCommand());
        commands.put("start", new HuntStartCommand());
        commands.put("tele", new HuntTeleCommand());
        commands.put("unban", new HuntUnbanCommand());
        commands.put("zone", new HuntZoneCommand()); 
        commands.put("buy", new HuntBuyCommand()); 
        commands.put("check", new HuntCheckCommand()); 
        commands.put("run", new HuntRunCommand()); 

        HuntWorldManager.timer();
        
        coreInstalled = Bukkit.getPluginManager().isPluginEnabled("Core");

    }

    private void initialize() {
        entityListener = new MonsterHuntListener();
        instance = this;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length < 1)
			return commands.get("signup").execute(sender, args);

		BaseMHCommand cmd = commands.get(args[0]);
		if (cmd != null)
			return cmd.execute(sender, args);
		else
			return commands.get("help").execute(sender, args);

    }

}
