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

package org.cirqwizard.fx.drilling;

import org.cirqwizard.fx.Tool;
import org.cirqwizard.fx.common.Message;
import org.cirqwizard.settings.ApplicationConstants;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class InsertDrill extends Message
{
    private NumberFormat drillFormat = new DecimalFormat("0.0#");

    @Override
    protected String getName()
    {
        return "Insert drill";
    }

    @Override
    public void refresh()
    {
        super.refresh();
        getMainApplication().getContext().setInsertedTool(null);
        header.setText("Insert drill: " +
                drillFormat.format((double) getMainApplication().getContext().getCurrentDrill() / ApplicationConstants.RESOLUTION) + "mm");
        text.setText("Insert drill");
    }

    @Override
    public void next()
    {
        getMainApplication().getContext().setInsertedTool(new Tool(Tool.ToolType.DRILL, getMainApplication().getContext().getCurrentDrill()));
        super.next();
    }
}
