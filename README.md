# OpenSeasons
###  Minecraft Seasons!
A minecraft mod that colors foliage and grass according to the current season.

### Building
In the project folder, open the console and run:
```
./gradlew build
```

The output ```.jar``` file should be in the ``` ./build/libs ``` folder.

### Requirements/Dependencies
* Java 17 or higher
* [Fabric Loader](https://fabricmc.net/use/installer/) 0.13.3 or higher
* Minecraft 1.18.2
* The [Fabric API](https://www.curseforge.com/minecraft/mc-mods/fabric-api)

### Customization

After booting up a world with the mod for the first time, an ``openseasons.json`` file with the default 
configuration should appear in the ``.minecraft/config`` folder.

This file can be edited to customize the mod.

#### Fields

| Field          | Type   | Description                                                                    |
|----------------|--------|--------------------------------------------------------------------------------|
| ``current_day``    | ``int``    | Records the current day of a season.                                           |
| ``current_season`` | ``String`` | Records the current season. Can be ``Summer``,``Fall``,``Winter`` or ``Spring`` |
| ``max_day_count``  | ``int``    | The amount of days each season lasts.                                          |
| ``summer_color``   | ``String`` | The decimal color code for Summer foliage                                      |
| ``fall_color``     | ``String`` | The decimal color code for Fall foliage                                        |
| ``winter_color``   | ``String`` | The decimal color code for Winter foliage                                      |
| ``spring_color``   | ``String`` | The decimal color code for Spring foliage                                      |


##### Example
###### openseasons.json
````
{
    "current_day":5,
    "current_season":"Fall",
    "max_day_count":10,
    "summer_color":"5538376",
    "fall_color":"15954710",
    "winter_color":"14921613",
    "spring_color":"14145310"
}
````