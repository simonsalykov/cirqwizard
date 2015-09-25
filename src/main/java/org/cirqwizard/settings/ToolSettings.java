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
package org.cirqwizard.settings;

public class ToolSettings
{
    private String name;
    private int diameter;
    private int speed;
    private int feedXY;
    private int feedZ;
    private int arcs;
    private int zOffset;
    private int additionalPasses;
    private int additionalPassesOverlap;
    private boolean additionalPassesPadsOnly;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public int getDiameter()
    {
        return diameter;
    }

    public void setDiameter(int diameter)
    {
        this.diameter = diameter;
    }

    public int getSpeed()
    {
        return speed;
    }

    public void setSpeed(int speed)
    {
        this.speed = speed;
    }

    public int getFeedXY()
    {
        return feedXY;
    }

    public void setFeedXY(int feedXY)
    {
        this.feedXY = feedXY;
    }

    public int getFeedZ()
    {
        return feedZ;
    }

    public void setFeedZ(int feedZ)
    {
        this.feedZ = feedZ;
    }

    public int getArcs()
    {
        return arcs;
    }

    public void setArcs(int arcs)
    {
        this.arcs = arcs;
    }

    public int getzOffset()
    {
        return zOffset;
    }

    public void setzOffset(int zOffset)
    {
        this.zOffset = zOffset;
    }

    public int getAdditionalPasses()
    {
        return additionalPasses;
    }

    public void setAdditionalPasses(int additionalPasses)
    {
        this.additionalPasses = additionalPasses;
    }

    public int getAdditionalPassesOverlap()
    {
        return additionalPassesOverlap;
    }

    public void setAdditionalPassesOverlap(int additionalPassesOverlap)
    {
        this.additionalPassesOverlap = additionalPassesOverlap;
    }

    public boolean isAdditionalPassesPadsOnly()
    {
        return additionalPassesPadsOnly;
    }

    public void setAdditionalPassesPadsOnly(boolean additionalPassesPadsOnly)
    {
        this.additionalPassesPadsOnly = additionalPassesPadsOnly;
    }
}
