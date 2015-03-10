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

package org.cirqwizard.fx.traces;

import org.cirqwizard.fx.Tool;
import org.cirqwizard.fx.common.Message;

public class InsertTool extends Message
{
    public static final Tool EXPECTED_TOOL = new Tool(Tool.ToolType.V_TOOL, 0);

    @Override
    protected String getName()
    {
        return "Tool";
    }

    @Override
    public void refresh()
    {
        super.refresh();
        getMainApplication().getContext().setInsertedTool(null);
        header.setText("Insert trace milling cutter into spindle");
        text.setText("Make sure the milling cutter is fully inserted.");
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
