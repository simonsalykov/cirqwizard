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

import java.util.prefs.Preferences;


public class Settings
{
    private static final String EXPORTED = "exported";

    private static final String MACHINE_Y_DIFF_NAME = "v12.machine.y.diff";
    private static final String MACHINE_REFERENCE_PIN_X = "v12.machine.reference.pin.x";
    private static final String MACHINE_REFERENCE_PIN_Y = "v12.machine.reference.pin.y";
    private static final String MACHINE_SMALL_PCB_WIDTH = "v12.machine.small.pcb.width";
    private static final String MACHINE_LARGE_PCB_WIDTH = "v12.machine.large.pcb.width";
    private static final String MACHINE_FAR_AWAY_Y = "v12.general.far.away.y";

    private static final String SERIAL_PORT_NAME = "general.serial.port";
    private static final String LOGGER_LOG_LEVEL = "general.log.level";
    private static final String PROCESSING_THREADS = "general.processing_threads";

    private static final String TRACES_TOOL_DIAMETER = "v12.defaults.traces.tool.diameter";
    private static final String TRACES_FEED_XY = "v12.defaults.traces.feed.xy";
    private static final String TRACES_FEED_ARC = "v12.defaults.traces.feed.arc";
    private static final String TRACES_FEED_Z = "v12.defaults.traces.feed.z";
    private static final String TRACES_SPEED = "defaults.traces.speed";
    private static final String TRACES_CLEARANCE = "v12.defaults.traces.clearance";
    private static final String TRACES_SAFETY_HEIGHT = "v12.defaults.traces.safety.height";
    private static final String TRACES_DEFAULT_Z_OFFSET = "v12.defaults.traces.z.offset";
    private static final String TRACES_WORKING_HEIGHT = "v12.defaults.traces.working.height";
    private static final String TRACES_ADDITIONAL_PASSES = "traces.additional.passes";
    private static final String TRACES_ADDITIONAL_PASSES_OVERLAP = "traces.additional.passes.overlap";
    private static final String TRACES_ADDITONAL_PASSES_PADS_ONLY = "traces.additional.passes.pads.only";

    private static final String DRILLING_FEED = "v12.defaults.drilling.feed";
    private static final String DRILLING_SPEED = "v12.defaults.drilling.speed";
    private static final String DRILLING_CLEARANCE = "v12.defaults.drilling.clearance";
    private static final String DRILLING_SAFETY_HEIGHT = "v12.defaults.drilling.safety.height";
    private static final String DRILLING_Z_OFFSET = "v12.defaults.drilling.z.offset";
    private static final String DRILLING_WORKING_HEIGHT = "v12.defaults.drilling.working.height";

    private static final String CONTOUR_FEED_XY = "v12.defaults.contour.feed.xy";
    private static final String CONTOUR_FEED_ARC = "v12.defaults.contour.feed.arc";
    private static final String CONTOUR_FEED_Z = "v12.defaults.contour.feed.z";
    private static final String CONTOUR_SPEED = "defaults.contour.speed";
    private static final String CONTOUR_CLEARANCE = "v12.defaults.contour.clearance";
    private static final String CONTOUR_SAFETY_HEIGHT = "v12.defaults.contour.safety.height";
    private static final String CONTOUR_Z_OFFSET = "v12.defaults.contour.z.offset";
    private static final String CONTOUR_WORKING_HEIGHT = "v12.defaults.contour.working.height";

    private static final String DISPENSING_NEEDLE_DIAMETER = "v12.defaults.dispensing.needle.diameter";
    private static final String DISPENSING_PREFEED_PAUSE = "v12.defaults.dispensing.prefeed.pause";
    private static final String DISPENSING_POSTFEED_PAUSE = "v12.defaults.dispensing.postfeed.pause";
    private static final String DISPENSING_FEED = "v12.defaults.dispensing.feed";
    private static final String DISPENSING_CLEARANCE = "v12.defaults.dispensing.clearance";
    private static final String DISPENSING_Z_OFFSET = "v12.defaults.dispensing.z.offset";
    private static final String DISPENSING_WORKING_HEIGHT = "v12.defaults.working.height";
    private static final String DISPENSING_BLEEDING_DURATION = "dispensing.bleeding.duration";

