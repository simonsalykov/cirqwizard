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
package org.cirqwizard.generation;

import org.cirqwizard.geom.Circle;

public class MatchedArc
{
    private Circle circle;
    private double uncertainty;

    public MatchedArc(Circle circle, double uncertainty)
    {
        this.circle = circle;
        this.uncertainty = uncertainty;
    }

    public Circle getCircle()
    {
        return circle;
    }

    public void setCircle(Circle circle)
    {
        this.circle = circle;
    }

    public double getUncertainty()
    {
        return uncertainty;
    }

    public void setUncertainty(double uncertainty)
    {
        this.uncertainty = uncertainty;
    }

    @Override
    public String toString()
    {
        return "MatchedArc{" +
                "circle=" + circle +
                ", uncertainty=" + uncertainty +
                '}';
    }
}
