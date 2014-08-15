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

import java.util.Arrays;
import java.util.List;

public class UserPreference<T>
{
    private String userName;
    private T defaultValue;
    private T value;
    private String units;
    private PreferenceType type;
    private List<T> items;
    private Instantiator instantiator;
    private boolean triggersInvalidation;   // Change of this setting invalidates already generated tool paths
    private boolean showInPopOver = true;

    public static interface Instantiator<T>
    {
        public T fromString(String str);
    }

    public UserPreference()
    {
    }

    public UserPreference(String userName, T defaultValue, String units)
    {
        this(userName, defaultValue, units, null);
    }

    public UserPreference(String userName, T defaultValue, String units, PreferenceType type)
    {
        this.userName = userName;
        this.defaultValue = defaultValue;
        this.units = units;
        this.type = type;
    }

    public String getUserName()
    {
        return userName;
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    public T getDefaultValue()
    {
        return defaultValue;
    }

    public void setDefaultValue(T defaultValue)
    {
        this.defaultValue = defaultValue;
    }

    public T getValue()
    {
        return value;
    }

    public void setValue(T value)
    {
        this.value = value;
    }

    public String getUnits()
    {
        return units;
    }

    public void setUnits(String units)
    {
        this.units = units;
    }

    public PreferenceType getType()
    {
        return type;
    }

    public UserPreference<T> setType(PreferenceType type)
    {
        this.type = type;
        return this;
    }

    public List<T> getItems()
    {
        return items;
    }

    public void setItems(List<T> items)
    {
        this.items = items;
    }

    public UserPreference<T> setItems(T... items)
    {
        this.items = Arrays.asList(items);
        return this;
    }

    public Instantiator getInstantiator()
    {
        return instantiator;
    }

    public UserPreference<T> setInstantiator(Instantiator<T> instantiator)
    {
        this.instantiator = instantiator;
        return this;
    }

    public boolean triggersInvalidation()
    {
        return triggersInvalidation;
    }

    public UserPreference<T> setTriggersInvalidation(boolean triggersInvalidation)
    {
        this.triggersInvalidation = triggersInvalidation;
        return this;
    }

    public boolean showInPopOver()
    {
        return showInPopOver;
    }

    public UserPreference<T> setShowInPopOver(boolean showInPopOver)
    {
        this.showInPopOver = showInPopOver;
        return this;
    }
}
