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

public class DispensingSettings extends SettingsGroup
{
    @PersistentPreference
    @PreferenceGroup(name = "Needle")
    private UserPreference<Integer> needleDiameter = new UserPreference<>("Needle diameter", 400, "mm");

    @PersistentPreference
    @PreferenceGroup(name = "Timing")
    private UserPreference<Integer> preFeedPause = new UserPreference<>("Pre-feed pause", 100, "sec");

    @PersistentPreference
    @PreferenceGroup(name = "Timing")
    private UserPreference<Integer> postFeedPause = new UserPreference<>("Post-feed pause", 200, "sec");

    @PersistentPreference
    @PreferenceGroup(name = "Timing")
    private UserPreference<Integer> bleedingDuration = new UserPreference<>("Bleeding duration", 500, "sec");

    @PersistentPreference
    @PreferenceGroup(name = "Feed")
    private UserPreference<Integer> feed = new UserPreference<>("Feed", 100_000, "mm/min");

    @PersistentPreference
    @PreferenceGroup(name = "Heights")
    private UserPreference<Integer> clearance = new UserPreference<>("Clearance", 5_000, "mm");

    @PersistentPreference
    @PreferenceGroup(name = "Heights")
    private UserPreference<Integer> zOffset = new UserPreference<>("Z offset", null, "mm");

    @PersistentPreference
    @PreferenceGroup(name = "Heights")
    private UserPreference<Integer> workingHeight = new UserPreference<>("Working height", 0, "mm");


    @Override
    public String getName()
    {
        return "Dispensing";
    }

    @Override
    public String getPreferencesPrefix()
    {
        return "dispensing";
    }

    public UserPreference<Integer> getNeedleDiameter()
    {
        return needleDiameter;
    }

    public void setNeedleDiameter(UserPreference<Integer> needleDiameter)
    {
        this.needleDiameter = needleDiameter;
    }

    public UserPreference<Integer> getPreFeedPause()
    {
        return preFeedPause;
    }

    public void setPreFeedPause(UserPreference<Integer> preFeedPause)
    {
        this.preFeedPause = preFeedPause;
    }

    public UserPreference<Integer> getPostFeedPause()
    {
        return postFeedPause;
    }

    public void setPostFeedPause(UserPreference<Integer> postFeedPause)
    {
        this.postFeedPause = postFeedPause;
    }

    public UserPreference<Integer> getFeed()
    {
        return feed;
    }

    public void setFeed(UserPreference<Integer> feed)
    {
        this.feed = feed;
    }

    public UserPreference<Integer> getClearance()
    {
        return clearance;
    }

    public void setClearance(UserPreference<Integer> clearance)
    {
        this.clearance = clearance;
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

    public UserPreference<Integer> getBleedingDuration()
    {
        return bleedingDuration;
    }

    public void setBleedingDuration(UserPreference<Integer> bleedingDuration)
    {
        this.bleedingDuration = bleedingDuration;
    }
}
