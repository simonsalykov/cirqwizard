package org.cirqwizard.settings;

/**
 * Created by simon on 04/08/14.
 */
public class InsulationMillingSettings extends SettingsGroup
{
    @PersistentPreference
    private UserPreference<Integer> toolDiameter = new UserPreference<>("Tool diameter", 300, "mm");

    @PersistentPreference
    @PreferenceGroup(name = "Feed")
    private UserPreference<Integer> feedXY = new UserPreference<>("X and Y axes", 300_000, "mm/min");

    @PersistentPreference
    @PreferenceGroup(name = "Feed")
    private UserPreference<Integer> feedZ = new UserPreference<>("Z axis", 200_000, "mm/min");

    @PersistentPreference
    @PreferenceGroup(name = "Feed")
    private UserPreference<Integer> feedArcs = new UserPreference<>("Arcs", 50, "%").setType(PreferenceType.PERCENT);

    @PersistentPreference
    private UserPreference<Integer> speed = new UserPreference<>("Speed", 1390, "µs").setType(PreferenceType.INTEGER);

    @PersistentPreference
    private UserPreference<Integer> clearance = new UserPreference<>("Clearance", 5000, "mm");

    @PersistentPreference
    private UserPreference<Integer> safetyHeight = new UserPreference<>("Safety height", 2000, "mm");

    @PersistentPreference
    private UserPreference<Integer> zOffset = new UserPreference<>("Z offset", null, "mm");

    @PersistentPreference
    private UserPreference<Integer> workingHeight = new UserPreference<>("Working height", -50, "mm");

    @PersistentPreference
    @PreferenceGroup(name = "Additional passes")
    private UserPreference<Integer> additionalPasses = new UserPreference<>("Count", 0, "").setType(PreferenceType.INTEGER);

    @PersistentPreference
    @PreferenceGroup(name = "Additional passes")
    private UserPreference<Integer> additionalPassesOverlap = new UserPreference<>("Overlap", 30, "%").setType(PreferenceType.PERCENT);

    @PersistentPreference
    @PreferenceGroup(name = "Additional passes")
    private UserPreference<Boolean> additionalPassesPadsOnly = new UserPreference<>("Only around pads", false, "");

    @Override
    public String getName()
    {
        return "Insulation milling";
    }

    @Override
    public String getPreferencesPrefix()
    {
        return "insulation-milling";
    }

    public UserPreference<Integer> getToolDiameter()
    {
        return toolDiameter;
    }

    public void setToolDiameter(UserPreference<Integer> toolDiameter)
    {
        this.toolDiameter = toolDiameter;
    }

    public UserPreference<Integer> getFeedXY()
    {
        return feedXY;
    }

    public void setFeedXY(UserPreference<Integer> feedXY)
    {
        this.feedXY = feedXY;
    }

    public UserPreference<Integer> getFeedZ()
    {
        return feedZ;
    }

    public void setFeedZ(UserPreference<Integer> feedZ)
    {
        this.feedZ = feedZ;
    }

    public UserPreference<Integer> getFeedArcs()
    {
        return feedArcs;
    }

    public void setFeedArcs(UserPreference<Integer> feedArcs)
    {
        this.feedArcs = feedArcs;
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

    public UserPreference<Integer> getAdditionalPasses()
    {
        return additionalPasses;
    }

    public void setAdditionalPasses(UserPreference<Integer> additionalPasses)
    {
        this.additionalPasses = additionalPasses;
    }

    public UserPreference<Integer> getAdditionalPassesOverlap()
    {
        return additionalPassesOverlap;
    }

    public void setAdditionalPassesOverlap(UserPreference<Integer> additionalPassesOverlap)
    {
        this.additionalPassesOverlap = additionalPassesOverlap;
    }

    public UserPreference<Boolean> getAdditionalPassesPadsOnly()
    {
        return additionalPassesPadsOnly;
    }

    public void setAdditionalPassesPadsOnly(UserPreference<Boolean> additionalPassesPadsOnly)
    {
        this.additionalPassesPadsOnly = additionalPassesPadsOnly;
    }
}
