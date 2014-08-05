package org.cirqwizard.settings;

/**
 * Created by simon on 05/08/14.
 */
public class PPSettings extends SettingsGroup
{
    @PersistentPreference
    @PreferenceGroup(name =  "Heights")
    private UserPreference<Integer> pickupHeight = new UserPreference<>("Pickup height", -14_200, "mm");

    @PersistentPreference
    @PreferenceGroup(name =  "Heights")
    private UserPreference<Integer> moveHeight = new UserPreference<>("Move height", 800, "mm");

    @PersistentPreference
    @PreferenceGroup(name =  "Rotation")
    private UserPreference<Integer> rotationFeed = new UserPreference<>("Rotation feed", 100_000, "mm/min");

    @Override
    public String getName()
    {
        return "Pick & Place";
    }

    @Override
    public String getPreferencesPrefix()
    {
        return "pp";
    }

    public UserPreference<Integer> getPickupHeight()
    {
        return pickupHeight;
    }

    public void setPickupHeight(UserPreference<Integer> pickupHeight)
    {
        this.pickupHeight = pickupHeight;
    }

    public UserPreference<Integer> getMoveHeight()
    {
        return moveHeight;
    }

    public void setMoveHeight(UserPreference<Integer> moveHeight)
    {
        this.moveHeight = moveHeight;
    }

    public UserPreference<Integer> getRotationFeed()
    {
        return rotationFeed;
    }

    public void setRotationFeed(UserPreference<Integer> rotationFeed)
    {
        this.rotationFeed = rotationFeed;
    }
}
