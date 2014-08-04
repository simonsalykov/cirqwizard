package org.cirqwizard.settings;

/**
 * Created by simon on 04/08/14.
 */
public abstract class SettingsGroup
{
    public abstract String getName();
    public abstract String getPreferencesPrefix();

    public void save()
    {

    }

    public void load()
    {

    }

    @Override
    public String toString()
    {
        return getName();
    }
}
