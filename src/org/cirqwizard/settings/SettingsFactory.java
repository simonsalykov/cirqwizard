package org.cirqwizard.settings;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by simon on 04/08/14.
 */
public class SettingsFactory
{
    private static MachineSettings machineSettings = new MachineSettings();
    private static InsulationMillingSettings insulationMillingSettings = new InsulationMillingSettings();
    private static DrillingSettings drillingSettings = new DrillingSettings();
    private static ContourMillingSettings contourMillingSettings = new ContourMillingSettings();
    private static DispensingSettings dispensingSettings = new DispensingSettings();
    private static PPSettings ppSettings = new PPSettings();

    public static MachineSettings getMachineSettings()
    {
        machineSettings.load();
        return machineSettings;
    }

    public static InsulationMillingSettings getInsulationMillingSettings()
    {
        insulationMillingSettings.load();
        return insulationMillingSettings;
    }

    public static DrillingSettings getDrillingSettings()
    {
        drillingSettings.load();
        return drillingSettings;
    }

    public static ContourMillingSettings getContourMillingSettings()
    {
        contourMillingSettings.load();
        return contourMillingSettings;
    }

    public static DispensingSettings getDispensingSettings()
    {
        dispensingSettings.load();
        return dispensingSettings;
    }

    public static PPSettings getPpSettings()
    {
        return ppSettings;
    }

    public static List<SettingsGroup> getAllGroups()
    {
        ArrayList<SettingsGroup> groups = new ArrayList<>();
        groups.add(getMachineSettings());
        groups.add(getInsulationMillingSettings());
        groups.add(getDrillingSettings());
        groups.add(getContourMillingSettings());
        groups.add(getDispensingSettings());
        groups.add(getPpSettings());
        return groups;
    }
}
