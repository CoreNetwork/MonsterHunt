package com.matejdro.bukkit.monsterhunt;

public class HuntSpecification {

	private String name;
	private String displayName;
	private int chance;
	private Settings settings;
	
	public HuntSpecification(String name, String displayName, int chance, Settings settings)
	{
		this.name = name;
		this.displayName = displayName;
		this.chance = chance;
		this.settings = settings;
	}
	public String getName()
	{
		return name;
	}
	public String getDisplayName()
	{
		return displayName;
	}
	public int getChance()
	{
		return chance;
	}
	public Settings getSettings()
	{
		return settings;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		return obj instanceof HuntSpecification && ((HuntSpecification)obj).name.equals(name);
	}
	@Override
	public int hashCode() {
	    return name.hashCode();
	}
	@Override
	public String toString()
	{
		return  "\nName: " + name + "\n" +
				"Display Name: " + displayName + "\n" +
				"Chance: " + chance;
	}
}
