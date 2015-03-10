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

public enum  DistanceUnit
{
    INCHES(25_400, "Inches"), MM(1000, "Millimeters");

    private int multiplier;
    private String name;

    DistanceUnit(int multiplier, String name)
    {
        this.multiplier = multiplier;
        this.name = name;
    }

    public int getMultiplier()
    {
        return multiplier;
    }

    public String getName()
    {
        return name;
    }

}
