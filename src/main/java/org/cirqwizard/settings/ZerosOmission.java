package org.cirqwizard.settings;

/**
 * Created by simon on 20.06.17.
 */
public enum ZerosOmission
{
    LEADING_OMITTED(false, "Leading omitted"), TRAILING_OMITTED(true, "Trailing omitted");

    ZerosOmission(boolean leadingZeros, String name)
    {
        this.leadingZeros = leadingZeros;
        this.name = name;
    }

    private boolean leadingZeros;
    private String name;

    public boolean isLeadingZeros()
    {
        return leadingZeros;
    }

    public String getName()
    {
        return name;
    }

    public static ZerosOmission forName(String name)
    {
        for (ZerosOmission u : values())
            if (u.getName().equals(name))
                return u;
        throw new IllegalArgumentException("Could not find ZerosOmission for " + name);
    }

    @Override
    public String toString()
    {
        return name;
    }
}
