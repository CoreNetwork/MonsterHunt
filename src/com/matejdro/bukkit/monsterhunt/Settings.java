package com.matejdro.bukkit.monsterhunt;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.potion.PotionEffectType;

public class Settings {
    public static YamlConfiguration globals;

    private YamlConfiguration config;

    private Map<String,String> potionEffectsMap = new HashMap<String, String>()
    		{
    		    {
    		        put("INCREASE_DAMAGE", "Strength");
    		        put("SPEED", "Speed");
    		        put("JUMP", "Jump");
    		        put("REGENERATION", "Regeneration");
    		        put("DAMAGE_RESISTANCE", "Resistance");
    		        put("FIRE_RESISTANCE", "FireResistance");
    		    }
    		};
    
    public Settings(File file) {
        if(file.exists())
        {
        	config = new YamlConfiguration();
        	try {
				config.load(file);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InvalidConfigurationException e) {
				Log.warning("Configuration file " + file.getName() + " is invalid. Swaping with global values.");
				config = globals;
			}
        }
        else
        {
        	config = globals;
        }
    }

    public int getInt(Setting setting) {
        Integer property = (Integer) config.get(setting.getString());
        if (property == null) {
            property = (Integer) globals.get(setting.getString());
        }
        return property;
    }
    
    public String getString(Setting setting) {
        String property = (String) config.get(setting.getString());
        if (property == null) {
            property = (String) globals.get(setting.getString());
        }
        return property;
    }

    public boolean getBoolean(Setting setting) {
        return config.getBoolean(setting.getString(), globals.getBoolean(setting.getString()));
    }

    public int getPlaceInt(Setting setting, int place) {
        Integer property = (Integer) config.get(setting.getString() + String.valueOf(place));
        if (property == null) {
            property = (Integer) globals.get(setting.getString() + String.valueOf(place));
        }
        return property;
    }

    public String getPlaceString(Setting setting, int place) {
        String property = (String) config.get(setting.getString() + String.valueOf(place));
        if (property == null) {
            property = (String) globals.get(setting.getString() + String.valueOf(place));
        }
        return property;
    }

    public int getMonsterValue(String mobname, String killer) {
        String setting = "Points.Mobs." + mobname + "." + killer;
        if (config.get(setting) != null) {
            return config.getInt(setting, 1);
        } else if (globals.get(setting) != null) {
            return globals.getInt(setting, 1);
        } else {
            setting = "Points.Mobs." + mobname + ".General";
            if (config.get(setting) != null) {
                return config.getInt(setting, 1);
            } else if (globals.get(setting) != null) {
                return globals.getInt(setting, 1);
            } else {
                return 0;
            }
        }
    }
    
    public String getEffectPenalty(String effect, int level)
    {
    	String potionAlias = potionEffectsMap.get(effect);
    	String setting = "Points.EffectPenalty." + potionAlias+level;
    	Util.Debug(setting);
    	if (config.get(setting) != null) {
    		return config.getString(setting);
    	} else if (globals.get(setting) != null) {
            return globals.getString(setting);
        } else
    	return "0";
    }
    
    public String getKillMessage(String cause) {
        String setting = "Messages.KillMessage" + cause;
        Util.Debug(setting);
        if (config.get(setting) != null) {
            return config.getString(setting);
        } else if (globals.get(setting) != null) {
            return globals.getString(setting);
        } else {
            setting = "Messages.KillMessageGeneral";
            if (config.get(setting) != null) {
                return config.getString(setting);
            } else if (globals.get(setting) != null) {
                return globals.getString(setting);
            } else {
                return "";
            }
        }
    }

	
}