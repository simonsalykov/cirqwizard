package org.cirqwizard.settings;

import org.cirqwizard.logging.LoggerFactory;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * Created by simon on 04/08/14.
 */
public abstract class SettingsGroup
{
    public abstract String getName();
    public abstract String getPreferencesPrefix();

    public void save()
    {
        try
        {
            Preferences prefs = Preferences.userRoot().node("org.cirqwizard." + getPreferencesPrefix());
            for (Field f : getClass().getDeclaredFields())
            {
                if (!f.isAnnotationPresent(PersistentPreference.class))
                    continue;

                Class argumentClass = (Class) ((ParameterizedType)f.getGenericType()).getActualTypeArguments()[0];
                UserPreference p = (UserPreference) new PropertyDescriptor(f.getName(), getClass()).getReadMethod().invoke(this);
                if (p.getValue() == null)
                    prefs.remove(f.getName());
                else
                {
                    if (Integer.class.equals(argumentClass))
                        prefs.put(f.getName(), p.getValue().toString());
                    else if (String.class.equals(argumentClass))
                        prefs.put(f.getName(), (String) p.getValue());
                    else if (Boolean.class.equals(argumentClass))
                        prefs.put(f.getName(), p.getValue().toString());
                }
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
            Preferences prefs = Preferences.userRoot().node("org.cirqwizard." + getPreferencesPrefix());
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
            }
        }
        catch (IllegalAccessException | InvocationTargetException | IntrospectionException e)
        {
            LoggerFactory.logException("Error loading user preferences", e);
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
