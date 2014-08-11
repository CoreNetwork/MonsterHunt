package com.matejdro.bukkit.monsterhunt;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

public class Settings {
	
	public static Settings globals;

	public YamlConfiguration config;
	private Settings parent;
	private File file;

	public static void loadGlobals(File file) throws FileNotFoundException, IOException, InvalidConfigurationException
	{
		globals = new Settings(new YamlConfiguration());
		globals.config.load(file);
	}

	public Settings(File file, Settings parent)
	{
		this.file = file;
		this.parent = parent;
		config = new YamlConfiguration();
		if (file.exists())
		{
			try
			{
				config.load(file);
			} catch (IOException e)
			{
				e.printStackTrace();
			} catch (InvalidConfigurationException e)
			{
				Log.warning("Configuration file " + file.getName() + " is invalid. Swaping with parent values.");
			}
		}
		else
		{
			Log.warning("Configuration file " + file.getName() + " is missing. Swaping with parent values.");
		}
	}

	public Settings(YamlConfiguration config)
	{
		this.config = config;
	}

	
	public Object getObject(Setting setting)
	{
		Object result = getObject(setting.getString());
		if (result == null)
			return setting.getDefault();
		
		return result;
	}
	
	public Object getObject(String path)
	{		
		if (config.contains(path))
		{
			return config.get(path);
		}
		else if (parent == null)
		{
			Log.warning("Missing config option: " + path);
			return null;
		}
		else
		{
			return parent.getObject(path);
		}
	}
		
	public int getInt(Setting setting)
	{
		return (Integer) getObject(setting);
	}

	public String getString(Setting setting)
	{
		return (String) getObject(setting);
	}

	public boolean getBoolean(Setting setting)
	{
		return (Boolean) getObject(setting);
	}

	public int getPlaceInt(Setting setting, int place)
	{
		return (Integer) getObject("Rewards.Place" +  String.valueOf(place) + "." + setting.getString());
	}

	public String getPlaceString(Setting setting, int place)
	{
		return (String) getObject("Rewards.Place" +  String.valueOf(place) + "." + setting.getString());
	}
	
	public int getMonsterValue(String mobname, String killer)
	{
		String setting = "Points.Mobs." + mobname + "." + killer;
		String generalSetting = "Points.Mobs." + mobname + ".General";

		Integer mobValue = (Integer) getFromHierarchy(setting);
		Integer generalMobValue = (Integer) getFromHierarchy(generalSetting);
		
		if (mobValue != null)
			return mobValue;
		else if (generalMobValue != null)
			return generalMobValue;
		else 
			return 0;
	}

	public String getEffectPenalty(String effect, int level)
	{
		String setting = "Points.EffectPenalty." + effect.toLowerCase() + "_" + level;

		Object penaltyO = getFromHierarchy(setting);
		
		
		if (penaltyO != null)
		{
			if (penaltyO instanceof Integer)
				return (Integer) penaltyO + "";
			else
				return (String) penaltyO;
		}
		else
			return "0";
	}

	public String getKillMessage(String cause)
	{
		String setting = "Messages.KillMessage" + cause;
		String generalSetting = "Messages.KillMessageGeneral";
		
		String killMessage = (String) getFromHierarchy(setting);
		String generalKillMessage = (String) getFromHierarchy(generalSetting);
		
		if (killMessage != null)
			return killMessage;
		else if (generalKillMessage != null)
			return generalKillMessage;
		else 
			return "";
		
	}
	
	private Object getFromHierarchy(String setting)
	{
		if (config.isSet(setting))
		{
			return config.get(setting);
		} 
		else if (parent != null)
		{
			return parent.getFromHierarchy(setting);
		} 
		else 
			return null;
	}
	
	public void setInt(Setting setting, int value)
	{
		config.set(setting.getString(), value);
	}
	
	public void save()
	{
		File file = this.file;
		
		if (!file.exists())
			file = parent.file;
		
		try
		{
			config.save(file);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<HuntSpecification> getListOfHunts()
	{
		List<Map<String, Object>> huntsRaw = (List<Map<String, Object>>) config.getList("Hunts");
		List<HuntSpecification> hunts = new ArrayList<HuntSpecification>();
		if (huntsRaw != null)
		{
	        for(Map<String, Object> huntSpecRaw : huntsRaw)
	        {
	        	String name = (String) huntSpecRaw.get("Name");
	        	String displayName = (String) huntSpecRaw.get("DisplayName");
	        	int chance = (Integer) huntSpecRaw.get("Chance");
	        	
	        	String filename = (String) huntSpecRaw.get("Name");
	        	Settings huntSettings;
	        	if(name.equals("Default"))
	        		huntSettings = this;
	        	else
	        	huntSettings = new Settings(new File("plugins" + File.separator + "MonsterHunt" + File.separator, filename + ".yml"), this);
	        	
	        	hunts.add(new HuntSpecification(name, displayName, chance, huntSettings));
	        }
		}
		return hunts;
	}
	
}