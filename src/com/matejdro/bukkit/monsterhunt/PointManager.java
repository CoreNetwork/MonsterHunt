package com.matejdro.bukkit.monsterhunt;

import java.util.Collection;

import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

public class PointManager {

	public static int getPointsForEquipment(LivingEntity monster, MonsterHuntWorld world)
	{
		int equipmentPoints = 0;

		EntityEquipment eq = monster.getEquipment();
		
		if(eq.getArmorContents().length == 0 && eq.getItemInHand() == null)
			return 0;
		
		ItemStack helmet = eq.getHelmet();
		ItemStack chestplate = eq.getChestplate();
		ItemStack leggings = eq.getLeggings();
		ItemStack boots = eq.getBoots();
		ItemStack weapon = eq.getItemInHand();

		int leatherPoints = world.getSettings().getInt(Setting.EqLeather);
		int goldPoints = world.getSettings().getInt(Setting.EqGold);
		int ironPoints = world.getSettings().getInt(Setting.EqIron);
		int chainPoints = world.getSettings().getInt(Setting.EqChain);
		int diamondPoints = world.getSettings().getInt(Setting.EqDiamond);
		int shovelPoints = world.getSettings().getInt(Setting.EqShovel);
		int swordPoints = world.getSettings().getInt(Setting.EqSword);
		int enchantedArmorPoints = world.getSettings().getInt(Setting.EqEnchArmor);
		int enchantedSwordPoints = world.getSettings().getInt(Setting.EqEnchSword);
		int enchantedShovelPoints = world.getSettings().getInt(Setting.EqEnchShovel);
		int enchantedBowPoints = world.getSettings().getInt(Setting.EqEnchBow);
		
		//Helmet
		if(helmet != null && eq.getHelmetDropChance() < 1)
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
		if(chestplate != null && eq.getChestplateDropChance() < 1)
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
		if(leggings != null && eq.getLeggingsDropChance() < 1)
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
		if(boots != null && eq.getBootsDropChance() < 1)
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
		if(weapon != null && eq.getItemInHandDropChance() < 1)
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

	public static int applyEffectPenalty(int points, Collection<PotionEffect> potionEffects, MonsterHuntWorld world) {
		int newPoints = points;
		
		for (PotionEffect potionEffect : potionEffects) {
			String name = potionEffect.getType().getName();
			int level = potionEffect.getAmplifier() + 1;
			String penalty = world.getSettings().getEffectPenalty(name, level);
			if (penalty.charAt(penalty.length() - 1) == '%')
			{
				newPoints -= Math.round(points * Integer.parseInt(penalty.substring(0, penalty.length() -1)) / 100 );
			}
			else
			{
				newPoints -= Integer.parseInt(penalty);
			}
		}
		
		if (newPoints < 0)
		{
			newPoints = 0;
		}
		
		return newPoints;
	}
}
