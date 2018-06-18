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

public class ContourMillingSettings extends SettingsGroup
{
    @PersistentPreference
    @PreferenceGroup(name = "Outline generation")
    private UserPreference<Integer> generationToolDiameter = new UserPreference<>("End mill diameter", 1_000, "mm");

    @PersistentPreference
    @PreferenceGroup(name = "Outline generation")
    private UserPreference<Integer> generationDrillDiameter = new UserPreference<>("Drill diameter", 600, "mm");

    @PersistentPreference
    @PreferenceGroup(name = "Outline generation")
    private UserPreference<Integer> generationHolesCount = new UserPreference<>("Holes count", 4, "holes").setType(PreferenceType.INTEGER);

    @PersistentPreference
    @PreferenceGroup(name = "Outline generation")
    private UserPreference<Integer> generationHolesSpacing = new UserPreference<>("Holes spacing", 50, "mm");

    @PersistentPreference
    @PreferenceGroup(name = "Feeds")
    private UserPreference<Integer> feedXY = new UserPreference<>("X and Y axes", 300_000, "mm/min");

    @PersistentPreference
    @PreferenceGroup(name = "Feeds")
    private UserPreference<Integer> feedZ = new UserPreference<>("Z axis", 200_000, "mm/min");

    @PersistentPreference
    @PreferenceGroup(name = "Feeds")
    private UserPreference<Integer> feedArcs = new UserPreference<>("Arcs", 50, "%", PreferenceType.PERCENT);

    @PersistentPreference
    @PreferenceGroup(name = "Speed")
    private UserPreference<Integer> speed = new UserPreference<>("Tool speed", 1390, "\u00B5s", PreferenceType.INTEGER);

    @PersistentPreference
    @PreferenceGroup(name = "Heights")
    private UserPreference<Integer> clearance = new UserPreference<>("Clearance", 5_000, "mm");

    @PersistentPreference
    @PreferenceGroup(name = "Heights")
    private UserPreference<Integer> safetyHeight = new UserPreference<>("Safety height", 2_000, "mm");

    @PersistentPreference
    @PreferenceGroup(name = "Heights")
    private UserPreference<Integer> zOffset = new UserPreference<>("Z offset", (Integer) null, "mm").setShowInPopOver(false);

    @PersistentPreference
    @PreferenceGroup(name = "Heights")
    private UserPreference<Integer> workingHeight = new UserPreference<>("Working height", -1_800, "mm");


    @PersistentPreference
    @PreferenceGroup(name = "Heights")
    private UserPreference<Integer> maxStepDown = new UserPreference<>("Max step down", 0, "mm");

    @Override
    public String getName()
    {
        return "Contour milling";
    }

    @Override
    public String getPreferencesPrefix()
    {
        return "contour-milling";
    }

    public UserPreference<Integer> getGenerationToolDiameter()
    {
        return generationToolDiameter;
    }

    public void setGenerationToolDiameter(UserPreference<Integer> generationToolDiameter)
    {
        this.generationToolDiameter = generationToolDiameter;
    }

    public UserPreference<Integer> getGenerationDrillDiameter()
    {
        return generationDrillDiameter;
    }

    public void setGenerationDrillDiameter(UserPreference<Integer> generationDrillDiameter)
    {
        this.generationDrillDiameter = generationDrillDiameter;
    }

    public UserPreference<Integer> getGenerationHolesCount()
    {
        return generationHolesCount;
    }

    public void setGenerationHolesCount(UserPreference<Integer> generationHolesCount)
    {
        this.generationHolesCount = generationHolesCount;
    }

    public UserPreference<Integer> getGenerationHolesSpacing()
    {
        return generationHolesSpacing;
    }

    public void setGenerationHolesSpacing(UserPreference<Integer> generationHolesSpacing)
    {
        this.generationHolesSpacing = generationHolesSpacing;
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

    public UserPreference<Integer> getMaxStepDown()
    {
        return maxStepDown;
    }

    public void setMaxStepDown(UserPreference<Integer> maxStepDown)
    {
        this.maxStepDown = maxStepDown;
    }
}
