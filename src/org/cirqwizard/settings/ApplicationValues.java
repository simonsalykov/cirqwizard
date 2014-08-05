package org.cirqwizard.settings;

import org.cirqwizard.fx.PCBSize;

/**
 * Created by simon on 05/08/14.
 */
public class ApplicationValues extends SettingsGroup
{
    @PersistentPreference
    private UserPreference<Integer> g54X = new UserPreference<>();

    @PersistentPreference
    private UserPreference<Integer> g54Y = new UserPreference<>();

    @PersistentPreference
    private UserPreference<PCBSize> pcbSize = new UserPreference<>();

    @PersistentPreference
    private UserPreference<Integer> scrapPlaceX = new UserPreference<>();

    @PersistentPreference
    private UserPreference<Integer> scrapPlaceY = new UserPreference<>();

    @PersistentPreference
    private UserPreference<Boolean> testCutDirection = new UserPreference<>();

    @Override
    public String getName()
    {
        return null;
    }

    @Override
    public String getPreferencesPrefix()
    {
        return "application-values";
    }

    public UserPreference<Integer> getG54X()
    {
        return g54X;
    }

    public void setG54X(UserPreference<Integer> g54X)
    {
        this.g54X = g54X;
    }

    public UserPreference<Integer> getG54Y()
    {
        return g54Y;
    }

    public void setG54Y(UserPreference<Integer> g54Y)
    {
        this.g54Y = g54Y;
    }

    public UserPreference<PCBSize> getPcbSize()
    {
        return pcbSize;
    }

    public void setPcbSize(UserPreference<PCBSize> pcbSize)
    {
        this.pcbSize = pcbSize;
    }

    public UserPreference<Integer> getScrapPlaceX()
    {
        return scrapPlaceX;
    }

    public void setScrapPlaceX(UserPreference<Integer> scrapPlaceX)
    {
        this.scrapPlaceX = scrapPlaceX;
    }

    public UserPreference<Integer> getScrapPlaceY()
    {
        return scrapPlaceY;
    }

    public void setScrapPlaceY(UserPreference<Integer> scrapPlaceY)
    {
        this.scrapPlaceY = scrapPlaceY;
    }

    public UserPreference<Boolean> getTestCutDirection()
    {
        return testCutDirection;
    }

    public void setTestCutDirection(UserPreference<Boolean> testCutDirection)
    {
        this.testCutDirection = testCutDirection;
    }
}
