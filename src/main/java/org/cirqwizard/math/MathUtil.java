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

package org.cirqwizard.math;

import java.math.BigDecimal;


public class MathUtil
{
    public static final RealNumber PI = new RealNumber(new BigDecimal(Math.PI));

    public static double bindAngle(double angle)
    {
        if (angle >= Math.PI)
            angle -= Math.PI * 2;
        if (angle < -Math.PI)
            angle += Math.PI * 2;
        return angle;
    }

    public static boolean between(RealNumber value, int lowBoundary, int highBoundary)
    {
        return between(value, lowBoundary, highBoundary, false);
    }


    public static boolean between(RealNumber value, int lowBoundary, int highBoundary, boolean strict)
    {
        return between(value, new RealNumber(lowBoundary), new RealNumber(highBoundary), strict);
    }

    public static boolean between(RealNumber value, RealNumber lowBoundary, RealNumber highBoundary, boolean strict)
    {
        if (!strict)
            return value.greaterOrEqualTo(lowBoundary) && value.lessOrEqualTo(highBoundary);
        return value.compareTo(lowBoundary) >= 0 && value.compareTo(highBoundary) <= 0;
    }

    public static RealNumber sqrt(RealNumber number)
    {
        return new RealNumber(new BigDecimal(Math.sqrt(number.doubleValue())));
    }

    public static RealNumber pow(RealNumber number, int power)
    {
        RealNumber n = number;
        for (int i = 0; i < power - 1; i++)
            n = n.multiply(number);
        return n;
    }

    public static RealNumber sin(RealNumber number)
    {
        return new RealNumber(new BigDecimal(Math.sin(number.doubleValue())));
    }

    public static RealNumber cos(RealNumber number)
    {
        return new RealNumber(new BigDecimal(Math.cos(number.doubleValue())));
    }

    public static RealNumber min(RealNumber number1, RealNumber number2)
    {
        if (number1.lessThan(number2))
            return number1;
        return number2;
    }

    public static RealNumber max(RealNumber number1, RealNumber number2)
    {
        if (number1.greaterThan(number2))
            return number1;
        return number2;
    }

    public static RealNumber atan2(RealNumber y, RealNumber x)
    {
        return new RealNumber(new BigDecimal(Math.atan2(y.doubleValue(), x.doubleValue())));
    }
}
