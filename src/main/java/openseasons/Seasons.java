package openseasons;

import net.minecraft.block.LeavesBlock;

public enum Seasons {
    SUMMER("Summer", 1f, 0f),
    FALL("Fall", 0.2f, 0.8f),
    WINTER("Winter", 0f, 0.5f),
    SPRING("Spring", 0.6f, 0.6f);

    private Seasons next;
    private final String name;
    private final float temperature;
    private final float humidity;

    static{
        SUMMER.next = FALL;
        FALL.next = WINTER;
        WINTER.next = SPRING;
        SPRING.next = SUMMER;
    }

    Seasons(String name, float temperature, float humidity){
        this.name = name;
        this.temperature = temperature;
        this.humidity = humidity;
    }

    @Override
    public String toString() { return this.name; }

    public Seasons next() { return this.next; }

    public float getTemperature() {
        return temperature;
    }

    public float getHumidity() { return humidity;}
}
