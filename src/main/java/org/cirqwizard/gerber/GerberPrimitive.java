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

package org.cirqwizard.gerber;

import org.cirqwizard.gerber.appertures.Aperture;
import org.cirqwizard.geom.Point;
import org.cirqwizard.layers.LayerElement;


public abstract class GerberPrimitive extends LayerElement
{
    protected Aperture aperture;
    private Polarity polarity;

    protected GerberPrimitive(Polarity polarity)
    {
        this.polarity = polarity;
    }

    public Polarity getPolarity()
    {
        return polarity;
    }

    public Aperture getAperture()
    {
        return aperture;
    }

    public abstract void rotate(boolean clockwise);
    public abstract void move(Point point);
    public abstract Point getMin();
    public abstract Point getMax();


    public enum Polarity
    {
        CLEAR, DARK
    }

}
