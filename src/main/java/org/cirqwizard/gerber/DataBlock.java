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

public class DataBlock
{
    private Integer x;
    private Integer y;
    private Integer i;
    private Integer j;
    private Integer g;
    private Integer d;
    private Integer m;

    public Integer getX()
    {
        return x;
    }

    public void setX(Integer x)
    {
        if (this.x == null)
            this.x = x;
    }

    public Integer getY()
    {
        return y;
    }

    public void setY(Integer y)
    {
        if (this.y == null)
            this.y = y;
    }

    public Integer getI()
    {
        return i;
    }

    public void setI(Integer i)
    {
        this.i = i;
    }

    public Integer getJ()
    {
        return j;
    }

    public void setJ(Integer j)
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
