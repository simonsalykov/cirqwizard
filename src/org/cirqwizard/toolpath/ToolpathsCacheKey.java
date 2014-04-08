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


import org.cirqwizard.fx.State;
import java.io.Serializable;


public class ToolpathsCacheKey implements Serializable
{
    private State state;
    private int angle;
    private int toolDiameter;
    private int additionalPasses;
    private int additionalPassesOverlap;
    private boolean additionalPassesAroundPadsOnly;

    public ToolpathsCacheKey(State state, int angle, int toolDiameter, int additionalPasses, int additionalPassesOverlap, boolean additionalPassesAroundPadsOnly)
    {
        this.state = state;
        this.angle = angle;
        this.toolDiameter = toolDiameter;
        this.additionalPasses = additionalPasses;
        this.additionalPassesOverlap = additionalPassesOverlap;
        this.additionalPassesAroundPadsOnly = additionalPassesAroundPadsOnly;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ToolpathsCacheKey that = (ToolpathsCacheKey) o;

        if (additionalPasses != that.additionalPasses) return false;
        if (additionalPassesAroundPadsOnly != that.additionalPassesAroundPadsOnly) return false;
        if (additionalPassesOverlap != that.additionalPassesOverlap) return false;
        if (angle != that.angle) return false;
        if (toolDiameter != that.toolDiameter) return false;
        if (state != that.state) return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = state != null ? state.hashCode() : 0;
        result = 31 * result + angle;
        result = 31 * result + toolDiameter;
        result = 31 * result + additionalPasses;
        result = 31 * result + additionalPassesOverlap;
        result = 31 * result + (additionalPassesAroundPadsOnly ? 1 : 0);
        return result;
    }

}
