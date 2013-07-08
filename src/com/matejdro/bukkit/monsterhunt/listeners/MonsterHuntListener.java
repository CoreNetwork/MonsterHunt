package com.matejdro.bukkit.monsterhunt.listeners;

import java.util.Map.Entry;

import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Blaze;
import org.bukkit.entity.CaveSpider;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.Giant;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.MagmaCube;
import org.bukkit.entity.MushroomCow;
import org.bukkit.entity.Pig;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Silverfish;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Skeleton.SkeletonType;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.Snowman;
import org.bukkit.entity.Spider;
import org.bukkit.entity.Squid;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Witch;
import org.bukkit.entity.Wither;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import com.matejdro.bukkit.monsterhunt.HuntWorldManager;
import com.matejdro.bukkit.monsterhunt.HuntZone;
import com.matejdro.bukkit.monsterhunt.HuntZoneCreation;
import com.matejdro.bukkit.monsterhunt.MonsterHunt;
import com.matejdro.bukkit.monsterhunt.MonsterHuntWorld;
import com.matejdro.bukkit.monsterhunt.Setting;
import com.matejdro.bukkit.monsterhunt.Settings;
import com.matejdro.bukkit.monsterhunt.Util;

public class MonsterHuntListener implements Listener {
    //HashMap<Integer, Player> lastHits = new HashMap<Integer, Player>();
    //HashMap<Integer, Integer> lastHitCauses = new HashMap<Integer, Integer>();

    @EventHandler()
    public void onEntityCombustByEntity(EntityCombustByEntityEvent event)
    {
    	Entity combusterEntity = event.getCombuster();
    	Entity combustee = event.getEntity();
    	if(combusterEntity == null || combustee == null)
    		return;
    	MonsterHuntWorld world = HuntWorldManager.getWorld(combustee.getWorld().getName());
    	
    	if(world != null && world.state == 2)
    	{
	    	Player damager = null;
	    	if(combusterEntity instanceof Arrow)
	    	{
	    		Arrow arrowEntity = (Arrow)combusterEntity;
	    	    if(arrowEntity.getShooter() instanceof Player)
	    	    {
	    	    	if(arrowEntity.getShooter() instanceof Player)
	    	    	{
		    	        damager = (Player) arrowEntity.getShooter();
		    	    	combustee.removeMetadata("ignitedByArrow", MonsterHunt.instance);
		    	    	combustee.removeMetadata("ignitedBySword", MonsterHunt.instance);
		    	        combustee.setMetadata("ignitedByArrow", new FixedMetadataValue(MonsterHunt.instance, damager));
	    	    	}
	    	    }
	    	}
	    	else if(combusterEntity instanceof Player)
	    	{
	    		damager = (Player) combusterEntity;
    	    	combustee.removeMetadata("ignitedByArrow", MonsterHunt.instance);
    	    	combustee.removeMetadata("ignitedBySword", MonsterHunt.instance);
    	        combustee.setMetadata("ignitedBySword", new FixedMetadataValue(MonsterHunt.instance, damager));
	    	}
	    	
    	}
    }

