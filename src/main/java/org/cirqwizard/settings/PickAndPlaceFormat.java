package org.cirqwizard.settings;

/**
 * Created by simon on 21.10.15.
 */
public enum PickAndPlaceFormat
{
    EAGLE("(?<name>\\S+)\\s+(?<x>\\d+.?\\d*)\\s+(?<y>\\d+.?\\d*)\\s+(?<angle>\\d+)\\s+(?<value>\\S+)\\s*(?<package>\\S+)?", "Eagle"),
    ALTIUM("(?<name>\\S+)\\s+(?<package>\\S+)\\s+(?<x>-?\\d+.?\\d*)mm\\s+(?<y>-?\\d+.?\\d*)mm\\s+\\S+\\s+\\S+\\s+\\S+\\s+\\S+\\s+\\S+\\s+(?<angle>-?\\d+.\\d*)\\s+(?<value>\\S+)\\s*", "Altium Designer"),
    ULTIBOARD("\"(?<name>\\S+)\",\"(?<value>\\S+)\",\"(?<package>\\S+)\",\"(?<x>\\d+.?\\d*)\",\"(?<y>\\d+.?\\d*)\",\"(?<angle>\\d+)\",\"TOP\",\"SMD\"", "UltiBoard"),
    DESIGNSPARK("\"(?<name>\\S+)\",\"(?<package>\\S+)\",\"Top\",\"(?<x>\\d+.?\\d*)\",\"(?<y>\\d+.?\\d*)\",\"(?<angle>\\d+.?\\d*)\"(?<value>.*)", "DesignSpark"),
    KICAD("(?<name>\\S+),.*,(?<value>.+?),(?<package>\\S+?),(?<x>-?\\d+.?\\d*),(?<y>-?\\d+.?\\d*),(?<angle>\\d+.?\\d*)", "KiCAD"),
    DIPTRACE("(?<name>\\S+),(?<package>.+?),(?<x>-?\\d+.?\\d*),(?<y>-?\\d+.?\\d*),Top,(?<angle>\\d+.?\\d*),(?<value>\\S*)", "DipTrace"),
    EASYPC("(?<name>\\S+),(?<x>-?\\d+.?\\d*),(?<y>-?\\d+.?\\d*),(?<angle>\\d+.?\\d*),(?<value>\\S*),(?<package>.*)", "EasyPC"),
    PROTEUS("\"(?<name>\\S+)\",\"(?<value>\\S*)\",\"(?<package>.*)\",TOP,(?<x>-?\\d+.?\\d*),(?<y>-?\\d+.?\\d*),(?<angle>\\d+.?\\d*)", "Proteus");

    private String regex;
    private String name;

    PickAndPlaceFormat(String regex, String name)
    {
        this.regex = regex;
        this.name = name;
    }

    public String getRegex()
    {
        return regex;
    }

    public String getName()
    {
        return name;
    }


    @Override
    public String toString()
    {
        return getName();
    }

    public static PickAndPlaceFormat forName(String name)
    {
        for (PickAndPlaceFormat f : values())
            if (f.getName().equals(name))
                return f;
        throw new IllegalArgumentException("No such format: " + name);
    }
}
