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

}
