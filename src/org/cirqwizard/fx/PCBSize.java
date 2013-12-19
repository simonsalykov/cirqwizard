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


public enum PCBSize
{
    Small(0, 75, 100),
    Large(1, 100, 160);

    private int storeValue;
    private int width;
    private int height;

    public static final double PCB_SIZE_CHECK_TOLERANCE = 0.1;

    private PCBSize(int storeValue, int width, int height)
    {
        this.storeValue = storeValue;
        this.width = width;
        this.height = height;
    }

    public int getStoreValue()
    {
        return storeValue;
    }

    public int getWidth()
    {
        return width;
    }

    public int getHeight()
    {
        return height;
    }

    public static PCBSize valueOf(int i)
    {
        switch (i)
        {
            case 0: return Small;
            case 1: return Large;
            default:
                return null;
        }
    }

    public boolean checkFit(double width, double height)
    {
        return ((width - getWidth()) < PCB_SIZE_CHECK_TOLERANCE) && ((height - getHeight()) < PCB_SIZE_CHECK_TOLERANCE);
    }
}
