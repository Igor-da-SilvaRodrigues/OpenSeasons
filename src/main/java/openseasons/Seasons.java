package openseasons;

public enum Seasons {
    SPRING("Spring"),
    SUMMER("Summer"),
    FALL("Fall"),
    WINTER("Winter");

    private final String name;
    Seasons(String name){this.name = name;}

    @Override
    public String toString() {
        return this.name;
    }
}