    @EventHandler()
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            MonsterHuntWorld world = HuntWorldManager.getWorld(player.getWorld().getName());
            if (world == null || world.getWorld() == null) {
                return;
            }
            if (world.settings.getInt(Setting.DeathPenalty) == 0) {
                return;
            }
            if (world.state > 1 && world.Score.containsKey(player.getName())) {
                double score = world.Score.get(player.getName()) + 0.00;
                score = score - (score * world.settings.getInt(Setting.DeathPenalty) / 100.00);
                world.Score.put(player.getName(), (int) Math.round(score));
                Util.Message(world.settings.getString(Setting.DeathMessage), player);
                if(world.settings.getBoolean(Setting.ScoreboardEnabled))
                {
                	world.refreshScoreboards();
                }
            }
        }
        MonsterHuntWorld world = HuntWorldManager.getWorld(event.getEntity().getWorld().getName());
        if (world == null || world.getWorld() == null || world.state < 2) {
            return;
        }
        
        if (!HuntZone.isInsideZone(event.getEntity().getLocation())) {
            return;
        }
        if (event.getEntity() == null || (event.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent == false && event.getEntity().getLastDamageCause().getCause().equals(DamageCause.FIRE_TICK) == false)) {
            return;
        }
        kill((LivingEntity) event.getEntity(), world);
    }

    private void kill(LivingEntity monster, MonsterHuntWorld world) {
        String name;
        Player player = null;
        String cause = "General";
        
        if(world.settings.getBoolean(Setting.DontCountNamedMobs) && monster.getCustomName() != null) {
        	return;
        }
        if (!(world.properlyspawned.contains(monster.getEntityId()))) {
            return;
        }
        
        if(monster.getLastDamageCause() instanceof EntityDamageByEntityEvent)
        {
        	EntityDamageByEntityEvent event = (EntityDamageByEntityEvent) monster.getLastDamageCause();
            if (event.getCause() == DamageCause.PROJECTILE && event.getDamager() instanceof Projectile) {
                if (event.getDamager() instanceof Snowball) {
                    cause = "Snowball";
                } else {
                    cause = "Arrow";
                }
                LivingEntity shooter = ((Projectile) event.getDamager()).getShooter();
                if (shooter instanceof Player) {
                    player = (Player) shooter;
                }
            }
            else if (event.getDamager() instanceof Wolf && ((Wolf) event.getDamager()).isTamed()) {
                cause = "Wolf";
                player = (Player) ((Wolf) event.getDamager()).getOwner();
            }

            if (player == null) {
                if (!(event.getDamager() instanceof Player))
                    return;
                player = (Player) event.getDamager();

                if (cause.equals("General")) {
                    if (player.getItemInHand() == null) {
                        cause = String.valueOf(0);
                    } else {
                        cause = String.valueOf(player.getItemInHand().getTypeId());
                    }
                }
            }
        }
        else if(monster.getLastDamageCause().getCause().equals(DamageCause.FIRE_TICK))
        {
        	if(monster.getMetadata("ignitedByArrow").size() != 0)
        	{
        		player = (Player) monster.getMetadata("ignitedByArrow").get(0).value();
        		cause = "Arrow";
        	}
        	else if(monster.getMetadata("ignitedBySword").size() != 0)
        	{
        		player = (Player) monster.getMetadata("ignitedBySword").get(0).value();
        		cause = "General";
        	}
        	else
        	{
        		return;
        	}
        	
        }

        if(player == null)
    		return;

        int points = 0;
        if (monster instanceof Skeleton) {
            Skeleton skeleton = (Skeleton) monster;
            SkeletonType skeletonType = skeleton.getSkeletonType();
            if (skeletonType.equals(SkeletonType.NORMAL)) {
                points = world.settings.getMonsterValue("Skeleton", cause);
                name = "Skeleton";
            } else {
                points = world.settings.getMonsterValue("WitherSkeleton", cause);
                name = "Wither Skeleton";
            }
        } else if (monster instanceof Spider) {
            points = world.settings.getMonsterValue("Spider", cause);
            name = "Spider";
        } else if (monster instanceof Creeper) {
            Creeper creeper = (Creeper) monster;
            if (creeper.isPowered()) {
                points = world.settings.getMonsterValue("ElectrifiedCreeper", cause);
                name = "Electrified Creeper";
            } else {
                points = world.settings.getMonsterValue("Creeper", cause);
                name = "Creeper";
            }
        } else if (monster instanceof Ghast) {
            points = world.settings.getMonsterValue("Ghast", cause);
            name = "Ghast";
        } else if (monster instanceof Slime) {
            points = world.settings.getMonsterValue("Slime", cause);
            name = "Slime";
        } else if (monster instanceof PigZombie) {
            points = world.settings.getMonsterValue("ZombiePigman", cause);
            name = "Zombie Pigman";
        } else if (monster instanceof Giant) {
            points = world.settings.getMonsterValue("Giant", cause);
            name = "Giant";
        } else if (monster instanceof Zombie) {
            points = world.settings.getMonsterValue("Zombie", cause);
            name = "Zombie";
        } else if (monster instanceof Wolf) {
            Wolf wolf = (Wolf) monster;
            if (wolf.isTamed()) {
                points = world.settings.getMonsterValue("TamedWolf", cause);
                name = "Tamed Wolf";
            } else {
                points = world.settings.getMonsterValue("WildWolf", cause);
                name = "Wild Wolf";
            }

        } else if (monster instanceof Player) {
            points = world.settings.getMonsterValue("Player", cause);
            name = "Player";
        } else if (monster instanceof Enderman) {
            points = world.settings.getMonsterValue("Enderman", cause);
            name = "Enderman";
        } else if (monster instanceof Silverfish) {
            points = world.settings.getMonsterValue("Silverfish", cause);
            name = "Silverfish";
        } else if (monster instanceof CaveSpider) {
            points = world.settings.getMonsterValue("CaveSpider", cause);
            name = "CaveSpider";
        } else if (monster instanceof EnderDragon) {
            points = world.settings.getMonsterValue("EnderDragon", cause);
            name = "Ender Dragon";
        } else if (monster instanceof MagmaCube) {
            points = world.settings.getMonsterValue("MagmaCube", cause);
            name = "Magma Cube";
        } else if (monster instanceof MushroomCow) {
            points = world.settings.getMonsterValue("Mooshroom", cause);
            name = "Mooshroom";
        } else if (monster instanceof Chicken) {
            points = world.settings.getMonsterValue("Chicken", cause);
            name = "Chicken";
        } else if (monster instanceof Cow) {
            points = world.settings.getMonsterValue("Cow", cause);
            name = "Cow";
        } else if (monster instanceof Blaze) {
            points = world.settings.getMonsterValue("Blaze", cause);
            name = "Blaze";
        } else if (monster instanceof Pig) {
            points = world.settings.getMonsterValue("Pig", cause);
            name = "Pig";
        } else if (monster instanceof Sheep) {
            points = world.settings.getMonsterValue("Sheep", cause);
            name = "Sheep";
        } else if (monster instanceof Snowman) {
            points = world.settings.getMonsterValue("SnowGolem", cause);
            name = "Snow Golem";
        } else if (monster instanceof Squid) {
            points = world.settings.getMonsterValue("Squid", cause);
            name = "Squid";
        } else if (monster instanceof Villager) {
            points = world.settings.getMonsterValue("Villager", cause);
            name = "Villager";
        } else if (monster instanceof IronGolem) {
            points = world.settings.getMonsterValue("IronGolem", cause);
            name = "Iron Golem";
        } else if (monster instanceof Witch) {
            points = world.settings.getMonsterValue("Witch", cause);
            name = "Witch";
        }else if (monster instanceof Wither) {
            points = world.settings.getMonsterValue("Wither", cause);
            name = "Wither";
        } else {
            return;
        }
        if (points < 1) {
            return;
        }

        points += pointsForEquipment(monster, world);
        
        if (!world.Score.containsKey(player.getName()) && !world.settings.getBoolean(Setting.EnableSignup)) {
            world.Score.put(player.getName(), 0);
        }
        if (world.Score.containsKey(player.getName())) {
            int newscore = world.Score.get(player.getName()) + points;

            if (world.settings.getBoolean(Setting.AnnounceLead)) {
                Entry<String, Integer> leadpoints = null;
                for (Entry<String, Integer> e : world.Score.entrySet()) {
                    if (leadpoints == null || e.getValue() > leadpoints.getValue() || (e.getValue() == leadpoints.getValue() && leadpoints.getKey().equalsIgnoreCase(player.getName()))) {
                        leadpoints = e;
                    }
                }
                Util.Debug(leadpoints.toString());
                Util.Debug(String.valueOf(newscore));
                Util.Debug(String.valueOf(!leadpoints.getKey().equals(player.getName())));
   
                if (leadpoints != null && newscore > leadpoints.getValue() && !leadpoints.getKey().equals(player.getName())) {
                    String message = world.settings.getString(Setting.MessageLead);
                    message = message.replace("<Player>", player.getName());
                    message = message.replace("<Points>", String.valueOf(newscore));
                    message = message.replace("<World>", world.name);
                    
                    long timeInWorld = monster.getWorld().getTime();
                    if(timeInWorld >= world.settings.getInt(Setting.AnnounceLeadInterval) + world.lastAnnounceTime)
                    {
                    	world.lastAnnounceTime = timeInWorld;
	                    if(world.settings.getBoolean(Setting.AnnounceLeadEveryone))
	                    	Util.Broadcast(message);
	                    else
	                    	Util.BroadcastToParticipants(message);
                    }
                }
            }

            world.Score.put(player.getName(), newscore);

            world.properlyspawned.remove((Object) monster.getEntityId());
            
            if (world.settings.getBoolean(Setting.ShowKillMessage)) 
            {
	            String message = world.settings.getKillMessage(cause);
	            message = message.replace("<MobValue>", String.valueOf(points));
	            message = message.replace("<MobName>", name);
	            message = message.replace("<Points>", String.valueOf(newscore));
	            Util.Message(message, player);
            }
            if(world.settings.getBoolean(Setting.ScoreboardEnabled))
            {
            	world.refreshScoreboards();
            }
        }
    }

    private int pointsForEquipment(LivingEntity monster, MonsterHuntWorld world) {
    	int equipmentPoints = 0;
		

		EntityEquipment eq = monster.getEquipment();
		
		if(eq.getArmorContents().length == 0 && eq.getItemInHand() == null)
			return 0;
		
		ItemStack helmet = eq.getHelmet();
		ItemStack chestplate = eq.getChestplate();
		ItemStack leggings = eq.getLeggings();
		ItemStack boots = eq.getBoots();
		ItemStack weapon = eq.getItemInHand();
		
		int leatherPoints = world.settings.getInt(Setting.EqLeather);
		int goldPoints = world.settings.getInt(Setting.EqGold);
		int ironPoints = world.settings.getInt(Setting.EqIron);
		int chainPoints = world.settings.getInt(Setting.EqChain);
		int diamondPoints = world.settings.getInt(Setting.EqDiamond);
		int shovelPoints = world.settings.getInt(Setting.EqShovel);
		int swordPoints = world.settings.getInt(Setting.EqSword);
		int enchantedArmorPoints = world.settings.getInt(Setting.EqEnchArmor);
		int enchantedSwordPoints = world.settings.getInt(Setting.EqEnchSword);
		int enchantedShovelPoints = world.settings.getInt(Setting.EqEnchShovel);
		int enchantedBowPoints = world.settings.getInt(Setting.EqEnchBow);
		
		//Helmet
		if(helmet != null)
		{
			Material helmetType = helmet.getType();
			if(helmetType.equals(Material.LEATHER_HELMET))
				equipmentPoints += leatherPoints;
			else if(helmetType.equals(Material.IRON_HELMET))
				equipmentPoints += ironPoints;
			else if(helmetType.equals(Material.CHAINMAIL_HELMET))
				equipmentPoints += chainPoints;
			else if(helmetType.equals(Material.DIAMOND_HELMET))
				equipmentPoints += diamondPoints;
			else if(helmetType.equals(Material.GOLD_HELMET))
				equipmentPoints += goldPoints;
			
			if(helmet.getEnchantments().size() != 0)
				equipmentPoints += enchantedArmorPoints;
		}
		
		//Chestplate
		if(chestplate != null)
		{
			Material chestplateType = chestplate.getType();
			if(chestplateType.equals(Material.LEATHER_CHESTPLATE))
				equipmentPoints += leatherPoints;
			else if(chestplateType.equals(Material.IRON_CHESTPLATE))
				equipmentPoints += ironPoints;
			else if(chestplateType.equals(Material.CHAINMAIL_CHESTPLATE))
				equipmentPoints += chainPoints;
			else if(chestplateType.equals(Material.DIAMOND_CHESTPLATE))
				equipmentPoints += diamondPoints;
			else if(chestplateType.equals(Material.GOLD_CHESTPLATE))
				equipmentPoints += goldPoints;
			
			if(chestplate.getEnchantments().size() != 0)
				equipmentPoints += enchantedArmorPoints;
		}
				
		//Leggings
		if(leggings != null)
		{
			Material leggingsType = leggings.getType();
			if(leggingsType.equals(Material.LEATHER_LEGGINGS))
				equipmentPoints += leatherPoints;
			else if(leggingsType.equals(Material.IRON_LEGGINGS))
				equipmentPoints += ironPoints;
			else if(leggingsType.equals(Material.CHAINMAIL_LEGGINGS))
				equipmentPoints += chainPoints;
			else if(leggingsType.equals(Material.DIAMOND_LEGGINGS))
				equipmentPoints += diamondPoints;
			else if(leggingsType.equals(Material.GOLD_LEGGINGS))
				equipmentPoints += goldPoints;
			
			if(leggings.getEnchantments().size() != 0)
				equipmentPoints += enchantedArmorPoints;
		}
		
		//Boots
		if(boots != null)
		{
			Material bootsType = boots.getType();
			if(bootsType.equals(Material.LEATHER_BOOTS))
				equipmentPoints += leatherPoints;
			else if(bootsType.equals(Material.IRON_BOOTS))
				equipmentPoints += ironPoints;
			else if(bootsType.equals(Material.CHAINMAIL_BOOTS))
				equipmentPoints += chainPoints;
			else if(bootsType.equals(Material.DIAMOND_BOOTS))
				equipmentPoints += diamondPoints;
			else if(bootsType.equals(Material.GOLD_BOOTS))
				equipmentPoints += goldPoints;
			
			if(boots.getEnchantments().size() != 0)
				equipmentPoints += enchantedArmorPoints;
		}
		
		//Weapon
		if(weapon != null)
		{
			Material weaponType = weapon.getType();
			if(weaponType.equals(Material.IRON_SPADE) || weaponType.equals(Material.WOOD_SPADE) || weaponType.equals(Material.GOLD_SPADE) ||weaponType.equals(Material.DIAMOND_SPADE) || weaponType.equals(Material.STONE_SPADE))
			{
				equipmentPoints += shovelPoints;
				if(weapon.getEnchantments().size() != 0)
					equipmentPoints += enchantedShovelPoints;
			}
			else if(weaponType.equals(Material.IRON_SWORD) || weaponType.equals(Material.WOOD_SWORD) || weaponType.equals(Material.GOLD_SWORD) ||weaponType.equals(Material.DIAMOND_SWORD) || weaponType.equals(Material.STONE_SWORD))
			{
				equipmentPoints += swordPoints;
				if(weapon.getEnchantments().size() != 0)
					equipmentPoints += enchantedSwordPoints;
			}
			else if(weaponType.equals(Material.BOW))
			{
				if(weapon.getEnchantments().size() != 0)
					equipmentPoints += enchantedBowPoints;
			}
			
		}
		return equipmentPoints;
	}

	@EventHandler()
    public void onCreatureSpawn(CreatureSpawnEvent event) 
	{
        MonsterHuntWorld world = HuntWorldManager.getWorld(event.getLocation().getWorld().getName());
        if (world == null || world.getWorld() == null) {
            return;
        }
        if (world.state == 0) {
            return;
        }
        if (world.settings.getBoolean(Setting.DontCountMobsFromSpawners) && event.getSpawnReason().equals(SpawnReason.SPAWNER)) {
        	return;
        }
        world.properlyspawned.add(event.getEntity().getEntityId());
    }

    @EventHandler()
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getPlayer().getItemInHand().getTypeId() == Settings.globals.getInt(Setting.SelectionTool.getString(), 268)) {
            if (HuntZoneCreation.players.containsKey(event.getPlayer().getName())) {
                HuntZoneCreation.select(event.getPlayer(), event.getClickedBlock());
                event.setCancelled(true);
            }
        }
    }
}