    private static final String PP_PICKUP_HEIGHT = "v12.pp.pickup.height";
    private static final String PP_MOVE_HEIGHT = "v12.pp.move.height";
    private static final String PP_ROTATION_FEED = "v12.pp.rotation.feed";

    private static final String IMPORT_EXCELLON_INTEGER_PLACES = "import.excellon.integer.places";
    private static final String IMPORT_EXCELLON_DECIMAL_PLACES = "import.excellon.decimal.places";
    private static final String IMPORT_EXCELLON_UNIT_CONVERSION_RATIO = "import.excellon.unit.conversion.ratio";
    private static final String IMPORT_PP_REGEX = "import.pp.regex";

    private static final String INTERFACE_RECENT_FILES = "interface.recent.files";
    private static final String INTERFACE_G54_X = "v12.interface.g54.x";
    private static final String INTERFACE_G54_Y = "v12.interface.g54.y";
    private static final String INTERFACE_PCB_SIZE = "interface.pcb.size";
    private static final String INTERFACE_SCRAP_PLACE_X = "v12.interface.scrap.place.x";
    private static final String INTERFACE_SCRAP_PLACE_Y = "v12.interface.scrap.place.y";
    private static final String INTERFACE_TEST_CUT_DIRECTION = "interface.test.cut.direction";

    private Preferences preferences;

    public Settings(Preferences preferences)
    {
        this.preferences = preferences;
    }

