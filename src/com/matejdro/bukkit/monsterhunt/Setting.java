package com.matejdro.bukkit.monsterhunt;

import java.util.ArrayList;

public enum Setting {

	EqLeather("Points.Equipment.Leather", 1),
	EqGold("Points.Equipment.Gold", 2),
	EqIron("Points.Equipment.Iron", 2),
	EqChain("Points.Equipment.Chain", 2),
	EqDiamond("Points.Equipment.Diamond", 4),
	EqShovel("Points.Equipment.Shovel", 1),
	EqSword("Points.Equipment.Sword", 2),
	EqEnchArmor("Points.Equipment.EnchantedArmor", 1),
	EqEnchSword("Points.Equipment.EnchantedSword", 1),
	EqEnchShovel("Points.Equipment.EnchantedShovel", 1),
	EqEnchBow("Points.Equipment.EnchantedBow", 2),
	  
	EnableReward("Rewards.EnableRewards", true),
    NumberOfWinners("Rewards.NumberOfWinners", 3),
    EnableRewardEveryonePermission("Rewards.EnableRewardEveryonePermission", false),
    RewardEveryone("Rewards.RewardEveryone.Enabled", false),
    RewardEveryoneIncludesWinners("Rewards.RewardEveryone.AlsoRewardWinners", true),
    MinimumPointsEveryone("Rewards.RewardEveryone.MinimumPoints", 1),
    RewardParametersEveryone("Rewards.RewardEveryone.RewardParameters", "3 1-1"),
    RewardCommandsEveryone("Rewards.RewardEveryone.Commands", new ArrayList<String>()),

    MinimumPointsPlace("MinimumPoints", "", false),
    RewardParametersPlace("RewardParameters", "", false),
    CommandsPlace("CommandsPlace", "", false),
    
	GiveRewardsImmediatelly("Rewards.GiveRewardImmediatelly", true),
	
    EnabledWorlds("EnabledWorlds", MonsterHunt.instance.getServer().getWorlds().get(0).getName()),
    Hunts("Hunts", ""),
    MinimumPlayers("MinimumPlayers", 2),
    StartChance("StartChance", 100),
    SkipDays("SkipDays", 0),
    HuntLimit("HuntLimit", -1),
    StartTime("StartTime", 13000),
    EndTime("EndTime", 23600),
    DeathPenalty("DeathPenalty", 30),

    EnableSignup("EnableSignup", true),
    SignUpPeriodTime("SignUpPeriodTime", 5),
    
    AllowSignUpAfterStart("AllowSignUpAfterStart", false),
    SkipToIfFailsToStart("SkipToIfFailsToStart", -1),
    
    PurgeAllHostileMobsOnStart("PurgeAllHostileMobsOnStart", true),
    DontCountNamedMobs("DontCountNamedMobs", true),
    DontCountMobsFromSpawners("DontCountMobsFromSpawners", true),
    AnnounceSignUp("AnnounceSignUp", true),
    AnnounceLead("AnnounceLead", true),
    AnnounceLeadEveryone("AnnounceLeadEveryone", true),
    AnnounceLeadInterval("AnnounceLeadInterval", 0),
    ShowKillMessage("ShowKillMessage", true),
    ScoreboardEnabled("ScoreboardEnabled", true),
    
    TellTime("TellTime", true),
    
