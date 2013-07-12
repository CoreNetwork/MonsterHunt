MonsterHunt
===========

Monster Hunt is an event happening at night. It uses natural monster spawning, competitive aspect and a set of defined prizes to convince people to not camp in safety during the night but rather go out and kill monsters for points.

* Download [latest build](http://build.mcnsa.com:8080).
* Problems, bugs, suggestions? [Open an issue](https://github.com/MCNSA/MonsterHunt/issues).

## Player commands

|**Command**|**Permission Node**|**Description**|
|:------|:------:|:----------|
|`/hunt [<world>]`|`monsterhunt.usercmd.hunt`|Sign up for the hunt in specified world. If no world is specified it will use your current world. Additionally, if there is only one world enabled you can skip the world name.|
|`/huntscore`|`monsterhunt.usercmd.huntscore`|Display your highest score.|
|`/huntscore <player>`|`monsterhunt.usercmd.huntscore`|Display player’s highest score.|
|`/huntscore rank`|`monsterhunt.usercmd.huntscore`|Display your position on the global scoreboard.|
|`/huntscore top`|`monsterhunt.usercmd.huntscore`|Display the global scoreboard (5 places).|
|`/huntscore top <places>`|`monsterhunt.usercmd.huntscore`|Display the global scoreboard (number of places defined by the last parameter).|
|`/huntstatus`|`monsterhunt.usercmd.huntstatus`|Display your score during current hunt and possibly, time left (if enabled in config).|
|`/hunttele`|`monsterhunt.usercmd.hunttele`|Teleport to hunt zone, if it’s been enabled (as opposed to using worlds to run hunts).|
	
## Administrator commands

|**Command**|**Permission Node**|**Description**|
|:------|:------:|:----------|
|`/huntstart [<world>]`|`monsterhunt.admincmd.huntstart`|Manually start the hunt. This type of hunt will not end on its own. If you use more than one world for hunting, you have to provide which world to start the hunt in.|
|`/huntstop [<world>]`|`monsterhunt.admincmd.huntstop`|Manually stop the hunt. Like with `/huntstart`, you have to provide world name if running it in more than one world.|
|`/huntzone`|`monsterhunt.admincmd.huntzone`|Select border and teleport point for hunt zone (if you configured to use it instead world/worlds).|
|`/huntreload`|`monsterhunt.admincmd.huntreload`|Reload configuration from the file. Harmless, it will only stop an ongoing hunt if you remove its world from config.|

## Additional permission nodes

* `monsterhunt.rewardeverytime` – always reward the owner regardless of their hunt score.
* `monsterhunt.noteleportrestrictions` – allow the owner to teleport to hunt zone even if hunt isn’t on.

## Configuration

Configuration consists of one or more .yml config files. The only required and created on start up is `global.yml`. You can create additional config for every world in `<world_name>.yml` file. World config will override the global one. You can ovveride any number of settings from `global.yml` in each world config.


#### Top to bottom explanation of global.yml

``` yaml
Points:
  Mobs:
    Mob:
      DamageKind: Points
  Equipment:
    Leather: 1
    Gold: 1
    Iron: 3
    Chain: 1
    Diamond: 4
    Shovel: 1
    Sword: 2
    EnchantedArmor: 2
    EnchantedSword: 1
    EnchantedShovel: 1
    EnchantedBow: 2
```

Use this to balance how many points players will get for certain mob types (monsters, bosses, players (if PvP is enabled) and even animals).

Possible mob types (world restriction applies, so killing a Ghast in Nether while hunt goes on in the Overworld won’t yield any points):

1. Overworld monsters: `Zombie`, `Skeleton`, `Creeper`, `ElectrifiedCreeper`, `Spider`, `CaveSpider`, `Silverfish`, `Slime`, `WildWolf`, `Enderman`, `Witch`.
2. Nether monsters: `Ghast`, `ZombiePigman`, `MagmaCube`, `Blaze`, `WitherSkeleton`.
3. Animals: `MushroomCow`, `Chicken`, `Cow`, `Pig`, `Sheep`, `Squid`, `Villager`, `SnowGolem`, `IronGolem`, `TamedWolf`.
4. PvP: `Player`
5. Bosses: `EnderDragon`, `Wither`, `Giant`.

Possible kinds of damage:

1. `General` – general damage (checked last)
2. `Arrow` – bow
3. `Snowball` – snowball (damages `EnderDragon`, `Blaze`)
4. `'id'` – item id number, always in `'` apostrophes. Examples:
   1. `'283'` – Golden sword.
   2. `'275'` – Stone axe.
   3. `'0'` – Bare fists.
   4. etc

Extra points awarded for mobs in armor or using special weapons.

---
``` yaml
Rewards:
```

Section describing rewards and reward settings.

``` yaml
  EnableReward: true
```

Whether to use rewards at all.

``` yaml
  NumberOfWinners: 5
```

How many top winners there should be.

``` yaml
  EnableRewardEveryonePermission: false
```

Whether to give prizes to everyone besides top winners, requiring a `monsterhunt.rewardeverytime` permission.

``` yaml
  RewardEveryone: true
```

Whether to give prizes to everyone besides top winners.


``` yaml
  MinimumPointsEveryone: 1
```

Whether to require minimal score to be eligible for a reward.

``` yaml
  RewardParametersEveryone: 288 1
```

Reward definition for everyone besides top winners. [Reward parameter syntax](http://dev.bukkit.org/bukkit-plugins/monsterhunt/pages/reward-parameters/).

``` yaml
RewardParametersPlace1: 371 32-48
MinimumPointsPlace1: 150

RewardParametersPlace2: 371 24-32
MinimumPointsPlace2: 100

RewardParametersPlace3: 371 12-18
MinimumPointsPlace3: 75

RewardParametersPlace4: 371 6-9
MinimumPointsPlace4: 50

RewardParametersPlace5: 371 1-3
MinimumPointsPlace5: 30
```

[Reward parameter syntax](http://dev.bukkit.org/bukkit-plugins/monsterhunt/pages/reward-parameters/).
Make sure players get minimum score before giving them their prize. Pattern is `MinimumPointsPlaceX` where max `X` is defined by `NumberOfWinners` (described above).

---
``` yaml
EnabledWorlds: world
```

Names of worlds where you want to enable hunts. They are exclusive and you have to be in the particular world to get points for killing mobs. Example to enable all default worlds: `EnabledWorlds: world, world_nether, world_the_end`.

``` yaml
MinimumPlayers: 3
```

How many players need to sign up for the hunt to begin.

``` yaml
StartChance: 35
```

Percent chance for a hunt to begin on a particular night.

``` yaml
SkipDays: 1
```

How many nights to skip after hunt happened – even if `StartChance` is `100` percent hunt won’t happen if you set this option to `1` or higher.

``` yaml
StartTime: 13400
EndTime: 23400
```

Number is an integer between 0 and 24000, inclusive, where 0 is dawn, 6000 midday, 12000 dusk and 18000 midnight. `EndTime` must be bigger than `StartTime`.

``` yaml
DeathPenalty: 100
```

How many percent of points to remove from player’s score upon death.

``` yaml
EnableSignup: true
```

Whether to give players time to sign up before hunt begins.

``` yaml
SignUpPeriodTime: 1
```

How many minutes to give players to sign up before the hunt.

``` yaml
AllowSignUpAfterStart: true
```

Whether players can be late to sign up for the hunt?

``` yaml
SkipToIfFailsToStart: -1
```

How much time to skip if hunt fails to begin. Use `-1` to disable. One minute in game is `1200`.

``` yaml
PurgeAllHostileMobsOnStart: true
```

Delete all mobs when hunt begins to prevent any grinder to be used to player’s advantage. Skips named mobs and mobs wearing picked up armor/weapon.

``` yaml
DontCountNamedMobs: true
```

Whether to give points for special mobs (`CustomName` via NBT) in your custom structures. Useful to prevent grinding.

``` yaml
DontCountMobsFromSpawners: true
```

Whether to give points for mobs spawned from mob spawner blocks. Useful to prevent grinding.

``` yaml
AnnounceSignUp: false
```

Whether to display in chat people who signed up for hunt.

``` yaml
AnnounceLead: true
```

Whether to display in chat if someone become a leader in points.

``` yaml
AnnounceLeadEveryone: false
```

Whether to send leader announcement (`AnnounceLead`) to everyone on the server or just players participating in hunt.

``` yaml
AnnounceLeadInterval: 25
```

Interval in seconds for leader announcement (`AnnounceLead`) to be displayed in chat. Use to prevent excessive chat spam.

``` yaml
ShowKillMessages: true
```

Whether to send message about points for kill in chat.

``` yaml
ScoreboardEnabled: true
```

Whether to display scoreboard with ongoing hunt scores.

``` yaml
TellTime: false
```

Whether or not to tell players how much time they have to hunt’s end when they use `/huntstatus` command.

``` yaml
HuntZoneMode: false
```

Whether to enable hunt zone.

``` yaml
SelectionTool: 268
```

Id number of the item you need to use to select hunt zone and teleport point when doing `/huntzone`.

``` yaml
HuntZone:
  World: world
  FirstCorner: 0,0,0
  SecondCorner: 0,0,0
  TeleportLocation: 0,0,0
```

Hunt zone specifications.

``` yaml
Messages:
```

All the messages used in Monster Hunt.

