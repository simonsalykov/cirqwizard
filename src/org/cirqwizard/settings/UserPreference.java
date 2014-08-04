package org.cirqwizard.settings;

/**
 * Created by simon on 04/08/14.
 */
public class UserPreference<T>
{
    private String userName;
    private T defaultValue;
    private T value;
    private String units;
    private PreferenceType type;

    public UserPreference()
    {
    }

    public UserPreference(String userName, T defaultValue, String units)
    {
        this.userName = userName;
        this.defaultValue = defaultValue;
        this.units = units;
    }

    public String getUserName()
    {
        return userName;
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    public T getDefaultValue()
    {
        return defaultValue;
    }

    public void setDefaultValue(T defaultValue)
    {
        this.defaultValue = defaultValue;
    }

    public T getValue()
    {
        return value;
    }

    public void setValue(T value)
    {
        this.value = value;
    }

    public String getUnits()
    {
        return units;
    }

    public void setUnits(String units)
    {
        this.units = units;
    }

    public PreferenceType getType()
    {
        return type;
    }

    public UserPreference<T> setType(PreferenceType type)
    {
        this.type = type;
        return this;
    }
}
