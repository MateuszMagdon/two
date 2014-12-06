package pl.agh.edu.model;

/**
 * Created by Michal
 * 2014-12-06.
 */
public class Map {

    private int width = 1100;
    private int height = 900;

    private static Map instance;

    private Map() {}

    public static Map getMap() {
        if(instance == null) {
            instance = new Map();
        }

        return instance;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
