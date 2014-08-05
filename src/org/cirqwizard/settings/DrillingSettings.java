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
