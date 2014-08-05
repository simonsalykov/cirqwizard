package org.cirqwizard.settings;

/**
 * Created by simon on 05/08/14.
 */
public class MachineSettings extends SettingsGroup
{
    @PersistentPreference
    @PreferenceGroup(name = "Calibration parameters")
    private UserPreference<Integer> yAxisDifference = new UserPreference<>("Y axis difference", null, "mm");

    @PersistentPreference
    @PreferenceGroup(name = "Calibration parameters")
    private UserPreference<Integer> referencePinX = new UserPreference<>("Reference pin X", null, "mm");

    @PersistentPreference
    @PreferenceGroup(name = "Calibration parameters")
    private UserPreference<Integer> referencePinY = new UserPreference<>("Reference pin Y", null, "mm");

    @PersistentPreference
    @PreferenceGroup(name = "Reference pin distances")
    private UserPreference<Integer> smallPcbWidth = new UserPreference<>("Small laminate", 65000, "mm");

    @PersistentPreference
    @PreferenceGroup(name = "Reference pin distances")
    private UserPreference<Integer> largePcbWidth = new UserPreference<>("Large laminate", 90000, "mm");

    @PersistentPreference
    @PreferenceGroup(name = "Predefined locations")
    private UserPreference<Integer> farAwayY = new UserPreference<>("Far away Y", 220000, "mm");

    @Override
    public String getName()
    {
        return "Machine settings";
    }

    @Override
    public String getPreferencesPrefix()
    {
        return "machine";
    }

    public UserPreference<Integer> getYAxisDifference()
    {
        return yAxisDifference;
    }

    public void setYAxisDifference(UserPreference<Integer> yAxisDifference)
    {
        this.yAxisDifference = yAxisDifference;
    }

    public UserPreference<Integer> getReferencePinX()
    {
        return referencePinX;
    }

    public void setReferencePinX(UserPreference<Integer> referencePinX)
    {
        this.referencePinX = referencePinX;
    }

    public UserPreference<Integer> getReferencePinY()
    {
        return referencePinY;
    }

    public void setReferencePinY(UserPreference<Integer> referencePinY)
    {
        this.referencePinY = referencePinY;
    }

    public UserPreference<Integer> getSmallPcbWidth()
    {
        return smallPcbWidth;
    }

    public void setSmallPcbWidth(UserPreference<Integer> smallPcbWidth)
    {
        this.smallPcbWidth = smallPcbWidth;
    }

    public UserPreference<Integer> getLargePcbWidth()
    {
        return largePcbWidth;
    }

    public void setLargePcbWidth(UserPreference<Integer> largePcbWidth)
    {
        this.largePcbWidth = largePcbWidth;
    }

    public UserPreference<Integer> getFarAwayY()
    {
        return farAwayY;
    }

    public void setFarAwayY(UserPreference<Integer> farAwayY)
    {
        this.farAwayY = farAwayY;
    }
}
