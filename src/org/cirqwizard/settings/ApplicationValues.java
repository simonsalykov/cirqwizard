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

import org.cirqwizard.fx.PCBSize;

public class ApplicationValues extends SettingsGroup
{
    @PersistentPreference
    private UserPreference<Integer> g54X = new UserPreference<>();

    @PersistentPreference
    private UserPreference<Integer> g54Y = new UserPreference<>();

    @PersistentPreference
    private UserPreference<PCBSize> pcbSize = new UserPreference<PCBSize>().setInstantiator(PCBSize::valueOf);

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
