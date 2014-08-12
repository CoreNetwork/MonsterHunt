package com.matejdro.bukkit.monsterhunt;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class BougthHuntsStorage {
	public static int getNumberOfBoughtHunts(UUID player)
	{
		int number = 0;
		
		try
		{
			PreparedStatement statement = InputOutput.getConnection().prepareStatement("SELECT Amount FROM monsterhunt_BoughtHunts WHERE UUID = ?");
			statement.setString(1, player.toString());
			ResultSet set = statement.executeQuery();
			if (set.next())
			{
				number = set.getInt(1);
			}
			
			statement.close();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		
		return number;
	}
	
	
	public static void setNumberOfBoughtHunts(UUID player, int amount)
	{
		try
		{
			PreparedStatement statement = InputOutput.getConnection().prepareStatement("REPLACE INTO monsterhunt_BoughtHunts (UUID, Amount) VALUES (?,?)");
			statement.setString(1, player.toString());
			statement.setInt(2, amount);
			statement.executeUpdate();
			statement.close();
			InputOutput.getConnection().commit();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}	
}
