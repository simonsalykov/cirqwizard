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

import org.cirqwizard.logging.LoggerFactory;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public abstract class SettingsGroup
{
    private final static String PREFERENCES_NODE_PREFIX = "org.cirqwizard.";

    public abstract String getName();
    public abstract String getPreferencesPrefix();

    public void save()
    {
        try
        {
            Preferences prefs = Preferences.userRoot().node(PREFERENCES_NODE_PREFIX + getPreferencesPrefix());
            for (Field f : getClass().getDeclaredFields())
            {
                if (!f.isAnnotationPresent(PersistentPreference.class))
                    continue;

                Class argumentClass = (Class) ((ParameterizedType)f.getGenericType()).getActualTypeArguments()[0];
                UserPreference p = (UserPreference) new PropertyDescriptor(f.getName(), getClass()).getReadMethod().invoke(this);
                if (p.getValue() == null)
                    prefs.remove(f.getName());
                else
                    prefs.put(f.getName(), p.getValue().toString());
            }
            prefs.sync();
        }
        catch (IllegalAccessException | InvocationTargetException | IntrospectionException | BackingStoreException e)
        {
            LoggerFactory.logException("Error saving user preferences", e);
        }
    }

    public void load()
    {
        try
        {
            Preferences prefs = Preferences.userRoot().node(PREFERENCES_NODE_PREFIX + getPreferencesPrefix());
            for (Field f : getClass().getDeclaredFields())
            {
                if (!f.isAnnotationPresent(PersistentPreference.class))
                    continue;

                Class argumentClass = (Class) ((ParameterizedType)f.getGenericType()).getActualTypeArguments()[0];
                UserPreference p = (UserPreference) new PropertyDescriptor(f.getName(), getClass()).getReadMethod().invoke(this);
                if (Integer.class.equals(argumentClass))
                {
                    String v = prefs.get(f.getName(), null);
                    p.setValue(v == null ? p.getDefaultValue() : Integer.valueOf(v));
                }
                else if (String.class.equals(argumentClass))
                    p.setValue(prefs.get(f.getName(), (String) p.getDefaultValue()));
                else if (Boolean.class.equals(argumentClass))
                {
                    String v = prefs.get(f.getName(), null);
                    p.setValue(v == null ? p.getDefaultValue() : Boolean.valueOf(v));
                }
                else
                {
                    String v = prefs.get(f.getName(), null);
                    p.setValue(v == null ? p.getDefaultValue() : p.getInstantiator().fromString(v));
                }
            }
        }
        catch (IllegalAccessException | InvocationTargetException | IntrospectionException e)
        {
            LoggerFactory.logException("Error loading user preferences", e);
        }
    }

    public void remove()
    {
        try
        {
            Preferences node = Preferences.userRoot().node(PREFERENCES_NODE_PREFIX + getPreferencesPrefix());
            node.removeNode();
            node.flush();
        }
        catch (BackingStoreException e)
        {
            LoggerFactory.logException("Error resetting user preferences", e);
        }
    }

    public boolean validate()
    {
        try
        {
            for (Field f : getClass().getDeclaredFields())
            {
                if (!f.isAnnotationPresent(PersistentPreference.class))
                    continue;

                Class argumentClass = (Class) ((ParameterizedType)f.getGenericType()).getActualTypeArguments()[0];
                UserPreference p = (UserPreference) new PropertyDescriptor(f.getName(), getClass()).getReadMethod().invoke(this);
                if (p.getValue() == null && p.getDefaultValue() == null)
                    return false;
            }
        }
        catch (IllegalAccessException | InvocationTargetException | IntrospectionException e)
        {
            LoggerFactory.logException("Error saving user preferences", e);
        }

        return true;
    }

    @Override
    public String toString()
    {
        return getName();
    }
}
