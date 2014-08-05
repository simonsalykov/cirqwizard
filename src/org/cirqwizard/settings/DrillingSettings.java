package org.cirqwizard.settings;

/**
 * Created by simon on 05/08/14.
 */
public class DrillingSettings extends SettingsGroup
{
    @PersistentPreference
    @PreferenceGroup(name = "Tool")
    private UserPreference<Integer> feed = new UserPreference<>("Feed", 200_000, "mm/min");

    @PersistentPreference
    @PreferenceGroup(name = "Tool")
    private UserPreference<Integer> speed = new UserPreference<>("Speed", 1390, "µs", PreferenceType.INTEGER);

    @PersistentPreference
    @PreferenceGroup(name = "Heights")
    private UserPreference<Integer> clearance = new UserPreference<>("Clearance", 5_000, "mm");

    @PersistentPreference
    @PreferenceGroup(name = "Heights")
    private UserPreference<Integer> safetyHeight = new UserPreference<>("Safety height", 2_000, "mm");

    @PersistentPreference
    @PreferenceGroup(name = "Heights")
    private UserPreference<Integer> zOffset = new UserPreference<>("Z offset", null, "mm");

    @PersistentPreference
    @PreferenceGroup(name = "Heights")
    private UserPreference<Integer> workingHeight = new UserPreference<>("Working height", -2_000, "mm");

    @Override
    public String getName()
    {
        return "Drilling";
    }

    @Override
    public String getPreferencesPrefix()
    {
        return "drilling";
    }

    public UserPreference<Integer> getFeed()
    {
        return feed;
    }

    public void setFeed(UserPreference<Integer> feed)
    {
        this.feed = feed;
    }

    public UserPreference<Integer> getSpeed()
    {
        return speed;
    }

    public void setSpeed(UserPreference<Integer> speed)
    {
        this.speed = speed;
    }

    public UserPreference<Integer> getClearance()
    {
        return clearance;
    }

    public void setClearance(UserPreference<Integer> clearance)
    {
        this.clearance = clearance;
    }

    public UserPreference<Integer> getSafetyHeight()
    {
        return safetyHeight;
    }

    public void setSafetyHeight(UserPreference<Integer> safetyHeight)
    {
        this.safetyHeight = safetyHeight;
    }

    public UserPreference<Integer> getZOffset()
    {
        return zOffset;
    }

    public void setZOffset(UserPreference<Integer> zOffset)
    {
        this.zOffset = zOffset;
    }

    public UserPreference<Integer> getWorkingHeight()
    {
        return workingHeight;
    }

    public void setWorkingHeight(UserPreference<Integer> workingHeight)
    {
        this.workingHeight = workingHeight;
    }
}
