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

package org.cirqwizard.fx.pp;

import org.cirqwizard.fx.Tool;
import org.cirqwizard.fx.common.Message;

public class InsertPPHead extends Message
{
    private final static Tool EXPECTED_TOOL = new Tool(Tool.ToolType.PICK_AND_PLACE, 0);

    @Override
    protected String getName()
    {
        return "Insert P&P head";
    }

    @Override
    public void refresh()
    {
        super.refresh();
        getMainApplication().getContext().setInsertedTool(null);
        header.setText("Insert pick & place header");
        text.setText("Connect it to the vacuum outlet at the base of the machine and to the socket. Choose a needle that will work for your components.");

    }
    @Override
    protected boolean isMandatory()
    {
        return EXPECTED_TOOL.equals(getMainApplication().getContext().getInsertedTool());
    }

    @Override
    public void next()
    {
        getMainApplication().getContext().setInsertedTool(EXPECTED_TOOL);
        super.next();
    }

}
