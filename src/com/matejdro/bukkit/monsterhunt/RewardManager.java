package com.matejdro.bukkit.monsterhunt;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.UUID;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

public class RewardManager {

    private static MonsterHunt plugin = MonsterHunt.instance;
    public static Economy economy = null;

    @SuppressWarnings("rawtypes")
    public static void RewardWinners(MonsterHuntWorld world)
    {

        HashMap<UUID, Integer>[] Winners = GetWinners(world);
        if (Winners[0].size() < 1)
        {
            String message = world.getSettings().getString(Setting.FinishMessageNotEnoughPlayers);
            message = message.replace("<World>", world.name);
            message = message.replace("<HuntName>", world.activeHuntSpecification.getDisplayName());
            HuntWorldManager.BroadcastToAllParticipants(message);
            Log.info(message);
            return;
        }
        int numberOfWinners = world.getSettings().getInt(Setting.NumberOfWinners);

        // keep track of players we reward so we don't reward them again
        // in the RewardEveryone section
        final ArrayList<UUID> rewardedPlayers = new ArrayList<UUID>();
        int score = Winners[0].values().iterator().next();

        final boolean enableReward=world.getSettings().getBoolean(Setting.EnableReward);
        Util.Debug("EnabledReward="+enableReward);

        //RewardEveryone
        if (!(!world.getSettings().getBoolean(Setting.EnableRewardEveryonePermission) && !world.getSettings().getBoolean(Setting.RewardEveryone)))
        {
            for (Entry<UUID, Integer> i : world.Score.entrySet())
            {
                if (((Integer) i.getValue()) < world.getSettings().getInt(Setting.MinimumPointsEveryone))
                    continue;
                Player player = plugin.getServer().getPlayer(i.getKey());
                if (player == null)
                    continue;

                // don't reward "everyone" reward if they already won a top award, except if config allows so
                if (!world.getSettings().getBoolean(Setting.RewardEveryoneIncludesWinners) && rewardedPlayers.contains(player.getUniqueId()))
                    continue;

                if (world.getSettings().getBoolean(Setting.RewardEveryone) || (player.hasPermission("monsterhunt.rewardeverytime") && world.getSettings().getBoolean(Setting.EnableRewardEveryonePermission)))
                {
                    Reward(i.getKey(), "RewardEveryone", world, i.getValue());
                }
            }
        }

        //Normal reward

        if (score < world.getSettings().getPlaceInt(Setting.MinimumPointsPlace, 1))
        {
            String message = world.getSettings().getString(Setting.FinishMessageNotEnoughPoints);
            message = message.replace("<HuntName>", world.activeHuntSpecification.getDisplayName());
            message = message.replace("<World>", world.name);
            HuntWorldManager.BroadcastToAllParticipants(message);
            Log.info(message);
            return;
        }

        if (enableReward) {
            for (int place = 0; place < numberOfWinners; place++) {
                Util.Debug("Checking place "+place);
                if (Winners[place].size() < 1)
                    continue;
                score = Winners[place].get(Winners[place].keySet().toArray()[0]);
                Util.Debug("score="+String.valueOf(score));
                Util.Debug("minscore="+String.valueOf(world.getSettings().getPlaceInt(Setting.MinimumPointsPlace, place + 1)));
                if (score >= world.getSettings().getPlaceInt(Setting.MinimumPointsPlace, place + 1)) {
//                    Winners[place].clear();
                    Util.Debug("score is >= minscore");
                    for (UUID uuid : Winners[place].keySet()) {
                        Util.Debug("player="+ uuid);
                        String rewardType = "Place" + (place + 1);
                        Reward(uuid, rewardType, world, score);

                        rewardedPlayers.add(uuid);
                    }
                }
            }
        }

        //Broadcast winner message
        Util.Debug("[MonterHunt][DEBUG - NEVEREND]Broadcasting Winners");
        String message;

        message = world.getSettings().getString(Setting.FinishMessageWinnersHeader);
        message = message.replace("<World>", world.name);
        message = message.replace("<HuntName>", world.activeHuntSpecification.getDisplayName());
        message = message.replace("<Player>", world.getCurrentSponsor());

        for (int place = 0; place < numberOfWinners; place++) {
            String players = "";
            String placeMessage = world.getSettings().getPlaceString(Setting.WinnerMessagePlace, place + 1);
            if (Winners[place].size() > 0 && placeMessage != null) {
                score = Winners[place].get(Winners[place].keySet().toArray()[0]);
                for (UUID uuid : Winners[place].keySet()) {
                    players += Bukkit.getOfflinePlayer(uuid).getName() + ", ";
                }
                players = players.substring(0, players.length() - 2);
                
                placeMessage = placeMessage.replace("<Names>", players);
                placeMessage = placeMessage.replace("<Points>", String.valueOf(score));
                message = message + placeMessage;
            }
        }
        message = message +  world.getSettings().getString(Setting.FinishMessageWinnersFooter);
        message = message.replace("<Sponsor>", world.getCurrentSponsor());

        HuntWorldManager.BroadcastToAllParticipants(message);
        Log.info(message);

        try
        {
            InputOutput.getConnection().commit();
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    private static void Reward(UUID playerUUID, String rewardType, MonsterHuntWorld world, int score)
    {
    	Player player = Bukkit.getPlayer(playerUUID);
    	if (world.getSettings().getBoolean(Setting.GiveRewardsImmediatelly) && player != null)
    		giveItems(player, rewardType, world.getSettings(), score);
    	else
    		storeReward(playerUUID, rewardType, world, score);
    }
    
    private static void storeReward(UUID playerUUID, String rewardType, MonsterHuntWorld world, int score)
    {
    	try
    	{
    		String huntName = world.activeHuntSpecification.getName();
    		if (huntName.equals("default"))
    			huntName = world.name;
    		
    		PreparedStatement statement = InputOutput.getConnection().prepareStatement("INSERT INTO monsterhunt_RewardsToClaim (PlayerName, HuntName, RewardType, Score) VALUES (?,?,?,?)"); 
    		statement.setString(1, playerUUID.toString());
    		statement.setString(2, huntName);
    		statement.setString(3, rewardType);
    		statement.setInt(4, score);
    		
    		statement.executeUpdate();
    		statement.close();
    	}
    	catch (SQLException e)
    	{
    		e.printStackTrace();
    	}
    }
    
    public static void giveItems(Player player, String rewardType, Settings activeSettings, int score) {
    	Util.Debug("Rewarding "+ player);
    	Util.Debug("RewardType="+rewardType);
    	
    	String rewardString = (String) activeSettings.getObject("Rewards." + rewardType + ".RewardParameters");
    	if (rewardString != null)
    	{
            String items = "";

            if (rewardString.contains(";")) {
    			rewardString = PickRandom(rewardString);
            }
    		
        	String[] split = rewardString.split(",");
        	
        	if (player == null)
        		return;
        	
        	for (String i2 : split) {
        		Util.Debug("Reward i2="+i2);
        		//Parse block ID
        		String BlockIdString = i2.substring(0, i2.indexOf(" "));
        		short data;
        		int BlockId;
        		if (BlockIdString.contains(":")) {
        			BlockId = Integer.valueOf(BlockIdString.substring(0, i2.indexOf(":")));
        			data = Short.valueOf(BlockIdString.substring(i2.indexOf(":") + 1));
        		} else {
        			BlockId = Integer.valueOf(BlockIdString);
        			data = 0;
        		}

        		//Parse block amount
        		String rv = i2.substring(i2.indexOf(" ") + 1);
        		boolean RelativeReward = false;
        		if (rv.startsWith("R")) {
        			RelativeReward = true;
        			rv = rv.substring(1);
        		}
        		
        		int StartValue, EndValue;
        		if (rv.contains("-")) {
        			StartValue = (int) Math.round(Double.valueOf(rv.substring(0, rv.indexOf("-"))) * 100.0);
        			EndValue = (int) Math.round(Double.valueOf(rv.substring(rv.indexOf("-") + 1)) * 100.0);
        		} else {
        			StartValue = (int) Math.round(Double.valueOf(rv) * 100.0);
        			EndValue = StartValue;
        		}
        		
        		int random=0;
        		if (EndValue == StartValue) {
        			random = EndValue;
        		}
        		else {
    				random = new Random().nextInt(EndValue - StartValue) + StartValue;
        		}
        		
    			if( random > 0 ) {
    	    		double number = random / 100.0;
    	    		if (RelativeReward) {
    	    			number *= score;
    	    		}
    	    		int amount = (int) Math.round(number);
    	
    	    		//give reward
    	    		if (BlockId == 0) {
    	    			Util.Debug("rewarding iConomy amount="+amount);
    	    			String item = iConomyReward(player.getName(), amount);
    	    			if (amount > 0) {
    	    				items += item + ", ";
    	    			}
    	    		} else if( amount > 0 ) {
    	    			Util.Debug("rewarding blockId of "+BlockId+", amount="+amount+", data="+data);
    	    			addItemFix(player, BlockId, amount, data);
    	    			if (amount > 0) {
    	    				items += String.valueOf(amount) + "x " + getMaterialName(Material.getMaterial(BlockId)) + ", ";
    	    			}
    	    			//plugin.getServer().getPlayer(i).giveItem(BlockId,amount);
    	    		}
    	    		else {
    					Util.Debug("blockId was "+BlockId+", amount was 0, no reward given");
    	    		}
    			}
    			else
    				Util.Debug("random value was 0, no rewards given");
        		
        	}

            if (!items.trim().isEmpty())
            {
                String message = activeSettings.getString(Setting.RewardMessage);
                items = items.substring(0, items.length() - 2);
                message = message.replace("<Items>", items);
                Util.Message(message, player);
            }

        }
    	List<String> commands = (List<String>) activeSettings.getObject("Rewards." + rewardType + ".Commands");
        Util.Debug("Reward commands=" + commands);
    	if (commands != null)
    	{
    		for (String s : commands)
    		{
                s = s.replace("<Player>", player.getName());
                Util.Debug("Sending command " + s);

                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), s);
    		}
    	}

    	
    }
    
