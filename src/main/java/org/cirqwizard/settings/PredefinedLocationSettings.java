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

public class PredefinedLocationSettings extends SettingsGroup
{
    @PersistentPreference
    @PreferenceGroup(name = "Far away")
    private UserPreference<Integer> farAwayX = new UserPreference<>("X", 0, "mm");

    @PersistentPreference
    @PreferenceGroup(name = "Far away")
    private UserPreference<Integer> farAwayY = new UserPreference<>("Y", 225_000, "mm");

    @PersistentPreference
    @PreferenceGroup(name = "Far away")
    private UserPreference<Integer> farAwayZ = new UserPreference<>("Z", 0, "mm");

    @PersistentPreference
    @PreferenceGroup(name = "Tool change")
    private UserPreference<Integer> toolChangeX = new UserPreference<>("X", 0, "mm");

    @PersistentPreference
    @PreferenceGroup(name = "Tool change")
    private UserPreference<Integer> toolChangeY = new UserPreference<>("Y", 0, "mm");

    @PersistentPreference
    @PreferenceGroup(name = "Tool change")
    private UserPreference<Integer> toolChangeZ = new UserPreference<>("Z", 0, "mm");

    @Override
    public String getName()
    {
        return "Predefined locations";
    }

    @Override
    public String getPreferencesPrefix()
    {
        return "predefined-locations";
    }

    public UserPreference<Integer> getFarAwayX()
    {
        return farAwayX;
    }

    public void setFarAwayX(UserPreference<Integer> farAwayX)
    {
        this.farAwayX = farAwayX;
    }

    public UserPreference<Integer> getFarAwayY()
    {
        return farAwayY;
    }

    public void setFarAwayY(UserPreference<Integer> farAwayY)
    {
        this.farAwayY = farAwayY;
    }

    public UserPreference<Integer> getFarAwayZ()
    {
        return farAwayZ;
    }

    public void setFarAwayZ(UserPreference<Integer> farAwayZ)
    {
        this.farAwayZ = farAwayZ;
    }

    public UserPreference<Integer> getToolChangeX()
    {
        return toolChangeX;
    }

    public void setToolChangeX(UserPreference<Integer> toolChangeX)
    {
        this.toolChangeX = toolChangeX;
    }

    public UserPreference<Integer> getToolChangeY()
    {
        return toolChangeY;
    }

    public void setToolChangeY(UserPreference<Integer> toolChangeY)
    {
        this.toolChangeY = toolChangeY;
    }

    public UserPreference<Integer> getToolChangeZ()
    {
        return toolChangeZ;
    }

    public void setToolChangeZ(UserPreference<Integer> toolChangeZ)
    {
        this.toolChangeZ = toolChangeZ;
    }
}
