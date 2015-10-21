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

import java.math.BigDecimal;

public enum DistanceUnit
{
    INCHES(new BigDecimal(25_400), "Inches"), MM(new BigDecimal(1000), "Millimeters"), THOU(new BigDecimal("25.4"), "Thou");

    private BigDecimal multiplier;
    private String name;

    DistanceUnit(BigDecimal multiplier, String name)
    {
        this.multiplier = multiplier;
        this.name = name;
    }

    public BigDecimal getMultiplier()
    {
        return multiplier;
    }

    public String getName()
    {
        return name;
    }

    @Override
    public String toString()
    {
        return name;
    }

    public static DistanceUnit forName(String name)
    {
        for (DistanceUnit u : values())
            if (u.getName().equals(name))
                return u;
        throw new IllegalArgumentException("Could not find DistanceUnit for " + name);
    }
}
