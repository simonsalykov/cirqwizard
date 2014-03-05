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

package org.cirqwizard.toolpath;


import java.io.*;
import java.util.List;


public class ToolpathsCache implements Serializable
{
    private int toolDiameter;
    private long lastModified;
    private int angle;
    private List<Toolpath> bottomLayer;
    private List<Toolpath> topLayer;

    public boolean hasValidData(int toolDiameter, int angle, long lastModified)
    {
        return this.toolDiameter == toolDiameter && this.lastModified == lastModified && this.angle == angle;
    }

    public List<Toolpath> getTopLayer()
    {
        return topLayer;
    }

    public List<Toolpath> getBottomLayer()
    {
        return bottomLayer;
    }

    public void setLastModified(long lastModified)
    {
        this.lastModified = lastModified;
    }

    public void setToolDiameter(int toolDiameter)
    {
        this.toolDiameter = toolDiameter;
    }

    public void setAngle(int angle)
    {
        this.angle = angle;
    }

    public void setTopLayer(List<Toolpath> topLayer)
    {
        this.topLayer = topLayer;
    }

    public void setBottomLayer(List<Toolpath> bottomLayer)
    {
        this.bottomLayer = bottomLayer;
    }
}