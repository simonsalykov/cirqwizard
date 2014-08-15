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

import java.util.ArrayList;
import java.util.List;

public class SettingsFactory
{
    private static MachineSettings machineSettings = new MachineSettings();
    private static PredefinedLocationSettings predefinedLocationSettings = new PredefinedLocationSettings();
    private static InsulationMillingSettings insulationMillingSettings = new InsulationMillingSettings();
    private static DrillingSettings drillingSettings = new DrillingSettings();
    private static ContourMillingSettings contourMillingSettings = new ContourMillingSettings();
    private static DispensingSettings dispensingSettings = new DispensingSettings();
    private static PPSettings ppSettings = new PPSettings();
    private static ApplicationSettings applicationSettings = new ApplicationSettings();
    private static ApplicationValues applicationValues = new ApplicationValues();

    public static MachineSettings getMachineSettings()
    {
        machineSettings.load();
        return machineSettings;
    }

    public static PredefinedLocationSettings getPredefinedLocationSettings()
    {
        predefinedLocationSettings.load();
        return predefinedLocationSettings;
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
        ppSettings.load();
        return ppSettings;
    }

    public static ApplicationSettings getApplicationSettings()
    {
        applicationSettings.load();
        return applicationSettings;
    }

    public static ApplicationValues getApplicationValues()
    {
        applicationValues.load();
        return applicationValues;
    }

    public static List<SettingsGroup> getAllGroups()
    {
        ArrayList<SettingsGroup> groups = new ArrayList<>();
        groups.add(getMachineSettings());
        groups.add(getPredefinedLocationSettings());
        groups.add(getInsulationMillingSettings());
        groups.add(getDrillingSettings());
        groups.add(getContourMillingSettings());
        groups.add(getDispensingSettings());
        groups.add(getPpSettings());
        groups.add(getApplicationSettings());
        return groups;
    }

    public static void resetAll()
    {
        getAllGroups().stream().forEach(SettingsGroup::remove);
    }
}
