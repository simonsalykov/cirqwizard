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
    Small(75 * ApplicationConstants.RESOLUTION, 100 * ApplicationConstants.RESOLUTION),
    Large(100 * ApplicationConstants.RESOLUTION, 160 * ApplicationConstants.RESOLUTION);

    private int width;
    private int height;

    public static final int PCB_SIZE_CHECK_TOLERANCE = 100;

    private PCBSize(int width, int height)
    {
        this.width = width;
        this.height = height;
    }

    public int getWidth()
    {
        return width;
    }

    public int getHeight()
    {
        return height;
    }

    public boolean checkFit(int width, int height)
    {
        return ((width - getWidth()) <= PCB_SIZE_CHECK_TOLERANCE) && ((height - getHeight()) <= PCB_SIZE_CHECK_TOLERANCE);
    }
}
