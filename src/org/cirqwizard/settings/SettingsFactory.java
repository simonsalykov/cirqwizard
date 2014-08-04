package org.cirqwizard.settings;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by simon on 04/08/14.
 */
public class SettingsFactory
{
    private static InsulationMillingSettings insulationMillingSettings = new InsulationMillingSettings();

    public static InsulationMillingSettings getInsulationMillingSettings()
    {
        return insulationMillingSettings;
    }

    public static List<SettingsGroup> getAllGroups()
    {
        ArrayList<SettingsGroup> groups = new ArrayList<>();
        groups.add(insulationMillingSettings);
        return groups;
    }
}