    private static String iConomyReward(String player, int number) {
        Plugin test = plugin.getServer().getPluginManager().getPlugin("Vault");
        if (test != null) {
            if (!setupEconomy()) {
                Log.warning("You have economy rewards enabled, but don't have any economy plugin installed!");
                return "";
            }
            economy.depositPlayer(player, number);
            return economy.format(number);
        } else {
            Log.warning("You have economy rewards enabled, but don't have Vault plugin installed! Some players may not get their reward! See http://dev.bukkit.org/server-mods/vault/");
            return "";
        }
    }

    private static String PickRandom(String RewardString) {
        String[] split = RewardString.split(";");
        int[] chances = new int[split.length];

        int totalchances = 0, numnochances = 0;
        for (int i = 0; i < split.length; i++) {
            if (split[i].startsWith(":")) {
                chances[i] = Integer.valueOf(split[i].substring(1, split[i].indexOf(" ")));
                split[i] = split[i].substring(split[i].indexOf(" ") + 1);
                totalchances += chances[i];
            } else {
                chances[i] = -1;
                numnochances++;
            }
        }

        if (totalchances > (100 - numnochances)) {
            Log.warning("Invalid Rewards configuration! Sum of all percentages should be exactly 100! MonsterHunt will now throw error and disable itself.");
            plugin.getPluginLoader().disablePlugin(plugin);
            return null;
        }

        if (numnochances > 0) {
            int averagechance = (100 - totalchances) / numnochances;
            for (int i = 0; i < chances.length; i++) {
                chances[i] = averagechance;
            }
        }

        int total = 0;

        for (int i = 0; i < split.length; i++) {
            total += chances[i];
            chances[i] = total;
        }

        int random = new Random().nextInt(100);
        for (int i = 0; i < split.length; i++) {
            if (random < chances[i] && (i < 1 || random >= chances[i - 1]))
                return split[i];
        }
        return "";
    }

