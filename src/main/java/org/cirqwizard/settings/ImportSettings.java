package org.cirqwizard.settings;

public class ImportSettings extends SettingsGroup
{
    @PersistentPreference
    @PreferenceGroup(name = "Excellon")
    private UserPreference<Integer> excellonIntegerPlaces = new UserPreference<>("Integer places", 2, "", PreferenceType.INTEGER);

    @PersistentPreference
    @PreferenceGroup(name = "Excellon")
    private UserPreference<Integer> excellonDecimalPlaces = new UserPreference<>("Decimal places", 4, "", PreferenceType.INTEGER);

    @PersistentPreference
    @PreferenceGroup(name = "Excellon")
    private UserPreference<DistanceUnit> excellonUnits = new UserPreference<>("Units", DistanceUnit.INCHES, "").setItems(DistanceUnit.values()).
            setInstantiator(DistanceUnit::forName);

    @PersistentPreference
    @PreferenceGroup(name = "Pick and place")
    private UserPreference<String> centroidFileFormat = new UserPreference<>("File format", "(?<name>\\S+)\\s+(?<x>\\d+.?\\d*)\\s+(?<y>\\d+.?\\d*)\\s+(?<angle>\\d+)\\s+(?<value>\\S+)\\s*(?<package>\\S+)?", "");

    @PersistentPreference
    @PreferenceGroup(name = "Pick and place")
    private UserPreference<Integer> centroidAngularOffset = new UserPreference<>("Angle offset", 0, "degrees", PreferenceType.INTEGER);

    @Override
    public String getName()
    {
        return "Import";
    }

    @Override
    public String getPreferencesPrefix()
    {
        return "import";
    }

    public UserPreference<Integer> getExcellonIntegerPlaces()
    {
        return excellonIntegerPlaces;
    }

    public void setExcellonIntegerPlaces(UserPreference<Integer> excellonIntegerPlaces)
    {
        this.excellonIntegerPlaces = excellonIntegerPlaces;
    }

    public UserPreference<Integer> getExcellonDecimalPlaces()
    {
        return excellonDecimalPlaces;
    }

    public void setExcellonDecimalPlaces(UserPreference<Integer> excellonDecimalPlaces)
    {
        this.excellonDecimalPlaces = excellonDecimalPlaces;
    }

    public UserPreference<DistanceUnit> getExcellonUnits()
    {
        return excellonUnits;
    }

    public void setExcellonUnits(UserPreference<DistanceUnit> excellonUnits)
    {
        this.excellonUnits = excellonUnits;
    }

    public UserPreference<String> getCentroidFileFormat()
    {
        return centroidFileFormat;
    }

    public void setCentroidFileFormat(UserPreference<String> centroidFileFormat)
    {
        this.centroidFileFormat = centroidFileFormat;
    }

    public UserPreference<Integer> getCentroidAngularOffset()
    {
        return centroidAngularOffset;
    }

    public void setCentroidAngularOffset(UserPreference<Integer> centroidAngularOffset)
    {
        this.centroidAngularOffset = centroidAngularOffset;
    }
}
