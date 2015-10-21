/*
This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License version 3 as published by
    the Free Software Foundation.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
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
    private UserPreference<PickAndPlaceFormat> centroidFileFormat = new UserPreference<>("File format", PickAndPlaceFormat.EAGLE, "").
            setItems(PickAndPlaceFormat.values()).
            setInstantiator(PickAndPlaceFormat::forName);

    @PersistentPreference
    @PreferenceGroup(name = "Pick and place")
    private UserPreference<Integer> centroidAngularOffset = new UserPreference<>("Angle offset", 0, "degrees", PreferenceType.INTEGER);

    @PersistentPreference
    @PreferenceGroup(name = "Pick and place")
    private UserPreference<DistanceUnit> centroidUnits = new UserPreference<>("Units", DistanceUnit.MM, "").
            setItems(DistanceUnit.values()).setInstantiator(DistanceUnit::forName);

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

    public UserPreference<PickAndPlaceFormat> getCentroidFileFormat()
    {
        return centroidFileFormat;
    }

    public void setCentroidFileFormat(UserPreference<PickAndPlaceFormat> centroidFileFormat)
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

    public UserPreference<DistanceUnit> getCentroidUnits()
    {
        return centroidUnits;
    }

    public void setCentroidUnits(UserPreference<DistanceUnit> centroidUnits)
    {
        this.centroidUnits = centroidUnits;
    }
}