    @SuppressWarnings("unchecked")
    private static HashMap<UUID, Integer>[] GetWinners(MonsterHuntWorld world) {
        HashMap<UUID, Integer> scores = new HashMap<UUID, Integer>();
        scores.putAll(world.Score);
        int num = world.getSettings().getInt(Setting.NumberOfWinners);
        HashMap<UUID, Integer>[] winners = new HashMap[num];
        for (int place = 0; place < num; place++) {
            winners[place] = new HashMap<UUID, Integer>();
            int tmp = 0;
            for (UUID uuid : scores.keySet()) {
                int value = scores.get(uuid);
                if (value > tmp) {
                    winners[place].clear();
                    winners[place].put(uuid, value);
                    tmp = value;
                } else if (value == tmp) {
                    winners[place].put(uuid, value);
                }
            }

            for (UUID uuid : winners[place].keySet()) {
                scores.remove(uuid);
            }
        }
        return winners;
    }

    // Material name snippet by TechGuard
    public static String getMaterialName(Material material) {
        String name = material.toString();
        name = name.replaceAll("_", " ");
        if (name.contains(" ")) {
            String[] split = name.split(" ");
            for (int i = 0; i < split.length; i++) {
                split[i] = split[i].substring(0, 1).toUpperCase() + split[i].substring(1).toLowerCase();
            }
            name = "";
            for (String s : split) {
                name += " " + s;
            }
            name = name.substring(1);
        } else {
            name = name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
        }
        return name;
    }

