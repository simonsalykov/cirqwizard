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

import java.util.ArrayList;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;


public class Settings
{

    private static class PropertyNames
    {
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
    }

    private Preferences preferences;

    public Settings(Preferences preferences)
    {
        this.preferences = preferences;
    }



}
