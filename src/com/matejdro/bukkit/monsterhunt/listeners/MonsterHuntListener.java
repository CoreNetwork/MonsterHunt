package com.matejdro.bukkit.monsterhunt.listeners;

import java.util.Map.Entry;
import java.util.UUID;

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
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.projectiles.ProjectileSource;

import com.matejdro.bukkit.monsterhunt.HuntWorldManager;
import com.matejdro.bukkit.monsterhunt.HuntZone;
import com.matejdro.bukkit.monsterhunt.HuntZoneCreation;
import com.matejdro.bukkit.monsterhunt.MonsterHunt;
import com.matejdro.bukkit.monsterhunt.MonsterHuntWorld;
import com.matejdro.bukkit.monsterhunt.PointManager;
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
    	
    	MonsterHuntWorld world = HuntWorldManager.getWorld(event.getEntity().getWorld().getName());
        if (world == null || world.getWorld() == null || world.state < 2) {
            return;
        }
    	
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (world.worldSettings.getInt(Setting.DeathPenalty) != 0 && world.Score.containsKey(player.getName())) {
                double score = world.Score.get(player.getName()) + 0.00;
                score = score - (score * world.worldSettings.getInt(Setting.DeathPenalty) / 100.00);
                world.Score.put(player.getUniqueId(), (int) Math.round(score));
                Util.Message(world.worldSettings.getString(Setting.DeathMessage), player);
                if(world.worldSettings.getBoolean(Setting.ScoreboardEnabled))
                {
                	world.refreshScoreboards();
                }
            }
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
        
        if(world.worldSettings.getBoolean(Setting.DontCountNamedMobs) && monster.getCustomName() != null) {
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
                ProjectileSource shooter = ((Projectile) event.getDamager()).getShooter();
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
                points = world.worldSettings.getMonsterValue("Skeleton", cause);
                name = "Skeleton";
            } else {
                points = world.worldSettings.getMonsterValue("WitherSkeleton", cause);
                name = "Wither Skeleton";
            }
        } else if (monster instanceof Spider) {
            points = world.worldSettings.getMonsterValue("Spider", cause);
            name = "Spider";
        } else if (monster instanceof Creeper) {
            Creeper creeper = (Creeper) monster;
            if (creeper.isPowered()) {
                points = world.worldSettings.getMonsterValue("ElectrifiedCreeper", cause);
                name = "Electrified Creeper";
            } else {
                points = world.worldSettings.getMonsterValue("Creeper", cause);
                name = "Creeper";
            }
        } else if (monster instanceof Ghast) {
            points = world.worldSettings.getMonsterValue("Ghast", cause);
            name = "Ghast";
        } else if (monster instanceof Slime) {
            points = world.worldSettings.getMonsterValue("Slime", cause);
            name = "Slime";
        } else if (monster instanceof PigZombie) {
            points = world.worldSettings.getMonsterValue("ZombiePigman", cause);
            name = "Zombie Pigman";
        } else if (monster instanceof Giant) {
            points = world.worldSettings.getMonsterValue("Giant", cause);
            name = "Giant";
        } else if (monster instanceof Zombie) {
            points = world.worldSettings.getMonsterValue("Zombie", cause);
            name = "Zombie";
        } else if (monster instanceof Wolf) {
            Wolf wolf = (Wolf) monster;
            if (wolf.isTamed()) {
                points = world.worldSettings.getMonsterValue("TamedWolf", cause);
                name = "Tamed Wolf";
            } else {
                points = world.worldSettings.getMonsterValue("WildWolf", cause);
                name = "Wild Wolf";
            }

        } else if (monster instanceof Player) {
            points = world.worldSettings.getMonsterValue("Player", cause);
            name = "Player";
        } else if (monster instanceof Enderman) {
            points = world.worldSettings.getMonsterValue("Enderman", cause);
            name = "Enderman";
        } else if (monster instanceof Silverfish) {
            points = world.worldSettings.getMonsterValue("Silverfish", cause);
            name = "Silverfish";
        } else if (monster instanceof CaveSpider) {
            points = world.worldSettings.getMonsterValue("CaveSpider", cause);
            name = "CaveSpider";
        } else if (monster instanceof EnderDragon) {
            points = world.worldSettings.getMonsterValue("EnderDragon", cause);
            name = "Ender Dragon";
        } else if (monster instanceof MagmaCube) {
            points = world.worldSettings.getMonsterValue("MagmaCube", cause);
            name = "Magma Cube";
        } else if (monster instanceof MushroomCow) {
            points = world.worldSettings.getMonsterValue("Mooshroom", cause);
            name = "Mooshroom";
        } else if (monster instanceof Chicken) {
            points = world.worldSettings.getMonsterValue("Chicken", cause);
            name = "Chicken";
        } else if (monster instanceof Cow) {
            points = world.worldSettings.getMonsterValue("Cow", cause);
            name = "Cow";
        } else if (monster instanceof Blaze) {
            points = world.worldSettings.getMonsterValue("Blaze", cause);
            name = "Blaze";
        } else if (monster instanceof Pig) {
            points = world.worldSettings.getMonsterValue("Pig", cause);
            name = "Pig";
        } else if (monster instanceof Sheep) {
            points = world.worldSettings.getMonsterValue("Sheep", cause);
            name = "Sheep";
        } else if (monster instanceof Snowman) {
            points = world.worldSettings.getMonsterValue("SnowGolem", cause);
            name = "Snow Golem";
        } else if (monster instanceof Squid) {
            points = world.worldSettings.getMonsterValue("Squid", cause);
            name = "Squid";
        } else if (monster instanceof Villager) {
            points = world.worldSettings.getMonsterValue("Villager", cause);
            name = "Villager";
        } else if (monster instanceof IronGolem) {
            points = world.worldSettings.getMonsterValue("IronGolem", cause);
            name = "Iron Golem";
        } else if (monster instanceof Witch) {
            points = world.worldSettings.getMonsterValue("Witch", cause);
            name = "Witch";
        }else if (monster instanceof Wither) {
            points = world.worldSettings.getMonsterValue("Wither", cause);
            name = "Wither";
        } else {
            return;
        }
        if (points == 0) {
            return;
        }
        points += PointManager.getPointsForEquipment(monster, world);
        
        int effectPenaltyPoints = Math.abs(points - PointManager.applyEffectPenalty(points, player.getActivePotionEffects(), world));
        points -= effectPenaltyPoints;
        
        if (!world.Score.containsKey(player.getName()) && !world.worldSettings.getBoolean(Setting.EnableSignup)) {
        	if (!world.isBanned(player.getUniqueId()) && !world.isKicked(player.getUniqueId()))
    		{
    			world.signUp(player.getUniqueId());
    		}
        }
        if (world.Score.containsKey(player.getName())) {
            int newscore = world.Score.get(player.getName()) + points;

            if (world.worldSettings.getBoolean(Setting.AnnounceLead)) {
                Entry<UUID, Integer> leadpoints = null;
                for (Entry<UUID, Integer> e : world.Score.entrySet()) {
                    if (leadpoints == null || e.getValue() > leadpoints.getValue() || (e.getValue() == leadpoints.getValue() && leadpoints.getKey().equals(player.getUniqueId()))) {
                        leadpoints = e;
                    }
                }
                
                if (leadpoints != null && newscore > leadpoints.getValue() && !leadpoints.getKey().equals(player.getName())) {
                    String message = world.worldSettings.getString(Setting.MessageLead);
                    message = message.replace("<Player>", player.getName());
                    message = message.replace("<Points>", String.valueOf(newscore));
                    message = message.replace("<World>", world.name);
                    
                    long timeInWorld = monster.getWorld().getTime();
                    if(timeInWorld >= world.worldSettings.getInt(Setting.AnnounceLeadInterval) + world.lastAnnounceTime)
                    {
                    	world.lastAnnounceTime = timeInWorld;
	                    if(world.worldSettings.getBoolean(Setting.AnnounceLeadEveryone))
	                    	Util.Broadcast(message);
	                    else
	                    	Util.BroadcastToParticipants(message);
                    }
                }
            }

            world.Score.put(player.getUniqueId(), newscore);

            world.properlyspawned.remove((Object) monster.getEntityId());
            
            if (world.worldSettings.getBoolean(Setting.ShowKillMessage)) 
            {
	            String message = world.worldSettings.getKillMessage(cause);
	            message = message.replace("<MobValue>", String.valueOf(points));
	            message = message.replace("<MobName>", name);
	            message = message.replace("<Points>", String.valueOf(newscore));
	            Util.Message(message, player);
	            
	            if (effectPenaltyPoints != 0)
	            {
	            	if (points == 0)
	            	{
	            		Util.SpamMessage(world.worldSettings.getString(Setting.KillMobUnderPotionNoPoints), player);
	            	}
	            	else
	            	{
	            		Util.SpamMessage(world.worldSettings.getString(Setting.KillMobUnderPotionSomePoints), player);
	            	}
	            }
            }
            if(world.worldSettings.getBoolean(Setting.ScoreboardEnabled))
            {
            	world.refreshScoreboards();
            }
        }
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
        if (world.worldSettings.getBoolean(Setting.DontCountMobsFromSpawners) && event.getSpawnReason().equals(SpawnReason.SPAWNER)) {
        	return;
        }
        world.properlyspawned.add(event.getEntity().getEntityId());
    }

    @EventHandler()
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getPlayer().getItemInHand().getTypeId() == Settings.globals.config.getInt(Setting.SelectionTool.getString(), 268)) {
            if (HuntZoneCreation.players.containsKey(event.getPlayer().getName())) {
                HuntZoneCreation.select(event.getPlayer(), event.getClickedBlock());
                event.setCancelled(true);
            }
        }
    }
}