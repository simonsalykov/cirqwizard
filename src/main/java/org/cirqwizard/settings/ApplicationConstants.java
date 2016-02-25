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

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class ApplicationConstants
{
    private static NumberFormat toolDiameterFormat = new DecimalFormat("0.0#");
    private static DecimalFormat decimalFormat = new DecimalFormat("0.0#");

    public static final int RESOLUTION = 1000;
    public static final int ROUNDING = 30;
    private final static int X_RAPIDS = 1_500_000;
    private final static int Y_RAPIDS = 1_000_000;
    private final static int Z_RAPIDS = 1_000_000;
    private final static int X_RAPID_ACCELERATION = 100_000;
    private final static int Y_RAPID_ACCELERATION = 50_000;
    private final static int Z_RAPID_ACCELERATION = 50_000;
    private final static int FEED_ACCELERATION = 50_000;
    private final static int ARC_FEED = 400_000;
    private final static int REGISTRATION_PINS_INSET = 5_000;

    public static int getXRapids()
    {
        return X_RAPIDS;
    }

    public static int getYRapids()
    {
        return Y_RAPIDS;
    }

    public static int getZRapids()
    {
        return Z_RAPIDS;
    }

    public static int getXRapidAcceleration()
    {
        return X_RAPID_ACCELERATION;
    }

    public static int getYRapidAcceleration()
    {
        return Y_RAPID_ACCELERATION;
    }

    public static int getZRapidAcceleration()
    {
        return Z_RAPID_ACCELERATION;
    }

    public static int getFeedAcceleration()
    {
        return FEED_ACCELERATION;
    }

    public static int getArcFeed()
    {
        return ARC_FEED;
    }

    public static int getRegistrationPinsInset()
    {
        return REGISTRATION_PINS_INSET;
    }

    public static NumberFormat getToolDiameterFormat()
    {
        return toolDiameterFormat;
    }

    public static String formatToolDiameter(int diameter)
    {
        return toolDiameterFormat.format((double)diameter / RESOLUTION);
    }

    public static String formatInteger(int value)
    {
        return decimalFormat.format((double)value / RESOLUTION);
    }
}
