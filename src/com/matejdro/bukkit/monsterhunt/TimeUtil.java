package com.matejdro.bukkit.monsterhunt;

public class TimeUtil
{
    public static int getTimeDifference(int start, int end)
    {
        if (end >= start)
            return end - start;
        else
            return (2400 - start) + end;
    }
}
