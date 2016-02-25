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

package org.cirqwizard.fx.contour;

import org.cirqwizard.fx.Tool;
import org.cirqwizard.fx.common.Message;
import org.cirqwizard.gerber.GerberPrimitive;
import org.cirqwizard.layers.Board;
import org.cirqwizard.settings.ApplicationConstants;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class InsertContourMill extends Message
{
    private static final Tool EXPECTED_TOOL = new Tool(Tool.ToolType.CONTOUR_END_MILL, 0);
    private static NumberFormat diameterFormat = new DecimalFormat("0.0#");

    @Override
    protected String getName()
    {
        return "Insert contour mill";
    }

    @Override
    public void refresh()
    {
        super.refresh();
        getMainApplication().getContext().setInsertedTool(null);
        GerberPrimitive primitive = (GerberPrimitive) getMainApplication().getContext().getPanel().getBoards().get(0).getBoard().
                getLayer(Board.LayerType.MILLING).getElements().get(0);
        String diameter = diameterFormat.format((double)primitive.getAperture().getWidth() / ApplicationConstants.RESOLUTION);
        header.setText("Insert contour end mill: " + diameter + "mm");
        text.setText("Insert contour end mill");
    }

    @Override
    protected boolean isMandatory()
    {
        return !EXPECTED_TOOL.equals(getMainApplication().getContext().getInsertedTool());
    }

    @Override
    public void next()
    {
        getMainApplication().getContext().setInsertedTool(EXPECTED_TOOL);
        super.next();
    }
}
