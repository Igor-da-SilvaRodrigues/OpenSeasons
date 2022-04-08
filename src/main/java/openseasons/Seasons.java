package openseasons;

import net.minecraft.world.biome.Biome;
import openseasons.util.Keys;

public enum Seasons {
    SUMMER("Summer", 0.6f, 0f,0x548248),
    FALL("Fall", 0.2f, 0.8f, 0xf37316),
    WINTER("Winter", 0.0f, 0.5f,0xe3af8d),
    SPRING("Spring", 0.2f, 0.6f,0xd7d71e);

    private Seasons next;
    private final String name;
    private final float temperature;
    private final float humidity;
    private int foliagecolor;

    static{
        SUMMER.next = FALL;
        FALL.next = WINTER;
        WINTER.next = SPRING;
        SPRING.next = SUMMER;
    }

    Seasons(String name, float temperature, float humidity, int foliagecolor){
        this.name = name;
        this.temperature = temperature;
        this.humidity = humidity;
        this.foliagecolor = foliagecolor;
    }

    @Override
    public String toString() { return this.name; }

    public Seasons next() { return this.next; }

    public float getTemperature() {
        return temperature;
    }

    public float getHumidity() { return humidity;}

    public int getFoliagecolor(){return foliagecolor;}

    public void setFoliagecolor(int newcolor){
        foliagecolor = newcolor;
    }
    public void setFoliagecolor(String newColor){
        setFoliagecolor(Integer.parseInt(newColor));
    }

    public Biome.Precipitation getPrecipitation() {
        return (this == Seasons.WINTER)? Biome.Precipitation.SNOW : Biome.Precipitation.RAIN;
    }

    public static Seasons getSeason(String id){
        switch (id.toLowerCase()){
            case "summer":
                return Seasons.SUMMER;
            case "fall":
                return Seasons.FALL;
            case "winter":
                return Seasons.WINTER;
            case "spring":
                return Seasons.SPRING;
            default:
                OpenSeasonsMod.LOGGER.error(Keys.MOD_ID +": Invalid Season type! Assuming Summer");
                return SUMMER;
        }
    }


    public static boolean hasSeason(String id){
        for (Seasons season : Seasons.values()){
            if ( season.toString().equalsIgnoreCase(id) ) return true;
        }
        return false;
    }
}