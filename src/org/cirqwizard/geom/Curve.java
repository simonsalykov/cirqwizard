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

package org.cirqwizard.geom;


public abstract class Curve
{
    protected Point from;
    protected Point to;

    public Point getFrom()
    {
        return from;
    }

    public void setFrom(Point from)
    {
        this.from = from;
    }

    public Point getTo()
    {
        return to;
    }

    public void setTo(Point to)
    {
        this.to = to;
    }

    public abstract Curve reverse();

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Curve curve = (Curve) o;

        if (from != null ? !from.equals(curve.from) : curve.from != null) return false;
        if (to != null ? !to.equals(curve.to) : curve.to != null) return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = from != null ? from.hashCode() : 0;
        result = 31 * result + (to != null ? to.hashCode() : 0);
        return result;
    }
}
