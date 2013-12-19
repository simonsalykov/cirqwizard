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

package org.cirqwizard.appertures;

public class CircularAperture extends Aperture
{
    private int diameter;

    public CircularAperture(int diameter)
    {
        super();
        this.diameter = diameter;
    }

    public CircularAperture(int diameter, int holeDiameter)
    {
        super(holeDiameter);
        this.diameter = diameter;
    }

    public CircularAperture(int diameter, int holeWidth, int holeHeight)
    {
        super(holeWidth, holeHeight);
        this.diameter = diameter;
    }

    public int getDiameter()
    {
        return diameter;
    }

    @Override
    public boolean isVisible()
    {
        return diameter > 0;
    }

    @Override
    public int getWidth(double angle)
    {
        return diameter;
    }

    @Override
    public Aperture rotate(boolean clockwise)
    {
        return this;
    }


}
