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

package org.cirqwizard.fx;

public class Tool
{
    public static enum ToolType {V_TOOL, RUBOUT, CONTOUR_END_MILL, DRILL, SYRINGE, PICK_AND_PLACE}

    private ToolType type;
    private int diameter;

    public Tool(ToolType type, int diameter)
    {
        this.type = type;
        this.diameter = diameter;
    }

    public ToolType getType()
    {
        return type;
    }

    public int getDiameter()
    {
        return diameter;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Tool tool = (Tool) o;

        if (diameter != tool.diameter) return false;
        if (type != tool.type) return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + diameter;
        return result;
    }
}
