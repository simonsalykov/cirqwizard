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

package org.cirqwizard.gerber.appertures;

public class OvalAperture extends Aperture
{
    private int width, height;

    public OvalAperture(int width, int height)
    {
        super();
        this.width = width;
        this.height = height;
    }

    public OvalAperture(int width, int height, int holeDiameter)
    {
        super(holeDiameter);
        this.width = width;
        this.height = height;
    }

    public OvalAperture(int width, int height, int holeWidth, int holeHeight)
    {
        super(holeWidth, holeHeight);
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

    public boolean isHorizontal()
    {
        return width > height;
    }

    @Override
    public boolean isVisible()
    {
        return height > 0 && width > 0;
    }

    @Override
    public Aperture rotate(boolean clockwise)
    {
        return new OvalAperture(height, width);
    }

    @Override
    public int getCircumRadius()
    {
        return (int) Math.sqrt(width * width + height * height);
    }
}
