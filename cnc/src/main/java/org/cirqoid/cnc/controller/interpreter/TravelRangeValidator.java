package org.cirqoid.cnc.controller.interpreter;

import org.cirqoid.cnc.controller.settings.HardwareSettings;

/**
 * Created by simon on 28.06.17.
 */
public class TravelRangeValidator
{

    public static boolean validate(int[] coordinates, HardwareSettings settings)
    {
        for (int i = 0; i < coordinates.length; i++)
            if (coordinates[i] < settings.getAxes()[i].getLowLimit() ||
                    coordinates[i] > settings.getAxes()[i].getHighLimit())
                return false;
        return true;
    }

}
