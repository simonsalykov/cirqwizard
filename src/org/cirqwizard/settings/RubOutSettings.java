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

public class RubOutSettings extends SettingsGroup
{
    @PersistentPreference
    @PreferenceGroup(name = "Tool")
    private UserPreference<Integer> toolDiameter = new UserPreference<>("Diameter", 800, "mm").setTriggersInvalidation(true);

    @PersistentPreference
    @PreferenceGroup(name = "Tool")
    private UserPreference<Integer> speed = new UserPreference<>("Speed", 1390, "µs").setType(PreferenceType.INTEGER);

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
    @PreferenceGroup(name = "Heights")
    private UserPreference<Integer> clearance = new UserPreference<>("Clearance", 5000, "mm");

    @PersistentPreference
    @PreferenceGroup(name = "Heights")
    private UserPreference<Integer> safetyHeight = new UserPreference<>("Safety height", 2000, "mm");

    @PersistentPreference
    @PreferenceGroup(name = "Heights")
    private UserPreference<Integer> zOffset = new UserPreference<>("Z offset", (Integer) null, "mm").setShowInPopOver(false);

    @PersistentPreference
    @PreferenceGroup(name = "Heights")
    private UserPreference<Integer> workingHeight = new UserPreference<>("Working height", -50, "mm");

    @PersistentPreference
    @PreferenceGroup(name = "Rub-out")
    private UserPreference<Integer> overlap = new UserPreference<>("Overlap", 20, "%").setType(PreferenceType.PERCENT).setTriggersInvalidation(true);

    @PersistentPreference
    @PreferenceGroup(name = "Rub-out")
    private UserPreference<Integer> initialOffset = new UserPreference<>("Initial offset", 200, "mm").setTriggersInvalidation(true);

    @Override
    public String getName()
    {
        return "Rub-out";
    }

    @Override
    public String getPreferencesPrefix()
    {
        return "rubout";
    }

    public UserPreference<Integer> getToolDiameter()
    {
        return toolDiameter;
    }

    public void setToolDiameter(UserPreference<Integer> toolDiameter)
    {
        this.toolDiameter = toolDiameter;
    }

    public UserPreference<Integer> getSpeed()
    {
        return speed;
    }

    public void setSpeed(UserPreference<Integer> speed)
    {
        this.speed = speed;
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

    public UserPreference<Integer> getOverlap()
    {
        return overlap;
    }

    public void setOverlap(UserPreference<Integer> overlap)
    {
        this.overlap = overlap;
    }

    public UserPreference<Integer> getInitialOffset()
    {
        return initialOffset;
    }

    public void setInitialOffset(UserPreference<Integer> initialOffset)
    {
        this.initialOffset = initialOffset;
    }
}
