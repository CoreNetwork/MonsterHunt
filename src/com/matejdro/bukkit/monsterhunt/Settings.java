package com.matejdro.bukkit.monsterhunt;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

public class Settings {
    public static YamlConfiguration globals;

    private YamlConfiguration config;
    

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
        String setting = "Value." + mobname + "." + killer;
        if (config.get(setting) != null) {
            return config.getInt(setting, 1);
        } else if (globals.get(setting) != null) {
            return globals.getInt(setting, 1);
        } else {
            setting = "Value." + mobname + ".General";
            if (config.get(setting) != null) {
                return config.getInt(setting, 1);
            } else if (globals.get(setting) != null) {
                return globals.getInt(setting, 1);
            } else {
                return 0;
            }
        }
    }
    
    public double getEquipmentValue(String eqName) {
    	String setting = "Equipment." + eqName;
    	if(config.get(setting) != null){
    		return config.getDouble(setting);
    	} else if (globals.get(setting) != null){
    		return globals.getDouble(setting);
    	}
    	else {
    		return 0;
    	}
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