    HuntZoneMode("HuntZoneMode", false),
    SelectionTool("SelectionTool", 268),
    HuntZoneWorld("HuntZone.World", MonsterHunt.instance.getServer().getWorlds().get(0).getName()),
    HuntZoneFirstCorner("HuntZone.FirstCorner", "0,0,0"),
    HuntZoneSecondCorner("HuntZone.SecondCorner", "0,0,0"),
    HuntZoneTeleportLocation("HuntZone.TeleportLocation", "0,0,0"),
    
 
    StartMessage("Messages.StartMessage", "&2Monster Hunt have started in world <World>! Go kill those damn mobs!"),
    FinishMessageWinnersHeader("Messages.FinishMessageWinners.Header", "Sun is rising, so monster Hunt is finished in world <World>! Winners of the today's match are: [NEWLINE] "),
    WinnerMessagePlace("WinMessage","",false),
    FinishMessageWinnersFooter("Messages.FinishMessageWinners.Footer", "Congratulations!"),
    KillMessageGeneral("Messages.KillMessageGeneral", "You got <MobValue> points from killing that <MobName>. You have <Points> points so far. Keep it up!"),
    KillMessageWolf("Messages.KillMessageWolf", "You got <MobValue> points because your wolf killed <MobName>. You have <Points> points so far. Keep it up!"),
    KillMessageArrow("Messages.KillMessageArrow", "You got only <MobValue> points because you used bow when killing <MobName>. You have <Points> points so far. Keep it up!"),
    KillMessageSnowball("Messages.KillMessageSnowball", "You got <MobValue> points for killing <MobName> with a snowball!. You have <Points> points so far. Keep it up!"),
    KillMobUnderPotionNoPoints("Messages.KillMobUnderPotionNoPoints", "&cThis kill was without honor. Hunt without Beacon or potions!"),
    KillMobUnderPotionSomePoints("Messages.KillMobUnderPotionSomePoints", "&cThis kill had little honor. Hunt without Beacon or potions!"),
    RewardMessage("Messages.RewardMessage", "Congratulations! You have received <Items>"),
    DeathMessage("Messages.DeathMessage", "You have died, so your Monster Hunt score is reduced by 30%. Be more careful next time!"),
    SignUpBeforeHuntMessage("Messages.SignupBeforeHuntMessage", "You have signed up for the next hunt in world <World>!"),
    SignUpAfterHuntMessage("Messages.SignupAtHuntMessage", "You have signed up for the hunt in in world <World>. Now hurry and kill some monsters!"),
    KickedPlayerSignUp("Messages.KickedPlayerSignUp", "You have been kicked. You can not sign up for this hunt."),
    BannedPlayerSignUp("Messages.BannedPlayerSignUp", "You have been banned from The Hunt."),
    AnnounceKick("Messages.AnnounceKick", "<PlayerName> has been kicked out of The Hunt!"),
    AnnounceBan("Messages.AnnounceBan", "<PlayerName> has been banned from The Hunt! Reason : <Reason>"),
    HighScoreMessage("Messages.HighScoreMessage", "You have reached a new high score: <Points> points!"),
    FinishMessageNotEnoughPoints("Messages.FinishMessageNotEnoughPoints", "Sun is rising, so monster Hunt is finished in world <World>! Unfortunately nobody killed enough monsters, so there is no winner."),
    FinishMessageNotEnoughPlayers("Messages.FinishMessageNotEnoughPlayers", "Sun is rising, so monster Hunt is finished in world <World>! Unfortunately there were not enough players participating, so there is no winner."),
    MessageSignUpPeriod("Messages.MessageSignUpPeriod", "Sharpen your swords, strengthen your armor and type /hunt, because Monster Hunt will begin in several mintues in world <World>!"),
    MessageTooLateSignUp("Messages.MessageTooLateSignUp", "Sorry, you are too late to sign up. More luck next time!"),
    MessageAlreadySignedUp("Messages.MessageAlreadySignedUp", "You are already signed up!"),
    MessageStartNotEnoughPlayers("Messages.MessageStartNotEnoughPlayers", "Monster Hunt was about to start, but unfortunately there were not enough players signed up. "),
    MessageHuntStatusNotActive("Messages.MessageHuntStatusNotActive", "Hunt is currently not active anywhere"),
    MessageHuntStatusHuntActive("Messages.MessageHuntStatusHuntActive", "Hunt is active in <Worlds>"),
    MessageHuntStatusLastScore("Messages.MessageHuntStatusLastScore", "Your last score in this world was <Points> points"),
    MessageHuntStatusNotInvolvedLastHunt("Messages.MessageHuntStatusNotInvolvedLastHunt", "You were not involved in last hunt in this world"),
    MessageHuntStatusNoKills("Messages.MessageHuntStatusNoKills", "You haven't killed any mob in this world's hunt yet. Hurry up!"),
    MessageHuntStatusCurrentScore("Messages.MessageHuntStatusCurrentScore", "Your current score in this world's hunt is <Points> points! Keep it up!"),
    MessageHuntStatusTimeReamining("Messages.MessageHuntStatusTimeReamining", "Keep up the killing! You have only <Timeleft>% of the night left in this world!"),
    MessageLead("Messages.MessageLead", "<Player> has just taken over lead with <Points> points!"),
    MessageHuntTeleNoHunt("Messages.MessageHuntTeleNoHunt", "You cannot teleport to hunt zone when there is no hunt!"),
    MessageHuntTeleNotSignedUp("Messages.MessageHuntTeleNotSignedUp", "You cannot teleport to hunt zone if you are not signed up to the hunt!"),
    MessageNoRewardToClaim("Messages.NoRewardToClaim", "You don't have any rewards waiting to claim!"),
    SignUpAnnouncement("Messages.SignUpAnnouncement", "<Player> has signed up for the hunt in world <World>!"),
	MessageNoPermission("Messages.NoPermission", "No permission!"),
	MessageBoughtHunts("Messages.BoughtHunts", "You have successfully bought <Amount> hunt<PluralS>! Total hunts on your account: <TotalHunts>."),
	MessageCheckHunts("Messages.CheckHunts", "You currently have <TotalHunts> hunt<PluralS> left to start."),
	MessageCantScheduleHunt("Messages.CantScheduleHunt", "Sorry, you don't have any hunts bought to start."),
	MessageHuntScheduled("Messages.HuntScheduled", "You just bought a hunt for entire server! It will start at next sunset in approximately <Time>."),
	MessageHuntScheduledMultiple("Messages.HuntScheduledMultiple", "You just bought a hunt for entire server! There are still previous hunts left to start, so one you bought will start after <QueueLength> days in approximately <Time>."),
	MessageHuntScheduledAnnouncement("Messages.HuntScheduledAnnouncement", "Player <Player> just bought a hunt for entire server! It will start at next sunset in approximately <Time>."),
	MessageHuntScheduledMultipleAnnouncement("Messages.HuntScheduledAnnouncementMultiple", "Player <Player> just bought another hunt for entire server! That means server will enjoy hunts for next <QueueLength> nights!"),


    UseMySQL("Database.UseMySQL", false),
    MySQLConn("Database.MySQLConn", "jdbc:mysql://localhost:3306/minecraft"),
    MySQLUsername("Database.MySQLUsername", "root"),
    MySQLPassword("Database.MySQLPassword", "password"),

    Debug("Debug", false);

    private String name;
    private Object def;
    private boolean WriteDefault;

    private Setting(String Name, Object Def) {
        name = Name;
        def = Def;
        WriteDefault = true;
    }

    private Setting(String Name, Object Def, boolean WriteDefault) {
        name = Name;
        def = Def;
        this.WriteDefault = WriteDefault;
    }

    public String getString() {
        return name;
    }

    public Object getDefault() {
        return def;
    }

    public boolean writeDefault() {
        return WriteDefault;
    }
}