    //add item color by fabe
    public static void addItemFix(Player players, int ID, int amount, short dur) {
        if (players.getInventory().firstEmpty() == -1) {
            players.getLocation().getWorld().dropItem(players.getLocation(), new ItemStack(ID, amount, dur));
            return;
        }
        if (players.getInventory().contains(ID) && (ID == 35 || ID == 351)) { // Wool or Dye
            HashMap<Integer, ? extends ItemStack> invItems = players.getInventory().all(ID);

            int restAmount = amount;
            for (Map.Entry<Integer, ? extends ItemStack> entry : invItems.entrySet()) {

                int index = entry.getKey();
                ItemStack item = entry.getValue();
                int stackAmount = item.getAmount();

                // e.g. same wool in inventory => put in to stack
                if (dur == item.getDurability()) {

                    if (stackAmount < 64) {
                        // Add to stack
                        int canGiveAmount = 64 - stackAmount;
                        int giveAmount;

                        if (canGiveAmount >= restAmount) {
                            giveAmount = restAmount;
                            restAmount = 0;
                        } else {
                            giveAmount = canGiveAmount;
                            restAmount = restAmount - giveAmount;
                        }
                        players.getInventory().setItem(index, new ItemStack(ID, stackAmount + giveAmount, dur));
                    }
                }
            }
            // If there is still a rest, add the rest to the inventory
            if (restAmount > 0) {
                int emptySlot = players.getInventory().firstEmpty();
                players.getInventory().setItem(emptySlot, new ItemStack(ID, restAmount, dur));
            }
        } else {
            // Standard usage of addItem
            players.getInventory().addItem(new ItemStack(ID, amount, dur));
        }
    }

    private static boolean setupEconomy() {
        if (economy != null)
            return true;

        RegisteredServiceProvider<Economy> economyProvider = Bukkit.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }
        return (economy != null);
    }

}
