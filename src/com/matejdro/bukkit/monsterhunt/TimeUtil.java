package com.matejdro.bukkit.monsterhunt;

public class TimeUtil
{
    public static int getTimeDifference(int start, int end)
    {
        if (end >= start)
            return end - start;
        else
            return (24000 - start) + end;
    }

    public static String formatTimeTicks(int ticks)
    {
        return formatTimeSeconds(ticks / 20);

    }

    public static String formatTimeSeconds(int seconds)
    {
        if (seconds > 60)
            return formatTimeMinutes((int) Math.round(seconds / 60.0));

        String out = Integer.toString(seconds).concat(" second");
        if (seconds != 1)
            out = out.concat("s");
        return out;
    }

    public static String formatTimeMinutes(int minutes)
    {
        if (minutes > 60)
            return formatTimeHours((int) Math.round(minutes / 60.0));

        String out = Integer.toString(minutes).concat(" minute");
        if (minutes != 1)
            out = out.concat("s");
        return out;

    }


    private static String formatTimeHours(int hours)
    {
        String out = Integer.toString(hours).concat(" hour");
        if (hours != 1)
            out = out.concat("s");
        return out;

    }

}
