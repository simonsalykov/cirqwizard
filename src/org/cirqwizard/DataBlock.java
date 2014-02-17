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

package org.cirqwizard;

import org.cirqwizard.math.RealNumber;


public class DataBlock
{
    private RealNumber x;
    private RealNumber y;
    private RealNumber i;
    private RealNumber j;
    private Integer g;
    private Integer d;
    private Integer m;

    public RealNumber getX()
    {
        return x;
    }

    public void setX(RealNumber x)
    {
        if (this.x == null)
            this.x = x;
    }

    public RealNumber getY()
    {
        return y;
    }

    public void setY(RealNumber y)
    {
        if (this.y == null)
            this.y = y;
    }

    public RealNumber getI()
    {
        return i;
    }

    public void setI(RealNumber i)
    {
        this.i = i;
    }

    public RealNumber getJ()
    {
        return j;
    }

    public void setJ(RealNumber j)
    {
        this.j = j;
    }

    public Integer getG()
    {
        return g;
    }

    public void setG(Integer g)
    {
        if (this.g == null)
            this.g = g;
    }

    public Integer getD()
    {
        return d;
    }

    public void setD(Integer d)
    {
        if (this.d == null)
            this.d = d;
    }

    public Integer getM()
    {
        return m;
    }

    public void setM(Integer m)
    {
        if (this.m == null)
            this.m = m;
    }
}
