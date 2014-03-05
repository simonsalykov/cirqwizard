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
import java.util.HashMap;
import java.util.List;


public class ToolpathsCache implements Serializable
{
    private long lastModified;
    private int angle;
    private HashMap<Integer, List<Toolpath>> bottomLayer = new HashMap<>();
    private HashMap<Integer, List<Toolpath>> topLayer = new HashMap<>();

    public boolean hasValidData(int angle, long lastModified)
    {
        return this.lastModified == lastModified && this.angle == angle;
    }

    public List<Toolpath> getTopLayer(int toolDiameter)
    {
        if (topLayer.containsKey(toolDiameter))
            return topLayer.get(toolDiameter);
        return null;
    }

    public List<Toolpath> getBottomLayer(int toolDiameter)
    {
        if (bottomLayer.containsKey(toolDiameter))
            return bottomLayer.get(toolDiameter);
        return null;
    }

    public void setLastModified(long lastModified)
    {
        this.lastModified = lastModified;
    }

    public void setAngle(int angle)
    {
        this.angle = angle;
    }

    public void setTopLayer(int toolDiameter, List<Toolpath> topLayer)
    {
        this.topLayer.put(toolDiameter, topLayer);
    }

    public void setBottomLayer(int toolDiameter, List<Toolpath> bottomLayer)
    {
        this.bottomLayer.put(toolDiameter, bottomLayer);
    }
}