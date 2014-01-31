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

package org.cirqwizard.optimizer;

import org.cirqwizard.toolpath.Toolpath;

import java.util.List;

public class Environment
{
    private List<Toolpath> toolpaths;
    private double feed;
    private double zFeed;
    private double clearance;
    private double safetyHeight;

    public Environment(List<Toolpath> toolpaths, double feed, double zFeed, double clearance, double safetyHeight)
    {
        this.toolpaths = toolpaths;
        this.feed = feed;
        this.zFeed = zFeed;
        this.clearance = clearance;
        this.safetyHeight = safetyHeight;
    }

    public List<Toolpath> getToolpaths()
    {
        return toolpaths;
    }

    public void setToolpaths(List<Toolpath> toolpaths)
    {
        this.toolpaths = toolpaths;
    }

    public double getFeed()
    {
        return feed;
    }

    public void setFeed(double feed)
    {
        this.feed = feed;
    }

    public double getZFeed()
    {
        return zFeed;
    }

    public void setZFeed(double zFeed)
    {
        this.zFeed = zFeed;
    }

    public double getClearance()
    {
        return clearance;
    }

    public void setClearance(double clearance)
    {
        this.clearance = clearance;
    }

    public double getSafetyHeight()
    {
        return safetyHeight;
    }

    public void setSafetyHeight(double safetyHeight)
    {
        this.safetyHeight = safetyHeight;
    }
}
