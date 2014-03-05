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
    private Integer angle;
    private Integer toolDiameter;

    public ToolpathsCacheKey(State state, int angle, int toolDiameter)
    {
        this.state = state;
        this.angle = angle;
        this.toolDiameter = toolDiameter;
    }

    @Override
    public boolean equals(Object obj)
    {
        if(obj != null && obj instanceof ToolpathsCacheKey)
        {
            ToolpathsCacheKey k = (ToolpathsCacheKey)obj;
            return state.equals(k.state) && angle.equals(k.angle) && toolDiameter.equals(k.toolDiameter);
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return angle.hashCode() + toolDiameter.hashCode() + state.hashCode();
    }
}
