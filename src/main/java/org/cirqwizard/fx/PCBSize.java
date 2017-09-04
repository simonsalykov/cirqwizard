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

package org.cirqwizard.fx;


import org.cirqwizard.settings.ApplicationConstants;

public enum PCBSize
{
    Small(75 * ApplicationConstants.RESOLUTION, 100 * ApplicationConstants.RESOLUTION,
            ApplicationConstants.getRegistrationPinsInset(), "75x100"),
    Large(100 * ApplicationConstants.RESOLUTION, 160 * ApplicationConstants.RESOLUTION,
            ApplicationConstants.getRegistrationPinsInset(), "100x160"),
    XLarge(100 * ApplicationConstants.RESOLUTION, 205 * ApplicationConstants.RESOLUTION,
            50 * ApplicationConstants.RESOLUTION, "100x205");

    private int width;
    private int height;
    private int wcsYOffset;
    private String name;

    PCBSize(int width, int height, int wcsYOffset, String name)
    {
        this.width = width;
        this.height = height;
        this.wcsYOffset = wcsYOffset;
        this.name = name;
    }

    public int getWidth()
    {
        return width;
    }

    public int getHeight()
    {
        return height;
    }

    public int getWcsYOffset()
    {
        return wcsYOffset;
    }

    @Override
    public String toString()
    {
        return name;
    }
}