    public void export()
    {
        if (preferences.get(LOGGER_LOG_LEVEL, null) == null)
            return;
        if (preferences.getBoolean(EXPORTED, false))
            return;

        MachineSettings machineSettings = SettingsFactory.getMachineSettings();
        setInt(MACHINE_Y_DIFF_NAME, machineSettings.getYAxisDifference());
        setInt(MACHINE_REFERENCE_PIN_X, machineSettings.getReferencePinX());
        setInt(MACHINE_REFERENCE_PIN_Y, machineSettings.getReferencePinY());
        setInt(MACHINE_SMALL_PCB_WIDTH, machineSettings.getSmallPcbWidth());
        setInt(MACHINE_LARGE_PCB_WIDTH, machineSettings.getLargePcbWidth());

        PredefinedLocationSettings predefinedLocationSettings = SettingsFactory.getPredefinedLocationSettings();
        setInt(MACHINE_FAR_AWAY_Y, predefinedLocationSettings.getFarAwayY());

        ApplicationSettings applicationSettings = SettingsFactory.getApplicationSettings();
        setString(SERIAL_PORT_NAME, applicationSettings.getSerialPort());
        setObject(LOGGER_LOG_LEVEL, applicationSettings.getLogLevel());
        setInt(PROCESSING_THREADS, applicationSettings.getProcessingThreads());

        InsulationMillingSettings insulationMillingSettings = SettingsFactory.getInsulationMillingSettings();
        setInt(TRACES_CLEARANCE, insulationMillingSettings.getClearance());
        setInt(TRACES_SAFETY_HEIGHT, insulationMillingSettings.getSafetyHeight());
        setInt(TRACES_WORKING_HEIGHT, insulationMillingSettings.getWorkingHeight());

        DrillingSettings drillingSettings = SettingsFactory.getDrillingSettings();
        setInt(DRILLING_FEED, drillingSettings.getFeed());
        setInt(DRILLING_SPEED, drillingSettings.getSpeed());
        setInt(DRILLING_CLEARANCE, drillingSettings.getClearance());
        setInt(DRILLING_SAFETY_HEIGHT, drillingSettings.getSafetyHeight());
        setInt(DRILLING_Z_OFFSET, drillingSettings.getZOffset());
        setInt(DRILLING_WORKING_HEIGHT, drillingSettings.getWorkingHeight());

        ContourMillingSettings contourMillingSettings = SettingsFactory.getContourMillingSettings();
        setInt(CONTOUR_FEED_XY, contourMillingSettings.getFeedXY());
        setInt(CONTOUR_FEED_ARC, contourMillingSettings.getFeedArcs());
        setInt(CONTOUR_FEED_Z, contourMillingSettings.getFeedZ());
        setInt(CONTOUR_SPEED, contourMillingSettings.getSpeed());
        setInt(CONTOUR_CLEARANCE, contourMillingSettings.getClearance());
        setInt(CONTOUR_SAFETY_HEIGHT, contourMillingSettings.getSafetyHeight());
        setInt(CONTOUR_Z_OFFSET, contourMillingSettings.getZOffset());
        setInt(CONTOUR_WORKING_HEIGHT, contourMillingSettings.getWorkingHeight());

        DispensingSettings dispensingSettings = new DispensingSettings();
        setInt(DISPENSING_NEEDLE_DIAMETER, dispensingSettings.getNeedleDiameter());
        setInt(DISPENSING_PREFEED_PAUSE, dispensingSettings.getPreFeedPause());
        setInt(DISPENSING_POSTFEED_PAUSE, dispensingSettings.getPostFeedPause());
        setInt(DISPENSING_FEED, dispensingSettings.getFeed());
        setInt(DISPENSING_CLEARANCE, dispensingSettings.getClearance());
        setInt(DISPENSING_Z_OFFSET, dispensingSettings.getZOffset());
        setInt(DISPENSING_WORKING_HEIGHT, dispensingSettings.getWorkingHeight());
        setInt(DISPENSING_BLEEDING_DURATION, dispensingSettings.getBleedingDuration());

        PPSettings ppSettings = SettingsFactory.getPpSettings();
        setInt(PP_PICKUP_HEIGHT, ppSettings.getPickupHeight());
        setInt(PP_MOVE_HEIGHT, ppSettings.getMoveHeight());
        setInt(PP_ROTATION_FEED, ppSettings.getRotationFeed());

        ImportSettings importSettings = SettingsFactory.getImportSettings();
        setInt(IMPORT_EXCELLON_INTEGER_PLACES, importSettings.getExcellonIntegerPlaces());
        setInt(IMPORT_EXCELLON_DECIMAL_PLACES, importSettings.getExcellonDecimalPlaces());
        if (preferences.get(IMPORT_EXCELLON_UNIT_CONVERSION_RATIO, null) != null)
            importSettings.getExcellonUnits().setValue("1000".equals(preferences.get(IMPORT_EXCELLON_UNIT_CONVERSION_RATIO, null)) ? DistanceUnit.MM : DistanceUnit.INCHES);

        ApplicationValues applicationValues = SettingsFactory.getApplicationValues();
        setInt(INTERFACE_G54_X, applicationValues.getG54X());
        setInt(INTERFACE_G54_Y, applicationValues.getG54Y());
        applicationValues.getPcbSize().setValue(preferences.getInt(INTERFACE_PCB_SIZE, 0) == 0 ? PCBSize.Small : PCBSize.Large);
        setInt(INTERFACE_SCRAP_PLACE_X, applicationValues.getScrapPlaceX());
        setInt(INTERFACE_SCRAP_PLACE_Y, applicationValues.getScrapPlaceY());
        applicationValues.getTestCutDirection().setValue(preferences.getInt(INTERFACE_TEST_CUT_DIRECTION, 0) == 0);

        preferences.putBoolean(EXPORTED, true);

        machineSettings.save();
        applicationSettings.save();
        insulationMillingSettings.save();
        drillingSettings.save();
        contourMillingSettings.save();
        dispensingSettings.save();
        ppSettings.save();
        importSettings.save();
        applicationValues.save();
    }

    private void setInt(String propertyName, UserPreference<Integer> preference)
    {
        String str = preferences.get(propertyName, null);
        if (str != null)
            preference.setValue(Integer.valueOf(str));
    }

    private void setString(String propertyName, UserPreference<String> preference)
    {
        String str = preferences.get(propertyName, null);
        if (str != null)
            preference.setValue(str);
    }

    private void setBoolean(String propertyName, UserPreference<Boolean> preference)
    {
        String str = preferences.get(propertyName, null);
        if (str != null)
            preference.setValue(Boolean.valueOf(str));
    }

    private void setObject(String propertyName, UserPreference preference)
    {
        String str = preferences.get(propertyName, null);
        if (str != null)
            preference.setValue(preference.getInstantiator().fromString(str));
    }





}
